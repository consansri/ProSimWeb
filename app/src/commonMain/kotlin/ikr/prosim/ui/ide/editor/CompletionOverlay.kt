package ui.ide.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import cengine.editor.completion.Completion
import uilib.UIState

@Composable
fun CompletionOverlay(
    modifier: Modifier,
    textStyle: TextStyle = UIState.BaseStyle.current,
    completions: List<Completion>,
    selectedCompletionIndex: Int
) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    val vScroll = rememberScrollState()

    if (completions.isNotEmpty()) {
        Column(
            modifier = modifier
                .background(theme.COLOR_BG_1, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                .width(200.dp)
                .heightIn(min = 0.dp, max = 400.dp)
                .verticalScroll(vScroll)
        ) {
            completions.forEachIndexed { index, completion ->
                Box(
                    modifier = Modifier
                        .background(if (index == selectedCompletionIndex) theme.COLOR_SELECTION else Color.Transparent, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                        .fillMaxWidth()
                        .padding(scale.SIZE_INSET_MEDIUM, 0.dp)
                ) {
                    Text(completion.displayText, color = theme.COLOR_FG_0, fontFamily = textStyle.fontFamily, fontSize = textStyle.fontSize)
                }
            }
        }
    }
}