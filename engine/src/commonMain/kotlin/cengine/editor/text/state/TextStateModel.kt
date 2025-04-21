package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.Editable
import cengine.editor.text.Informational
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.DurationUnit

class TextStateModel(override val editable: Editable, override val informational: Informational, private val selector: Selector, private val editGroupTimeThreshold: Long = 500L) : StateModel {
    private val undoStack = mutableListOf<EditGroup>()
    private val redoStack = mutableListOf<EditGroup>()
    private var currentEditGroup: EditGroup? = null

    // StateModel Interface

    override val canUndo: Boolean
        get() = undoStack.isNotEmpty() || currentEditGroup?.commands?.isNotEmpty() == true
    override val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    override fun undo() {
        endEditGroup()
        if (undoStack.isNotEmpty()) {
            val group = undoStack.removeAt(undoStack.lastIndex)
            group.undo(selector)
            redoStack.add(group)
        }
    }

    override fun redo() {
        endEditGroup()
        if (redoStack.isNotEmpty()) {
            val command = redoStack.removeAt(redoStack.lastIndex)
            command.execute(selector)
            undoStack.add(command)
        }
    }

    // TextModel Interface

    override fun insert(index: Int, new: String) = execute(InsertModCmd(index, new), selector)

    override fun delete(start: Int, end: Int) = execute(DeleteModCmd(start, end), selector)

    override fun replaceAll(new: String) = execute(ReplaceAllModCmd(new), selector)

    // StateModel Implementation

    private fun execute(command: TextModCmd, selector: Selector) {
        val now = Clock.System.now()
        if (currentEditGroup == null || (now - currentEditGroup!!.lastEditTime).toLong(DurationUnit.MILLISECONDS) > editGroupTimeThreshold) {
            endEditGroup()
            currentEditGroup = EditGroup()
        }
        currentEditGroup?.addCommand(command)
        command.execute(editable, informational, selector)
        redoStack.clear()
    }

    private fun endEditGroup() {
        currentEditGroup?.let {
            if (it.commands.isNotEmpty()) {
                undoStack.add(it)
            }
        }
        currentEditGroup = null
    }

    private inner class EditGroup {
        val commands = mutableListOf<TextModCmd>()
        var lastEditTime: Instant = Clock.System.now()

        fun addCommand(command: TextModCmd) {
            commands.add(command)
            lastEditTime = Clock.System.now()
        }

        fun execute(selector: Selector) {
            commands.forEach { it.execute(editable, informational, selector) }
        }

        fun undo(selector: Selector) {
            commands.asReversed().forEach { it.undo(editable, informational, selector) }
        }
    }

}