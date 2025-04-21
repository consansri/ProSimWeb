package cengine.lang.asm

import cengine.lang.asm.psi.*
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.PsiBuilder
import cengine.psi.parser.PsiTreeParser
import cengine.psi.parser.StringExprParser
import cengine.psi.parser.pratt.ConfigurableExpressionParser

open class AsmParser(
    /** InstructionTypes which should get parsed */
    val instructionTypes: Map<String, AsmInstructionT>,
    /** DirectiveTypes which should get parsed */
    val directiveTypes: Map<String, AsmDirectiveT>,
) : PsiTreeParser, ConfigurableExpressionParser, StringExprParser {

    override val operatorConfig: ConfigurableExpressionParser.OpConfig
        get() = ConfigurableExpressionParser.OpConfig(
            *AsmLexer.OPERATORS.toTypedArray()
        )

    /** Character sequence terminating a label definition (e.g. ":"). */
    open val labelTerminator: String = ":"

    // --- Concrete Members & Default Implementations ---

    override fun PsiBuilder.parseFileContent() {
        io.debug { "parseFileContent" }
        while (!isAtEnd()) {
            parseLine()
        }
    }

    /**
     * Parses a single logical line of assembly code.
     * Handles optional labels, instructions/directives/expressions, and end-of-line markers.
     * Skips empty lines or lines containing only comments.
     */
    private fun PsiBuilder.parseLine() {
        skipWhitespaceAndComments()
        // Skip empty lines efficiently
        if (currentIs(PsiTokenType.LINEBREAK)) {
            advance() // Consume the linebreak
            return
        }
        // Also handle case where file ends after whitespace/comments
        if (isAtEnd()) {
            return
        }

        io.debug { "parseLine starting at: ${peek()}" }
        val statementMarker = mark()
        var contentParsed = false // Track if anything meaningful was parsed on the line

        // 1. Optional Label Definition
        // Check for IDENTIFIER followed immediately by the labelTerminator
        if (getTokenType() == PsiTokenType.IDENTIFIER && getTokenText(1) == labelTerminator) {
            // Use lookahead to avoid marking+rolling back if it's not a label
            if (parseLabelDefinition()) { // This function now handles its own marker
                contentParsed = true
                skipWhitespaceAndComments() // Skip whitespace after the label colon
            }
            // If parseLabelDefinition returned false, it handled rollback/error
        }

        // 2. Instruction, Directive, or Expression (if not end of line yet)
        if (!currentIs(PsiTokenType.LINEBREAK, PsiTokenType.EOF)) {
            if (parseInstructionOrDirective()) { // This now returns true if something was attempted/parsed
                contentParsed = true
            }
            // If it returned false, it means nothing could be parsed at the current position
            // (e.g., unexpected token at the start of instruction/directive/expression part)
        }

        // 3. End of Line Handling & Error Recovery
        skipWhitespaceAndComments()
        if (currentIs(PsiTokenType.LINEBREAK)) {
            advance() // Consume the expected linebreak
        } else if (!isAtEnd()) {
            // Found unexpected tokens before EOF. This indicates a syntax error on the line.
            error("Unexpected token(s): '${peek()?.value}'. Expected linebreak or end of file.")
            contentParsed = true // Mark line as having content, even if erroneous
            // Consume tokens until the end of the line or file is reached
            while (!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) {
                advance()
            }

            if (currentIs(PsiTokenType.LINEBREAK)) {
                advance() // Consume the terminating linebreak
            }
        }
        // else: Correctly positioned at EOF

        // 4. Finalize the AsmLine marker
        if (contentParsed) {
            // Only complete the marker if we parsed a label, instr, directive, expr, or encountered errors after content
            statementMarker.done(AsmLine)
            io.debug { "Completed AsmLine marker ${statementMarker.id}" }
        } else {
            // Line contained only whitespace/comments, or failed label parse before linebreak/EOF
            statementMarker.drop()
            io.debug { "Dropped empty/comment-only line marker ${statementMarker.id}" }
        }
    }

    /**
     * Parses a label definition (e.g., "myLabel:").
     * Returns true if successful, false otherwise.
     */
    private fun PsiBuilder.parseLabelDefinition(): Boolean {
        io.debug { "parseLabelDef" }
        val labelMarker = mark()
        // We already confirmed IDENTIFIER and ':' via lookahead in parseLine
        // So expect calls should succeed unless internal state is wrong
        if (expect(PsiTokenType.IDENTIFIER, "Internal Error: Expected label name") &&
            expect(labelTerminator, "Internal Error: Expected '$labelTerminator' after label name")
        ) {
            labelMarker.done(AsmLabelDecl)
            return true
        } else {
            // Should ideally not happen if lookahead was correct. Error already reported by expect.
            labelMarker.rollbackTo() // Rollback marker on unexpected failure
            return false
        }
    }

    /**
     * Parses either an instruction or a directive based on the first token.
     * Returns true if successful, false otherwise.
     */
    private fun PsiBuilder.parseInstructionOrDirective(): Boolean {
        io.debug { "parseInstrOrDirective starting at: ${peek()?.value}" }
        skipWhitespaceAndComments() // Ensure we start at a significant token
        val firstToken = peek() ?: return false // Nothing left on the line

        // --- Case 1: Keyword Found (Instruction or Directive) ---
        if (firstToken.type == PsiTokenType.KEYWORD) {
            val marker = mark() // Start marker for the instruction/directive node
            advance() // Consume keyword

            val keywordText = firstToken.value
            io.debug { "Found keyword: $keywordText" }

            val instrType = instructionTypes[keywordText]
            if (instrType != null) {
                io.debug { "Parsing as instruction: $keywordText" }
                with(instrType) {
                    // Assume instrType.parse is responsible for completing/dropping the marker
                    parse(this@AsmParser, marker)
                }
                return true // Keyword processed
            }

            val directiveType = directiveTypes[keywordText]
            if (directiveType != null) {
                io.debug { "Parsing as directive: $keywordText" }
                with(directiveType) {
                    // Assume directiveType.parse is responsible for completing/dropping the marker
                    parse(this@AsmParser, marker)
                }
                return true // Keyword processed
            }

            // Keyword not recognized
            error("Unknown instruction or directive keyword: '$keywordText'")
            // Marker is completed as error, keyword consumed
            return true // Indicate keyword was handled (as an error)
        }

        // --- Case 2: No Keyword - Attempt Expression Parsing ---
        io.debug { "No keyword found, attempting to parse as expression." }
        // Use the Pratt parser's entry point. It returns a completed marker or null.
        val exprMarker = parseExpression() // This function handles its own markers
        if (exprMarker != null) {
            io.debug { "Successfully parsed expression marker ${exprMarker.id}" }
            return true // Expression parsed successfully
        } else {
            io.debug { "Failed to parse expression at: ${peek()?.value}" }
            // parseExpression should ideally report specific errors if it fails
            // If it returns null without error, maybe report a generic one here?
            // error("Expected instruction, directive, or expression") // Optional
            return false // Failed to parse as expression
        }
    }

    override fun PsiBuilder.atomParser(): PsiBuilder.Marker? {
        skipWhitespaceAndComments()
        val currentToken = peek() ?: return null // EOF
        io.debug { "parseAtom -> ${currentToken.value} (${currentToken.type.typeName})" }

        val type = currentToken.type

        // --- Handle Parenthesized Expressions ---
        if (type == PsiTokenType.PUNCTUATION && currentToken.value == "(") {
            val parenMarker = mark() // Marker for the entire grouped expression
            advance() // Consume '('
            val exprMarker = parseExpression(0) // Parse expression inside parens
            if (exprMarker == null) {
                // Error reported by parseExpression or if nothing found
                error("Expected expression inside parentheses") // Add specific error if needed
                // Decide on recovery: drop marker? complete as error?
                error("Incomplete parenthesized expression") // Complete as error node
                // Skip until ')' maybe? For now, rely on outer parser recovery.
                return parenMarker // Return the error marker
            }
            if (!expect(")", "Expected ')' after expression in parentheses")) {
                // Error reported by expect. The inner expression (exprMarker) is valid.
                // Complete the parenMarker as an error containing the valid inner expression.
                error("Missing closing parenthesis")
                return parenMarker // Return the error marker
            }
            // Success: ')' was found and consumed
            parenMarker.done(PsiStatement.Expr.Grouped) // Create explicit Grouped node
            // Alternative (common Pratt): Drop parenMarker, return exprMarker directly.
            // If using alternative:
            // parenMarker.drop()
            // return exprMarker
            return parenMarker // Return the Grouped marker
        }

        // --- Handle Other Atoms ---
        val marker = mark() // Start marker for other atom types

        if (type == PsiTokenType.IDENTIFIER) {
            advance()
            marker.done(PsiStatement.Expr.Identifier)
            return marker
        }

        if (type == PsiTokenType.KEYWORD) {
            if (currentToken.value == "true" || currentToken.value == "false") {
                advance()
                marker.done(PsiStatement.Expr.Literal.Bool)
                return marker
            }
        }

        if (type !is PsiTokenType.LITERAL) {
            // Not parentheses, not identifier, not a literal -> cannot start an atom here.
            marker.drop()
            return null
        }

        // --- Handle Literals ---
        when (type) {
            // Handle BOOL if present in your lexer
            is PsiTokenType.LITERAL.CHAR -> {
                advance()
                marker.done(PsiStatement.Expr.Literal.Char)
                return marker
            }

            is PsiTokenType.LITERAL.FP.DOUBLE -> {
                advance()
                marker.done(PsiStatement.Expr.Literal.FloatingPoint.Double)
                return marker
            }

            is PsiTokenType.LITERAL.FP.FLOAT -> {
                advance()
                // Decide if Float maps to Float or Double node type
                marker.done(PsiStatement.Expr.Literal.FloatingPoint.Float) // Or Double
                return marker
            }

            is PsiTokenType.LITERAL.INTEGER -> {
                // Could check specific type (Hex, Dec, Bin, Oct) if needed for node type
                advance()
                marker.done(PsiStatement.Expr.Literal.Integer)
                return marker
            }

            PsiTokenType.LITERAL.STRING.MlStart, PsiTokenType.LITERAL.STRING.SlStart -> {
                // Correct: parseString handles its own marker. Drop the tentative atom marker.
                marker.drop()
                return parseString() // parseString() returns Marker?
            }

            else -> {
                // It IS a literal, but not one handled above (e.g., maybe a custom literal type?)
                // Don't rollback. Report error or just fail atom parse.
                error("Unsupported literal type encountered: ${type.typeName}")
                advance() // Consume the unsupported literal token
                return marker // Return marker marked as error
                // Alternative: marker.drop(); return null
            }
        }
    }

    // For .type arguments like @function
    fun PsiBuilder.parseTypeDescriptor(): PsiBuilder.Marker? {
        skipWhitespaceAndComments()
        if (currentIs("@")) {
            val marker = mark()
            advance() // Consume '@'
            // Expect an identifier after '@'
            if (getTokenType() == PsiTokenType.IDENTIFIER) {
                advance()
                marker.done(AsmTypeDescriptor)
                return marker
            } else {
                error("Expected identifier after '@' for type descriptor")
                marker.drop()
                return null
            }
        }
        return null
    }

    fun PsiBuilder.parseIdentifier(): PsiBuilder.Marker? {
        skipWhitespaceAndComments()
        if (getTokenType() == PsiTokenType.IDENTIFIER) {
            val marker = mark()
            advance()
            marker.done(PsiStatement.Expr.Identifier) // Or your specific Identifier expression type
            return marker
        }
        return null
    }

    fun PsiBuilder.parseIntegerLiteral(): PsiBuilder.Marker? {
        skipWhitespaceAndComments()
        if (getTokenType() is PsiTokenType.LITERAL.INTEGER) {
            val marker = mark()
            advance()
            marker.done(PsiStatement.Expr.Literal.Integer) // Or your specific Int literal type
            return marker
        }
        return null
    }

}