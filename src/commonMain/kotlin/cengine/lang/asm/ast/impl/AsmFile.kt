package cengine.lang.asm.ast.impl

import cengine.editor.annotation.Annotation
import cengine.lang.asm.AsmLang
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

class AsmFile(override val file: VirtualFile, override val manager: PsiManager<*, *>, program: ASNode.Program) : PsiFile {

    var program: ASNode.Program = program
        private set

    override val annotations: List<Annotation>
        get() = program.annotations
    override val additionalInfo: String
        get() = program.additionalInfo

    override var parent: PsiElement?
        set(value) {
            // nothing
        }
        get() = program.parent
    override val children: List<PsiElement> get() = program.children

    override var range: IntRange = program.range
        set(value) {
            program.range = value
            field = value
        }
        get() = program.range

    fun getFormattedString(identSize: Int): String = program.getFormatted(identSize)

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }
}