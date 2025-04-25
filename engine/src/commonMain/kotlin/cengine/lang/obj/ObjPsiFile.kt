package cengine.lang.obj

import cengine.lang.asm.AsmBinaryProvider
import cengine.psi.elements.PsiFile
import cengine.psi.visitor.PsiElementVisitor
import cengine.vfs.VirtualFile

abstract class ObjPsiFile(file: VirtualFile, valid: Boolean) : PsiFile(file, valid), AsmBinaryProvider {

    // PsiFile

    final override val additionalInfo: String = ""

    override fun accept(visitor: PsiElementVisitor) {

    }

    // Initializer

    final override val id: String
        get() = file.name


}