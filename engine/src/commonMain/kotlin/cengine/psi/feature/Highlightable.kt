package cengine.psi.feature

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiFile
import cengine.psi.style.CodeStyle
import cengine.psi.visitor.PsiElementVisitor
import cengine.util.integer.IntNumber.Companion.overlaps

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

    class Collector(val inRange: IntRange) : PsiElementVisitor {
        val styles = mutableListOf<AnnotatedString.Range<SpanStyle>>()

        override fun visitFile(file: PsiFile) {
            file.children.forEach {
                if (it.range.overlaps(inRange)) it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            element.children.forEach {
                if (it.range.overlaps(inRange)) it.accept(this)
            }

            if (element is Highlightable) {
                element.style?.let {
                    styles.add(AnnotatedString.Range(SpanStyle(color = it.color), element.range.first, element.range.last + 1))
                }
            }
        }
    }
}