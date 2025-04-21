package cengine.psi.parser.pratt

import cengine.psi.parser.PsiBuilder

/**
 * Defines the core contract for parsing expressions within a PsiBuilder context.
 * Implementations are responsible for consuming tokens from the builder and
 * producing a Marker representing the parsed expression structure, respecting
 * language-specific syntax and operator precedence.
 */
interface ExpressionParser {

    /**
     * Parses an expression starting from the current position in the PsiBuilder,
     * respecting operator precedence up to a specified minimum binding power.
     *
     * This is the main entry point for initiating expression parsing.
     *
     * @receiver The PsiBuilder instance providing the token stream and marker capabilities.
     * @param minBp The minimum binding power (precedence level) an operator needs to have
     * to be considered in the current parsing context. Typically starts at 0
     * for a top-level expression parse.
     * @return A completed [Marker] representing the root of the parsed expression tree,
     * or `null` if no valid expression could be parsed starting at the current
     * position (an error should have been reported to the builder in this case).
     */
    fun PsiBuilder.parseExpression(minBp: Int = 0): PsiBuilder.Marker?

}