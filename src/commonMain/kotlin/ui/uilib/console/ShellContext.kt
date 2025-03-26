package ui.uilib.console

import ConsoleContext
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import cengine.project.Project


class ShellContext(val project: Project) : ConsoleContext {

    override var directory by mutableStateOf(project.fileSystem.root)
    var terminalState by mutableStateOf(TextFieldValue(prompt()))

    // This keeps track of the index where user input begins.
    var inputStartIndex by mutableStateOf(terminalState.text.length)

    val shellCmdHistory = mutableStateListOf<String>()

    private fun prompt(): String = "${directory.path}$ "

    override fun stream(message: String) {
        terminalState = terminalState.copy(terminalState.text + message)
    }

    fun streamprompt() {
        stream(prompt())
        terminalState = terminalState.copy(selection = TextRange(terminalState.text.length))
        inputStartIndex = terminalState.text.length
    }

    fun clear() {
        terminalState = TextFieldValue("")
    }

    fun complete(completion: String) {
        val newText = terminalState.text.substring(0, terminalState.selection.start) + completion + terminalState.text.substring(terminalState.selection.end)
        val newTextRange = TextRange(terminalState.selection.start + completion.length)
        terminalState = terminalState.copy(newText, selection = newTextRange)
    }

    fun replaceCommand(newCommand: String = "") {
        val newText = terminalState.text.substring(0, inputStartIndex) + newCommand
        terminalState = terminalState.copy(newText, selection = TextRange(newText.length))
    }

}