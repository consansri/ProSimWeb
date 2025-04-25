package ui.uilib.console

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import cengine.console.ShellCmd
import cengine.console.ShellContext
import cengine.util.string.commonPrefix
import cengine.util.string.splitBySpaces
import cengine.vfs.FPath.Companion.toFPath
import kotlinx.coroutines.launch
import uilib.UIState
import uilib.text.KeywordHighlightTransformation


@Composable
fun UnifiedTerminalShell(context: ShellContext) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val runConfigs = context.project.services.map { it.runConfig }

    val consoleExecutionScope = rememberCoroutineScope()

    // Create a FocusRequester to manage focus manually
    val focusRequester = remember { FocusRequester() }

    val keyWordHighlighter = KeywordHighlightTransformation(
        ShellCmd.BASE.map { it.keyword } to SpanStyle(color = theme.COLOR_BLUE),
        runConfigs.map { it.name } to SpanStyle(color = theme.COLOR_ORANGE)
    )

    // A helper function to process commands.
    suspend fun processCommand(command: String) {
        // Replace with real command parsing/execution logic.
        val attrs = command.splitBySpaces()
        if (attrs.isEmpty()) {
            return
        }

        val baseCmd = ShellCmd.BASE.firstOrNull {
            attrs.first().lowercase() == it.keyword.lowercase()
        }

        if (baseCmd != null) {
            return baseCmd.onPrompt(context, attrs.drop(1))
        }

        val runConfig = runConfigs.firstOrNull { attrs.first().lowercase() == it.name.lowercase() }
        if (runConfig != null) {
            runConfig.run(context, context.project, *attrs.drop(1).toTypedArray())
            return
        }

        context.error("$command is invalid")
        context.info(
            """
            Commands:
                Basic:  ${ShellCmd.BASE.joinToString(", ") { it.keyword }}
                Runner: ${runConfigs.joinToString(", ") { it.name }}
            """.trimIndent()
        )
    }

    suspend fun processCommands(input: String) {
        if (input.trim().isEmpty()) return

        context.shellCmdHistory.remove(input)
        context.shellCmdHistory.add(input)

        val commands = input.split(";").map { it.trim() }
        commands.forEach {
            processCommand(it)
        }
    }


    BasicTextField(
        value = context.terminalState,
        onValueChange = { newValue ->
            // Ensure that modifications before the current prompt are ignored.
            if (newValue.text.startsWith(context.terminalState.text.substring(0, context.inputStartIndex))) {
                context.terminalState = newValue
            }
        },
        textStyle = UIState.CodeStyle.current.copy(UIState.Theme.value.COLOR_FG_0),
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .background(theme.COLOR_BG_0)
            .padding(scale.SIZE_INSET_MEDIUM)
            .onPreviewKeyEvent { keyEvent ->
                // Only Handle KeyDown events.
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Enter -> {
                            consoleExecutionScope.launch {
                                // Extract current user command.
                                val input = context.terminalState.text.substring(context.inputStartIndex).trim()
                                // Add LineBreak
                                context.streamln()
                                // Process command (this is synchronous here; consider using coroutines for longer tasks)
                                processCommands(input)
                                // Append command output and a new prompt.
                                context.streamprompt()
                                // Re-request focus to avoid losing it.
                                focusRequester.requestFocus()
                            }

                            // Consume the key event.
                            return@onPreviewKeyEvent true
                        }

                        Key.Backspace -> {
                            // Prevent deleting characters before the prompt.
                            if (context.terminalState.selection.start <= context.inputStartIndex) {
                                return@onPreviewKeyEvent true
                            }
                        }

                        Key.DirectionUp -> {
                            if (context.shellCmdHistory.isNotEmpty()) {
                                val currCommand = context.terminalState.text.substring(context.inputStartIndex).trimEnd()
                                val historyIndex = context.shellCmdHistory.indexOf(currCommand)

                                val nextCommand = if (historyIndex == -1) {
                                    context.shellCmdHistory.last()
                                } else {
                                    context.shellCmdHistory.getOrNull(historyIndex - 1) ?: return@onPreviewKeyEvent true
                                }

                                context.replaceCommand(nextCommand)
                            }

                            return@onPreviewKeyEvent true
                        }

                        Key.DirectionDown -> {
                            if (context.shellCmdHistory.isNotEmpty()) {
                                val currCommand = context.terminalState.text.substring(context.inputStartIndex).trimEnd()
                                val historyIndex = context.shellCmdHistory.indexOf(currCommand)

                                val nextCommand = if (historyIndex == -1) {
                                    return@onPreviewKeyEvent true
                                } else {
                                    context.shellCmdHistory.getOrNull(historyIndex + 1) ?: ""
                                }

                                context.replaceCommand(nextCommand)
                            }

                            return@onPreviewKeyEvent true
                        }

                        Key.MoveHome -> {
                            if (context.terminalState.selection.start > context.inputStartIndex) {
                                if (keyEvent.isShiftPressed) {
                                    context.terminalState = context.terminalState.copy(selection = TextRange(context.terminalState.selection.start, context.inputStartIndex))
                                } else {
                                    context.terminalState = context.terminalState.copy(selection = TextRange(context.inputStartIndex))
                                }
                                return@onPreviewKeyEvent true
                            }
                        }

                        Key.Tab -> {
                            // When Tab is pressed, attempt to complete directory paths.
                            val inputText = context.terminalState.text.substring(context.inputStartIndex, context.terminalState.selection.start)
                            // Identify the last token in the current command (assuming tokens are space-separated)
                            val tokens = inputText.splitBySpaces()
                            val currentToken = tokens.lastOrNull() ?: ""
                            if (currentToken.isNotEmpty()) {
                                val path = currentToken.toFPath()
                                var directory = context.directory
                                var completion = ""
                                for (part in path) {
                                    if (part == "..") {
                                        completion = ""
                                        directory = directory.parent() ?: return@onPreviewKeyEvent false
                                    }

                                    val subdir = directory.getChildren().firstOrNull { it.name == part }
                                    if (subdir != null && subdir.isDirectory) {
                                        completion = ""
                                        directory = subdir
                                    }

                                    val completions = directory.getChildren().map { it.name }.filter { it.startsWith(part) }

                                    if (completions.isNotEmpty()) {
                                        val completedPart = completions.commonPrefix()
                                        completion = completedPart.drop(part.length)
                                    }
                                }
                                context.complete(completion)
                                return@onPreviewKeyEvent true
                            }
                            return@onPreviewKeyEvent false
                        }

                        else -> {}
                    }
                }
                false
            },
        visualTransformation = keyWordHighlighter,
        cursorBrush = SolidColor(theme.COLOR_FG_0)
    )

    // Make sure we initially request focus when the shell appears.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

}