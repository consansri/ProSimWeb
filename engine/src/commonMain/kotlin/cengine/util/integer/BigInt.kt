package cengine.util.integer

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger

data class BigInt(override val value: BigInteger) : UnlimitedSizeIntNumber<BigInt> {

    companion object : UnlimitedSizeIntNumberT<BigInt> {

        override val ZERO = BigInt(BigInteger.ZERO)
        override val ONE = BigInt(BigInteger.ONE)

        // Masks for efficient conversion to unsigned types
        private val MASK_8 = (ONE.value shl 8) - ONE.value
        private val MASK_16 = (ONE.value shl 16) - ONE.value
        private val MASK_32 = (ONE.value shl 32) - ONE.value
        private val MASK_64 = (ONE.value shl 64) - ONE.value
        private val MASK_128 = (ONE.value shl 128) - ONE.value

        override fun to(number: IntNumber<*>): BigInt = number.toBigInt()

        override fun of(value: Int): BigInt = BigInt(value.toBigInteger())
        override fun parse(string: String, radix: Int): BigInt = BigInt(BigInteger.parseString(string, radix))
        override fun createBitMask(bitWidth: Int): BigInt {
            return BigInt((ONE.value shl bitWidth) - 1)
        }

        fun Float.toBigInt(): BigInt = BigInt(this.toBigDecimal().toBigInteger())
        fun Double.toBigInt(): BigInt = BigInt(this.toBigDecimal().toBigInteger())
        fun Int.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun Long.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun UInt.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun ULong.toBigInt(): BigInt = BigInt(this.toBigInteger())
    }

    override val type: UnlimitedSizeIntNumberT<BigInt>
        get() = BigInt

    override fun plus(other: BigInt): BigInt = BigInt(value + other.value)
    override fun minus(other: BigInt): BigInt = BigInt(value - other.value)
    override fun times(other: BigInt): BigInt = BigInt(value * other.value)
    override fun div(other: BigInt): BigInt = BigInt(value / other.value)
    override fun rem(other: BigInt): BigInt = BigInt(value % other.value)

    operator fun unaryMinus(): BigInt = BigInt(value.negate())
    override fun inc(): BigInt = BigInt(value.inc())
    override fun dec(): BigInt = BigInt(value.dec())
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)

    override fun plus(other: Int): BigInt = BigInt(value + other)
    override fun plus(other: Long): BigInt = BigInt(value + other)

    override fun minus(other: Int): BigInt = BigInt(value - other)
    override fun minus(other: Long): BigInt = BigInt(value - other)

    override fun times(other: Int): BigInt = BigInt(value * other)
    override fun times(other: Long): BigInt = BigInt(value * other)

    override fun div(other: Int): BigInt = BigInt(value / other)
    override fun div(other: Long): BigInt = BigInt(value / other)

    override fun rem(other: Int): BigInt = BigInt(value % other)
    override fun rem(other: Long): BigInt = BigInt(value % other)

    override fun compareTo(other: BigInt): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = if (other is IntNumber<*>) value == other.value else value == other

    override fun toInt8(): Int8 = Int8(value.byteValue(false))
    override fun toInt16(): Int16 = Int16(value.shortValue(false))
    override fun toInt32(): Int32 = Int32(value.intValue(false))
    override fun toInt64(): Int64 = Int64(value.longValue(false))
    override fun toInt128(): Int128 = Int128(value)

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toBigInt(): BigInt = this
    override fun toUInt8(): UInt8 = UInt8((value and MASK_8).ubyteValue(false))
    override fun toUInt16(): UInt16 = UInt16((value and MASK_16).ushortValue(false))
    override fun toUInt32(): UInt32 = UInt32((value and MASK_32).uintValue(false))
    override fun toUInt64(): UInt64 = UInt64((value and MASK_64).ulongValue(false))

    override fun toUInt128(): UInt128 = UInt128(value and MASK_128)

    override fun toString(): String = value.toString()
    override fun toString(radix: Int): String = value.toString(radix)

    override fun fitsInSigned(bitWidth: Int): Boolean {
        val minValue = -(ONE.value shl (bitWidth - 1)) // -2^(bitWidth-1)
        val maxValue = (ONE.value shl (bitWidth - 1)) - 1 // 2^(bitWidth-1) - 1
        return value in minValue..maxValue
    }

    override fun fitsInUnsigned(bitWidth: Int): Boolean {
        val maxValue = (ONE.value shl bitWidth) - 1 // 2^bitWidth - 1
        return value in BigInteger.ZERO..maxValue
    }

    override fun hashCode(): Int = value.hashCode()
}