package cengine.editor.indentation

import cengine.editor.text.Editable
import cengine.editor.text.Informational

/**
 * A very basic indentation provider. It simply inserts or deletes spaces at the given location.
 * It will not indent or unindent any blocks of text.
 *
 * @property spaces The number of spaces to indent or unindent with.
 */
class BasicIndenation(private val editable: Editable, private val informational: Informational, override val spaces: Int = 4) : IndentationProvider {

    override fun indentAtIndex(index: Int): Int {
        val (line, column) = informational.getLineAndColumn(index)
        val location = column % spaces
        val spacesToIndent = spaces - location
        editable.insert(index, " ".repeat(spacesToIndent))
        return spacesToIndent
    }

    override fun addLineIndent(line: Int): Int {
        val index = informational.indexOf(line,0)
        editable.insert(index, " ".repeat(spaces))
        return spaces
    }

    override fun removeLineIndent(line: Int): Int {
        val lineStartIndex = informational.indexOf(line, 0)
        for (length in spaces downTo 1) {
            if (informational.substring(lineStartIndex, lineStartIndex + length) == " ".repeat(length)) {
                editable.delete(lineStartIndex, lineStartIndex + length)
                return length
            }
        }
        return 0
    }
}