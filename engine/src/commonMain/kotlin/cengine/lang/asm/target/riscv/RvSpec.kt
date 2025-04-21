package cengine.lang.asm.target.riscv

import cengine.lang.asm.AsmSpec
import cengine.lang.asm.gas.AsmCodeGenerator

sealed interface RvSpec<T : AsmCodeGenerator<*>> : AsmSpec<T> {

    val xlen: XLEN

    enum class XLEN(val bits: Int) {
        X32(32), X64(64)
    }

}