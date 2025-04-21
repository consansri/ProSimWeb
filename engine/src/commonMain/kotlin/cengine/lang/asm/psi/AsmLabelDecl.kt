package cengine.lang.asm.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.feature.Highlightable
import cengine.psi.feature.Named
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.style.CodeStyle

/**
 * Represents a label declaration (e.g., "my_label:").
 */
class AsmLabelDecl(
    range: IntRange,
    vararg children: PsiElement,
) : PsiStatement(AsmLabelDecl, range, *children), Named, Highlightable {
    val identifierToken = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.IDENTIFIER || it.type == PsiTokenType.LITERAL.INTEGER.Dec }
    override val name: String? = identifierToken?.value
    val isLocal = identifierToken?.type is PsiTokenType.LITERAL.INTEGER

    companion object : PsiStatementTypeDef {
        override val typeName: String = "LabelDecl"
        override val builder: NodeBuilderFn = { markerInfo, children, range ->
            if (
                children.size == 2
                && (children[0].type == PsiTokenType.IDENTIFIER || children[0].type is PsiTokenType.LITERAL.INTEGER)
                && children[1].type == PsiTokenType.PUNCTUATION
            ) {
                AsmLabelDecl(range, *children)
            } else null
        }
    }

    override val style: CodeStyle get() = CodeStyle.identifier
}