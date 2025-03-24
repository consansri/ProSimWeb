package ui.uilib.console

import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.SpanStyle
import ui.uilib.text.KeywordHighlightTransformation


data class ShellCmd(val keyword: String, val onPrompt: ShellContext.(attrs: List<String>) -> String) {

    companion object {
        val BASE = listOf(
            ShellCmd("clear") { attrs ->
                clear()
                ""
            },
            ShellCmd("ls") { attrs ->
                directory.getChildren().joinToString { it.name }
            }
        )
    }

}