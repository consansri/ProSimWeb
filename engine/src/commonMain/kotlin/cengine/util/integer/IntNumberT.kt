package cengine.util.integer

/**
 * Base static interface for IntNumber companions.
 * Defines universally applicable static properties and methods.
 */
sealed interface IntNumberT<out T: IntNumber<*>> {
    val ZERO: T
    val ONE: T

    /** Creates a new IntNumber of this type from an Int value (potentially truncating). */
    fun of(value: Int): T
    /** Parses a string representation into an IntNumber of this type. */
    fun parse(string: String, radix: Int): T
    /** Converts an existing IntNumber to this type (potentially truncating or extending). */
    fun to(number: IntNumber<*>): T
    /** Creates a bit mask of this integer type. For BigInt, this mask can be arbitrarily large. */
    fun createBitMask(bitWidth: Int): T

}