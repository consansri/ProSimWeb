package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.ionspin.kotlin.bignum.integer.toBigInteger

class Int128(value: BigInteger) : IntNumber<Int128> {

    override val value: BigInteger = value.truncateTo128Bits()

    companion object: IntNumberStatic<Int128> {

        override val BITS: Int = 128
        override val BYTES: Int = 16
        override val ZERO = Int128(BigInteger.ZERO)
        override val ONE = Int128(BigInteger.ONE)
        private val UMASK_128 = BigInteger.fromByteArray(ByteArray(16) { 0xFF.toByte() }, Sign.POSITIVE)  // 2^128 - 1

        /** Enforces 128-bit range by truncating the value. */
        private fun BigInteger.truncateTo128Bits(): BigInteger = and(UMASK_128)
        fun fromUInt64(value1: UInt64, value0: UInt64): Int128 = (value1.toInt128() shl 64) or value0.toInt128()

        override fun to(number: IntNumber<*>): Int128 = number.toInt128()
        override fun split(number: IntNumber<*>): List<Int128> = number.int128s()
        override fun of(value: Int): Int128 = Int128(value.toBigInteger())
        override fun parse(string: String, radix: Int): Int128 = Int128(BigInteger.parseString(string, radix))

        override fun createBitMask(bitWidth: Int): Int128 {
            require(bitWidth in 0..128) { "$bitWidth exceeds 0..128" }
            return (ONE shl bitWidth) - 1
        }
    }

    override val bitWidth: Int
        get() = BITS

    override val byteCount: Int
        get() = BYTES

    override val type: IntNumberStatic<Int128>
        get() = Int128

    override fun plus(other: Int128): Int128 = Int128(value + other.value)
    override fun minus(other: Int128): Int128 = Int128(value - other.value)
    override fun times(other: Int128): Int128 = Int128(value * other.value)
    override fun div(other: Int128): Int128 = Int128(value / other.value)
    override fun rem(other: Int128): Int128 = Int128(value % other.value)

    override fun unaryMinus(): Int128 = Int128(value.negate())
    override fun inc(): Int128 = Int128(value.inc())
    override fun dec(): Int128 = Int128(value.dec())

    override fun inv(): Int128 = Int128(value.not())
    override fun and(other: Int128): Int128 = Int128(value and other.value)
    override fun or(other: Int128): Int128 = Int128(value or other.value)
    override fun xor(other: Int128): Int128 = Int128(value xor other.value)

    override fun shl(bits: Int128): Int128 = Int128(value shl bits.value.intValue(false))
    override fun shr(bits: Int128): Int128 = Int128(value shr bits.value.intValue(false))


    override fun plus(other: Int): Int128 = Int128(value + other)
    override fun plus(other: Long): Int128 = Int128(value + other)

    override fun minus(other: Int): Int128 = Int128(value - other)
    override fun minus(other: Long): Int128 = Int128(value - other)

    override fun times(other: Int): Int128 = Int128(value * other)
    override fun times(other: Long): Int128 = Int128(value * other)

    override fun div(other: Int): Int128 = Int128(value / other)
    override fun div(other: Long): Int128 = Int128(value / other)

    override fun rem(other: Int): Int128 = Int128(value % other)
    override fun rem(other: Long): Int128 = Int128(value % other)

    override fun and(other: Int): Int128 = Int128(value and other.toBigInteger())
    override fun and(other: Long): Int128 = Int128(value and other.toBigInteger())

    override fun or(other: Int): Int128 = Int128(value or other.toBigInteger())
    override fun or(other: Long): Int128 = Int128(value or other.toBigInteger())

    override fun xor(other: Int): Int128 = Int128(value xor other.toBigInteger())
    override fun xor(other: Long): Int128 = Int128(value xor other.toBigInteger())

    override fun shl(bits: Int): Int128 = Int128(value shl bits)
    override fun shr(bits: Int): Int128 = Int128(value shr bits)
    override fun lowest(bitWidth: Int): Int128 = this and createBitMask(bitWidth)


    override fun compareTo(other: Int128): Int = value.compareTo(other.value)
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)
    override fun equals(other: Any?): Boolean {
        if (other is IntNumber<*>) return value == other.value
        return value == other
    }

    override fun toInt8(): Int8 = Int8(value.byteValue(false))
    override fun toInt16(): Int16 = Int16(value.shortValue(false))
    override fun toInt32(): Int32 = Int32(value.intValue(false))
    override fun toInt64(): Int64 = Int64(value.longValue(false))

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toInt128(): Int128 = this
    override fun toBigInt(): BigInt = BigInt(value)
    override fun toUInt8(): UInt8 = UInt8(value.ubyteValue(false))
    override fun toUInt16(): UInt16 = UInt16(value.ushortValue(false))
    override fun toUInt32(): UInt32 = UInt32(value.uintValue(false))
    override fun toUInt64(): UInt64 = UInt64(value.ulongValue(false))
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun toUInt128(): UInt128 = UInt128(BigInteger.fromUByteArray(value.toUByteArray(), Sign.POSITIVE))

    override fun toString(radix: Int): String = value.toString(radix)

    override fun toUnsigned(): UInt128 = toUInt128()

    override fun toString(): String = value.toString()
    override fun fitsInSigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val minValue = -(ONE shl (bitWidth - 1)) // -2^(bitWidth-1)
        val maxValue = (ONE shl (bitWidth - 1)) - 1 // 2^(bitWidth-1) - 1
        return value in minValue.value..maxValue.value
    }

    override fun fitsInUnsigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val maxValue = (ONE shl bitWidth) - 1 // 2^bitWidth - 1
        return value in ZERO.value..maxValue.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun int8s() = (this shr bitWidth / 2).toInt64().int8s() + this.toInt64().int8s()

}