package ikr.prosim.ui.ide.editor.state

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import cengine.console.SysOut
import ikr.prosim.ui.ide.editor.StringUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

class TextStateModelImpl(
    initialTfv: TextFieldValue,
    private val editGroupTimeThresholdMillis: Long = 500L
) : UndoRedoState {

    private var currentTfv: TextFieldValue = initialTfv
    private val undoStack = mutableListOf<EditGroup>()
    private val redoStack = mutableListOf<EditGroup>()
    private var currentEditGroup: EditGroup? = null


    var onStateUpdate: (TextFieldValue) -> Unit = {
        // Needs to be set
    }

    override val canUndo: Boolean
        get() = undoStack.isNotEmpty() || currentEditGroup?.commands?.isNotEmpty() == true

    override val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    private fun endCurrentEditGroup() {
        currentEditGroup?.let {
            if (it.commands.isNotEmpty()) {
                undoStack.add(it)
                if (undoStack.size > 100) { // Limit undo history size
                    undoStack.removeFirstOrNull()
                }
            }
        }
        currentEditGroup = null
    }

    private fun ensureEditGroup() {
        val now = Clock.System.now()
        if (currentEditGroup == null || (now - currentEditGroup!!.lastEditTime) > editGroupTimeThresholdMillis.milliseconds) {
            endCurrentEditGroup()
            currentEditGroup = EditGroup(lastEditTime = now)
        } else {
            currentEditGroup?.lastEditTime = now
        }
    }

    override fun recordChange(oldValue: TextFieldValue, newValue: TextFieldValue) {
        val oldText = oldValue.text
        val newText = newValue.text

        if (oldText == newText) return

        ensureEditGroup()

        val commonPrefixLen = StringUtils.commonPrefixLength(oldText, newText)
        val commonSuffixLen = StringUtils.commonSuffixLength(oldText, newText)

        val deletedText = oldText.substring(commonPrefixLen, oldText.length - commonSuffixLen)
        val insertedText = newText.substring(commonPrefixLen, newText.length - commonSuffixLen)

        val command = when {
            deletedText.isNotEmpty() && insertedText.isNotEmpty() ->
                TextModCommand.CharacterReplaceCommand(commonPrefixLen, deletedText, insertedText)
            insertedText.isNotEmpty() ->
                TextModCommand.InsertCommand(commonPrefixLen, insertedText)
            deletedText.isNotEmpty() ->
                TextModCommand.DeleteCommand(commonPrefixLen, deletedText)
            else -> null // Should not happen if oldText != newText
        }

        command?.let {
            currentEditGroup?.commands?.add(it)
            this.currentTfv = newValue
            redoStack.clear()
        }

        SysOut.debug { "RecordChange: \n\tRedoStack: ${redoStack.joinToString("\n")}\n\tUndoStack: ${undoStack.joinToString("\n")}" }
    }

    override fun recordReplaceAll(oldFullText: String, newFullText: String, newSelection: TextRange) {
        ensureEditGroup()
        val command = TextModCommand.ReplaceAllCommand(oldFullText, newFullText)
        currentEditGroup!!.commands.add(command)
        this.currentTfv = TextFieldValue(newFullText, newSelection)
        redoStack.clear()
        endCurrentEditGroup() // A "Replace All" is a significant, distinct action
        SysOut.debug { "ReplaceAll: \n\tRedoStack: ${redoStack.joinToString("\n")}\n\tUndoStack: ${undoStack.joinToString("\n")}" }
    }


    override fun undo() {
        endCurrentEditGroup()
        if (undoStack.isEmpty()) return

        val groupToUndo = undoStack.removeLast()
        val undoneTfv = groupToUndo.undo(currentTfv)

        currentTfv = undoneTfv
        onStateUpdate(undoneTfv)
        redoStack.add(groupToUndo)
        SysOut.debug { "Undo: \n\tRedoStack: ${redoStack.joinToString("\n")}\n\tUndoStack: ${undoStack.joinToString("\n")}" }
    }

    override fun redo() {
        if (redoStack.isEmpty()) return

        val groupToRedo = redoStack.removeLast()
        val redoneTfv = groupToRedo.redo(currentTfv)

        currentTfv = redoneTfv
        onStateUpdate(redoneTfv)
        undoStack.add(groupToRedo)
        SysOut.debug { "Redo: \n\tRedoStack: ${redoStack.joinToString("\n")}\n\tUndoStack: ${undoStack.joinToString("\n")}" }
    }

    override fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        currentEditGroup = null
    }

    private data class EditGroup(
        val commands: MutableList<TextModCommand> = mutableListOf(),
        var lastEditTime: Instant = Clock.System.now()
    ) {
        fun undo(initialTfv: TextFieldValue): TextFieldValue {
            var tfv = initialTfv
            commands.asReversed().forEach { cmd ->
                tfv = cmd.undo(tfv)
            }
            return tfv
        }

        fun redo(initialTfv: TextFieldValue): TextFieldValue {
            var tfv = initialTfv
            commands.forEach { cmd ->
                tfv = cmd.redo(tfv)
            }
            return tfv
        }

        override fun toString(): String {
            return "{time=$lastEditTime: ${commands.joinToString(",") { it.toString() }}}"
        }
    }
}