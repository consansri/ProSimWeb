package ui.uilib.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import ui.uilib.interactable.CButton
import ui.uilib.label.CLabel
import ui.uilib.layout.FormRect
import ui.uilib.layout.FormRow
import uilib.UIState

@Composable
fun ConfirmDialog(title: String, onConfirm: (Boolean) -> Unit) {

    Dialog(onDismissRequest = {
        onConfirm(false)
    }) {

        FormRect(
            modifier = Modifier
                .background(UIState.Theme.value.COLOR_BG_OVERLAY, RoundedCornerShape(UIState.Scale.value.SIZE_CORNER_RADIUS)),
            contentPadding = PaddingValues(UIState.Scale.value.SIZE_INSET_MEDIUM),
            rowSpacing = UIState.Scale.value.SIZE_INSET_MEDIUM
        ) {

            FormRow {
                CLabel(Modifier.weight(1.0f), text = title)
            }

            FormRow {
                CButton(
                    text = "Confirm",
                    onClick = {
                        onConfirm(true)
                    },
                    modifier = Modifier.weight(1.0f)
                )
                CButton(
                    text = "Cancel",
                    onClick = {
                        onConfirm(false)
                    },
                    modifier = Modifier.weight(1.0f)
                )
            }
        }
    }

}
