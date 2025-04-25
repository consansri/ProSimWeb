package uilib.interactable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import uilib.UIState


@Composable
fun <T> Selector(
    items: Collection<T>,
    initial: T? = items.firstOrNull(),
    onSelectionChanged: (T) -> Unit,
    itemContent: @Composable (isSelected: Boolean, value: T) -> Unit
) {
// Track the selected item, default to the first one
    var selectedItem by remember { mutableStateOf(initial) }

    // Update the selection when a new item is clicked
    LaunchedEffect(selectedItem) {
        selectedItem?.let(onSelectionChanged)
    }

    val scale = UIState.Scale.value
    val theme = UIState.Theme.value

    Column(Modifier.border(scale.SIZE_BORDER_THICKNESS, theme.COLOR_BORDER, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))) {
        items.forEachIndexed { index, item ->
            val isSelected = item == selectedItem

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) theme.COLOR_SELECTION else Color.Transparent,
                        when (index) {
                            0 -> RoundedCornerShape(topStart = scale.SIZE_CORNER_RADIUS, topEnd = scale.SIZE_CORNER_RADIUS)
                            items.size - 1 -> RoundedCornerShape(bottomStart = scale.SIZE_CORNER_RADIUS, bottomEnd = scale.SIZE_CORNER_RADIUS)
                            else -> RectangleShape
                        }
                    )
                    .clickable {
                        selectedItem = item
                    }.padding(horizontal = scale.SIZE_INSET_SMALL)
            ) {
                itemContent(isSelected, item)
            }
        }
    }
}
