package cengine.psi.elements

import cengine.editor.annotation.Annotation
import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementType

class PsiError(val errorMessage: String, override var range: IntRange, type: PsiElementType = PsiError, vararg children: PsiElement) : PsiElement(type, *children) {


    override val annotations: MutableList<Annotation> = mutableListOf(Annotation.error(this, errorMessage))

    companion object : PsiElementType {
        override val typeName = "PsiError"
    }

    data class ErrorT(val message: String) : PsiStatement.PsiStatementTypeDef {

        override val typeName: String = "error($message)"

        override val builder: NodeBuilderFn = { markerInfo, children, range ->
            PsiError(message, range, this@ErrorT, *children)
        }
    }

}