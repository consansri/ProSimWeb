package cengine.util.integer

/**
 * Defines a static interface for [IntNumber]s of a certain size.
 *
 * @property BYTES Number of bytes this integer uses.
 * @property BITS Number of bits this integer uses.
 * @property ZERO The number 0 represented as this integer type.
 * @property ONE The number 1 represented as this integer type.
 */
interface IntNumberStatic<out T: IntNumber<*>> {

    val BYTES: Int
    val BITS: Int
    val ZERO: T
    val ONE: T


    /**
     * Converts an [IntNumber] to this integer type.
     */
    fun to(number: IntNumber<*>): T

    /**
     * Splits an [IntNumber] into a list of [IntNumber]s of this integer type.
     */
    fun split(number: IntNumber<*>): List<T>

    /**
     * Creates a new [IntNumber] of this integer type.
     */
    fun of(value: Int): T

    /**
     * Creates a new [IntNumber] of this integer type from a string.
     */
    fun parse(string: String, radix: Int): T

    /**
     * Creates a new bit mask of this integer type.
     */
    fun createBitMask(bitWidth: Int): T

}