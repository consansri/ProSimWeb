package cengine.editor.selection

import cengine.editor.text.TextModel

data class Caret(
    val model: TextModel
) {
    var index: Int = 0
        set(value) {
            field = value
            val res = model.getLineAndColumn(value)
            line = res.first
            col = res.second
        }

    var line: Int = 0
    var col: Int = 0

    fun set(line: Int, col: Int) {
        index = model.indexOf(line, col)
    }

    fun set(newIndex: Int) {
        index = newIndex
    }

    fun moveUp(offset: Int) {
        index = if (line - offset >= 0) {
            model.indexOf(line - offset, col)
        } else {
            0
        }
    }

    fun moveDown(offset: Int) {
        index = if (line + offset <= model.lines) {
            model.indexOf(line + offset, col)
        } else {
            model.length
        }
    }

    operator fun plusAssign(other: Int) {
        if (index + other <= model.length) {
            index += other
        }
    }

    operator fun minusAssign(other: Int) {
        if (index - other >= 0) {
            index -= other
        }
    }
}