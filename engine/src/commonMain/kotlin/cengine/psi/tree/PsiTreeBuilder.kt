package cengine.psi.tree

import cengine.console.IOContext
import cengine.console.SysOut
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiError
import cengine.psi.elements.PsiFile
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.CompletedMarkerInfo
import cengine.psi.parser.IndexedParseError
import cengine.psi.parser.ParseResult
import cengine.psi.tree.PsiTreeDebugger.dumpTree
import cengine.vfs.VirtualFile
import kotlin.math.min

class PsiTreeBuilder(
    private val result: ParseResult,
    private val io: IOContext = SysOut,
    private val attachComments: Boolean = false,
    private val attachSpaces: Boolean = false,
) {

    internal val tokens = result.tokens
    private val markerMap: Map<Long, CompletedMarkerInfo> = result.completedMarkers.associateBy { it.id }
    private val builtNodes = mutableMapOf<Long, PsiElement>()
    private val consumedTokenIndices = mutableSetOf<Int>() // Keep track of consumed token indices from both markers AND errors

    fun build(file: VirtualFile): PsiFile {
        io.log("Starting PsiTree construction for ${file.path}...")
        consumedTokenIndices.clear()
        builtNodes.clear()

        // 1. Identify top-level markers
        val topLevelMarkers = findTopLevelMarkers()
        io.debug { "Identified ${topLevelMarkers.size} top-level markers: ${topLevelMarkers.map { "${it.id}(${it.elementType.typeName})" }}" }

        // 2. Build nodes for top-level markers (This populates builtNodes and consumedTokenIndices)
        val topLevelParsedNodes = topLevelMarkers.map { markerInfo ->
            try {
                buildNode(markerInfo, mutableSetOf())
            } catch (e: Exception) {
                io.error("Exception during top-level buildNode for marker ${markerInfo.id}: ${e.message}\n${e.stackTraceToString()}")
                // Create an error node if building fails catastrophically at the top level
                buildErrorFromMarker(markerInfo, "Top-level build failed: $e") // Use existing helper
            }
        }
        io.debug { "Built ${topLevelParsedNodes.size} nodes from top-level markers." }

        // Create PsiError nodes from parser errors
        val errorElements = mutableListOf<PsiError>()
        result.errors.forEach { parseError ->
            val errorElement = createPsiErrorFromParseError(parseError)
            errorElements.add(errorElement)
            io.debug { "Created PsiError node for parser error: '${parseError.message}' at token index ${parseError.startTokenIndex}" }
        }
        // Debug: Error count
        io.debug { "Created ${errorElements.size} PsiError nodes from parser errors." }

        // 3. Find loose top-level tokens (NOW respects indices consumed by markers AND errors)
        val looseTopLevelTokens = findLooseTopLevelTokens()
        io.debug { "Found ${looseTopLevelTokens.size} loose top-level tokens." }

        // 4. Combine and sort *all* children: parsed nodes, error nodes, and loose tokens
        val allFileChildren = (topLevelParsedNodes + errorElements + looseTopLevelTokens)
            .sortedBy { it.range.first } // Sort by the start of the element's range

        // 5. Determine validity and create PsiFile
        val hasParseErrors = result.errors.isNotEmpty()
        // Check if the final tree contains ANY PsiError elements (either from parser or build failures)
        val hasTreeErrors = allFileChildren.any { containsErrorElement(it) }
        val isValid = !hasParseErrors && !hasTreeErrors // File is valid only if parser had no errors AND tree build resulted in no PsiError nodes

        // Minimal: Report final validity status
        io.log("PsiTree construction finished. File Validity: $isValid")
        if (hasParseErrors) {
            io.log(" -> Found ${result.errors.size} parse errors reported by the parser.")
        }
        if (hasTreeErrors && !hasParseErrors) {
            // Only report tree errors if there weren't already parse errors (avoid redundancy)
            val treeErrorCount = allFileChildren.count { containsErrorElement(it) }
            io.log(" -> Found $treeErrorCount PsiError nodes in the final tree (potentially due to build issues).")
        }

        io.debug { "Building PsiFile (isValid=$isValid) with ${allFileChildren.size} children." }
        val psiFile = PsiFile(PsiFile.PsiFileT(file, isValid), *allFileChildren.toTypedArray())

        // Dump the built tree
        io.debug {
            """
            --- Built PSI Tree Dump ---
            ${psiFile.dumpTree(true)}
            ---  End PSI Tree Dump  ---
        """.trimIndent()
        }

        return psiFile
    }

    /** Recursively checks if a PsiElement or any of its children is a PsiError node. */
    private fun containsErrorElement(element: PsiElement): Boolean {
        if (element is PsiError) {
            return true
        }
        return element.children.any { containsErrorElement(it) }
    }

    // --- Marker Processing Logic ---

    private fun findTopLevelMarkers(): List<CompletedMarkerInfo> {
        // Filter markers that are not preceded or whose preceding marker doesn't exist (robustness)
        val potentialRoots = result.completedMarkers.filter { marker ->
            marker.precededMarkerId == null || !markerMap.containsKey(marker.precededMarkerId)
        }

        // Filter markers that are not fully contained within another potential root
        val topLevelMarkers = potentialRoots.filter { potential ->
            potentialRoots.none { other ->
                other.id != potential.id && // Not the same marker
                        other.start <= potential.start && // Other starts at or before potential
                        other.end >= potential.end &&   // Other ends at or after potential
                        (other.start < potential.start || other.end > potential.end) // Other is strictly larger
            }
        }
        // Sort primarily by start index, then by end index descending (larger scopes first if starts match)
        return topLevelMarkers.sortedWith(compareBy({ it.start }, { -it.end }))
    }

    /**
     * Finds tokens that are not part of any successfully built node (from markers)
     * NOR part of any generated PsiError node.
     * Respects the `consumedTokenIndices` set which is populated by both processes.
     */
    private fun findLooseTopLevelTokens(): List<PsiToken> {
        val looseTokens = mutableListOf<PsiToken>()
        for (i in tokens.indices) {
            if (tokens[i].type == PsiTokenType.EOF) continue // Skip EOF

            if (i !in consumedTokenIndices) {
                val token = tokens[i]
                // Decide whether to include whitespace/comments based on builder flags
                when (token.type) {
                    is PsiTokenType.WHITESPACE -> if (attachSpaces) looseTokens.add(token)
                    is PsiTokenType.COMMENT -> if (attachComments) looseTokens.add(token)
                    else -> looseTokens.add(token) // Always include other token types if loose
                }
                // Crucially, mark this loose token as consumed now so it doesn't get picked up again
                // Although not strictly necessary if only called once, it's good practice.
                // consumedTokenIndices.add(i) // Reconsider if this is needed. If only called once at the end, it's not. Let's omit for now.
            }
        }
        return looseTokens
    }


    /**
     * Recursively builds a PsiElement node for the given marker information.
     * Populates `consumedTokenIndices` for the range covered by the marker if successful.
     */
    private fun buildNode(markerInfo: CompletedMarkerInfo, currentlyBuilding: MutableSet<Long>): PsiElement {

        // 1. Memoization Check
        builtNodes[markerInfo.id]?.let { return it } // Memoization

        // 2. Recursion Cycle Detection
        if (!currentlyBuilding.add(markerInfo.id)) {
            io.error("Build Recursion Detected: Trying to build marker ${markerInfo.id} (${markerInfo.elementType}) which is already in the current build stack: $currentlyBuilding")
            return buildErrorFromMarker(markerInfo, "Build recursion detected involing marker ${markerInfo.id}")
        }

        io.debug { "Building node for marker ${markerInfo.id} (${markerInfo.elementType.typeName}) [${markerInfo.start}..${markerInfo.end})" }

        var builtElement: PsiElement
        try {
            // 3. Find Direct Child Markers
            val directChildMarkers = findDirectChildMarkers(markerInfo)

            // 4. Build Child Nodes Recursively
            val childNodes = directChildMarkers.map { childMarker ->
                try {
                    buildNode(childMarker, currentlyBuilding)
                } catch (e: Exception) {
                    io.error("Exception during recursive buildNode for child marker ${childMarker.id} of parent ${markerInfo.id}: ${e.message}")
                    buildErrorFromMarker(childMarker, "Child build failed: $e")
                }
            }

            // 5. Find Loose Tokens within this marker's range
            val looseTokens = findLooseTokensInMarker(markerInfo, directChildMarkers)

            // 6. Combine children and sort
            val children = (childNodes + looseTokens).sortedBy { it.range.first }.toTypedArray()

            // 7. Construct the Node
            try {
                val nodeBuilder = markerInfo.elementType.builder
                val startChar = if (markerInfo.start < tokens.size) tokens[markerInfo.start].range.first else 0
                val endTokenIndex = markerInfo.end - 1
                val endChar = if (endTokenIndex >= 0 && endTokenIndex < tokens.size) {
                    tokens[endTokenIndex].range.last + 1
                } else if (markerInfo.start < tokens.size) {
                    tokens[markerInfo.start].range.first
                } else {
                    startChar
                }
                val nodeRange = startChar until endChar

                builtElement = nodeBuilder(markerInfo, children, nodeRange) ?: run {
                    io.warn("Node builder for ${markerInfo.elementType.typeName} returned null. Marker: $markerInfo")
                    io.debug { " -> Children for null build: ${children.contentToString()}" }
                    buildErrorFromMarker(markerInfo, "Node builder returned null for ${markerInfo.elementType.typeName}", *children)
                }
            } catch (e: Exception) {
                io.error("Exception during node construction for marker ${markerInfo.id} (${markerInfo.elementType.typeName}): ${e.message}\n${e.stackTraceToString()}")
                builtElement = buildErrorFromMarker(markerInfo, "Internal error during construction: $e", *children)
            }

            builtNodes[markerInfo.id] = builtElement

            // Consume tokens
            io.debug { " <- Finished building ${builtElement::class.simpleName} for marker ${markerInfo.id}. Consuming tokens [${markerInfo.start}..${markerInfo.end})" }
            for (i in markerInfo.start until min(markerInfo.end, tokens.size)) {
                consumedTokenIndices.add(i)
            }

            return builtElement

        } finally {
            // Remove ID from set when returning up the stack
            currentlyBuilding.remove(markerInfo.id)
            io.debug { "Exiting build for marker ${markerInfo.id}. (Stack: $currentlyBuilding)" }
        }
    }

    // Helper to find direct child markers (logic seems okay, minor robustness)
    private fun findDirectChildMarkers(parentMarker: CompletedMarkerInfo): List<CompletedMarkerInfo> {
        // 1. Find markers fully contained within the parent (excluding parent itself)
        val containedMarkers = result.completedMarkers.filter { child ->
            child.id != parentMarker.id &&
                    child.start >= parentMarker.start &&
                    child.end <= parentMarker.end &&
                    markerMap.containsKey(child.id) // Ensure child marker exists (sanity check)
        }

        // 2. Handle 'preceded' marker if it exists and is within bounds
        val precededChild = parentMarker.precededMarkerId?.let { markerMap[it] }?.takeIf {
            it.start >= parentMarker.start && it.end <= parentMarker.end
        }

        // 3. Combine and remove duplicates
        val potentialChildren = (containedMarkers + listOfNotNull(precededChild)).distinctBy { it.id }

        // If no potential children, return early
        if (potentialChildren.isEmpty()) {
            return emptyList()
        }

        // 4. Sort by start index (asc), then by end index (desc - larger range first if starts match)
        // This sort is key to the single-pass filtering logic.
        val sortedPotentials = potentialChildren.sortedWith(
            compareBy<CompletedMarkerInfo> { it.start }.thenByDescending { it.end }
        )

        val directChildren = mutableListOf<CompletedMarkerInfo>()
        // Use parent's start as the initial boundary for the first potential child.
        // Or simply use -1 as no valid token index is negative.
        var lastDirectChildEnd = -1 // Tracks the end of the last marker added to directChildren

        for (currentMarker in sortedPotentials) {
            // If the current marker starts strictly *before* the end of the last added direct child,
            // it means it's nested within or equal to the previous one (due to sorting). Skip it.
            // Use '<' because a marker starting exactly where the previous one ended is a sibling, not nested.
            if (currentMarker.start < lastDirectChildEnd) {
                io.debug { " -> Skipping nested marker ${currentMarker.id} (${currentMarker.elementType.typeName}) [${currentMarker.start}..${currentMarker.end}) as it starts before last end $lastDirectChildEnd" } // Debug log
                continue // Not a direct child
            }

            // Otherwise, this is a direct child.
            directChildren.add(currentMarker)
            // Update the end boundary.
            lastDirectChildEnd = currentMarker.end
            io.debug { " -> Adding direct child marker ${currentMarker.id} (${currentMarker.elementType.typeName}) [${currentMarker.start}..${currentMarker.end}). Updating last end to $lastDirectChildEnd" } // Debug log
        }

        // The list is already sorted by start index due to the initial sort and iteration order.
        return directChildren
    }


    // Helper to find loose tokens *within a specific marker's range*
    private fun findLooseTokensInMarker(
        markerInfo: CompletedMarkerInfo,
        directChildMarkers: List<CompletedMarkerInfo>, // Already built direct children
    ): List<PsiToken> {
        val childNodeRanges = directChildMarkers.map { it.start until it.end } // Ranges covered by children
        val looseTokens = mutableListOf<PsiToken>()

        for (i in markerInfo.start until min(markerInfo.end, tokens.size)) {
            // Check if the token index falls within any direct child marker's range
            val partOfChild = childNodeRanges.any { range -> i in range }

            if (!partOfChild) {
                // Check if token is already consumed by a different node (e.g., error node, sibling processed earlier)
                // This check might be redundant if buildNode guarantees consumption, but adds safety.
                // if (i !in consumedTokenIndices) { // No, this is wrong here. We are *within* buildNode. Consumption happens *after*.
                val token = tokens[i]
                when (token.type) {
                    is PsiTokenType.WHITESPACE -> if (attachSpaces) looseTokens.add(token)
                    is PsiTokenType.COMMENT -> if (attachComments) looseTokens.add(token)
                    else -> looseTokens.add(token) // Add non-whitespace/comment tokens
                }
                // } // End redundant check
            }
        }
        return looseTokens
    }


    // --- NEW: Helper to create PsiError from IndexedParseError ---

    /**
     * Creates a PsiError element from a parser-reported error.
     * Marks the corresponding token index as consumed.
     * Returns null if the token index was already consumed (e.g., by a successful marker).
     */
    private fun createPsiErrorFromParseError(errorInfo: IndexedParseError): PsiError {
        val errorTokenIndex = errorInfo.startTokenIndex

        // If the token where the error *started* is already part of a valid node,
        // we might choose not to create a separate top-level error element for it,
        // as the error might have been part of an unsuccessful parsing branch that was recovered from.
        // However, it's often useful to *still* see the error marker. Let's create it,
        // but be aware it might visually overlap with structured nodes.
        // The `isValid` flag on PsiFile handles the overall status.

        // Let's check consumption *before* creating the element to avoid redundant errors in the tree structure if recovery was very successful.
        if (errorTokenIndex in consumedTokenIndices) {
            io.debug { "Note: Parser error at index $errorTokenIndex ('${errorInfo.message}') occurred at a token already consumed by a built node." }
            // return null // Option 1: Skip creating the error node if token consumed
        }


        // Try to get the token associated with the error start index
        val errorToken: PsiToken? = tokens.getOrNull(errorTokenIndex)

        // Children of the error node: typically just the token where the error occurred, if available.
        val errorChildren = mutableListOf<PsiElement>()
        if (errorToken != null) {
            // Check again if this specific token was consumed. Add if not, or always add?
            // Let's add it for visibility, even if consumed by a larger structure.
            errorChildren.add(errorToken)
            // Mark this specific token index as consumed by this error node
            consumedTokenIndices.add(errorTokenIndex) // Mark consumed HERE
        } else {
            // Handle case where error occurs at EOF or invalid index
            io.warn("Parser error reported at index $errorTokenIndex which is out of token bounds (${tokens.size}). Creating PsiError without token child.")
        }

        // Use the character range provided by the parser error
        val errorRange = errorInfo.characterRange

        return PsiError(
            errorMessage = errorInfo.message,
            range = errorRange, // Use the range from the parser error directly
            children = errorChildren.toTypedArray()
        )
    }


    // --- buildError helper (renamed for clarity) ---

    /**
     * Helper to create a PsiErrorElement when node construction *from a marker* fails.
     * Tries to associate relevant tokens or child nodes.
     */
    private fun buildErrorFromMarker(
        markerInfo: CompletedMarkerInfo,
        message: String,
        vararg associatedChildNodes: PsiElement, // Nodes that were potentially built before failure
    ): PsiError {
        io.debug { " -> Error building node for marker ${markerInfo.id} (${markerInfo.elementType.typeName}): $message" }

        // Calculate the character range based on marker's token indices
        val startChar = if (markerInfo.start < tokens.size) tokens[markerInfo.start].range.first else 0
        val endTokenIndex = markerInfo.end - 1
        val endChar = if (endTokenIndex >= 0 && endTokenIndex < tokens.size) {
            tokens[endTokenIndex].range.last + 1 // Use exclusive end
        } else if (markerInfo.start < tokens.size) {
            tokens[markerInfo.start].range.first // Handle zero-token markers
        } else {
            startChar // Fallback
        }
        val errorRange = startChar until endChar

        val errorChildren = mutableListOf<PsiElement>()
        // Prioritize using the children that were already built/passed in
        if (associatedChildNodes.isNotEmpty()) {
            errorChildren.addAll(associatedChildNodes)
        } else {
            // Fallback: Add relevant *unconsumed* tokens from the marker's range
            val startIdx = markerInfo.start
            val endIdx = min(markerInfo.end, tokens.size)
            for (i in startIdx until endIdx) {
                if (i < tokens.size) { // Ensure index is valid
                    if (i !in consumedTokenIndices) { // Only add if not already part of another node/error
                        val token = tokens[i]
                        // Optionally filter whitespace/comments here too if desired for error nodes
                        if (token.type !is PsiTokenType.WHITESPACE && token.type !is PsiTokenType.COMMENT) {
                            errorChildren.add(token)
                            // consumedTokenIndices.add(i) // Mark token consumed by *this* error node
                            // Consumption for marker errors is handled in buildNode loop now.
                        }
                    }
                }
            }
            // Limit the number of fallback tokens? Optional.
            // errorChildren = errorChildren.take(5).toMutableList()
        }

        // Ensure the error node itself has the correct range
        return PsiError(message, errorRange, children = errorChildren.toTypedArray())
    }
}