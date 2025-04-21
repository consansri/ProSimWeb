package emulator.archs.ikrmini

import androidx.compose.runtime.mutableStateListOf
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt16
import cengine.util.integer.UInt16.Companion.toUInt16
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile

class IKRMiniBaseRegs : RegFile<UInt16> {
    override val name: String = "base"
    override val indentificators: List<FieldProvider> = listOf(
        object : FieldProvider {
            override val name: String = "NAME"

            override fun get(id: Int): String = when (id) {
                0 -> "AC"
                1 -> "NZVC"
                else -> ""
            }
        }
    )
    override val descriptors: List<FieldProvider> = listOf(

    )
    override val regValues = mutableStateListOf(*Array(2) { UInt16.ZERO })

    override fun set(index: Int, value: IntNumber<*>) {
        require(index in 0..<2) { "index $index must be in 0..2" }
        regValues[index] = value.toUInt16()
    }

    operator fun set(index: Int, value: UShort) {
        regValues[index] = value.toUInt16()
    }

    override fun isVisible(index: Int): Boolean = true

    override fun clear() {
        for (i in regValues.indices) {
            regValues[i] = UInt16.ZERO
        }
    }
}