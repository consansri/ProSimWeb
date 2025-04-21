package cengine.lang.obj.elf

import cengine.util.Endianness
import cengine.util.integer.Int8

interface BinaryProvider {
    fun build(endianness: Endianness): Array<Int8>

    fun byteSize(): Int

}