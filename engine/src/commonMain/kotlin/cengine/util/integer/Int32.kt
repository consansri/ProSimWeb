package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger

class Int32(override val value: Int) : IntNumber<Int32> {

    constructor(value: Long): this(value.toInt())

    companion object: IntNumberStatic<Int32> {

        override val BITS: Int = 32
        override val BYTES: Int = 4
        override val ZERO = Int32(0)
        override val ONE = Int32(1)

        fun Int.toInt32() = Int32(this)
        fun fromUInt16(value1: UInt16, value0: UInt16): Int32  = (value1.toInt32() shl 16) or value0.toInt32()

        override fun to(number: IntNumber<*>): Int32 = number.toInt32()
        override fun split(number: IntNumber<*>): List<Int32> = number.int32s()
        override fun of(value: Int): Int32 = Int32(value)
        override fun parse(string: String,radix: Int): Int32 = Int32(string.toInt(radix))

        override fun createBitMask(bitWidth: Int): Int32 {
            require(bitWidth in 0..32) { "$bitWidth exceeds 0..32"}
            return (ONE shl bitWidth) - 1
        }
    }

    override val bitWidth: Int
        get() = BITS

    override val byteCount: Int
        get() = BYTES

    override val type: IntNumberStatic<Int32>
        get() = Int32

    override fun plus(other: Int32): Int32 = Int32(value + other.value)
    override fun minus(other: Int32): Int32 = Int32(value - other.value)
    override fun times(other: Int32): Int32 = Int32(value * other.value)
    override fun div(other: Int32): Int32 = Int32(value / other.value)
    override fun rem(other: Int32): Int32 = Int32(value % other.value)

    override fun unaryMinus(): Int32 = Int32(-value)
    override fun inc(): Int32 = Int32(value.inc())
    override fun dec(): Int32 = Int32(value.dec())

    override fun inv(): Int32 = Int32(value.inv())
    override fun and(other: Int32): Int32 = Int32(value and other.value)
    override fun or(other: Int32): Int32 = Int32(value or other.value)
    override fun xor(other: Int32): Int32 = Int32(value xor other.value)
    override fun shl(bits: Int32): Int32 = Int32(value shl bits.value)
    override fun shr(bits: Int32): Int32 = Int32(value shr bits.value)


    override fun plus(other: Int): Int32 = Int32(value + other)
    override fun plus(other: Long): Int32 = Int32(value + other)

    override fun minus(other: Int): Int32 = Int32(value - other)
    override fun minus(other: Long): Int32 = Int32(value - other)

    override fun times(other: Int): Int32 = Int32(value * other)
    override fun times(other: Long): Int32 = Int32(value * other)

    override fun div(other: Int): Int32 = Int32(value / other)
    override fun div(other: Long): Int32 = Int32(value / other)

    override fun rem(other: Int): Int32 = Int32(value % other)
    override fun rem(other: Long): Int32 = Int32(value % other)

    override fun and(other: Int): Int32 = Int32(value and other)
    override fun and(other: Long): Int32 = Int32(value and other.toInt())

    override fun or(other: Int): Int32 = Int32(value or other)
    override fun or(other: Long): Int32 = Int32(value or other.toInt())

    override fun xor(other: Int): Int32 = Int32(value xor other)
    override fun xor(other: Long): Int32 = Int32(value xor other.toInt())

    override fun shl(bits: Int): Int32 = Int32(value shl bits)
    override fun shr(bits: Int): Int32 = Int32(value shr bits)
    override fun lowest(bitWidth: Int): Int32 = this and createBitMask(bitWidth)

    override fun compareTo(other: Int32): Int = value.compareTo(other.value)
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)
    override fun equals(other: Any?): Boolean {
        if (other is IntNumber<*>) return value == other.value
        return value == other
    }

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toInt32(): Int32 = this
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromInt(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromInt(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())
    override fun toUInt128(): UInt128 = UInt128(BigInteger.fromUInt(value.toUInt()))

    override fun toUnsigned(): UInt32 = toUInt32()

    override fun toString(radix: Int): String = value.toString(radix)
    override fun toString(): String = value.toString()

    override fun fitsInSigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val minValue = -(ONE shl (bitWidth - 1)) // -2^(bitWidth-1)
        val maxValue = (ONE shl (bitWidth - 1)) - 1 // 2^(bitWidth-1) - 1
        return value in minValue.value..maxValue.value
    }

    override fun fitsInUnsigned(bitWidth: Int): Boolean = toUInt32().fitsInUnsigned(bitWidth)

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun int8s() = (this shr bitWidth / 2).toInt16().int8s() + this.toInt16().int8s()


}