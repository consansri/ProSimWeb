package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.Editable
import cengine.editor.text.Informational

class DeleteModCmd(private val start: Int, private val end: Int) : TextModCmd {
    private lateinit var deletedText: String
    private lateinit var oldSelection: Pair<Int?, Int?>
    private var oldCaretIndex: Int = 0
    override fun execute(editable: Editable, informational: Informational, selector: Selector) {
        deletedText = informational.substring(start, end)
        oldSelection = selector.selection.start to selector.selection.end
        oldCaretIndex = selector.caret.index
        editable.delete(start, end)
    }

    override fun undo(editable: Editable, informational: Informational, selector: Selector) {
        editable.insert(start, deletedText)
        selector.caret.set(oldCaretIndex)
        selector.selection.select(oldSelection)
    }
}