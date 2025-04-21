package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt8(override val value: UByte) : IntNumber<UInt8>, UnsignedExtension {

    constructor(value: UInt) : this(value.toUByte())
    constructor(value: ULong) : this(value.toUByte())

    companion object: IntNumberStatic<UInt8> {

        override val BITS: Int = 8
        override val BYTES: Int = 1
        override val ZERO = UInt8(0U)
        override val ONE = UInt8(1U)

        fun UByte.toUInt8() = UInt8(this)
        fun UInt.toUInt8() = UInt8(this)

        override fun to(number: IntNumber<*>): UInt8 = number.toUInt8()
        override fun split(number: IntNumber<*>): List<UInt8> = number.uInt8s()
        override fun parse(string: String,radix: Int): UInt8 = UInt8(string.toUByte(radix))
        override fun of(value: Int): UInt8 = UInt8(value.toUInt().toUByte())

        override fun createBitMask(bitWidth: Int): UInt8 {
            require(bitWidth in 0..8) { "$bitWidth exceeds 0..8"}
            return (ONE shl bitWidth) - 1
        }
    }

    override val bitWidth: Int
        get() = BITS

    override val byteCount: Int
        get() = BYTES

    override val type: IntNumberStatic<UInt8>
        get() = UInt8

    override fun plus(other: UInt8): UInt8 = UInt8(value + other.value)
    override fun minus(other: UInt8): UInt8 = UInt8(value - other.value)
    override fun times(other: UInt8): UInt8 = UInt8(value * other.value)
    override fun div(other: UInt8): UInt8 = UInt8(value / other.value)
    override fun rem(other: UInt8): UInt8 = UInt8(value % other.value)

    @Deprecated("Can't negotiate unsigned value!", ReplaceWith("toInt8().unaryMinus().toUInt8()"))
    override fun unaryMinus(): UInt8 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt8 = UInt8(value.inc())
    override fun dec(): UInt8 = UInt8(value.dec())

    override fun inv(): UInt8 = UInt8(value.inv())
    override fun and(other: UInt8): UInt8 = UInt8(value.toUInt() and other.value.toUInt())
    override fun or(other: UInt8): UInt8 = UInt8(value.toUInt() or other.value.toUInt())
    override fun xor(other: UInt8): UInt8 = UInt8(value.toUInt() xor other.value.toUInt())

    override fun shl(bits: UInt8): UInt8 = UInt8(value.toUInt() shl bits.value.toInt())
    override fun shr(bits: UInt8): UInt8 = UInt8(value.toUInt() shr bits.value.toInt())


    override fun plus(other: Int): UInt8 = UInt8(value + other.toUInt())
    override fun plus(other: Long): UInt8 = UInt8(value + other.toULong())

    override fun minus(other: Int): UInt8 = UInt8(value - other.toUInt())
    override fun minus(other: Long): UInt8 = UInt8(value - other.toULong())

    override fun times(other: Int): UInt8 = UInt8(value * other.toUInt())
    override fun times(other: Long): UInt8 = UInt8(value * other.toULong())

    override fun div(other: Int): UInt8 = UInt8(value / other.toUInt())
    override fun div(other: Long): UInt8 = UInt8(value / other.toULong())

    override fun rem(other: Int): UInt8 = UInt8(value % other.toUInt())
    override fun rem(other: Long): UInt8 = UInt8(value % other.toULong())

    override fun and(other: Int): UInt8 = UInt8(value and other.toUByte())
    override fun and(other: Long): UInt8 = UInt8(value and other.toUByte())

    override fun or(other: Int): UInt8 = UInt8(value or other.toUByte())
    override fun or(other: Long): UInt8 = UInt8(value or other.toUByte())

    override fun xor(other: Int): UInt8 = UInt8(value xor other.toUByte())
    override fun xor(other: Long): UInt8 = UInt8(value xor other.toUByte())

    override fun shl(bits: Int): UInt8 = UInt8(value.toUInt() shl bits)
    override fun shr(bits: Int): UInt8 = UInt8(value.toUInt() shr bits)
    override fun lowest(bitWidth: Int): UInt8 = this and createBitMask(bitWidth)


    override fun compareTo(other: UInt): Int = value.compareTo(other)
    override fun compareTo(other: ULong): Int = value.compareTo(other)

    override fun compareTo(other: Int): Int = compareTo(other.toUInt())
    override fun compareTo(other: Long): Int = compareTo(other.toULong())

    override fun compareTo(other: UInt8): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (other is IntNumber<*>) return value == other.value
        return value == other
    }

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromUByte(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromUByte(value))

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toUInt8(): UInt8 = this
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())
    override fun toUInt128(): UInt128 = UInt128(BigInteger.fromUByte(value))

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toUnsigned(): UInt8 = this

    override fun toString(radix: Int): String = value.toString(radix)
    override fun toString(): String = value.toString()
    override fun fitsInSigned(bitWidth: Int): Boolean = toInt8().fitsInSigned(bitWidth)

    override fun fitsInUnsigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val maxValue = (ONE shl bitWidth) - 1 // 2^bitWidth - 1
        return value in ZERO.value..maxValue.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun int8s() = listOf(this.toInt8())

}