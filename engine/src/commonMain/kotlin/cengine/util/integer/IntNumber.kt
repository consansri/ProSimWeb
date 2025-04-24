package cengine.util.integer

/**
 * Base sealed interface for all integer number types.
 * Provides common operations and properties.
 */
sealed interface IntNumber<T : IntNumber<T>> : Comparable<T> {

    // Arithmetic Operations
    val value: Any // Underlying value (Byte, Short, Int, Long, UByte, UShort, UInt, ULong, BigInteger)
    val type: IntNumberT<T> // Access to static type information

    // --- Arithmetic Operations ---
    operator fun plus(other: T): T
    operator fun minus(other: T): T
    operator fun times(other: T): T
    operator fun div(other: T): T
    operator fun rem(other: T): T

    // Kotlin Int Operations
    operator fun plus(other: Int): T
    operator fun minus(other: Int): T
    operator fun times(other: Int): T
    operator fun div(other: Int): T
    operator fun rem(other: Int): T

    // Kotlin Long Operations
    operator fun plus(other: Long): T
    operator fun minus(other: Long): T
    operator fun times(other: Long): T
    operator fun div(other: Long): T
    operator fun rem(other: Long): T

    // --- Common Operations ---
    operator fun inc(): T
    operator fun dec(): T

    // --- Comparison ---
    override operator fun compareTo(other: T): Int
    operator fun compareTo(other: Long): Int
    operator fun compareTo(other: Int): Int

    // --- Conversion ---
    fun toInt8(): Int8
    fun toInt16(): Int16
    fun toInt32(): Int32
    fun toInt64(): Int64
    fun toInt128(): Int128
    fun toBigInt(): BigInt

    fun toUInt8(): UInt8
    fun toUInt16(): UInt16
    fun toUInt32(): UInt32
    fun toUInt64(): UInt64
    fun toUInt128(): UInt128

    // Kotlin integer type conversion
    fun toByte(): Byte = toInt8().value
    fun toUByte(): UByte = toUInt8().value
    fun toShort(): Short = toInt16().value
    fun toUShort(): UShort = toUInt16().value
    fun toInt(): Int = toInt32().value
    fun toUInt(): UInt = toUInt32().value
    fun toLong(): Long = toInt64().value
    fun toULong(): ULong = toUInt64().value


    // --- Transformation ---
    override fun toString(): String
    fun toString(radix: Int = 10): String

    // --- Checks ---
    fun fitsInSigned(bitWidth: Int): Boolean
    fun fitsInUnsigned(bitWidth: Int): Boolean
    fun fitsInSignedOrUnsigned(bitWidth: Int): Boolean = fitsInSigned(bitWidth) || fitsInUnsigned(bitWidth)

    // --- Standard Overrides ---
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

}