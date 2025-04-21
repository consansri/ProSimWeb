package ui.uilib.interactable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import ui.uilib.params.IconType
import uilib.UIState

@Composable
fun CToggle(
    onClick: (toggled: Boolean) -> Unit,
    value: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String? = null,
    textAlign: TextAlign = TextAlign.Center,
    iconType: IconType = IconType.MEDIUM,
    textStyle: TextStyle = UIState.BaseStyle.current,
    softWrap: Boolean = false,
    active: Boolean = true
) {

    val scaling by UIState.Scale
    val theme by UIState.Theme

    val interactionSource = remember { MutableInteractionSource() }

    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = if (value) {
        theme.COLOR_ICON_BG_ACTIVE
    } else if (isHovered) {
        theme.COLOR_ICON_BG_HOVER
    } else {
        Color.Transparent
    }

    Row(
        modifier
            .background(backgroundColor, shape = RoundedCornerShape(scaling.SIZE_CORNER_RADIUS))
            .clickable(interactionSource, indication = null, onClick = if (active) {
                {
                    onClick(!value)
                }
            } else {
                {}
            })
            .hoverable(interactionSource)
            .padding(scaling.SIZE_INSET_MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
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

        if (text != null) {
            Text(
                text,
                textAlign = textAlign,
                fontFamily = textStyle.fontFamily,
                fontSize = textStyle.fontSize,
                softWrap = softWrap,
                color = if (!active) UIState.Theme.value.COLOR_FG_0.copy(0.5f) else UIState.Theme.value.COLOR_FG_0,
            )
        }
    }


}