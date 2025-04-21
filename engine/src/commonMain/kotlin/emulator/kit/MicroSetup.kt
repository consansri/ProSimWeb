package emulator.kit

import androidx.compose.runtime.mutableStateListOf
import emulator.kit.memory.Memory
import emulator.kit.register.RegFile

/**
 * [MicroSetup] Always holds the Architecture Components to expose them to the UI.
 *
 * This Object will be setup by each arch through [Architecture.setupMicroArch].
 */
object MicroSetup {
    val memories = mutableStateListOf<Memory<*,*>>()
    val regFiles = mutableStateListOf<RegFile<*>>()

    fun getMemoryInstances(): List<Memory<*,*>> = memories
    fun getRegFiles(): List<RegFile<*>> = regFiles

    fun append(regfile: RegFile<*>) {
        regFiles.add(regfile)
    }

    fun append(mem: Memory<*,*>) {
        memories.add(mem)
    }

    fun clear() {
        memories.clear()
        regFiles.clear()
    }

    override fun toString(): String {
        return """
            Setup:
                mems: ${memories.joinToString { it.name }}
                regs: ${regFiles.joinToString { it.name }}
                            
        """.trimIndent()
    }
}