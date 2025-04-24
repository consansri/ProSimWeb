package cengine.lang.asm

import cengine.util.integer.FixedSizeIntNumber
import cengine.util.integer.FixedSizeIntNumberT
import cengine.util.integer.UnsignedFixedSizeIntNumber
import cengine.util.integer.UnsignedFixedSizeIntNumberT

interface AsmBinaryProvider {

    val id: String
    val addrType: UnsignedFixedSizeIntNumberT<*>
    val wordType: FixedSizeIntNumberT<*>

    /**
     * @return Program Entry Address
     */
    fun entry(): UnsignedFixedSizeIntNumber<*>

    /**
     * @return address mapped section content
     */
    fun contents(): Map<UnsignedFixedSizeIntNumber<*>, Pair<List<FixedSizeIntNumber<*>>, List<AsmDisassembler.Label>>>

}