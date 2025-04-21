package ui.uilib.layout

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScrollableRow(leftContent: @Composable () -> Unit) {
    // Remember the scroll state
    val scrollState = rememberScrollState()

    // Wrap the Column with a verticalScroll modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState) // Apply the scroll state to the Column
    ) {
        leftContent() // This will display the passed content inside the scrollable Column
    }

}