package emulator.archs.riscv.riscv32

import androidx.compose.runtime.mutableStateListOf
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt32
import cengine.util.integer.UnsignedFixedSizeIntNumberT
import emulator.archs.riscv.RV
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile

class RV32BaseRegs : RegFile<UInt32> {
    override val name: String = "base"

    override val type: UnsignedFixedSizeIntNumberT<UInt32>
        get() = UInt32

    override val indentificators: List<FieldProvider> = listOf(
        RV.BaseNameProvider
    )

    override val descriptors: List<FieldProvider> = listOf(
        RV.BaseCCProvider,
        RV.BaseProvider
    )

    override val regValues = mutableStateListOf(*Array(32) {
        UInt32.ZERO
    })

    override fun set(index: Int, value: UInt32) {
        when (index) {
            0 -> return
            else -> regValues[index] = value
        }
    }

    override fun isVisible(index: Int): Boolean = true

    override fun clear() {
        for (i in regValues.indices) {
            regValues[i] = UInt32.ZERO
        }
    }
}