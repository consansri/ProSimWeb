package ikr.prosim.ui.ide.editor.state

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

interface UndoRedoState {
    val canUndo: Boolean
    val canRedo: Boolean
    fun undo()
    fun redo()
    fun recordChange(oldValue: TextFieldValue, newValue: TextFieldValue)
    fun recordReplaceAll(oldFullText: String, newFullText: String, newSelection: TextRange = TextRange(newFullText.length))
    fun clearHistory()
}