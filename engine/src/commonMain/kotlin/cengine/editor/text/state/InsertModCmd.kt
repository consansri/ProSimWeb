package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.Editable
import cengine.editor.text.Informational

class InsertModCmd(private val index: Int, private val text: String) : TextModCmd {
    private lateinit var oldSelection: Pair<Int?, Int?>
    private var oldCaretIndex: Int = 0
    override fun execute(editable: Editable, informational: Informational, selector: Selector) {
        editable.insert(index, text)
        oldSelection = selector.selection.start to selector.selection.end
        oldCaretIndex = selector.caret.index
    }

    override fun undo(editable: Editable, informational: Informational, selector: Selector) {
        editable.delete(index, index + text.length)
        selector.caret.set(oldCaretIndex)
        selector.selection.select(oldSelection)
    }
}