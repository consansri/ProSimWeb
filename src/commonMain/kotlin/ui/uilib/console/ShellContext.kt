package ui.uilib.console

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import cengine.project.Project


class ShellContext(val project: Project) {

    var directory by  mutableStateOf(project.fileSystem.root)
    var terminalState by  mutableStateOf(TextFieldValue(prompt()))

    // This keeps track of the index where user input begins.
    var inputStartIndex by  mutableStateOf(terminalState.text.length)

    fun prompt(): String = "${directory.path}: "

    fun clear() {
        terminalState = TextFieldValue("")
    }

}