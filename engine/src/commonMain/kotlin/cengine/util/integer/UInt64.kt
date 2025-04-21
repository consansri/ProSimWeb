package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger

class UInt64(override val value: ULong) : IntNumber<UInt64>, UnsignedExtension {

    companion object: IntNumberStatic<UInt64> {

        override val BITS: Int = 64
        override val BYTES: Int = 8
        override val ZERO = UInt64(0U)
        override val ONE = UInt64(1U)

        fun Long.toUInt64() = UInt64(this.toULong())
        fun Int.toUInt64() = UInt64(this.toULong())
        fun ULong.toUInt64() = UInt64(this)
        fun UInt.toUInt64() = UInt64(this.toULong())

        fun String.parseUInt64(radix: Int): UInt64 = UInt64(toULong(radix))
        fun fromUInt32(value1: UInt32, value0: UInt32): UInt64 = (value1.toUInt64() shl 32) or value0.toUInt64()

        override fun to(number: IntNumber<*>): UInt64 = number.toUInt64()
        override fun split(number: IntNumber<*>): List<UInt64> = number.uInt64s()
        override fun of(value: Int): UInt64 = UInt64(value.toUInt().toULong())
        override fun parse(string: String, radix: Int): UInt64 = UInt64(string.toULong(radix))

        override fun createBitMask(bitWidth: Int): UInt64 {
            require(bitWidth in 0..64) { "$bitWidth exceeds 0..64"}
            return (ONE shl bitWidth) - 1
        }
    }

    override val bitWidth: Int
        get() = BITS

    override val byteCount: Int
        get() = BYTES

    override val type: IntNumberStatic<UInt64>
        get() = UInt64

    override fun plus(other: UInt64): UInt64 = UInt64(value + other.value)
    override fun minus(other: UInt64): UInt64 = UInt64(value - other.value)
    override fun times(other: UInt64): UInt64 = UInt64(value * other.value)
    override fun div(other: UInt64): UInt64 = UInt64(value / other.value)
    override fun rem(other: UInt64): UInt64 = UInt64(value % other.value)

    @Deprecated("Can't negotiate unsigned value!", ReplaceWith("toInt64().unaryMinus().toUInt64()"))
    override fun unaryMinus(): UInt64 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt64 = UInt64(value.inc())
    override fun dec(): UInt64 = UInt64(value.dec())

    override fun inv(): UInt64 = UInt64(value.inv())
    override fun and(other: UInt64): UInt64 = UInt64(value and other.value)
    override fun or(other: UInt64): UInt64 = UInt64(value or other.value)
    override fun xor(other: UInt64): UInt64 = UInt64(value xor other.value)

    override fun shl(bits: UInt64): UInt64 = UInt64(value shl bits.value.toInt())
    override fun shr(bits: UInt64): UInt64 = UInt64(value shr bits.value.toInt())

    override fun plus(other: Int): UInt64 = UInt64(value + other.toUInt())
    override fun plus(other: Long): UInt64 = UInt64(value + other.toULong())

    override fun minus(other: Int): UInt64 = UInt64(value - other.toUInt())
    override fun minus(other: Long): UInt64 = UInt64(value - other.toULong())

    override fun times(other: Int): UInt64 = UInt64(value * other.toUInt())
    override fun times(other: Long): UInt64 = UInt64(value * other.toULong())

    override fun div(other: Int): UInt64 = UInt64(value / other.toUInt())
    override fun div(other: Long): UInt64 = UInt64(value / other.toULong())

    override fun rem(other: Int): UInt64 = UInt64(value % other.toUInt())
    override fun rem(other: Long): UInt64 = UInt64(value % other.toULong())

    override fun and(other: Int): UInt64 = UInt64(value and other.toULong())
    override fun and(other: Long): UInt64 = UInt64(value and other.toULong())

    override fun or(other: Int): UInt64 = UInt64(value or other.toULong())
    override fun or(other: Long): UInt64 = UInt64(value or other.toULong())

    override fun xor(other: Int): UInt64 = UInt64(value xor other.toULong())
    override fun xor(other: Long): UInt64 = UInt64(value xor other.toULong())

    override fun shl(bits: Int): UInt64 = UInt64(value shl bits)
    override fun shr(bits: Int): UInt64 = UInt64(value shr bits)
    override fun lowest(bitWidth: Int): UInt64 = this and createBitMask(bitWidth)


    override fun compareTo(other: UInt64): Int = value.compareTo(other.value)

    override fun compareTo(other: UInt): Int = value.compareTo(other)
    override fun compareTo(other: ULong): Int = value.compareTo(other)

    override fun compareTo(other: Int): Int = compareTo(other.toUInt())
    override fun compareTo(other: Long): Int = compareTo(other.toULong())

    override fun equals(other: Any?): Boolean {
        if (other is IntNumber<*>) return value == other.value
        return value == other
    }

    override fun toInt8(): Int8 = Int8(value.toByte())
    override fun toInt16(): Int16 = Int16(value.toShort())
    override fun toInt32(): Int32 = Int32(value.toInt())
    override fun toInt64(): Int64 = Int64(value.toLong())
    override fun toInt128(): Int128 = Int128(BigInteger.fromULong(value))
    override fun toBigInt(): BigInt = BigInt(BigInteger.fromULong(value))
    override fun toUInt8(): UInt8 = UInt8(value.toUByte())
    override fun toUInt16(): UInt16 = UInt16(value.toUShort())
    override fun toUInt32(): UInt32 = UInt32(value.toUInt())

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toUInt64(): UInt64 = this
    override fun toUInt128(): UInt128 = UInt128(BigInteger.fromULong(value))

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toUnsigned(): UInt64 = this

    override fun toString(radix: Int): String = value.toString(radix)
    override fun toString(): String = value.toString()
    override fun fitsInSigned(bitWidth: Int): Boolean = toInt64().fitsInSigned(bitWidth)

    override fun fitsInUnsigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val maxValue = (ONE shl bitWidth) - 1 // 2^bitWidth - 1
        return value in ZERO.value..maxValue.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun int8s() = (this shr bitWidth / 2).toUInt32().int8s() + this.toUInt32().int8s()

}