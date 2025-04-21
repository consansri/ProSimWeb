package cengine.psi.parser.lookahead

import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.PsiBuilder

interface LookaheadSupport {

    /** Checks if the next non-whitespace/comment tokens match the sequence. Does NOT advance. */
    fun PsiBuilder.matches(vararg types: PsiTokenType): Boolean {
        var lookahead = 0
        for (type in types) {
            var currentToken: PsiToken?
            var offset = 0
            do { // Skip whitespace/comments for lookahead comparison
                currentToken = peek(lookahead + offset)
                offset++
            } while (currentToken != null && (currentToken.type is PsiTokenType.WHITESPACE || currentToken.type is PsiTokenType.COMMENT))

            if (currentToken?.type != type) return false
            lookahead += offset
        }
        return true
    }

}