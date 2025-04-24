package cengine.util.integer

/**
 * Static interface for companions of FixedSizeIntNumber types.
 * Adds fixed size properties (BITS, BYTES) and splitting capability.
 */
sealed interface FixedSizeIntNumberT<out T: FixedSizeIntNumber<*>> : IntNumberT<T> {
    /** The fixed number of bits for this type. */
    val BITS: Int
    /** The fixed number of bytes for this type. */
    val BYTES: Int

    /**
     * Splits a *fixed-size* number into a list of numbers of type T.
     * Example: Int32.split(someInt64) could return two Int32s.
     * Requires the input number's size to be a multiple of T's size.
     */
    fun split(number: FixedSizeIntNumber<*>): List<T>

    // override val isFixedSize: Boolean get() = true // Example flag
}
