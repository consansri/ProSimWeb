package emulator.kit.register

import androidx.compose.runtime.snapshots.SnapshotStateList
import cengine.util.integer.FixedSizeIntNumber
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt32
import cengine.util.integer.UnsignedFixedSizeIntNumber
import cengine.util.integer.UnsignedFixedSizeIntNumberT

interface RegFile<T : UnsignedFixedSizeIntNumber<*>> {

    /**
     * Name of [RegFile]
     */
    val name: String

    /**
     * Type of [RegFile] values
     */
    val type: UnsignedFixedSizeIntNumberT<T>

    /**
     * Each [FieldProvider] will be displayed as a column ahead of the value in the UI.
     */
    val indentificators: List<FieldProvider>

    /**
     * Each [FieldProvider] will be displayed as a column behind the value in the UI.
     */
    val descriptors: List<FieldProvider>

    /**
     * Contains Actual Values
     */
    val regValues: SnapshotStateList<T>

    /**
     *
     */
    operator fun set(index: Int, value: T)

    operator fun set(index: UInt32, value: T) = set(index.toInt(), value)

    operator fun set(index: Int, value: IntNumber<*>) = set(index, type.to(value))

    operator fun set(index: UInt32, value: IntNumber<*>) = set(index.toInt(), value)

    operator fun get(index: Int): T = regValues[index]

    operator fun get(index: UInt32): T = regValues[index.toInt()]

    fun isVisible(index: Int): Boolean

    fun clear()

}