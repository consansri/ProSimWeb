package cengine.util.integer

/**
 * Sealed interface for integer numbers with unlimited size (like BigInt).
 */
sealed interface UnlimitedSizeIntNumber<T : UnlimitedSizeIntNumber<T>> : IntNumber<T> {

    override val type: UnlimitedSizeIntNumberT<T>

}