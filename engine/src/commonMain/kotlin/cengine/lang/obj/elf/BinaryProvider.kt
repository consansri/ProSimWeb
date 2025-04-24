package cengine.lang.obj.elf

import cengine.util.Endianness
import cengine.util.integer.Int8
import cengine.util.integer.UInt8

interface BinaryProvider {
    fun build(endianness: Endianness): Array<UInt8>

    fun byteSize(): Int

}