package cengine.util.string




/**
 * Converts an ASCII string to its hexadecimal representation.
 *
 * @return The hexadecimal representation of this string.
 */
fun String.asciiToHexString(): String {
    return this.map {
        it.code.toString(16).padStart(2, '0')
    }.joinToString("") { it }
}