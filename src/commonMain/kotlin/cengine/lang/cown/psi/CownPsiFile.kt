package cengine.lang.cown.psi

import cengine.editor.annotation.Annotation
import cengine.lang.cown.CownLang
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

class CownPsiFile(override val file: VirtualFile, override val manager: PsiManager<*, *>): PsiFile {
    override var parent: PsiElement? = null
    override val children: MutableList<PsiElement> = mutableListOf()
    override val additionalInfo: String = ""

    override val annotations: List<Annotation> = listOf()
    override var range: IntRange = 0..<file.getContent().size

    override fun accept(visitor: PsiElementVisitor) {

    }
}