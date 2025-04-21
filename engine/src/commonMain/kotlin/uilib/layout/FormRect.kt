package ui.uilib.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ui.uilib.label.CLabel
import uilib.UIState


@Composable
fun FormRect(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    rowSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {

    Layout(
        content = content,
        modifier = modifier
            .wrapContentSize()
            .padding(contentPadding),
        measurePolicy = { measurables, constraints ->
            // Measure each child
            val placeables = measurables.map { it.measure(constraints) }

            // Determine the maximum width of any child
            val maxWidth = placeables.maxOfOrNull { it.width } ?: constraints.minWidth

            // Calculate total height considering spacing between rows
            val totalHeight = placeables.sumOf { it.height } + (placeables.size - 1) * rowSpacing.roundToPx()

            // Set the layout size to maxWidth x totalHeight
            layout(width = maxWidth, height = totalHeight) {
                var yPosition = 0
                placeables.forEach { placeable ->
                    placeable.placeRelative(x = 0, y = yPosition)
                    yPosition += placeable.height + rowSpacing.roundToPx()
                }
            }
        }
    )
}

@Composable
fun FormRow(
    labelIcon: ImageVector? = null,
    labelText: String? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceEvenly,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        if (labelIcon != null || labelText != null) {
            CLabel(
                modifier = Modifier.weight(1f),
                icon = labelIcon,
                text = labelText,
                textAlign = TextAlign.Right
            )

            Spacer(Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))
        }
        content()
    }
}

