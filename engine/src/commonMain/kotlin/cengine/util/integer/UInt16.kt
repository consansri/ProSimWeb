package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt16(override val value: UShort) : IntNumber<UInt16>, UnsignedExtension {

    constructor(value: UInt) : this(value.toUShort())
    constructor(value: ULong) : this(value.toUShort())

    companion object: IntNumberStatic<UInt16> {

        override val BITS: Int = 16
        override val BYTES: Int = 2
        override val ZERO = UInt16(0U)
        override val ONE = UInt16(1U)

        fun Short.toUInt16() = UInt16(this.toUShort())
        fun Int.toUInt16() = UInt16(this.toUShort())

        fun UShort.toUInt16() = UInt16(this)
        fun UInt.toUInt16() = UInt16(this)
        fun fromUInt8(byte1: UInt8, byte0: UInt8): UInt16 = (byte1.toUInt16() shl 8) or byte0.toUInt16()

        override fun to(number: IntNumber<*>): UInt16 = number.toUInt16()
        override fun split(number: IntNumber<*>): List<UInt16> = number.uInt16s()
        override fun parse(string: String, radix: Int): UInt16 = UInt16(string.toUShort(radix))
        override fun of(value: Int): UInt16 = UInt16(value.toUInt().toUShort())

        override fun createBitMask(bitWidth: Int): UInt16 {
            require(bitWidth in 0..16) { "$bitWidth exceeds 0..16"}
            return (ONE shl bitWidth) - 1
        }
    }

    override val bitWidth: Int
        get() = BITS

    override val byteCount: Int
        get() = BYTES

    override val type: IntNumberStatic<UInt16>
        get() = UInt16

    override fun plus(other: UInt16): UInt16 = UInt16(value + other.value)
    override fun minus(other: UInt16): UInt16 = UInt16(value - other.value)
    override fun times(other: UInt16): UInt16 = UInt16(value * other.value)
    override fun div(other: UInt16): UInt16 = UInt16(value / other.value)
    override fun rem(other: UInt16): UInt16 = UInt16(value % other.value)

    @Deprecated("Can't negotiate unsigned value!", ReplaceWith("toInt16().unaryMinus().toUInt16()"))
    override fun unaryMinus(): UInt16 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt16 = UInt16(value.inc())
    override fun dec(): UInt16 = UInt16(value.dec())

    override fun inv(): UInt16 = UInt16(value.inv())
    override fun and(other: UInt16): UInt16 = UInt16(value.toUInt() and other.value.toUInt())
    override fun or(other: UInt16): UInt16 = UInt16(value.toUInt() or other.value.toUInt())
    override fun xor(other: UInt16): UInt16 = UInt16(value.toUInt() xor other.value.toUInt())

    override fun shl(bits: UInt16): UInt16 = UInt16(value.toUInt() shl bits.value.toInt())
    override fun shr(bits: UInt16): UInt16 = UInt16(value.toUInt() shr bits.value.toInt())


    override fun plus(other: Int): UInt16 = UInt16(value + other.toUInt())
    override fun plus(other: Long): UInt16 =UInt16(value + other.toULong())

    override fun minus(other: Int): UInt16 = UInt16(value - other.toUInt())
    override fun minus(other: Long): UInt16 = UInt16(value - other.toULong())

    override fun times(other: Int): UInt16 = UInt16(value * other.toUInt())
    override fun times(other: Long): UInt16 = UInt16(value * other.toULong())

    override fun div(other: Int): UInt16 = UInt16(value / other.toUInt())
    override fun div(other: Long): UInt16 = UInt16(value / other.toULong())

    override fun rem(other: Int): UInt16 = UInt16(value % other.toUInt())
    override fun rem(other: Long): UInt16 = UInt16(value % other.toULong())

    override fun and(other: Int): UInt16 = UInt16(value and other.toUShort())
    override fun and(other: Long): UInt16 = UInt16(value and other.toUShort())

    override fun or(other: Int): UInt16 = UInt16(value or other.toUShort())
    override fun or(other: Long): UInt16 = UInt16(value or other.toUShort())

    override fun xor(other: Int): UInt16 = UInt16(value xor other.toUShort())
    override fun xor(other: Long): UInt16 = UInt16(value xor other.toUShort())

    override fun shl(bits: Int): UInt16 = UInt16(value.toUInt() shl bits)
    override fun shr(bits: Int): UInt16 = UInt16(value.toUInt() shr bits)
    override fun lowest(bitWidth: Int): UInt16 = this and createBitMask(bitWidth)


    override fun compareTo(other: UInt): Int = value.compareTo(other)
    override fun compareTo(other: ULong): Int = value.compareTo(other)

    override fun compareTo(other: Int): Int = compareTo(other.toUInt())
    override fun compareTo(other: Long): Int = compareTo(other.toULong())

    override fun compareTo(other: UInt16): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (other is IntNumber<*>) return value == other.value
        return value == other
    }

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromUShort(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromUShort(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toUInt16(): UInt16 = this
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())
    override fun toUInt64(): UInt64 = UInt64(value.toULong())
    override fun toUInt128(): UInt128 = UInt128(BigInteger.fromUShort(value))

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toUnsigned(): UInt16 = this

    override fun toString(radix: Int): String = value.toString(radix)
    override fun toString(): String = value.toString()
    override fun fitsInSigned(bitWidth: Int): Boolean = toInt16().fitsInSigned(bitWidth)

    override fun fitsInUnsigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val maxValue = (ONE shl bitWidth) - 1 // 2^bitWidth - 1
        return value in ZERO.value..maxValue.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun int8s() = (this shr bitWidth / 2).toUInt8().int8s() + this.toUInt8().int8s()
}