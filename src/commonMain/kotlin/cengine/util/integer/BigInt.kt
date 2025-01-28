package cengine.util.integer

import cengine.util.integer.Int8.Companion.toInt8
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.ionspin.kotlin.bignum.integer.toBigInteger

data class BigInt(override val value: BigInteger) : IntNumber<BigInt> {

    companion object : IntNumberStatic<BigInt> {

        @Deprecated("This throws an error! BigInt has no fixed bit count!")
        override val BITS: Int get() = throw Exception("No specific bit count defined for BigInt")

        @Deprecated("This throws an error! BigInt has no fixed byte count!")
        override val BYTES: Int get() = throw Exception("No specific byte count defined for BigInt")
        override val ZERO = BigInt(BigInteger.ZERO)
        override val ONE = BigInt(BigInteger.ONE)

        override fun to(number: IntNumber<*>): BigInt = number.toBigInt()

        @Deprecated("This throws an error! You can't split into BigInt cause it has no fixed byte count!", ReplaceWith("throw Exception(\"Can't split into BigInt cause it has no fixed bytecount!\")"))
        override fun split(number: IntNumber<*>): List<BigInt> = throw Exception("Can't split into BigInt cause it has no fixed bytecount!")
        override fun of(value: Int): BigInt = BigInt(value.toBigInteger())
        override fun parse(string: String, radix: Int): BigInt = BigInt(BigInteger.parseString(string, radix))
        override fun createBitMask(bitWidth: Int): BigInt {
            return (ONE shl bitWidth) - 1
        }

        fun Float.toBigInt(): BigInt = BigInt(this.toBigDecimal().toBigInteger())
        fun Double.toBigInt(): BigInt = BigInt(this.toBigDecimal().toBigInteger())
        fun Int.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun Long.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun UInt.toBigInt(): BigInt = BigInt(this.toBigInteger())
        fun ULong.toBigInt(): BigInt = BigInt(this.toBigInteger())
    }

    override val bitWidth: Int
        get() = value.bitLength()

    override val byteCount: Int
        get() = value.toByteArray().size

    override val type: IntNumberStatic<BigInt>
        get() = BigInt

    override fun plus(other: BigInt): BigInt = BigInt(value + other.value)
    override fun minus(other: BigInt): BigInt = BigInt(value - other.value)
    override fun times(other: BigInt): BigInt = BigInt(value * other.value)
    override fun div(other: BigInt): BigInt = BigInt(value / other.value)
    override fun rem(other: BigInt): BigInt = BigInt(value % other.value)

    override fun unaryMinus(): BigInt = BigInt(value.negate())
    override fun inc(): BigInt = BigInt(value.inc())
    override fun dec(): BigInt = BigInt(value.dec())
    override fun compareTo(other: Long): Int = value.compareTo(other)
    override fun compareTo(other: Int): Int = value.compareTo(other)

    override fun inv(): BigInt = BigInt(value.not())

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

    override fun and(other: Int): BigInt = BigInt(value and other.toBigInteger())
    override fun and(other: Long): BigInt = BigInt(value and other.toBigInteger())

    override fun or(other: Int): BigInt = BigInt(value or other.toBigInteger())
    override fun or(other: Long): BigInt = BigInt(value or other.toBigInteger())

    override fun xor(other: Int): BigInt = BigInt(value xor other.toBigInteger())
    override fun xor(other: Long): BigInt = BigInt(value xor other.toBigInteger())

    override fun shl(bits: Int): BigInt = BigInt(value shl bits)
    override fun shr(bits: Int): BigInt = BigInt(value shr bits)
    override fun lowest(bitWidth: Int): BigInt = this and createBitMask(bitWidth)

    override fun and(other: BigInt): BigInt = BigInt(value and other.value)
    override fun or(other: BigInt): BigInt = BigInt(value or other.value)
    override fun xor(other: BigInt): BigInt = BigInt(value xor other.value)

    override fun shl(bits: BigInt): BigInt = BigInt(value shl bits.value.intValue(false))
    override fun shr(bits: BigInt): BigInt = BigInt(value shr bits.value.intValue(false))

    override fun compareTo(other: BigInt): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = if (other is IntNumber<*>) value == other.value else value == other

    override fun toInt8(): Int8 = Int8(value.byteValue(false))
    override fun toInt16(): Int16 = Int16(value.shortValue(false))
    override fun toInt32(): Int32 = Int32(value.intValue(false))
    override fun toInt64(): Int64 = Int64(value.longValue(false))
    override fun toInt128(): Int128 = Int128(value)

    @Deprecated("Unnecessary", ReplaceWith("this"))
    override fun toBigInt(): BigInt = this

    override fun toUInt8(): UInt8 = UInt8(value.ubyteValue(false))
    override fun toUInt16(): UInt16 = UInt16(value.ushortValue(false))
    override fun toUInt32(): UInt32 = UInt32(value.uintValue(false))
    override fun toUInt64(): UInt64 = UInt64(value.ulongValue(false))

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun toUInt128(): UInt128 = UInt128(BigInteger.fromUByteArray(value.toUByteArray(), Sign.POSITIVE))

    override fun toString(): String = value.toString()
    override fun toString(radix: Int): String = value.toString(radix)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun toUnsigned(): BigInt = if (value.getSign() != Sign.POSITIVE) {
        BigInt(BigInteger.fromUByteArray(value.toUByteArray(), Sign.POSITIVE))
    } else this

    override fun fitsInSigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val minValue = -(ONE shl (bitWidth - 1)) // -2^(bitWidth-1)
        val maxValue = (ONE shl (bitWidth - 1)) - 1 // 2^(bitWidth-1) - 1
        return value in minValue.value..maxValue.value
    }

    override fun fitsInUnsigned(bitWidth: Int): Boolean {
        if (bitWidth >= this.bitWidth) return true
        val maxValue = (ONE shl bitWidth) - 1 // 2^bitWidth - 1
        return value in BigInteger.ZERO..maxValue.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun int8s() = value.toByteArray().map { it.toInt8() }

}