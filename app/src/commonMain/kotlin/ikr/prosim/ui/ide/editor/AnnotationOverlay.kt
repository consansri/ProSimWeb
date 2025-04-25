package ikr.prosim.ui.ide.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import cengine.editor.annotation.Annotation
import uilib.UIState

@Composable
fun AnnotationOverlay(
    modifier: Modifier,
    textStyle: TextStyle = UIState.BaseStyle.current,
    annotations: Set<Annotation>
) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    val vScroll = rememberScrollState()

    if (annotations.isNotEmpty()) {
        Column(
            modifier = modifier
                .background(theme.COLOR_BG_1, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                .width(400.dp)
                .heightIn(min = 0.dp, max = 400.dp)
                .verticalScroll(vScroll)
        ) {
            annotations.forEachIndexed { index, annotation ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(scale.SIZE_INSET_MEDIUM, 0.dp)
                ) {
                    Text(annotation.displayText, color = theme.getColor(annotation.severity.color), fontFamily = textStyle.fontFamily, fontSize = textStyle.fontSize)
                }
            }
        }
    }
}