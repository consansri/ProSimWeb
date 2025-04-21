package cengine.editor.text.state

import cengine.editor.text.Editable
import cengine.editor.text.Informational

interface StateModel : Editable {
    val editable: Editable
    val informational: Informational
    val canUndo: Boolean
    val canRedo: Boolean

    fun undo()
    fun redo()
}