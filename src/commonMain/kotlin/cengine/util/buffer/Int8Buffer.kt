package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.*
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

    // -------------------------- CONVERSION
    override fun toArray(): Array<Int8> = data.toTypedArray()
    fun toByteArray(): ByteArray = data.map { it.toByte() }.toByteArray()

    // -------------------------- GET

    // BYTE
    override fun getUInt8(index: Int): UInt8 = data[index].toUInt8()

    // SHORT

    override fun getUInt16(index: Int): UInt16 {
        return when (endianness) {
            Endianness.LITTLE -> (get(index + 1).toUInt16() shl 8) or (get(index).toUInt16() and 0xFF)
            Endianness.BIG -> (get(index).toUInt16() shl 8) or (get(index + 1).toUInt16() and 0xFF)
        }
    }

    // INT

    override fun getUInt32(index: Int): UInt32 {
        return when (endianness) {
            Endianness.LITTLE -> (
                    (get(index + 3).toUInt32() shl 24) or
                            (get(index + 2).toUInt32() and 0xFF shl 16) or
                            (get(index + 1).toUInt32() and 0xFF shl 8) or
                            (get(index).toUInt32() and 0xFF)
                    )

            Endianness.BIG -> (
                    (get(index).toUInt32() shl 24) or
                            (get(index + 1).toUInt32() and 0xFF shl 16) or
                            (get(index + 2).toUInt32() and 0xFF shl 8) or
                            (get(index + 3).toUInt32() and 0xFF)
                    )
        }
    }

    // LONG

    override fun getUInt64(index: Int): UInt64 {
        return when (endianness) {
            Endianness.LITTLE -> (
                    (get(index + 7).toUInt64() shl 56) or
                            (get(index + 6).toUInt64() and 0xFF shl 48) or
                            (get(index + 5).toUInt64() and 0xFF shl 40) or
                            (get(index + 4).toUInt64() and 0xFF shl 32) or
                            (get(index + 3).toUInt64() and 0xFF shl 24) or
                            (get(index + 2).toUInt64() and 0xFF shl 16) or
                            (get(index + 1).toUInt64() and 0xFF shl 8) or
                            (get(index).toUInt64() and 0xFF)
                    )

            Endianness.BIG -> (
                    (get(index).toUInt64() shl 56) or
                            (get(index + 1).toUInt64() and 0xFF shl 48) or
                            (get(index + 2).toUInt64() and 0xFF shl 40) or
                            (get(index + 3).toUInt64() and 0xFF shl 32) or
                            (get(index + 4).toUInt64() and 0xFF shl 24) or
                            (get(index + 5).toUInt64() and 0xFF shl 16) or
                            (get(index + 6).toUInt64() and 0xFF shl 8) or
                            (get(index + 7).toUInt64() and 0xFF)
                    )
        }
    }


    // -------------------------- PUT

    override fun putUInt8s(bytes: Array<UInt8>) {
        data.addAll(bytes.map { it.toInt8() })
    }

    // BYTE

    override fun put(value: UInt8) {
        data.add(value.toInt8())
    }

    // SHORT

    override fun put(value: UInt16) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value and 0xFF).toInt8())
                data.add(((value shr 8) and 0xFF).toInt8())
            }

            Endianness.BIG -> {
                data.add(((value shr 8) and 0xFF).toInt8())
                data.add((value and 0xFF).toInt8())
            }
        }
    }

    // INT

    override fun put(value: UInt32) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value and 0xFF).toInt8())
                data.add(((value shr 8) and 0xFF).toInt8())
                data.add(((value shr 16) and 0xFF).toInt8())
                data.add(((value shr 24) and 0xFF).toInt8())
            }

            Endianness.BIG -> {
                data.add(((value shr 24) and 0xFF).toInt8())
                data.add(((value shr 16) and 0xFF).toInt8())
                data.add(((value shr 8) and 0xFF).toInt8())
                data.add((value and 0xFF).toInt8())
            }
        }
    }

    // LONG

    override fun put(value: UInt64) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value and 0xFF).toInt8())
                data.add(((value shr 8) and 0xFF).toInt8())
                data.add(((value shr 16) and 0xFF).toInt8())
                data.add(((value shr 24) and 0xFF).toInt8())
                data.add(((value shr 32) and 0xFF).toInt8())
                data.add(((value shr 40) and 0xFF).toInt8())
                data.add(((value shr 48) and 0xFF).toInt8())
                data.add(((value shr 56) and 0xFF).toInt8())
            }

            Endianness.BIG -> {
                data.add(((value shr 56) and 0xFF).toInt8())
                data.add(((value shr 48) and 0xFF).toInt8())
                data.add(((value shr 40) and 0xFF).toInt8())
                data.add(((value shr 32) and 0xFF).toInt8())
                data.add(((value shr 24) and 0xFF).toInt8())
                data.add(((value shr 16) and 0xFF).toInt8())
                data.add(((value shr 8) and 0xFF).toInt8())
                data.add((value and 0xFF).toInt8())
            }
        }
    }

    // -------------------------- SET

    override operator fun set(index: Int, value: UInt8) {
        data[index] = value.toInt8()
    }

    override operator fun set(index: Int, value: UInt16) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (value and 0xFF).toInt8()
                data[index + 1] = ((value shr 8) and 0xFF).toInt8()
            }

            Endianness.BIG -> {
                data[index] = ((value shr 8) and 0xFF).toInt8()
                data[index + 1] = (value and 0xFF).toInt8()
            }
        }
    }

    override operator fun set(index: Int, value: UInt32) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (value and 0xFF).toInt8()
                data[index + 1] = ((value shr 8) and 0xFF).toInt8()
                data[index + 2] = ((value shr 16) and 0xFF).toInt8()
                data[index + 3] = ((value shr 24) and 0xFF).toInt8()
            }

            Endianness.BIG -> {
                data[index] = ((value shr 24) and 0xFF).toInt8()
                data[index + 1] = ((value shr 16) and 0xFF).toInt8()
                data[index + 2] = ((value shr 8) and 0xFF).toInt8()
                data[index + 3] = (value and 0xFF).toInt8()
            }
        }
    }

    override operator fun set(index: Int, value: UInt64) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (value and 0xFF).toInt8()
                data[index + 1] = ((value shr 8) and 0xFF).toInt8()
                data[index + 2] = ((value shr 16) and 0xFF).toInt8()
                data[index + 3] = ((value shr 24) and 0xFF).toInt8()
                data[index + 4] = ((value shr 32) and 0xFF).toInt8()
                data[index + 5] = ((value shr 40) and 0xFF).toInt8()
                data[index + 6] = ((value shr 48) and 0xFF).toInt8()
                data[index + 7] = ((value shr 56) and 0xFF).toInt8()
            }

            Endianness.BIG -> {
                data[index] = ((value shr 56) and 0xFF).toInt8()
                data[index + 1] = ((value shr 48) and 0xFF).toInt8()
                data[index + 2] = ((value shr 40) and 0xFF).toInt8()
                data[index + 3] = ((value shr 32) and 0xFF).toInt8()
                data[index + 4] = ((value shr 24) and 0xFF).toInt8()
                data[index + 5] = ((value shr 16) and 0xFF).toInt8()
                data[index + 6] = ((value shr 8) and 0xFF).toInt8()
                data[index + 7] = (value and 0xFF).toInt8()
            }
        }
    }

    override fun dataAsString(index: Int, radix: Int): String = data[index].toString(radix)

}