package ui.uilib.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import emulator.kit.nativeLog
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.layout.FormRect
import ui.uilib.layout.FormRow
import ui.uilib.text.CTextField


@Composable
fun InputDialog(title: String, init: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit, valid: (String) -> Boolean) {

    var text by remember { mutableStateOf(init) }

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
                    error = text.isEmpty()
                )
            }

            FormRow {
                CButton(
                    text = "Confirm",
                    onClick = {
                        if (text.isNotEmpty()) {
                            nativeLog("Confirm: -> ")
                            onConfirm(text)
                        }
                    }, active = text.isNotEmpty(),
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
