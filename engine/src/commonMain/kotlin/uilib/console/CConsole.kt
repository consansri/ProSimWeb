package uilib.console

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.style.TextAlign
import uilib.interactable.CButton
import uilib.label.CLabel
import uilib.text.CTextField
import uilib.UIState

@Composable
fun CConsole(
    messages: List<String>,
    onSend: (String) -> Unit,
) {

    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(UIState.Scale.value.SIZE_INSET_MEDIUM)) {

        // Message log area
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().fillMaxSize().padding(UIState.Scale.value.SIZE_INSET_MEDIUM)) {
            items(messages) { message ->
                CLabel(text = message, textStyle = UIState.CodeStyle.current, textAlign = TextAlign.Left)
            }
        }

        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

        // Input Area
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UIState.Scale.value.SIZE_INSET_MEDIUM)) {
            CTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f).onKeyEvent {
                    if (it.key == Key.Enter && !it.isShiftPressed && !it.isCtrlPressed && !it.isAltPressed) {
                        onSend(inputText)
                        inputText = ""
                        true
                    } else {
                        false
                    }
                },
                textStyle = UIState.CodeStyle.current,
                showBorder = false
            )

            CButton(text = "Send", onClick = {
                onSend(inputText)
                inputText = ""
            })
        }

    }

}