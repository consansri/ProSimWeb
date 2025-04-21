package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger

class Int64(override val value: Long) : IntNumber<Int64> {

    companion object: IntNumberStatic<Int64> {

        override val BITS: Int = 64
        override val BYTES: Int = 8
        override val ZERO = Int64(0)
        override val ONE = Int64(1)

        fun Long.toInt64() = Int64(this)
        fun fromUInt32(value1: UInt32, value0: UInt32): Int64 = (value1.toInt64() shl 32) or value0.toInt64()

        override fun to(number: IntNumber<*>): Int64 = number.toInt64()
        override fun split(number: IntNumber<*>): List<Int64> = number.int64s()
        override fun of(value: Int): Int64 = Int64(value.toLong())
        override fun parse(string: String,radix: Int): Int64 = Int64(string.toLong(radix))

        override fun createBitMask(bitWidth: Int): Int64 {
            require(bitWidth in 0..64) { "$bitWidth exceeds 0..64"}
            return (ONE shl bitWidth) - 1
        }
    }

    override val bitWidth: Int
        get() = BITS

    override val byteCount: Int
        get() = BYTES

    override val type: IntNumberStatic<Int64>
        get() = Int64

    override fun plus(other: Int64): Int64 = Int64(value + other.value)
    override fun minus(other: Int64): Int64 = Int64(value - other.value)
    override fun times(other: Int64): Int64 = Int64(value * other.value)
    override fun div(other: Int64): Int64 = Int64(value / other.value)
    override fun rem(other: Int64): Int64 = Int64(value % other.value)

    override fun unaryMinus(): Int64 = Int64(-value)
    override fun inc(): Int64 = Int64(value.inc())
    override fun dec(): Int64 = Int64(value.dec())

    override fun inv(): Int64 = Int64(value.inv())
    override fun and(other: Int64): Int64 = Int64(value and other.value)
    override fun or(other: Int64): Int64 = Int64(value or other.value)
    override fun xor(other: Int64): Int64 = Int64(value xor other.value)

    override fun shl(bits: Int64): Int64 = Int64(value shl bits.value.toInt())
    override fun shr(bits: Int64): Int64 = Int64(value shr bits.value.toInt())


    override fun plus(other: Int): Int64 = Int64(value + other)
    override fun plus(other: Long): Int64 = Int64(value + other)

    override fun minus(other: Int): Int64 = Int64(value - other)
    override fun minus(other: Long): Int64 = Int64(value - other)

    override fun times(other: Int): Int64 = Int64(value * other)
    override fun times(other: Long): Int64 = Int64(value * other)

    override fun div(other: Int): Int64 = Int64(value / other)
    override fun div(other: Long): Int64 = Int64(value / other)

    override fun rem(other: Int): Int64 = Int64(value % other)
    override fun rem(other: Long): Int64 = Int64(value % other)

    override fun and(other: Int): Int64 = Int64(value and other.toLong())
    override fun and(other: Long): Int64 = Int64(value and other)

    override fun or(other: Int): Int64 = Int64(value or other.toLong())
    override fun or(other: Long): Int64 = Int64(value or other)

    override fun xor(other: Int): Int64 = Int64(value xor other.toLong())
    override fun xor(other: Long): Int64 = Int64(value xor other)

    override fun shl(bits: Int): Int64 = Int64(value shl bits)
    override fun shr(bits: Int): Int64 = Int64(value shr bits)
    override fun lowest(bitWidth: Int): Int64 = this and createBitMask(bitWidth)

    override fun compareTo(other: Int64): Int = value.compareTo(other.value)
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)
    override fun equals(other: Any?): Boolean {
        if (other is IntNumber<*>) return value == other.value
        return value == other
    }

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toInt64(): Int64 = this
    override fun toInt128(): Int128 = Int128(BigInteger.fromLong(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromLong(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())
    override fun toUInt128(): UInt128 = UInt128(BigInteger.fromULong(value.toULong()))

    override fun toUnsigned(): UInt64 = toUInt64()

    override fun toString(radix: Int): String = value.toString(radix)
    override fun toString(): String = value.toString()

    override fun fitsInSigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val minValue = -(ONE shl (bitWidth - 1)) // -2^(bitWidth-1)
        val maxValue = (ONE shl (bitWidth - 1)) - 1 // 2^(bitWidth-1) - 1
        return value in minValue.value..maxValue.value
    }

    override fun fitsInUnsigned(bitWidth: Int): Boolean = toUInt64().fitsInUnsigned(bitWidth)

    override fun hashCode(): Int = value.hashCode()

    override fun int8s() = (this shr bitWidth / 2).toInt32().int8s() + this.toInt32().int8s()

}