package cengine.psi.parser

import cengine.psi.lexer.PsiToken

/** Parses an infix expression construct (binary operators, function calls, indexing, etc.) */
interface InfixParselet {
    /**
     * Attempt to parse an infix element, given the left-hand side expression already parsed.
     * @param builder The PsiBuilder instance.
     * @param leftMarkerId The marker ID of the completed left-hand-side expression.
     * @param token The token that triggered this parselet (the infix operator/symbol).
     * @return The marker ID of the *newly completed* infix expression marker if successful, null otherwise.
     * Implementations MUST handle preceding the `leftMarkerId` and completing the new marker.
     */
    fun parse(builder: PsiBuilder, leftMarkerId: Long, token: PsiToken): Long?

    /** The binding power (precedence) of this infix operator. */
    fun getPrecedence(): Int
}