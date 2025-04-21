package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.ionspin.kotlin.bignum.integer.toBigInteger

class UInt128(value: BigInteger) : IntNumber<UInt128> {

    override val value: BigInteger

    init {
        require(value.getSign() != Sign.NEGATIVE) { "$value can't be negative!" }

        this.value = value.truncateTo128Bits()
    }

    companion object: IntNumberStatic<UInt128> {

        override val BITS: Int = 128
        override val BYTES: Int = 16
        override val ZERO = UInt128(BigInteger.ZERO)
        override val ONE = UInt128(BigInteger.ONE)
        private val UMASK_128 = BigInteger.fromByteArray(ByteArray(16) { 0xFF.toByte() }, Sign.ZERO)  // 2^128 - 1

        /** Enforces 128-bit range by truncating the value. */
        private fun BigInteger.truncateTo128Bits(): BigInteger = and(UMASK_128)
        fun fromUInt64(value1: UInt64, value0: UInt64): Int128 = (value1.toInt128() shl 64) or value0.toInt128()

        override fun to(number: IntNumber<*>): UInt128 = number.toUInt128()
        override fun split(number: IntNumber<*>): List<UInt128> = number.uInt128s()
        override fun of(value: Int): UInt128 = UInt128(value.toUInt().toBigInteger())
        override fun parse(string: String, radix: Int): UInt128 = UInt128(BigInteger.parseString(string, radix))

        override fun createBitMask(bitWidth: Int): UInt128 {
            require(bitWidth in 0..128) { "$bitWidth exceeds 0..128" }
            return (ONE shl bitWidth) - 1
        }
    }

    override val bitWidth: Int
        get() = BITS

    override val byteCount: Int
        get() = BYTES

    override val type: IntNumberStatic<UInt128>
        get() = UInt128

    override fun plus(other: UInt128): UInt128 = UInt128(value + other.value)
    override fun minus(other: UInt128): UInt128 = UInt128(value - other.value)
    override fun times(other: UInt128): UInt128 = UInt128(value * other.value)
    override fun div(other: UInt128): UInt128 = UInt128(value / other.value)
    override fun rem(other: UInt128): UInt128 = UInt128(value % other.value)

    @Deprecated("Can't negotiate unsigned value!", ReplaceWith("toInt128().unaryMinus().toUInt128()"))
    override fun unaryMinus(): UInt128 = throw Exception("Can't negotiate unsigned value!")
    override fun inc(): UInt128 = UInt128(value.inc())
    override fun dec(): UInt128 = UInt128(value.dec())

    override fun inv(): UInt128 = UInt128(value.not())
    override fun and(other: UInt128): UInt128 = UInt128(value and other.value)
    override fun or(other: UInt128): UInt128 = UInt128(value or other.value)
    override fun xor(other: UInt128): UInt128 = UInt128(value xor other.value)

    override fun shl(bits: UInt128): UInt128 = UInt128(value shl bits.value.intValue(false))
    override fun shr(bits: UInt128): UInt128 = UInt128(value shr bits.value.intValue(false))


    override fun plus(other: Int): UInt128 = UInt128(value + other)
    override fun plus(other: Long): UInt128 = UInt128(value + other)

    override fun minus(other: Int): UInt128 = UInt128(value - other)
    override fun minus(other: Long): UInt128 = UInt128(value - other)

    override fun times(other: Int): UInt128 = UInt128(value * other)
    override fun times(other: Long): UInt128 = UInt128(value * other)

    override fun div(other: Int): UInt128 = UInt128(value / other)
    override fun div(other: Long): UInt128 = UInt128(value / other)

    override fun rem(other: Int): UInt128 = UInt128(value % other)
    override fun rem(other: Long): UInt128 = UInt128(value % other)

    override fun and(other: Int): UInt128 = UInt128(value and other.toBigInteger())
    override fun and(other: Long): UInt128 = UInt128(value and other.toBigInteger())

    override fun or(other: Int): UInt128 = UInt128(value or other.toBigInteger())
    override fun or(other: Long): UInt128 = UInt128(value or other.toBigInteger())

    override fun xor(other: Int): UInt128 = UInt128(value xor other.toBigInteger())
    override fun xor(other: Long): UInt128 = UInt128(value xor other.toBigInteger())

    override fun shl(bits: Int): UInt128 = UInt128(value shl bits)
    override fun shr(bits: Int): UInt128 = UInt128(value shr bits)
    override fun lowest(bitWidth: Int): UInt128 = this and createBitMask(bitWidth)

    override fun compareTo(other: UInt128): Int = value.compareTo(other.value)
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
    override fun toInt128(): Int128 = Int128(
        if (value.bitAt(127)) {
            BigInteger.fromByteArray(value.negate().toByteArray(), Sign.NEGATIVE)
        } else {
            BigInteger.fromByteArray(value.toByteArray(), Sign.POSITIVE)
        }
    )

    override fun toBigInt(): BigInt = BigInt(value)
    override fun toUInt8(): UInt8 = UInt8(value.ubyteValue(false))
    override fun toUInt16(): UInt16 = UInt16(value.ushortValue(false))
    override fun toUInt32(): UInt32 = UInt32(value.uintValue(false))
    override fun toUInt64(): UInt64 = UInt64(value.ulongValue(false))

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toUInt128(): UInt128 = this

    override fun toString(radix: Int): String = value.toString(radix)

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toUnsigned(): UInt128 = this

    override fun toString(): String = value.toString()
    override fun fitsInSigned(bitWidth: Int): Boolean = toInt128().fitsInSigned(bitWidth)

    override fun fitsInUnsigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val maxValue = (ONE shl bitWidth) - 1 // 2^bitWidth - 1
        return value in ZERO.value..maxValue.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun int8s() = (this shr bitWidth / 2).toInt64().int8s() + this.toInt64().int8s()
}