package cengine.lang.asm.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.util.collection.firstInstance

/**
 * Represents a single line in the assembly file.
 * Can contain an optional label, an optional instruction/directive, and potentially comments/whitespace.
 */
class AsmLine(range: IntRange, vararg children: PsiElement) : PsiStatement(AsmLine, range, *children) {

    val label: AsmLabelDecl? = children.firstInstance<AsmLabelDecl>()
    val instruction: AsmInstruction? = children.firstInstance<AsmInstruction>()
    val directive: AsmDirective? = children.firstInstance<AsmDirective>()
    val expr: Expr? = children.firstInstance<Expr>()

    // Comments might be attached directly or filtered from children

    companion object : PsiStatementTypeDef {
        override val typeName: String = "Line"
        override val builder: NodeBuilderFn = { markerInfo, children, range ->
            AsmLine(range, *children)
        }
    }


}