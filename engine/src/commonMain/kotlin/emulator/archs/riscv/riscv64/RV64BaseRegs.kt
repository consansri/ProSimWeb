package emulator.archs.riscv.riscv64

import androidx.compose.runtime.mutableStateListOf
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt64
import cengine.util.integer.UnsignedFixedSizeIntNumberT
import emulator.archs.riscv.RV
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile

class RV64BaseRegs : RegFile<UInt64> {
    override val name: String = "base"

    override val type: UnsignedFixedSizeIntNumberT<UInt64>
        get() = UInt64

    override val indentificators: List<FieldProvider> = listOf(
        RV.BaseNameProvider
    )

    override val descriptors: List<FieldProvider> = listOf(
        RV.BaseCCProvider,
        RV.BaseProvider
    )

    override val regValues = mutableStateListOf(*Array(32) {
        UInt64.ZERO
    })

    override fun set(index: Int, value: UInt64) {
        when (index) {
            0 -> return
            else -> regValues[index] = value
        }
    }


    override fun isVisible(index: Int): Boolean = true

    override fun clear() {
        for (i in regValues.indices) {
            regValues[i] = UInt64.ZERO
        }
    }
}