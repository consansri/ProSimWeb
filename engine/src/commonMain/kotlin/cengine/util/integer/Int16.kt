package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

class Int16(override val value: Short) : IntNumber<Int16> {

    constructor(value: Int) : this(value.toShort())
    constructor(value: Long) : this(value.toShort())

    companion object: IntNumberStatic<Int16> {

        override val BITS: Int = 16
        override val BYTES: Int = 2
        override val ZERO = Int16(0)
        override val ONE = Int16(1)

        fun UShort.toInt16() = Int16(this.toShort())
        fun UInt.toInt16() = Int16(this.toShort())
        fun Short.toInt16() = Int16(this)
        fun Int.toInt16() = Int16(this)
        fun fromUInt8(byte1: UInt8, byte0: UInt8): Int16 = (byte1.toInt16() shl 8) or byte0.toInt16()

        override fun to(number: IntNumber<*>): Int16 = number.toInt16()
        override fun split(number: IntNumber<*>): List<Int16> = number.int16s()
        override fun of(value: Int): Int16 = Int16(value.toShort())
        override fun parse(string: String, radix: Int): Int16 = Int16(string.toShort(radix))

        override fun createBitMask(bitWidth: Int): Int16 {
            require(bitWidth in 0..16) { "$bitWidth exceeds 0..16"}
            return (ONE shl bitWidth) - 1
        }
    }

    override val bitWidth: Int
        get() = BITS

    override val byteCount: Int
        get() = BYTES

    override val type: IntNumberStatic<Int16>
        get() = Int16

    override fun plus(other: Int16): Int16 = Int16(value + other.value)
    override fun minus(other: Int16): Int16 = Int16(value - other.value)
    override fun times(other: Int16): Int16 = Int16(value * other.value)
    override fun div(other: Int16): Int16 = Int16(value / other.value)
    override fun rem(other: Int16): Int16 = Int16(value % other.value)

    override fun unaryMinus(): Int16 = Int16(-value)
    override fun inc(): Int16 = Int16(value.inc())
    override fun dec(): Int16 = Int16(value.dec())

    override fun inv(): Int16 = Int16(value.inv())
    override fun and(other: Int16): Int16 = Int16(value.toInt() and other.value.toInt())
    override fun or(other: Int16): Int16 = Int16(value.toInt() or other.value.toInt())
    override fun xor(other: Int16): Int16 = Int16(value.toInt() xor other.value.toInt())

    override fun shl(bits: Int16): Int16 = Int16(value.toInt() shl bits.value.toInt())
    override fun shr(bits: Int16): Int16 = Int16(value.toInt() shr bits.value.toInt())


    override fun plus(other: Int): Int16 = Int16(value + other)
    override fun plus(other: Long): Int16 = Int16(value + other)

    override fun minus(other: Int): Int16 = Int16(value - other)
    override fun minus(other: Long): Int16 = Int16(value - other)

    override fun times(other: Int): Int16 = Int16(value * other)
    override fun times(other: Long): Int16 = Int16(value * other)

    override fun div(other: Int): Int16 = Int16(value / other)
    override fun div(other: Long): Int16 = Int16(value / other)

    override fun rem(other: Int): Int16 = Int16(value % other)
    override fun rem(other: Long): Int16 = Int16(value % other)

    override fun and(other: Int): Int16 = Int16(value and other.toShort())
    override fun and(other: Long): Int16 = Int16(value and other.toShort())

    override fun or(other: Int): Int16 = Int16(value or other.toShort())
    override fun or(other: Long): Int16 = Int16(value or other.toShort())

    override fun xor(other: Int): Int16 = Int16(value xor other.toShort())
    override fun xor(other: Long): Int16 = Int16(value xor other.toShort())

    override fun shl(bits: Int): Int16 = Int16(value.toInt() shl bits)
    override fun shr(bits: Int): Int16 = Int16(value.toInt() shr bits)
    override fun lowest(bitWidth: Int): Int16 = this and createBitMask(bitWidth)

    override fun compareTo(other: Int16): Int = value.compareTo(other.value)
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)
    override fun equals(other: Any?): Boolean {
        if (other is IntNumber<*>) return value == other.value
        return value == other
    }

    override fun toInt8(): Int8 = Int8(value.toByte())

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toInt16(): Int16 = this
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromShort(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromShort(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())
    override fun toUInt128(): UInt128 = UInt128(BigInteger.fromUShort(value.toUShort()))

    override fun toUnsigned(): UInt16 = toUInt16()

    override fun toString(radix: Int): String = value.toString(radix)
    override fun toString(): String = value.toString()

    override fun fitsInSigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val minValue = -(ONE shl (bitWidth - 1)) // -2^(bitWidth-1)
        val maxValue = (ONE shl (bitWidth - 1)) - 1 // 2^(bitWidth-1) - 1
        return value in minValue.value..maxValue.value
    }

    override fun fitsInUnsigned(bitWidth: Int): Boolean = toUInt16().fitsInUnsigned(bitWidth)

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun int8s() = (this shr bitWidth / 2).toInt8().int8s() + this.toInt8().int8s()

}