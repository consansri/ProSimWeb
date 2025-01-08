package cengine.psi.feature

import cengine.lang.asm.CodeStyle

/**
 * Interface to mark a [PsiElement] as having a defined highlighting color.
 */
interface Highlightable {
    val style: CodeStyle?

    enum class Type(val style: CodeStyle) {
        KEYWORD(CodeStyle.ORANGE),
        FUNCTION(CodeStyle.BLUE),
        IDENTIFIER(CodeStyle.MAGENTA),
        STRING(CodeStyle.ALTGREEN),
        COMMENT(CodeStyle.comment),
        NUMBER_LITERAL(CodeStyle.ALTBLUE)
    }
}