package cengine.lang.mif.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.feature.Highlightable
import cengine.psi.lexer.PsiToken
import cengine.psi.style.CodeStyle

/** Represents a numeric value (could be decimal literal or identifier token). */
class MifNumericValue(range: IntRange, vararg children: PsiElement) : PsiStatement(MifNumericValue, range, *children), Highlightable {
    val valueToken: PsiToken = children.filterIsInstance<PsiToken>().first()

    override val style: CodeStyle?
        get() = CodeStyle.number

    companion object : PsiStatementTypeDef {
        override val typeName: String = "NumericValue"
        override val builder: NodeBuilderFn = { _, children, range ->
            if (children.size == 1 && children[0] is PsiToken) {
                MifNumericValue(range, *children)
            } else null
        }
    }
}