package cengine.psi.parser

import cengine.console.IOContext
import cengine.console.SysOut
import cengine.psi.core.PsiElementTypeDef
import cengine.psi.core.PsiFileTypeDef
import cengine.psi.elements.PsiFile
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

class PsiBuilder(initialTokens: List<PsiToken>, val psiFileType: PsiFileTypeDef = PsiFile, val io: IOContext = SysOut) {

    // Use a unique ID generator for markers
    @OptIn(ExperimentalAtomicApi::class)
    private val markerIdCounter = AtomicLong(0)

    private val tokens: List<PsiToken> = initialTokens // Consider filtering whitespace/comments upfront? Or handle dynamically. Current approach is flexible.

    var current: Int = 0
        private set

    // Use ArrayDeque if stack operations (addLast, removeLast) are dominant and indexing isn't frequent.
    // Stick with MutableList if indexed access (like in rollback) is common or simpler. Let's keep MutableList for now.
    private val activeMarkers = mutableListOf<Marker>()

    // Store the final results here
    val completedMarkers = mutableListOf<CompletedMarkerInfo>()
    val errors = mutableListOf<IndexedParseError>()

    // --- Token Access ---

    fun isAtEnd(): Boolean = current >= tokens.size || tokens[current].type == PsiTokenType.EOF

    fun peek(lookAhead: Int = 0): PsiToken? {
        val idx = current + lookAhead
        return if (idx < tokens.size) tokens[idx] else null
    }

    fun getTokenType(lookAhead: Int = 0): PsiTokenType? = peek(lookAhead)?.type
    fun getTokenText(lookAhead: Int = 0): String? = peek(lookAhead)?.value

    // --- Advancement ---

    /**
     * Advances the current position and returns the previous token
     */
    fun advance(): PsiToken? {
        if (!isAtEnd()) {
            val token = tokens[current]
            current++
            return token
        }
        return null
    }

    /** Advances the pointer *past* any whitespace or comment tokens. */
    fun skipWhitespaceAndComments(skipLinebreaks: Boolean = false) {
        while (!isAtEnd()) {
            val type = getTokenType()
            if (type is PsiTokenType.WHITESPACE || type is PsiTokenType.COMMENT || (skipLinebreaks && type == PsiTokenType.LINEBREAK)) {
                advance()
            } else {
                break
            }
        }
    }

    // --- ErrorHandling ---

    fun error(message: String) {
        val errorTokenIndex = current // Capture the index *before* potential peeking/advancing
        val range = peek()?.range ?: tokens.getOrNull(errorTokenIndex - 1)?.range ?: (0..0) // Adjusted range finding slightly
        // Ensure tokenIndex is valid
        val validTokenIndex = if (errorTokenIndex < tokens.size) errorTokenIndex else tokens.size - 1

        errors.add(IndexedParseError(message, range, validTokenIndex)) // Store the richer info
    }

    /**
     * Consumes the next token if it matches the expected type, otherwise reports an error.
     * NOTE: This does NOT skip whitespace/comments. The parser should call skipWhitespaceAndComments() first if needed.
     */
    fun expect(expected: PsiTokenType, errorMessage: String? = null): Boolean = currentIs(expected).apply {
        if (this) advance() else error(errorMessage ?: "Expected ${expected.typeName} but found ${getTokenType()?.typeName}")
    }

    /**
     * Consumes the next token if it matches the expected [PsiTokenType]s, otherwise reports an error.
     * NOTE: This does NOT skip whitespace/comments.
     */
    fun expect(vararg expectedTypes: PsiTokenType, errorMessage: String? = null): Boolean = currentIs(*expectedTypes).apply {
        if (this) advance() else error(errorMessage ?: "Expected one of ${expectedTypes.joinToString(" or ") { it.typeName }} but found ${getTokenType()?.typeName}")
    }

    /**
     * Consumes the next token if it matches the expected value (as String), otherwise reports an error.
     * NOTE: This does NOT skip whitespace/comments.
     */
    fun expect(expectedValue: String, errorMessage: String? = null, ignoreCase: Boolean = false): Boolean = currentIs(expectedValue, ignoreCase = ignoreCase).apply {
        if (this) advance() else error(errorMessage ?: "Expected '$expectedValue' but found '${getTokenText()}'")
    }

    /**
     * Consumes the next token if it matches the expected [PsiTokenType]s, otherwise reports an error.
     * NOTE: This does NOT skip whitespace/comments.
     */
    fun expect(vararg expectedValues: String, errorMessage: String? = null): Boolean = currentIs(*expectedValues).apply {
        if (this) advance() else error(errorMessage ?: "Expected one of ${expectedValues.joinToString(" or ") { "'$it'" }} but found '${getTokenText()}'.")
    }

    // --- Marker API ---

    /**
     * Represents a potential PSI element during parsing.
     * Markers should only be manipulated via PsiBuilder methods (`done`, `drop`, `precede`, `rollbackTo`).
     */
    inner class Marker(
        internal val id: Long,
        internal val start: Int,
    ) {

        // These flags prevent misuse but shouldn't be part of the primary user interaction model.
        internal var completed: Boolean = false
        internal var explicitlyDropped: Boolean = false
        internal var precededMarkerId: Long? = null // Set by `precede()` on the *new* marker

        private var elementType: PsiElementTypeDef? = null

        fun precededBy(idToPrecede: Long) {
            this.precededMarkerId = idToPrecede
        }

        /**
         * Creates a new marker that logically precedes this one (e.g., for wrapping in a binary expression).
         * The new marker starts at the same position as this marker.
         * The new marker MUST be completed (`done`) or dropped (`drop`) later.
         *
         * Usage (Pratt Parsing Example):
         * ```kotlin
         * // marker representing the left-hand expression (already created)
         * val leftMarker: Marker = parsePrimaryExpression()
         * // ... encounter an infix operator ...
         * val operationMarker = leftMarker.precede() // Create a new marker starting at the same place
         * // ... parse the operator and right-hand expression ...
         * operationMarker.done(Type.Expression.Binary) // Complete the wrapping marker
         * ```
         *
         * @return The new marker that precedes this one.
         */
        fun precede(): Marker {
            // Create the new marker
            val newMarker = this@PsiBuilder.createMarkerObject(this.start) // New marker starts at the same place
            newMarker.precededMarkerId = this.id // Link the new marker back to *this* marker

            // Add the new marker to the active stack. Adding to the end is simpler
            // and sufficient, as the tree builder relies on `precededMarkerId`.
            activeMarkers.add(newMarker)
            io.debug { "Marker ${newMarker.id} Precedes Marker ${this.id} (Started at ${newMarker.start})" }
            return newMarker
        }

        /**
         * Completes this marker, marking the corresponding token range as a PSI element
         * of the given [type]. Removes the marker from the active stack.
         * This is a terminal operation for this Marker instance.
         */
        fun done(type: PsiElementTypeDef) {
            if (completed || explicitlyDropped) {
                error("Internal Error: Marker $id already completed ($completed) or dropped ($explicitlyDropped). Cannot call done().")
                // Potentially throw an exception in debug/test modes?
                return
            }
            if (!activeMarkers.contains(this)) {
                error("Internal Error: Trying to complete marker $id which is not active (completed/dropped/rolled back?).")
                return
            }

            // Remove from active first to prevent issues if error occurs during completion recording
            activeMarkers.remove(this) // Simple removal by object identity
            this.completed = true

            val end = this@PsiBuilder.current // End index is exclusive

            val info = CompletedMarkerInfo(
                id = this.id,
                start = this.start,
                end = end,
                elementType = type,
                precededMarkerId = this.precededMarkerId // Pass the link info
            )
            completedMarkers.add(info)

            io.debug { "Marker $id Done: ${type.typeName} [${this.start}..$end). Precedes: ${this.precededMarkerId ?: "None"}" }
        }

        /**
         * Abandons this marker. Removes it from the active stack. No PSI element will be created.
         * This is a terminal operation for this Marker instance.
         */
        fun drop() {
            if (completed || explicitlyDropped) {
                error("Internal Error: Marker $id already completed or dropped. Cannot call drop().")
                return
            }
            if (!activeMarkers.contains(this)) {
                error("Internal Error: Trying to drop marker $id which is not active (completed/dropped/rolled back?).")
                return
            }

            // Remove from active stack
            activeMarkers.remove(this)
            this.explicitlyDropped = true

            io.debug { "Marker $id Dropped: Covered tokens from $start up to ${this@PsiBuilder.current}" }
            // Dropped markers are usually not added to results, unless needed for complex error recovery.
        }

        /**
         * Resets the PsiBuilder's state to the point where this marker was created.
         * Undoes token advancements, removes subsequent markers (active & completed),
         * and removes subsequent errors. Used for backtracking.
         * The marker itself is removed from the active stack as part of the rollback.
         * This is a terminal operation for this Marker instance (it's effectively discarded).
         */
        fun rollbackTo() {
            if (completed) {
                error("Internal Error: Cannot rollback marker $id because it was already completed.")
                return
            }
            if (explicitlyDropped) {
                error("Internal Error: Cannot rollback marker $id because it was already dropped.")
                return
            }

            val ownIndexInActive = activeMarkers.indexOf(this)
            if (ownIndexInActive == -1) {
                io.debug { "Marker $id rollback requested, but it's no longer active. Assuming already handled by outer rollback." }
                return // Already handled or invalid state
            }

            io.debug { "Rolling back to Marker $id (start index: $start)" }

            // 1. Reset Pointer
            this@PsiBuilder.current = this.start

            // 2. Remove Subsequent Active Markers (Including this one)
            // Iterate backwards for safe removal based on index >= ownIndexInActive
            for (i in activeMarkers.indices.reversed()) {
                if (i >= ownIndexInActive) {
                    val markerToRemove = activeMarkers.removeAt(i)
                    // Mark as dropped conceptually, even though they are just removed here
                    markerToRemove.explicitlyDropped = true // Mark state for safety, though object is inaccessible
                    io.debug { "  - Removing rolled-back active marker ${markerToRemove.id} (started at ${markerToRemove.start})" }
                }
            }

            // 3. Remove Subsequent Completed Markers
            completedMarkers.removeAll { completedInfo ->
                val shouldRemove = completedInfo.start >= this.start // Remove if started within rolled-back range
                if (shouldRemove) {
                    io.debug { "  - Removing rolled-back completed marker ${completedInfo.id} (${completedInfo.elementType.typeName} at [${completedInfo.start}..${completedInfo.end}))" }
                }
                shouldRemove
            }

            // 4. Remove Subsequent Errors
            errors.removeAll { errorPair ->
                val errorStartOffset = errorPair.startTokenIndex // Get start offset of error range
                val shouldRemove = errorStartOffset >= this.start // Remove if error occurred within rolled-back range
                if (shouldRemove) {
                    io.debug { "  - Removing rolled-back error: '${errorPair.message}' at ${errorPair.startTokenIndex}" }
                }
                shouldRemove
            }
            // No need to reset lastCompletedExpressionMarkerId anymore
        }
    }

    // Internal helper to create marker instance
    @OptIn(ExperimentalAtomicApi::class)
    private fun createMarkerObject(startIndex: Int): Marker {
        return Marker(markerIdCounter.incrementAndFetch(), startIndex)
    }

    /**
     * Creates a new marker starting at the current token position.
     * Add the marker to the active stack.
     *
     * @return The newly created Marker instance.
     */
    fun mark(startIndex: Int = current): Marker {
        val marker = createMarkerObject(startIndex)
        activeMarkers.add(marker)
        io.debug { "Marker ${marker.id} Started at $startIndex" }
        return marker
    }

    // --- Convenience ---

    fun currentIs(type: PsiTokenType) = getTokenType() == type
    fun currentIs(vararg types: PsiTokenType) = types.contains(getTokenType())
    fun currentIs(value: String, ignoreCase: Boolean = false) = getTokenText()?.equals(value, ignoreCase) == true
    fun currentIs(vararg values: String) = values.contains(getTokenText())

    fun advanceIf(type: PsiTokenType): Boolean = currentIs(type).apply { if (this) advance() }
    fun advanceIf(vararg types: PsiTokenType): Boolean = currentIs(*types).apply { if (this) advance() }
    fun advanceIf(value: String, ignoreCase: Boolean = false): Boolean = currentIs(value, ignoreCase).apply { if (this) advance() }
    fun advanceIf(vararg values: String) = currentIs(*values).apply { if (this) advance() }

    // --- Result ---

    /**
     * Finishes parsing and returns the collected results.
     * Reports an error if any markers are still active (indicating incomplete parsing logic).
     */
    fun getResult(): ParseResult {
        // Check for dangling markers - should be handled by the parser calling done/drop appropriately.
        if (activeMarkers.isNotEmpty()) {
            val remaining = activeMarkers.joinToString { "Marker ${it.id} (started at ${it.start})" }
            // This is usually a parser bug, make it clear.
            error("FATAL PARSER ERROR: Parsing finished but active markers remain: $remaining. Ensure all markers are completed or dropped.")
            // Optionally auto-drop them here to allow tree building, but the error is important.
            activeMarkers.forEach { it.drop() } // Attempt cleanup, but the error is the main point.
            activeMarkers.clear() // Ensure list is empty
        }

        return ParseResult(
            psiFileType = psiFileType,
            tokens = this.tokens, // Pass the original token list
            completedMarkers = this.completedMarkers.toList(), // Return immutable copies
            errors = this.errors.toList()
        ).apply {
            io.debug {
                """
                    --- Start PSI Parse Dump ---
                    ${PsiResultDebugger.dump(this)}
                    ---  End PSI Parse Dump  ---
                """.trimIndent()
            }
        }
    }
}