package cengine.lang.asm.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken

class AsmTypeDescriptor(range: IntRange, vararg children: PsiElement) : PsiStatement(AsmTypeDescriptor, range, *children) {

    val at = children[0] as PsiToken
    val typeDescriptorName = children[1] as PsiToken

    companion object : PsiStatementTypeDef {
        override val typeName: String = "TypeDescriptor"
        override val builder: NodeBuilderFn = { markerInfo, children, range ->
            if(children.size == 2) {
                AsmTypeDescriptor(range, *children)
            }else null
        }
    }

}