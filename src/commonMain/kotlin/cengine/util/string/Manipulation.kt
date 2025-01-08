package cengine.util.string

/**
 *  This Object contains some often needed [String] helping functions.
 */

private val leadingZerosRegex = Regex("^0+(?!$)")

/**
 * Removes leading zeros from a string.
 * Using [Regex]!
 *
 * @return the input string with the leading zeros removed, or "0" if the input string is empty
 */
fun String.removeLeadingZeros(): String = replaceFirst(leadingZerosRegex, "").ifEmpty { "0" }
