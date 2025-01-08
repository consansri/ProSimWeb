package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.*

/**
 * A buffer for [Int32] values.
 *
 * This class is used to create a buffer that can store [Int32] values. It is
 * backed by a [MutableList] of [Int32] values and provides methods to interact with
 * the buffer.
 */
class Int32Buffer(endianness: Endianness, initial: Array<Int> = emptyArray()) : Buffer<Int32>(endianness, Int32) {
    
    override fun toArray(): Array<Int32> = data.toTypedArray()

    override fun getUInt8(index: Int): UInt8 = data[index].toUInt8()

    override fun getUInt16(index: Int): UInt16 = data[index].toUInt16()

    override fun getUInt32(index: Int): UInt32 = data[index].toUInt32()

    override fun getUInt64(index: Int): UInt64 = when (endianness) {
        Endianness.LITTLE -> get(index + 1).toUInt64() shl 32 or get(index).toUInt64()
        Endianness.BIG -> get(index).toUInt64() shl 32 or get(index + 1).toUInt64()
    }

    override fun putUInt8s(bytes: Array<UInt8>) {
        val ints = bytes.toList().chunked(4).map {
            val i0 = it.getOrNull(0)?.toInt32() ?: type.ZERO
            val i1 = it.getOrNull(1)?.toInt32() ?: type.ZERO
            val i2 = it.getOrNull(2)?.toInt32() ?: type.ZERO
            val i3 = it.getOrNull(3)?.toInt32() ?: type.ZERO

            when (endianness) {
                Endianness.BIG -> {
                    i0 shl 24 or i1 shl 16 or i2 shl 8 or i3
                }

                Endianness.LITTLE -> {
                    i3 shl 24 or i2 shl 16 or i1 shl 8 or i0
                }
            }
        }
        putAll(ints.toTypedArray())
    }

    override fun put(value: UInt8) {
        data.add(value.toInt32())
    }

    override fun put(value: UInt16) {
        data.add(value.toInt32())
    }

    override fun put(value: UInt32) {
        data.add(value.toInt32())
    }

    override fun put(value: UInt64) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add(value.toInt32())
                data.add((value shr 32).toInt32())
            }

            Endianness.BIG -> {
                data.add((value shr 32).toInt32())
                data.add(value.toInt32())
            }
        }
    }

    override fun set(index: Int, value: UInt8) {
        data[index] = value.toInt32()
    }

    override fun set(index: Int, value: UInt16) {
        data[index] = value.toInt32()
    }

    override fun set(index: Int, value: UInt32) {
        data[index] = value.toInt32()
    }

    override fun set(index: Int, value: UInt64) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = value.toInt32()
                data[index + 1] = (value shr 32).toInt32()
            }

            Endianness.BIG -> {
                data[index] = (value shr 32).toInt32()
                data[index + 1] = value.toInt32()
            }
        }
    }

    override fun dataAsString(index: Int, radix: Int): String = data[index].toString(radix)
}