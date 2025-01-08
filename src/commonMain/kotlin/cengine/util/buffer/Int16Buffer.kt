package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.*

/**
 * A buffer for [Int16] values.
 *
 * This class is used to create a buffer that can store [Int16] values. It is
 * backed by a [MutableList] of [Int16] values and provides methods to interact with
 * the buffer.
 */
class Int16Buffer(endianness: Endianness, initial: Array<Int16> = emptyArray()) : Buffer<Int16>(endianness, Int16) {
    
    override fun toArray(): Array<Int16> = data.toTypedArray()

    override fun getUInt8(index: Int): UInt8 = data[index].toUInt8()

    override fun getUInt16(index: Int): UInt16 = data[index].toUInt16()

    override fun getUInt32(index: Int): UInt32 {
        return when (endianness) {
            Endianness.LITTLE -> get(index + 1).toUInt32() shl 16 or get(index).toUInt32()
            Endianness.BIG -> get(index).toUInt32() shl 16 or get(index + 1).toUInt32()
        }
    }

    override fun getUInt64(index: Int): UInt64 {
        return when (endianness) {
            Endianness.LITTLE -> get(index + 3).toUInt64() shl 48 or
                    get(index + 2).toUInt64() shl 32 or
                    get(index + 1).toUInt64() shl 16 or
                    get(index).toUInt64()

            Endianness.BIG -> get(index).toUInt64() shl 48 or
                    get(index + 2).toUInt64() shl 32 or
                    get(index + 1).toUInt64() shl 16 or
                    get(index).toUInt64()
        }
    }

    override fun putUInt8s(bytes: Array<UInt8>) {
        val shorts = bytes.toList().chunked(2).map {
            val i0 = it.getOrNull(0)?.toInt16() ?: type.ZERO
            val i1 = it.getOrNull(1)?.toInt16() ?: type.ZERO
            when (endianness) {
                Endianness.BIG -> {
                    i0 shl 8 or i1
                }

                Endianness.LITTLE -> {
                    i1 shl 8 or i0
                }
            }
        }
        putAll(shorts.toTypedArray())
    }

    override fun put(value: UInt8) {
        data.add(value.toInt16())
    }

    override fun put(value: UInt16) {
        data.add(value.toInt16())
    }

    override fun put(value: UInt32) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add(value.toInt16())
                data.add((value shr 16).toInt16())
            }

            Endianness.BIG -> {
                data.add((value shr 16).toInt16())
                data.add(value.toInt16())
            }
        }
    }

    override fun put(value: UInt64) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add(value.toInt16())
                data.add((value shr 16).toInt16())
                data.add((value shr 32).toInt16())
                data.add((value shr 48).toInt16())
            }

            Endianness.BIG -> {
                data.add((value shr 48).toInt16())
                data.add((value shr 32).toInt16())
                data.add((value shr 16).toInt16())
                data.add(value.toInt16())
            }
        }
    }

    override fun set(index: Int, value: UInt8) {
        data[index] = value.toInt16()
    }

    override fun set(index: Int, value: UInt16) {
        data[index] = value.toInt16()
    }

    override fun set(index: Int, value: UInt32) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = value.toInt16()
                data[index + 1] = (value shr 16).toInt16()
            }

            Endianness.BIG -> {
                data[index] = (value shr 16).toInt16()
                data[index + 1] = value.toInt16()
            }
        }
    }

    override fun set(index: Int, value: UInt64) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = value.toInt16()
                data[index + 1] = (value shr 16).toInt16()
                data[index + 2] = (value shr 32).toInt16()
                data[index + 3] = (value shr 48).toInt16()
            }

            Endianness.BIG -> {
                data[index] = (value shr 48).toInt16()
                data[index + 1] = (value shr 32).toInt16()
                data[index + 2] = (value shr 16).toInt16()
                data[index + 3] = value.toInt16()
            }
        }
    }

    override fun dataAsString(index: Int, radix: Int): String = data[index].toString(radix)
}