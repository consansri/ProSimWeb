package ui.uilib.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScrollableColumn(leftContent: @Composable () -> Unit) {
    // Remember the scroll state
    val scrollState = rememberScrollState()

    // Wrap the Column with a verticalScroll modifier
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(scrollState) // Apply the scroll state to the Column
    ) {
        leftContent() // This will display the passed content inside the scrollable Column
    }
}
