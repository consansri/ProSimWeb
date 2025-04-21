package cengine.lang.asm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber

abstract class AsmDisassembler {

    val decodedContent: MutableState<List<DecodedSegment>> = mutableStateOf(emptyList())

    fun disassemble(initializer: AsmBinaryProvider): List<DecodedSegment> {
        val contents = initializer.contents()
        val mapped = contents.map { (addr, content) ->
            val (data, labels) = content

            DecodedSegment(
                addr,
                labels,
                disassemble(addr, data)
            )
        }

        return mapped
    }

    abstract fun disassemble(startAddr: BigInt, buffer: List<IntNumber<*>>): List<Decoded>

    interface InstrProvider {

        fun decode(segmentAddr: BigInt, offset: Int): Decoded
    }

    data class DecodedSegment(
        val addr: BigInt,
        val labels: List<Label>,
        val decodedContent: List<Decoded>,
    )

    /**
     * @param offset Offset in Segment
     */
    data class Label(
        val offset: Int,
        val name: String,
    )

    /**
     * @param offset must be unique in combination with [DecodedSegment.addr]!
     */
    data class Decoded(
        val offset: Int,
        val data: IntNumber<*>,
        val disassembled: String,
        val target: BigInt? = null,
    )
}