package emulator.archs.t6502

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt8
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile

class T6502BaseRegs : RegFile<UInt8> {
    override val name: String = "base"
    override val indentificators: List<FieldProvider> = listOf(object : FieldProvider {
        override val name: String = "NAME"

        override fun get(id: Int): String = when (id) {
            0 -> "AC"
            1 -> "X"
            2 -> "Y"
            3 -> "SR"
            4 -> "SP"
            else -> ""
        }

    })
    override val descriptors: List<FieldProvider> = listOf(object : FieldProvider {
        override val name: String = "DESCR"

        override fun get(id: Int): String = when (id) {
            0 -> "accumulator"
            1 -> "X register"
            2 -> "Y register"
            3 -> "status register [NV-BDIZC]"
            4 -> "stack pointer"
            else -> ""
        }

    })
    override val regValues: SnapshotStateList<UInt8> = mutableStateListOf(
        UInt8.ZERO,
        UInt8.ZERO,
        UInt8.ZERO,
        UInt8(0b00100000U),
        UInt8(0b11111111U),
    )

    override fun set(index: Int, value: IntNumber<*>) {
        regValues[index] = value.toUInt8()
    }

    override fun isVisible(index: Int): Boolean = true

    override fun clear() {
        regValues[0] = UInt8.ZERO
        regValues[1] = UInt8.ZERO
        regValues[2] = UInt8.ZERO
        regValues[3] = UInt8(0b00100000U)
        regValues[4] = UInt8(0b11111111U)
    }
}