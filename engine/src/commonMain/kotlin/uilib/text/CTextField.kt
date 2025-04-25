package uilib.text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import uilib.UIState


@Composable
fun CTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusLost: (TextFieldValue) -> Unit = {},
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    readonly: Boolean = false,
    textStyle: TextStyle = UIState.BaseStyle.current,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = UIState.Theme.value.COLOR_BORDER,
    error: Boolean = false,
    showBorder: Boolean = true,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit = { innerTextField ->
        innerTextField()
    },
) {
    val scale = UIState.Scale.value
    val theme = UIState.Theme.value
    var isFocused by remember { mutableStateOf(false) }

    val modifiedModifier = if (showBorder) {
        modifier.border(scale.SIZE_BORDER_THICKNESS, if (!error) borderColor else theme.COLOR_RED, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
    } else modifier

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifiedModifier
            .background(backgroundColor)
            .padding(scale.SIZE_INSET_MEDIUM)
            .onFocusChanged { focusState ->
                if (isFocused && !focusState.isFocused) {
                    onFocusLost(value)
                }
                isFocused = focusState.isFocused
            },
        textStyle = textStyle.copy(theme.COLOR_FG_0),
        singleLine = singleLine,
        readOnly = readonly,
        cursorBrush = SolidColor(theme.COLOR_FG_0),
        decorationBox = decorationBox
    )
}

@Composable
fun CTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    readonly: Boolean = false,
    textStyle: TextStyle = UIState.BaseStyle.current,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = UIState.Theme.value.COLOR_BORDER,
    error: Boolean = false,
    showBorder: Boolean = true,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit = { innerTextField ->
        innerTextField()
    },
) {
    val scale = UIState.Scale.value
    val theme = UIState.Theme.value
    var isFocused by remember { mutableStateOf(false) }

    val modifiedModifier = if (showBorder) {
        modifier.border(scale.SIZE_BORDER_THICKNESS, if (!error) borderColor else theme.COLOR_RED, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
    } else modifier

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifiedModifier
                .background(backgroundColor)
                .padding(scale.SIZE_INSET_MEDIUM)
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused) {
                        onFocusLost(value)
                    }
                    isFocused = focusState.isFocused
                },
        textStyle = textStyle.copy(theme.COLOR_FG_0),
        singleLine = singleLine,
        readOnly = readonly,
        cursorBrush = SolidColor(theme.COLOR_FG_0),
        decorationBox = decorationBox
    )
}
