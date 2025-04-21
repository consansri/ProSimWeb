package ui.uilib.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import cengine.console.SysOut
import ui.uilib.interactable.CButton
import ui.uilib.layout.FormRect
import ui.uilib.layout.FormRow
import ui.uilib.text.CTextField
import uilib.UIState


@Composable
fun InputDialog(title: String, init: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit, valid: (String) -> Boolean) {

    var text by remember { mutableStateOf(TextFieldValue(init)) }

    Dialog(onDismissRequest = {
        onDismiss()
    }) {

        FormRect(
            modifier = Modifier
                .background(UIState.Theme.value.COLOR_BG_OVERLAY, RoundedCornerShape(UIState.Scale.value.SIZE_CORNER_RADIUS)),
            contentPadding = PaddingValues(UIState.Scale.value.SIZE_INSET_MEDIUM),
            rowSpacing = UIState.Scale.value.SIZE_INSET_MEDIUM
        ) {

            FormRow(
                labelText = title
            ) {
                CTextField(
                    value = text,
                    singleLine = true,
                    readonly = false,
                    onValueChange = {
                        text = it
                    },
                    modifier = Modifier.weight(1f),
                    error = text.text.isEmpty()
                )
            }

            FormRow {
                CButton(
                    text = "Confirm",
                    onClick = {
                        if (text.text.isNotEmpty()) {
                            SysOut.log("Confirm: -> ")
                            onConfirm(text.text)
                        }
                    }, active = text.text.isNotEmpty(),
                    modifier = Modifier.weight(1.0f)
                )
                CButton(
                    text = "Cancel",
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.0f)
                )
            }
        }
    }

}
