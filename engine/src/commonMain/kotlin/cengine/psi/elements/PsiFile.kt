package cengine.psi.elements

import cengine.psi.core.FileBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiFileTypeDef
import cengine.psi.feature.Named
import cengine.psi.visitor.PsiElementVisitor
import cengine.vfs.VirtualFile

/**
 * Represents a file in the PSI structure
 */
open class PsiFile(val file: VirtualFile, val valid: Boolean, type: PsiFileTypeDef? = null, vararg children: PsiElement) : PsiElement(type ?: PsiFile, *children), Named {

    override val name: String get() = file.name

    override var range: IntRange = file.getAsUTF8String().indices

    companion object : PsiFileTypeDef {
        override val builder: FileBuilderFn = { file, valid, children ->
            PsiFile(file, valid, PsiFile, *children)
        }
        override val typeName: String = "PsiFile"

    }

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }
}