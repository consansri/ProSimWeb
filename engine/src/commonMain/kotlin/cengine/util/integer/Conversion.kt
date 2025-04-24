package cengine.util.integer

// This file provides utility functions for integer conversions.

/**
 * This function generates a space-separated string of zero-padded hexadecimal values from an array of [IntNumber] objects.
 */
inline fun <reified T : UnsignedFixedSizeIntNumber<*>> Array<T>.hexDump(): String = joinToString(" ") { it.uPaddedHex() }

