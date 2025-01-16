package cengine.lang.obj

import cengine.editor.annotation.Annotation
import cengine.lang.asm.Initializer
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

abstract class ObjPsiFile(final override val file: VirtualFile, final override val manager: PsiManager<*, *>): PsiFile, Initializer {

    // PsiFile

    final override val children: List<PsiElement>
        get() = emptyList()
    final override var parent: PsiElement? = null

    final override val annotations: List<Annotation> = emptyList()
    final override val additionalInfo: String = ""
    final override var range: IntRange = IntRange.EMPTY

    override fun accept(visitor: PsiElementVisitor) {

    }

    // Initializer

    final override val id: String
        get() = file.name


}