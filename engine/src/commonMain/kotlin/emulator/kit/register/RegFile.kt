package emulator.kit.register

import androidx.compose.runtime.snapshots.SnapshotStateList
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt32

interface RegFile<T : IntNumber<*>> {

    /**
     * Name of [RegFile]
     */
    val name: String

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
    operator fun set(index: Int, value: IntNumber<*>)

    operator fun set(index: UInt32, value: IntNumber<*>) = set(index.toInt(), value)

    operator fun get(index: Int): T = regValues[index]

    operator fun get(index: UInt32): T = regValues[index.toInt()]

    fun isVisible(index: Int): Boolean

    fun clear()

}