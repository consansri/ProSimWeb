package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Int8
import cengine.util.integer.Int8.Companion.toInt8

/**
 * A buffer for [Int8] values.
 *
 * This class is used to create a buffer that can store [Int8] values. It is
 * backed by a [MutableList] of [Int8] values and provides methods to interact with
 * the buffer.
 */
class Int8Buffer(endianness: Endianness) : Buffer<Int8>(endianness, Int8) {
    companion object {
        fun String.toASCIIByteArray(): List<Int8> = this.encodeToByteArray().map { it.toInt8() }
        fun Array<Int8>.toASCIIString(): String = this.map { it.toByte() }.toByteArray().decodeToString()
        fun List<Int8>.toASCIIString(): String = this.map { it.toByte() }.toByteArray().decodeToString()
    }

    fun toByteArray(): ByteArray = data.map { it.value }.toByteArray()
}