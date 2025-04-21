package cengine.lang.mif

import cengine.lang.asm.AsmBinaryProvider
import cengine.lang.asm.AsmDisassembler
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementType
import cengine.psi.elements.PsiFile
import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import cengine.vfs.VirtualFile
import emulator.kit.memory.Memory

class MifPsiFile(file: VirtualFile, valid: Boolean, vararg children: PsiElement, type: PsiElementType? = null): PsiFile(file, valid, MifPsiFile, *children), AsmBinaryProvider {

    companion object: PsiElementType{
        override val typeName: String = "MifPsiFile"

    }

    override val id: String = this.type.typeName

    override fun initialize(memory: Memory<*, *>) {
        TODO()
    }

    override fun entry(): IntNumber<*> = TODO()

    override fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<AsmDisassembler.Label>>> {
        TODO("Not yet implemented")
    }

}