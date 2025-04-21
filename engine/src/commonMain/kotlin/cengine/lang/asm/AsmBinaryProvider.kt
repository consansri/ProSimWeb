package cengine.lang.asm

import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import emulator.kit.memory.Memory

interface AsmBinaryProvider {

    val id: String

    /**
     * Initializes Emulator [Memory]
     *
     * @param memory to be initialized
     */
    fun initialize(memory: Memory<*, *>)

    /**
     * @return Program Entry Address
     */
    fun entry(): IntNumber<*>

    /**
     * @return address mapped section content
     */
    fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<AsmDisassembler.Label>>>

}