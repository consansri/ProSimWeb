package cengine.psi.parser.pratt

import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.PsiBuilder

/**
 * Interface for parsers that want to use the Pratt parsing strategy for expressions.
 * The parser implementation needs to provide the binding powers (precedence) and
 * the parsing logic (nud/led) specific to the language's operators and expression atoms.
 */
interface PrattParserSupport: ExpressionParser {

    // --- Methods to be Implemented by the Language Parser ---

    /**
     * Gets the Null Denotation (prefix/atom) binding power for the given *significant* token type.
     * Return -1 if the token type cannot start an expression.
     * This is checked *after* skipping whitespace/comments.
     */
    fun PsiBuilder.getNudBindingPower(tokenType: PsiTokenType): Int

    /**
     * Gets the Left Denotation (infix/postfix) binding power for the given *significant* token type.
     * Return -1 if the token type cannot act as an infix/postfix operator.
     * This is checked *after* skipping whitespace/comments.
     */
    fun PsiBuilder.getLedBindingPower(tokenType: PsiTokenType): Int

    /**
     * Parses a Null Denotation (prefix or atomic expression) given the starting *significant* token.
     *
     * This function MUST:
     * 1. Consume the starting `token` (and potentially subsequent tokens) using `builder.advance()`.
     * 2. Create and return a completed `Marker` for the parsed element.
     * 3. Report errors using `builder.error()` and return `null` on failure.
     *
     * It does NOT need to handle whitespace before the `token` itself.
     *
     * @param token The significant token that triggers this nud parse (already identified).
     * @param bp The binding power associated with this token's nud action (provided for context, e.g., for prefix operators needing to parse operands).
     * @return A completed Marker for the parsed element, or null on failure.
     */
    fun PsiBuilder.parseNud(token: PsiToken, bp: Int): PsiBuilder.Marker?

    /**
     * Parses a Left Denotation (infix or postfix expression) given the *significant* operator token
     * and the marker for the left-hand side expression.
     *
     * This function MUST:
     * 1. Consume the `operatorToken` (and potentially subsequent tokens, like a RHS) using `builder.advance()`.
     * 2. Create a *new* marker that `precede()`s the `leftMarker`.
     * 3. Complete the new preceding marker (e.g., as a BinaryExpression).
     * 4. Report errors using `builder.error()` and return `null` on failure (leaving `leftMarker` potentially valid).
     *
     * It does NOT need to handle whitespace before the `operatorToken` itself.
     *
     * @param operatorToken The significant token triggering this led parse (e.g., the infix/postfix operator).
     * @param leftMarker The marker representing the expression parsed immediately to the left.
     * @param bp The binding power of the `operatorToken` (provided for context, e.g., for parsing RHS with correct precedence).
     * @return A completed Marker for the new combined element (e.g., BinaryExpression), or null on failure.
     */
    fun PsiBuilder.parseLed(operatorToken: PsiToken, leftMarker: PsiBuilder.Marker, bp: Int): PsiBuilder.Marker?

    /**
     * Optional: Provides a context-specific error message when a token cannot start an expression.
     */
    fun PsiBuilder.errorExpectedExpression(foundToken: PsiToken?) {
        val found = foundToken?.let { "'${it.value}' (${it.type.typeName})" } ?: "end of file"
        error("Expected an expression, but found $found.")
    }

    /**
     * Optional: Provides a context-specific error message when an unexpected token is found
     * where an expression atom or prefix operator was expected.
     */
    fun PsiBuilder.errorUnexpectedTokenInExpression(foundToken: PsiToken) {
        error("Unexpected token '${foundToken.value}' (${foundToken.type.typeName}) is not valid at the start of an expression.")
    }

    /**
     * The core Pratt parsing function, implemented for parsers implementing [PrattParserSupport].
     *
     * Parses an expression using the precedence climbing algorithm. Handles whitespace/comment skipping.
     * It drives the process by calling the language-specific `getNud/LedBindingPower` and `parseNud/Led` methods.
     *
     * @param this The PrattParserSupport implementer (which holds the builder).
     * @param minBp The minimum binding power (precedence level) required for an operator
     * to be processed in the current context.
     * @return A Marker representing the parsed expression, or null if no expression could be parsed.
     * The marker returned is the *final* marker for the expression parsed at this level.
     */
    override fun PsiBuilder.parseExpression(minBp: Int): PsiBuilder.Marker? {
        skipWhitespaceAndComments()
        val currentSignificantToken = peek() ?: run {
            errorExpectedExpression(null) // Expected expression, found EOF
            return null
        }
        val currentSignificantType = currentSignificantToken.type

        // --- 1. Parse Nud (Prefix / Atom) ---
        val nudBp = getNudBindingPower(currentSignificantType)
        if (nudBp == -1) {
            errorUnexpectedTokenInExpression(currentSignificantToken)
            return null // Token cannot start an expression
        }

        // Call the language-specific NUD parser. It MUST advance the builder.
        var leftMarker = parseNud(currentSignificantToken, nudBp) ?: return null // Error handled within parseNud

        // --- 2. Loop for Led (Infix / Postfix) ---
        while (!isAtEnd()) {
            // Find the next significant token for potential operator
            skipWhitespaceAndComments()
            val potentialOpToken = peek() ?: break // Reached EOF, expression finished
            val potentialOpType = potentialOpToken.type

            val ledBp = getLedBindingPower(potentialOpType)

            // Stop if the operator's precedence is not high enough
            // or if the token doesn't have a led parser (ledBp == -1)
            if (ledBp == -1 || ledBp < minBp) {
                break
            }

            // We have a valid infix/postfix operator. Call the language-specific LED parser.
            // It MUST advance the builder past the operator (and RHS if applicable).
            val newLeft = parseLed(potentialOpToken, leftMarker, ledBp)
            if (newLeft == null) {
                // parseLed reported an error. It should have advanced past the problematic operator
                // (or recovered). We return the expression parsed *before* the error.
                return leftMarker
            }
            // Update the left marker to the result of the LED parse (e.g., the BinaryExpression marker)
            leftMarker = newLeft
        }

        // Return the marker representing the fully parsed expression at this precedence level
        return leftMarker
    }
}