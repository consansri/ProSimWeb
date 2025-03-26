package cengine.util.string

/**
 *  This Object contains some often needed [String] helping functions.
 */

private val leadingZerosRegex = Regex("^0+(?!$)")
private val spacesRegex = "\\s+".toRegex()

/**
 * Removes leading zeros from a string.
 * Using [Regex]!
 *
 * @return the input string with the leading zeros removed, or "0" if the input string is empty
 */
fun String.removeLeadingZeros(): String = replaceFirst(leadingZerosRegex, "").ifEmpty { "0" }

fun String.splitBySpaces(): List<String> = split(spacesRegex).filter { it.isNotEmpty() }

fun List<String>.commonPrefix(): String {
    if (isEmpty()) return ""
    var prefix = first()
    for (s in this) {
        while (!s.startsWith(prefix)) {
            prefix = prefix.dropLast(1)
            if (prefix.isEmpty()) break
        }
    }
    return prefix
}
