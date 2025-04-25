package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Int8
import cengine.util.integer.Int8.Companion.toInt8
import cengine.util.integer.UInt8
import cengine.util.integer.UInt8.Companion.toUInt8

/**
 * A buffer for [UInt8] values.
 *
 * This class is used to create a buffer that can store [UInt8] values. It is
 * backed by a [MutableList] of [UInt8] values and provides methods to interact with
 * the buffer.
 */
class Buffer8(endianness: Endianness) : Buffer<UInt8>(endianness, UInt8) {
    companion object {
        fun String.toASCIIByteArray(): List<UInt8> = this.encodeToByteArray().map { it.toUByte().toUInt8() }
        fun Array<UInt8>.toASCIIString(): String = this.map { it.toByte() }.toByteArray().decodeToString()
        fun List<UInt8>.toASCIIString(): String = this.map { it.toByte() }.toByteArray().decodeToString()
    }

    fun toByteArray(): ByteArray = data.map { it.value.toByte() }.toByteArray()
}