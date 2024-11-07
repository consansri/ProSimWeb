package ui.uilib.label

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import ui.uilib.UIState
import ui.uilib.params.IconType

@Composable
fun CLabel(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    annotatedString: AnnotatedString? = null,
    text: String? = null,
    color: Color? = null,
    textAlign: TextAlign = TextAlign.Center,
    textDecoration: TextDecoration = TextDecoration.None,
    iconType: IconType = IconType.SMALL,
    textStyle: TextStyle = UIState.BaseStyle.current,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    softWrap: Boolean = true,
    active: Boolean = true
) {

    val scaling by UIState.Scale
    val theme by UIState.Theme

    val text = remember { text }
    val icon = remember { icon }

    Row(
        modifier
            .background(Color.Transparent, shape = RoundedCornerShape(scaling.SIZE_CORNER_RADIUS))
            .padding(scaling.SIZE_INSET_MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {

        if (icon != null) {
            Image(
                icon,
                colorFilter = ColorFilter.tint(theme.COLOR_FG_0),
                modifier = Modifier.size(iconType.getSize()),
                contentDescription = "Add Icon"
            )
        }

        if (icon != null && text != null) {
            Spacer(Modifier.width(scaling.SIZE_INSET_MEDIUM))
        }

        if (annotatedString != null) {
            Text(
                annotatedString,
                overflow = TextOverflow.Clip,
                textAlign = textAlign,
                fontFamily = textStyle.fontFamily,
                fontSize = textStyle.fontSize,
                softWrap = softWrap,
                color = if (!active) UIState.Theme.value.COLOR_FG_0.copy(0.5f) else UIState.Theme.value.COLOR_FG_0,
            )
        } else if (text != null) {
            Text(
                text,
                overflow = TextOverflow.Clip,
                textAlign = textAlign,
                fontFamily = textStyle.fontFamily,
                fontSize = textStyle.fontSize,
                textDecoration = textDecoration,
                softWrap = softWrap,
                color = color ?: (if (!active) UIState.Theme.value.COLOR_FG_0.copy(0.5f) else UIState.Theme.value.COLOR_FG_0),
            )
        }
    }
}
