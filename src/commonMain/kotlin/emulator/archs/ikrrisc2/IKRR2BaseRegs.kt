package emulator.archs.ikrrisc2

import androidx.compose.runtime.mutableStateListOf
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt32
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile

class IKRR2BaseRegs : RegFile<UInt32> {
    override val name: String = "base"

    override val indentificators: List<FieldProvider> = listOf(BaseNameProvider)
    override val descriptors: List<FieldProvider> = listOf(BaseProvider)
    override val regValues = mutableStateListOf(*Array(32) { UInt32.ZERO })

    override fun set(index: Int, value: IntNumber<*>) {
        when (index) {
            0 -> {}
            else -> regValues[index] = value.toUInt32()
        }
    }

    override fun isVisible(index: Int): Boolean = true

    override fun clear() {
        for (i in regValues.indices) {
            regValues[i] = UInt32.ZERO
        }
    }

    object BaseNameProvider : FieldProvider {
        override val name: String = "NAME"

        override fun get(id: Int): String = when (id) {
            in 0..31 -> "r$id"
            else -> ""
        }
    }

    object BaseProvider : FieldProvider {
        override val name: String = "DESCR"
        override fun get(id: Int): String = when (id) {
            0 -> "hardwired zero"
            31 -> "return address"
            else -> ""
        }
    }
}