package cengine.editor.selection

import cengine.editor.text.TextModel

class Selector(textModel: TextModel) {

    val caret: Caret = Caret(textModel)
    val selection: Selection = Selection()

    private var tempMaxColOfUpDownMovement: Int = 0

    companion object {
        val DEFAULT_SYMBOL_CHARS = ('a'.rangeTo('z') + 'A'.rangeTo('Z') + '0'.rangeTo('9') + '_').toCharArray()
        val DEFAULT_SPACING_SET = charArrayOf(' ', '\n')
        val ONLY_SPACES = charArrayOf(' ')
    }

    // Modification

    private fun internalMoveCaret(index: Int, shift: Boolean) {
        if (shift) {
            if (!selection.valid()) {
                selection.select(caret.index, index)
            } else {
                selection.moveEnd(index)
            }
        } else {
            selection.deselect()
        }
        caret.set(index)
    }

    fun moveCaretTo(index: Int, shift: Boolean) {
        internalMoveCaret(index, shift)
        tempMaxColOfUpDownMovement = caret.col
    }


    fun internalMoveCaret(line: Int, column: Int, shift: Boolean) {
        val index = caret.model.indexOf(line, column)
        internalMoveCaret(index, shift)
        tempMaxColOfUpDownMovement = caret.col
    }

    fun moveCaretLeft(offset: Int, shift: Boolean) {
        val range = selection.asRange()
        val newIndex = if (!shift && range != null && range.first != caret.index) {
            // move the caret to the beginning of the selection if it isn't already there.
            range.first
        } else {
            (caret.index - offset).coerceAtLeast(0)
        }

        internalMoveCaret(newIndex, shift)
        tempMaxColOfUpDownMovement = caret.col
    }

    fun moveCaretRight(offset: Int, shift: Boolean) {
        val range = selection.asRange()
        val newIndex = if (!shift && range != null && range.last + 1 != caret.index) {
            // move the caret to the end of the selection if it isn't already there.
            range.last + 1
        } else {
            (caret.index + offset).coerceAtMost(caret.model.length)
        }

        internalMoveCaret(newIndex, shift)
        tempMaxColOfUpDownMovement = caret.col
    }

    fun moveCaretUp(offset: Int, shift: Boolean) {
        val range = selection.asRange()

        val newLine = if (!shift && range != null && range.first != caret.index) {
            // move the caret above the current selection if it isn't already at the lower bound of the selection.
            (caret.model.getLineAndColumn(range.first).first - 1).coerceAtLeast(0)
        } else {
            (caret.line - offset).coerceAtLeast(0)
        }
        val newIndex = caret.model.indexOf(newLine, tempMaxColOfUpDownMovement)

        internalMoveCaret(newIndex, shift)
        tempMaxColOfUpDownMovement = maxOf(caret.col, tempMaxColOfUpDownMovement)
    }

    fun moveCaretDown(offset: Int, shift: Boolean) {
        val range = selection.asRange()

        val newLine = if (!shift && range != null && range.last + 1 != caret.index) {
            // move the caret under the current selection if it isn't already at the higher bound of the selection.
            (caret.model.getLineAndColumn(range.last + 1).first + 1).coerceAtMost(caret.model.lines)
        } else {
            (caret.line + offset).coerceAtMost(caret.model.lines)
        }
        val newIndex = caret.model.indexOf(newLine, tempMaxColOfUpDownMovement)

        internalMoveCaret(newIndex, shift)
        tempMaxColOfUpDownMovement = maxOf(caret.col, tempMaxColOfUpDownMovement)
    }

    fun home(shift: Boolean, ctrl: Boolean) {
        if (ctrl) {
            internalMoveCaret(0, shift)
        } else {
            val rowStartIndex = caret.model.indexOf(caret.line, 0)
            val indexOfFirstValidInCol = indexOfWordEnd(rowStartIndex, ONLY_SPACES, true)
            if (caret.index != indexOfFirstValidInCol) {
                internalMoveCaret(indexOfFirstValidInCol, shift)
            } else {
                internalMoveCaret(rowStartIndex, shift)
            }
        }

        tempMaxColOfUpDownMovement = caret.col
    }

    fun end(shift: Boolean, ctrl: Boolean) {
        if (ctrl) {
            internalMoveCaret(caret.model.length, shift)
        } else {
            internalMoveCaret(caret.line, Int.MAX_VALUE, shift)
        }
        tempMaxColOfUpDownMovement = caret.col
    }

    // Information

    fun caretIsAtHigherBoundOfSel(): Boolean = caret.index == selection.higher

    fun caretIsAtLowerBoundsOfSel(): Boolean = caret.index == selection.lower

    /**
     * Select the word at the caret.
     */
    fun selectCurrentWord(index: Int, chars: CharArray, isValidSet: Boolean) {
        val start = indexOfWordStart(index, chars, isValidSet)
        val end = indexOfWordEnd(index, chars, isValidSet)
        selection.select(start, end)
        caret.set(end)
    }

    /**
     * Returns the index of the start of the current word that the caret is in.
     * If the caret is at a word boundary, it returns the start of the next word.
     * @param chars Array of characters that are allowed or not allowed depending on [isValidSet] to be part of the result.
     */
    fun indexOfWordStart(beginningIndex: Int, chars: CharArray, isValidSet: Boolean): Int {
        var index = beginningIndex

        // Move backward until we find an invalid character or the start of the text
        while (index > 0 && (caret.model.charAt(index - 1) in chars) == isValidSet) {
            index--
        }

        return index
    }

    /**
     * Returns the index of the end of the current word that the caret is in.
     * If the caret is at a word boundary, it returns the end of the previous word.
     * @param chars Array of characters that are allowed or not allowed depending on [isValidSet] to be part of the result.
     */
    fun indexOfWordEnd(beginningIndex: Int, chars: CharArray, isValidSet: Boolean): Int {
        var index = beginningIndex

        // Move forward until we find an invalid character or the end of the text
        while (index < caret.model.length && (caret.model.charAt(index) in chars) == isValidSet) {
            index++
        }

        return index
    }
}