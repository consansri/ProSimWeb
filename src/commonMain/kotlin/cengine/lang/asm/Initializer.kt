package cengine.lang.asm

import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import emulator.kit.memory.Memory

interface Initializer {

    val id: String

    /**
     * @return Program Entry Address
     */
    fun initialize(memory: Memory<*, *>): IntNumber<*>
    fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<Disassembler.Label>>>

}