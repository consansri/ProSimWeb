package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.Editable
import cengine.editor.text.Informational

class ReplaceAllModCmd(private val newText: String) : TextModCmd {
    private lateinit var oldText: String
    private lateinit var oldSelection: Pair<Int?, Int?>
    private var oldCaretIndex: Int = 0

    override fun execute(editable: Editable, informational: Informational, selector: Selector) {
        oldText = informational.substring(0, informational.length)
        oldSelection = selector.selection.start to selector.selection.end
        oldCaretIndex = selector.caret.index
        editable.replaceAll(newText)
    }

    override fun undo(editable: Editable, informational: Informational, selector: Selector) {
        editable.replaceAll(oldText)
        selector.caret.set(oldCaretIndex)
        selector.selection.select(oldSelection)
    }
}