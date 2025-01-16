package cengine.lang.obj

import cengine.lang.asm.Disassembler
import cengine.psi.PsiManager
import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import cengine.vfs.VirtualFile
import emulator.kit.memory.Memory

class InvalObjFile(file: VirtualFile, manager: PsiManager<*,*>): ObjPsiFile(file, manager) {
    override fun initialize(memory: Memory<*, *>) {

    }

    override fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<Disassembler.Label>>> {
        return emptyMap()
    }
}