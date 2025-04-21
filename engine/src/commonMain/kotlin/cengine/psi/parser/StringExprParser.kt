package cengine.psi.parser

import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiTokenType
import com.ionspin.kotlin.bignum.integer.Quadruple

interface StringExprParser {

    /**
     * Parses statements within a string interpolation block, like `${ ... }`.
     * Stops when it encounters the BlockEnd token or the end of input.
     * NOTE: This current implementation simply advances past tokens. For actual
     * expression parsing within interpolation, you would likely recursively call
     * your main expression parser here.
     */
    fun PsiBuilder.parseInterpBlockStatements() {
        // TODO: Replace simple advance with actual expression parsing if needed.
        while (!isAtEnd() && !currentIs(PsiTokenType.LITERAL.STRING.INTERP.BlockEnd)) {
            // Check for EOF before advancing to prevent infinite loops if BlockEnd is missing
            if (peek() == null) {
                error("Unexpected end of file within interpolation block.")
                break
            }
            advance() // Consume the token within the block
        }
    }

    /**
     * Parses a complete string literal (single or multi-line) including delimiters.
     * It expects the current token to be either SlStart or MlStart.
     * Creates and completes a marker for the entire string literal node.
     *
     * @return The completed Marker for the string literal node, or null if parsing failed.
     */
    fun PsiBuilder.parseString(): PsiBuilder.Marker? {
        val startTokenType = getTokenType() // Check type *before* consuming

        // Determine string type and required end token
        val (isSingleLine, startToken, endToken, nodeType) = when (startTokenType) {
            PsiTokenType.LITERAL.STRING.SlStart -> Quadruple(true, PsiTokenType.LITERAL.STRING.SlStart, PsiTokenType.LITERAL.STRING.SlEnd, PsiStatement.Expr.Literal.String.SingleLine)
            PsiTokenType.LITERAL.STRING.MlStart -> Quadruple(false, PsiTokenType.LITERAL.STRING.MlStart, PsiTokenType.LITERAL.STRING.MlEnd, PsiStatement.Expr.Literal.String.MultiLine)
            else -> {
                // Not starting with a known string delimiter
                // Caller (e.g., atomParser) should handle this, parseString assumes it's called correctly.
                // error("Expected string literal start") // Optional: report error if called inappropriately
                return null // Indicate this isn't a string start
            }
        }

        // Start the marker now that we know it's a string
        val marker = mark()

        // 1. Consume Start Token
        if (!expect(startToken, "Expected string literal start delimiter")) {
            marker.drop() // Expect already reported error
            return null
        }

        // 2. Parse Content
        if (isSingleLine) {
            parseSlStringContent()
        } else {
            parseMlStringContent()
        }

        // 3. Consume End Token
        if (!expect(endToken, "Expected string literal end delimiter")) {
            // Still complete the marker as an error node containing parsed content + start token
            marker.done(nodeType) // Complete with the intended type
            // The error is already reported by expect()
            // The resulting node will span up to the point where the end token was expected
            return marker // Return the (potentially incomplete) marker
            // Alternative: marker.drop() if you don't want incomplete string nodes
            // return null
        }

        // 4. Success: Complete Marker
        marker.done(nodeType)
        return marker
    }

    /**
     * Parses content within a single-line string, stopping *before* the SlEnd token.
     */
    private fun PsiBuilder.parseSlStringContent() {
        while (!isAtEnd() && !currentIs(PsiTokenType.LITERAL.STRING.SlEnd)) {
            when {
                currentIs(PsiTokenType.LITERAL.STRING.INTERP.Single) -> parseInterpDirect()
                currentIs(PsiTokenType.LITERAL.STRING.INTERP.BlockStart) -> parseInterpBlock()
                currentIs(PsiTokenType.LITERAL.STRING.CONTENT.Basic) -> {
                    val marker = mark()
                    advance()
                    marker.done(PsiStatement.PsiStringElement.Basic)

                } // Consume content
                currentIs(PsiTokenType.LITERAL.STRING.CONTENT.Escaped) -> {
                    val marker = mark()
                    advance()
                    marker.done(PsiStatement.PsiStringElement.Escaped)
                }
                else -> {
                    // Found unexpected token or EOF before SlEnd
                    // Report error but let parseString handle the missing SlEnd
                    error("Unexpected token or structure within string literal: ${peek()?.value}")
                    break // Stop parsing content
                }
            }
        }
        // Loop finishes when SlEnd or EOF is encountered, or on error.
    }

    /**
     * Parses content within a multi-line string, stopping *before* the MlEnd token.
     */
    private fun PsiBuilder.parseMlStringContent() {
        while (!isAtEnd() && !currentIs(PsiTokenType.LITERAL.STRING.MlEnd)) {
            when {
                currentIs(PsiTokenType.LITERAL.STRING.INTERP.Single) -> parseInterpDirect()
                currentIs(PsiTokenType.LITERAL.STRING.INTERP.BlockStart) -> parseInterpBlock()
                currentIs(PsiTokenType.LITERAL.STRING.CONTENT.Basic, PsiTokenType.LITERAL.STRING.CONTENT.Escaped) -> advance() // Consume content
                // Add handling for line breaks if they are treated as separate tokens within multiline content
                // currentIs(PsiTokenType.LINEBREAK) -> advance()
                else -> {
                    // Found unexpected token or EOF before MlEnd
                    error("Unexpected token or structure within multi-line string literal: ${peek()?.value}")
                    break // Stop parsing content
                }
            }
        }
        // Loop finishes when MlEnd or EOF is encountered, or on error.
    }

    /** Parses direct interpolation: $Identifier */
    private fun PsiBuilder.parseInterpDirect() {
        val interpMarker = mark()
        if (expect(PsiTokenType.LITERAL.STRING.INTERP.Single) &&
            expect(PsiTokenType.IDENTIFIER, "Expected identifier after '$' in string interpolation")) {
            interpMarker.done(PsiStatement.PsiStringElement.Interpolated.InterpIdentifier)
        } else {
            // Error handled by expect, marker may contain only '$' if identifier missing
            error("Invalid direct string interpolation")
            interpMarker.drop()
        }
    }

    /** Parses block interpolation: ${ ... } */
    private fun PsiBuilder.parseInterpBlock() {
        val blockMarker = mark()
        if (!expect(PsiTokenType.LITERAL.STRING.INTERP.BlockStart)) {
            blockMarker.drop(); return // Should not happen if called correctly
        }
        parseInterpBlockStatements() // Consumes tokens inside the block
        if (!expect(PsiTokenType.LITERAL.STRING.INTERP.BlockEnd, "Expected closing brace '}' for interpolation block")) {
            error("Incomplete interpolation block")
            blockMarker.drop()
        } else {
            blockMarker.done(PsiStatement.PsiStringElement.Interpolated.InterpBlock)
        }
    }

}