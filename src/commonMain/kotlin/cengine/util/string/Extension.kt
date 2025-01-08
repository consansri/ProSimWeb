package cengine.util.string


/**
 * Returns the line and column of the given index in the given string.
 *
 * @param index index in the string
 * @return line and column of the index, 0-indexed
 */
fun String.lineAndColumn(index: Int): Pair<Int, Int> {
    val relString = substring(0, index)
    if (relString.isEmpty()) {
        return 0 to 0
    }
    val lastLineCountIndex = relString.indexOfLast { it == '\n' }
    return relString.count { it == '\n' } to index - lastLineCountIndex - 1
}
