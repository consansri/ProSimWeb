package cengine.editor.text

class StringModel(private var text: String) : cengine.editor.text.TextModel {
    override val length: Int get() = text.length
    override val lines: Int get() = text.count { it == '\n' } + 1
    override val maxColumns: Int get() = calculateMaxColumns()

    override fun insert(index: Int, new: String) {
        text = text.substring(0, index) + new + text.substring(index)
    }

    override fun delete(start: Int, end: Int) {
        text = text.substring(0, start) + text.substring(end)
    }

    override fun replaceAll(new: String) {
        text = new
    }

    override fun substring(start: Int, end: Int): String {
        return text.substring(start, end)
    }

    override fun charAt(index: Int): Char {
        return text[index]
    }

    override fun getLineAndColumn(index: Int): Pair<Int, Int> {
        val relString = text.substring(0, index)
        if (relString.isEmpty()) {
            return 0 to 0
        }
        val lastLineCountIndex = relString.indexOfLast { it == '\n' }
        return relString.count { it == '\n' } to index - lastLineCountIndex - 1
    }

    override fun indexOf(line: Int, column: Int): Int {
        require(line >= 0 && column >= 0) { "Line ($line) and Column ($column) must be non-negative" }

        var currLine = 0
        var currColumn = 0
        var lastLineStart = 0

        for (index in text.indices) {
            if (currLine == line && currColumn == column) {
                return index
            }

            if (text[index] == '\n') {
                if (line == currLine) {
                    // If we're on the correct line but haven't reached the column,
                    // return the index of the newline character
                    return index
                }
                currLine++
                currColumn = 0
                lastLineStart = index + 1
            } else {
                currColumn++
            }
        }

        // If we've reached here, we're either at the end of the text
        // or the requested line/column is beyond the text
        return if (currLine == line) {
            // We're on the correct line, but the column is beyond the text length
            // Return the last character index of this line
            minOf(lastLineStart + column, text.length)
        } else {
            // The requested line is beyond the text, return the last index
            text.length
        }
    }

    override fun findAllOccurrences(searchString: String, ignoreCase: Boolean): List<IntRange> {
        if (searchString.isEmpty() || text.isEmpty()) {
            return emptyList()
        }

        val results = mutableListOf<IntRange>()
        var startIndex = 0

        while (true) {
            val index = text.indexOf(searchString, startIndex, ignoreCase)

            if(index == -1) break

            results.add(index until index + searchString.length)
            startIndex = index + 1
        }

        return results
    }

    override fun toString(): String = text

    private fun calculateMaxColumns(): Int = text.split('\n').maxOfOrNull { it.length } ?: 0
}