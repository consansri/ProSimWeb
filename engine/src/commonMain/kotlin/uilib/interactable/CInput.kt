package ui.uilib.interactable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import cengine.util.integer.Format
import ui.uilib.text.CTextField
import uilib.UIState

@Composable
fun CInput(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusLost: (TextFieldValue) -> Unit,
    numberFormat: Format,
    showBorder: Boolean = false,
    textStyle: TextStyle = UIState.CodeStyle.current,
) {

    CTextField(modifier = modifier, value = value, textStyle = textStyle, showBorder = showBorder, onValueChange = { newVal ->
        val filtered = newVal.copy(text = numberFormat.filter(newVal.text))
        onValueChange(filtered)
    }, onFocusLost = onFocusLost)
}