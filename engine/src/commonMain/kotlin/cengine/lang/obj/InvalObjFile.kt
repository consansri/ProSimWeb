package cengine.lang.obj

import cengine.lang.asm.AsmDisassembler
import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import cengine.vfs.VirtualFile
import emulator.kit.memory.Memory

class InvalObjFile(file: VirtualFile) : ObjPsiFile(file, false) {

    override fun initialize(memory: Memory<*, *>) {}

    override fun entry(): IntNumber<*> = BigInt.ZERO

    override fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<AsmDisassembler.Label>>> {
        return emptyMap()
    }
}