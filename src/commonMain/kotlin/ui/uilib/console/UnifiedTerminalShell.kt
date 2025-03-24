package ui.uilib.console

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import cengine.project.Project
import cengine.util.string.splitBySpaces
import ui.uilib.UIState
import ui.uilib.interactable.CVerticalScrollBar
import ui.uilib.text.KeywordHighlightTransformation


@Composable
fun UnifiedTerminalShell(project: Project) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    val shellContext = remember { ShellContext(project) }

    // A helper function to process commands.
    fun processCommand(command: String): String {
        // Replace with real command parsing/execution logic.
        val attrs = command.splitBySpaces()
        if (attrs.isEmpty()) {
            return ""
        }

        val baseCmd = ShellCmd.BASE.firstOrNull {
            attrs.first().lowercase() == it.keyword.lowercase()
        }

        if (baseCmd != null) {
            return baseCmd.onPrompt(shellContext, attrs.drop(1))
        }

        return "Error: $command is invalid"
    }

    val vScrollState = rememberScrollState()

    CVerticalScrollBar(vScrollState) {
        BasicTextField(
            value = shellContext.terminalState,
            onValueChange = { newValue ->
                // Ensure that modifications before the current prompt are ignored.
                if (newValue.text.startsWith(shellContext.terminalState.text.substring(0, shellContext.inputStartIndex))) {
                    shellContext.terminalState = newValue
                }
            },
            textStyle = UIState.CodeStyle.current,
            modifier = Modifier
                .fillMaxSize()
                .background(theme.COLOR_BG_0)
                .padding(scale.SIZE_INSET_MEDIUM)
                .onPreviewKeyEvent { keyEvent ->
                    // Only Handle KeyDown events.
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.Enter -> {
                                // Extract current user command.
                                val command = shellContext.terminalState.text.substring(shellContext.inputStartIndex).trimEnd()
                                // Process command (this is synchronous here; consider using coroutines for longer tasks)
                                val output = processCommand(command)
                                // Append command output and a new prompt.
                                val newContent = buildString {
                                    val prevContent = shellContext.terminalState.text
                                    if (prevContent.isNotEmpty()) {
                                        append(prevContent)
                                        append("\n")
                                    }
                                    if (output.isNotEmpty()) {
                                        append(output)
                                        append("\n")
                                    }
                                    append(shellContext.prompt())
                                }
                                shellContext.terminalState = TextFieldValue(newContent, TextRange(newContent.length))
                                // Update the input start index to be at the end of the new prompt.
                                shellContext.inputStartIndex = newContent.length
                                // Consume the key event.
                                return@onPreviewKeyEvent true
                            }

                            Key.Backspace -> {
                                // Prevent deleting characters before the prompt.
                                if (shellContext.terminalState.selection.start <= shellContext.inputStartIndex) {
                                    return@onPreviewKeyEvent true
                                }
                            }

                            else -> {}
                        }
                    }
                    false
                },
            visualTransformation = KeywordHighlightTransformation(
                ShellCmd.BASE.map { it.keyword },
                SpanStyle(color = theme.COLOR_BLUE)
            )

        )
    }


}