package emulator.kit

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cengine.lang.asm.AsmDisassembler
import kotlin.enums.EnumEntries

interface ArchConfig {

    val DESCR: Description
    val SETTINGS: List<Setting<*>>
    val DISASSEMBLER: AsmDisassembler?

    data class Description(val name: String, val fullName: String)

    sealed class Setting<T>(val name: String, val init: T, private val valueToString: (T) -> String, private val parseValueFromString: (String) -> T, val onChange: (Architecture<*,*>, Setting<T>) -> Unit) {

        val trimmedName = name.replace(spaceRegex, "")
        val state: MutableState<T> = mutableStateOf(init)

        fun set(arch: Architecture<*,*>, new: T) {
            state.value = new
            onChange(arch, this)
        }

        fun get(): T = state.value

        fun valueToString(): String {
            return valueToString(state.value)
        }

        fun loadFromString(arch: Architecture<*,*>, string: String) {
            set(arch, parseValueFromString(string))
        }

        class Bool(name: String, init: Boolean, onChange: (Architecture<*,*>, Setting<Boolean>) -> Unit) : Setting<Boolean>(name, init, { it.toString() }, { it.toBoolean() }, onChange)
        class Enumeration<T : Enum<T>>(name: String, val enumValues: EnumEntries<T>, init: T, onChange: (Architecture<*,*>, Setting<T>) -> Unit) : Setting<T>(name, init, { it.name }, { value -> enumValues.firstOrNull { it.name == value } ?: init }, onChange)

        class Any<T>(name: String, init: T, valueToString: (T) -> String, parseValueFromString: (String) -> T, onChange: (Architecture<*,*>, Setting<T>) -> Unit) : Setting<T>(name, init, valueToString, parseValueFromString, onChange)

        companion object {
            val spaceRegex = Regex("""\s""")
        }

    }

}