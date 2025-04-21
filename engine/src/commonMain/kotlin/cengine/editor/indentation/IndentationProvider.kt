package cengine.editor.indentation

import cengine.editor.selection.Selector

interface IndentationProvider {

    val spaces: Int

    /**
     * @return number of spaces which where inserted.
     */
    fun indentAtIndex(index: Int): Int

    /**
     * @return number of spaces which where inserted.
     */
    fun addLineIndent(line: Int): Int

    /**
     * @return number of spaces which where removed.
     */
    fun removeLineIndent(line: Int): Int

    fun indentSelection(selector: Selector) {
        val range = selector.selection.asRange()
        if (range == null) {
            // Index Indentation
            selector.caret += indentAtIndex(selector.caret.index)
        } else {
            // Selection Indentation
            val (firstLine, firstCol) = selector.caret.model.getLineAndColumn(range.first)
            val (lastLine, lastCol) = selector.caret.model.getLineAndColumn(range.last + 1)

            var insideSelectionSpaceChange = 0
            var beforeSelectionSpaceChange = 0

            val caretIsAtHigherBound = selector.caretIsAtHigherBoundOfSel()

            for (line in lastLine downTo firstLine) {
                if (line == firstLine) {
                    beforeSelectionSpaceChange += addLineIndent(line)
                } else {
                    insideSelectionSpaceChange += addLineIndent(line)
                }
            }

            val newStartIndex: Int = range.first + beforeSelectionSpaceChange
            val newEndIndex: Int = range.last + 1 + beforeSelectionSpaceChange + insideSelectionSpaceChange

            if (caretIsAtHigherBound) {
                selector.selection.select(newStartIndex, newEndIndex)
                selector.caret.set(newEndIndex)
            } else {
                selector.selection.select(newEndIndex, newStartIndex)
                selector.caret.set(newStartIndex)
            }
        }
    }

    fun unindentSelection(selector: Selector) {
        val range = selector.selection.asRange()
        if (range == null) {
            // Index Unindent
            val length = removeLineIndent(selector.caret.line)
            selector.caret.set(selector.caret.line, (selector.caret.col - length).coerceAtLeast(0))
        } else {
            // Selection Unindent
            // Selection Indentation
            val (firstLine, firstCol) = selector.caret.model.getLineAndColumn(range.first)
            val (lastLine, lastCol) = selector.caret.model.getLineAndColumn(range.last + 1)

            var insideSelectionSpaceChange = 0
            var beforeSelectionSpaceChange = 0

            val caretIsAtHigherBound = selector.caretIsAtHigherBoundOfSel()

            for (line in lastLine downTo firstLine) {
                if (line == firstLine) {
                    beforeSelectionSpaceChange += removeLineIndent(line)
                } else {
                    insideSelectionSpaceChange += removeLineIndent(line)
                }
            }

            val newStartIndex: Int = if (firstCol != 0) range.first - beforeSelectionSpaceChange else range.first
            val newEndIndex: Int = range.last + 1 - beforeSelectionSpaceChange - insideSelectionSpaceChange

            if (caretIsAtHigherBound) {
                selector.selection.select(newStartIndex, newEndIndex)
                selector.caret.set(newEndIndex)
            } else {
                selector.selection.select(newEndIndex, newStartIndex)
                selector.caret.set(newStartIndex)
            }
        }

    }
}