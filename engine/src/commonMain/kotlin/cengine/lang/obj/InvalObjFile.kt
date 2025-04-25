package cengine.lang.obj

import cengine.lang.asm.AsmDisassembler
import cengine.util.integer.UInt32
import cengine.util.integer.UnsignedFixedSizeIntNumber
import cengine.util.integer.UnsignedFixedSizeIntNumberT
import cengine.vfs.VirtualFile

class InvalObjFile(file: VirtualFile) : ObjPsiFile(file, false) {
    override val addrType: UnsignedFixedSizeIntNumberT<UInt32> = UInt32
    override val wordType: UnsignedFixedSizeIntNumberT<UInt32> = UInt32

    override fun entry(): UInt32 = UInt32.ZERO

    override fun contents(): Map<UnsignedFixedSizeIntNumber<*>, Pair<List<UInt32>, List<AsmDisassembler.Label>>> {
        return emptyMap()
    }
}