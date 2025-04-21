package cengine.psi.parser

import cengine.psi.lexer.PsiToken

/** Parses a prefix expression construct (literals, prefix operators, grouping, custom constructs) */
interface PrefixParselet {
    /**
     * Attempt to parse a prefix element.
     * @param builder The PsiBuilder instance.
     * @param token The token that triggered this parselet.
     * @return true if parsing was successful (and tokens were consumed, marker completed), false otherwise.
     * Implementations MUST complete or drop markers appropriately.
     */
    fun parse(builder: PsiBuilder, token: PsiToken): Boolean
}