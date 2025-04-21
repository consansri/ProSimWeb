package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.Editable
import cengine.editor.text.Informational

interface TextModCmd {
    fun execute(editable: Editable, informational: Informational, selector: Selector)
    fun undo(editable: Editable, informational: Informational, selector: Selector)
}