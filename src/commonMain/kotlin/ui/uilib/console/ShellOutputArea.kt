package ui.uilib.console

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import ui.uilib.UIState


@Composable
fun ShellOutputArea(messages: List<String>) {

    val theme = UIState.Theme.value

    Surface(Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier) {
            items(messages) { message ->
                Text(
                    text = message,
                    style = UIState.CodeStyle.current,
                    color = when {
                        message.startsWith("Error") -> theme.COLOR_RED
                        message.startsWith("Warn") -> theme.COLOR_YELLOW
                        message.startsWith("Log") -> theme.COLOR_FG_1
                        else -> theme.COLOR_FG_0
                    }
                )
            }

        }

    }

}