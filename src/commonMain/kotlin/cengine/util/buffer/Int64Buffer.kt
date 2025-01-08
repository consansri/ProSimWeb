package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.*

/**
 * A buffer for [Int64] values.
 *
 * This class is used to create a buffer that can store [Int64] values. It is
 * backed by a [MutableList] of [Int64] values and provides methods to interact with
 * the buffer.
 */
class Int64Buffer(endianness: Endianness, initial: Array<Int64> = emptyArray()) : Buffer<Int64>(endianness, Int64) {
    
    override fun toArray(): Array<Int64> = data.toTypedArray()

    override fun getUInt8(index: Int): UInt8 = data[index].toUInt8()

    override fun getUInt16(index: Int): UInt16 = data[index].toUInt16()

    override fun getUInt32(index: Int): UInt32 = data[index].toUInt32()

    override fun getUInt64(index: Int): UInt64 = data[index].toUInt64()

    override fun putUInt8s(bytes: Array<UInt8>) {
        val longs = bytes.toList().chunked(8).map {
            val i0 = it.getOrNull(0)?.toInt64() ?: type.ZERO
            val i1 = it.getOrNull(1)?.toInt64() ?: type.ZERO
            val i2 = it.getOrNull(2)?.toInt64() ?: type.ZERO
            val i3 = it.getOrNull(3)?.toInt64() ?: type.ZERO
            val i4 = it.getOrNull(4)?.toInt64() ?: type.ZERO
            val i5 = it.getOrNull(5)?.toInt64() ?: type.ZERO
            val i6 = it.getOrNull(6)?.toInt64() ?: type.ZERO
            val i7 = it.getOrNull(7)?.toInt64() ?: type.ZERO
            when (endianness) {
                Endianness.BIG -> {
                    i0 shl 56 or i1 shl 48 or i2 shl 40 or i3 shl 32 or i4 shl 24 or i5 shl 16 or i6 shl 8 or i7
                }

                Endianness.LITTLE -> {
                    i7 shl 56 or i6 shl 48 or i5 shl 40 or i4 shl 32 or i3 shl 24 or i2 shl 16 or i1 shl 8 or i0
                }
            }
        }
        putAll(longs.toTypedArray())
    }

    override fun put(value: UInt8) {
        data.add(value.toInt64())
    }

    override fun put(value: UInt16) {
        data.add(value.toInt64())
    }

    override fun put(value: UInt32) {
        data.add(value.toInt64())
    }

    override fun put(value: UInt64) {
        data.add(value.toInt64())
    }

    override fun set(index: Int, value: UInt8) {
        data[index] = value.toInt64()
    }

    override fun set(index: Int, value: UInt16) {
        data[index] = value.toInt64()
    }

    override fun set(index: Int, value: UInt32) {
        data[index] = value.toInt64()
    }

    override fun set(index: Int, value: UInt64) {
        data[index] = value.toInt64()
    }

    override fun dataAsString(index: Int, radix: Int): String = data[index].toString(radix)
}