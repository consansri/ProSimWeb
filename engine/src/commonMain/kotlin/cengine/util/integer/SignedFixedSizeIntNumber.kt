package cengine.util.integer

/**
 * Sealed interface for fixed-size signed integer numbers.
 */
sealed interface SignedFixedSizeIntNumber<T : SignedFixedSizeIntNumber<T>> : FixedSizeIntNumber<T> {

    operator fun unaryMinus(): T

    /** Converts the number to its unsigned representation if applicable, otherwise returns itself. */
    fun toUnsigned(): UnsignedFixedSizeIntNumber<*>

    override fun uPaddedBin(): String = toUnsigned().uPaddedBin()
    override fun uPaddedHex(): String = toUnsigned().uPaddedHex()
}
