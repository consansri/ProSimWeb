package cengine.psi.elements

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementType
import cengine.psi.core.PsiElementTypeDef
import cengine.psi.feature.Named
import cengine.psi.visitor.PsiElementVisitor
import cengine.vfs.VirtualFile

/**
 * Represents a file in the PSI structure
 */
open class PsiFile(val file: VirtualFile, val valid: Boolean, type: PsiElementType? = null, vararg children: PsiElement) : PsiElement(type ?: PsiFile, *children), Named {

    constructor(psiFileT: PsiFileT, vararg children: PsiElement) : this(psiFileT.file, psiFileT.valid, psiFileT, *children)

    override val name: String get() = file.name

    override var range: IntRange = file.getAsUTF8String().indices

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }

    data class PsiFileT(val file: VirtualFile, val valid: Boolean) : PsiElementTypeDef {
        override val builder: NodeBuilderFn = { markerInfo, children, range -> PsiFile(this@PsiFileT, *children) }
        override val typeName: String = "PsiFile"
    }

    companion object : PsiElementType {
        override val typeName: String = "PsiFile"
    }
}