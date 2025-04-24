package cengine.util.integer

/**
 * Sealed interface for fixed-size unsigned integer numbers.
 */
sealed interface UnsignedFixedSizeIntNumber<T : UnsignedFixedSizeIntNumber<T>> : FixedSizeIntNumber<T> {
    // Add comparison operators from UnsignedExtension
    operator fun compareTo(other: UInt): Int
    operator fun compareTo(other: ULong): Int

    /** Converts the number to its unsigned representation if applicable, otherwise returns itself. */
    fun toSigned(): IntNumber<*>

    override fun uPaddedBin(): String = toString(2).padStart(bitWidth, '0')
    override fun uPaddedHex(): String = toString(16).padStart(byteCount * 2, '0')
}