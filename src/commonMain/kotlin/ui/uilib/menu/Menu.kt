package ui.uilib.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ui.uilib.UIState
import ui.uilib.interactable.CInput
import ui.uilib.label.CLabel
import ui.uilib.params.IconType
import ui.uilib.text.CTextField

@Composable
fun Menu(position: Offset, onDismiss: () -> Unit = {}, items: @Composable ColumnScope.() -> Unit) {

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
        offset = position.round()
    ) {

        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(UIState.Theme.value.COLOR_BG_OVERLAY, shape = RoundedCornerShape(UIState.Scale.value.SIZE_CORNER_RADIUS))
                .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
        ) {
            Column {
                items()
            }
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        Modifier
            .clickable(interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource)
            .background(if (isHovered) UIState.Theme.value.COLOR_ICON_BG_HOVER else Color.Transparent)
    ) {
        CLabel(text = text, icon = icon, iconType = IconType.SMALL, textStyle = UIState.BaseStyle.current)
    }
}

@Composable
fun MenuItemWithAttrs(icon: ImageVector, text: String, defaultAttrs: List<String>, onClick: (attrs: List<String>) -> Unit) {

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var value by remember { mutableStateOf(TextFieldValue(defaultAttrs.joinToString(" "))) }

    Row(
        Modifier
            .clickable(interactionSource, indication = null) {
                onClick(value.text.split("\\s+".toRegex()).filter { it.isNotEmpty() })
            }
            .hoverable(interactionSource)
            .background(if (isHovered) UIState.Theme.value.COLOR_ICON_BG_HOVER else Color.Transparent)
    ) {
        CLabel(text = text, icon = icon, iconType = IconType.SMALL, textStyle = UIState.BaseStyle.current)
        CTextField(value, onValueChange = {
            value = it
        }, modifier = Modifier.focusable(true).clickable { }, textStyle = UIState.CodeStyle.current)
    }

}

