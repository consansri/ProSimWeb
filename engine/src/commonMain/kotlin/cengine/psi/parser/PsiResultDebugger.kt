package cengine.psi.parser

import cengine.psi.lexer.PsiToken

/**
 * Utility to dump the parse tree structure from a ParseResult for debugging.
 */
object PsiResultDebugger {

    fun dump(result: ParseResult): String {
        val sb = StringBuilder()
        val markers = result.completedMarkers
        val tokens = result.tokens

        if (markers.isEmpty()) {
            sb.appendLine("No completed markers found.")
            appendErrors(sb, result.errors)
            return sb.toString()
        }

        // Build a map for quick lookup
        val idToMarkerInfo = markers.associateBy { it.id }

        // Find root nodes: those not preceded AND not contained within any other marker
        val rootNodes = markers.filter { marker ->
            // Not preceded by any *other* marker in the list
            // (A marker might precede itself conceptually if logic allows, filter that)
            val isPrecededByOther = markers.any { other -> other.precededMarkerId == marker.id && other.id != marker.id }

            // Not strictly contained within any other marker
            val isContained = markers.any { potentialParent ->
                marker.id != potentialParent.id && // Not itself
                        marker.start >= potentialParent.start &&
                        marker.end <= potentialParent.end &&
                        // Ensure strict containment (parent is not identical in range)
                        !(marker.start == potentialParent.start && marker.end == potentialParent.end)
            }
            !isPrecededByOther && !isContained
        }.sortedBy { it.start } // Sort roots by start position

        if (rootNodes.isEmpty() && markers.isNotEmpty()) {
            sb.appendLine("Warning: No root nodes identified. Dumping all nodes sequentially (may indicate cycles or unusual structure):")
            markers.sortedBy { it.start }.forEach { dumpNode(it, idToMarkerInfo, markers, tokens, 0, sb) }
        } else {
            rootNodes.forEach { root ->
                dumpNode(root, idToMarkerInfo, markers, tokens, 0, sb)
            }
        }

        appendErrors(sb, result.errors)
        return sb.toString()
    }

    private fun appendErrors(sb: StringBuilder, errors: List<IndexedParseError>) {
        if (errors.isNotEmpty()) {
            sb.appendLine("\n--- Errors (${errors.size}) ---")
            errors.forEach { (message, range) ->
                sb.appendLine("  Error at $range: $message")
            }
        }
    }

    private fun dumpNode(
        markerInfo: CompletedMarkerInfo,
        idToMarkerInfo: Map<Long, CompletedMarkerInfo>,
        allMarkers: List<CompletedMarkerInfo>,
        tokens: List<PsiToken>,
        indent: Int,
        sb: StringBuilder
    ) {
        // 1. Print current node's info
        sb.append("  ".repeat(indent))
        sb.append(markerInfo.elementType.typeName)
        sb.append(" [${markerInfo.start}..${markerInfo.end})")
        if (markerInfo.precededMarkerId != null) {
            sb.append(" (Precedes: ${markerInfo.precededMarkerId})")
        }

        // 2. Find children
        val children = findChildren(markerInfo, idToMarkerInfo, allMarkers)

        // 3. Append token text for leaf nodes (optional condition)
        // Condition: No children markers AND the range covers 1 or 2 tokens (heuristic for leaves)
        // Adjust condition as needed.
        if (children.isEmpty() && (markerInfo.end - markerInfo.start) <= 2 && markerInfo.end <= tokens.size) {
            val tokenText = (markerInfo.start until markerInfo.end)
                .mapNotNull { tokens.getOrNull(it)?.value }
                .joinToString(separator = " ") { "\"$it\"" } // Quote token values
            if (tokenText.isNotEmpty()) {
                sb.append("  | Tokens: $tokenText")
            }
        }
        sb.appendLine() // Finish the current node's line

        // 4. Recursively dump children
        children.forEach { child ->
            dumpNode(child, idToMarkerInfo, allMarkers, tokens, indent + 1, sb)
        }
    }

    private fun findChildren(
        parent: CompletedMarkerInfo,
        idToMarkerInfo: Map<Long, CompletedMarkerInfo>,
        allMarkers: List<CompletedMarkerInfo>
    ): List<CompletedMarkerInfo> {

        val directChildren = mutableListOf<CompletedMarkerInfo>()

        // --- Pass 1: Handle `precededMarkerId` ---
        // Find the node B that parent A precedes (`parent.precededMarkerId == B.id`)
        val precededNode = parent.precededMarkerId?.let { idToMarkerInfo[it] }
        if (precededNode != null && precededNode.start >= parent.start && precededNode.end <= parent.end) {
            // If parent A wraps B (which is typical for `precede`), add B as a direct child
            directChildren.add(precededNode)
        }
        // Note: Sometimes `precede` is used differently. If A replaces B in the tree,
        // B might not be a direct child here. This logic assumes A wraps B.

        // --- Pass 2: Find top-level nested children ---
        val potentialNested = allMarkers.filter { candidate ->
            candidate.id != parent.id && // Not the parent itself
                    candidate.start >= parent.start && candidate.end <= parent.end && // Is contained
                    candidate.id != precededNode?.id && // Not the node already added via precede link
                    // Crucially: Is not contained within any *other* direct child already found
                    directChildren.none { existingChild ->
                        candidate.id != existingChild.id && // Avoid self-comparison if candidate somehow got added
                                candidate.start >= existingChild.start && candidate.end <= existingChild.end
                    }
        }

        // Among potentialNested, keep only those that are not contained within *each other*
        // (Select the outermost layer of nested nodes)
        val topLevelNested = potentialNested.filter { candidate ->
            potentialNested.none { otherCandidate ->
                candidate.id != otherCandidate.id && // Not itself
                        candidate.start >= otherCandidate.start && candidate.end <= otherCandidate.end &&
                        // Strict containment check (not identical ranges)
                        !(candidate.start == otherCandidate.start && candidate.end == otherCandidate.end)
            }
        }

        directChildren.addAll(topLevelNested)

        // --- Sort children by start index ---
        return directChildren.sortedBy { it.start }
    }
}