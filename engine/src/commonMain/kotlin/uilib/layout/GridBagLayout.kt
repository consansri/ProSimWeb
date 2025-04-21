package ui.uilib.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class GridBagConstraints(
    val gridX: Int,
    val gridY: Int,
    val gridWidth: Int = 1,
    val gridHeight: Int = 1,
    val weightX: Float = 0f,
    val weightY: Float = 0f,
    val fill: Fill = Fill.NONE,
    val padding: Dp = 0.dp
)

enum class Fill{
    NONE, HORIZONTAL, VERTICAL, BOTH
}

@Composable
fun GridBagLayout(
    modifier: Modifier = Modifier,
    vararg gbc: GridBagConstraints,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content,
        measurePolicy = { measurables, constraints ->
            val placeables = measurables.mapIndexed { index, measurable ->
                val gridConstraint = gbc.getOrNull(index) ?: GridBagConstraints(0, 0)
                val itemConstraints = when (gridConstraint.fill) {
                    Fill.NONE -> constraints.copy(
                        minWidth = 0,
                        minHeight = 0,
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight
                    )
                    Fill.HORIZONTAL -> constraints.copy(
                        minWidth = 0,
                        maxHeight = constraints.maxHeight
                    )
                    Fill.VERTICAL -> constraints.copy(
                        minHeight = 0,
                        maxWidth = constraints.maxWidth
                    )
                    Fill.BOTH -> constraints
                }

                measurable.measure(itemConstraints)
            }

            // Determine the layout size
            val totalColumns = gbc.maxByOrNull { it.gridX + it.gridWidth }?.let { it.gridX + it.gridWidth } ?: 1
            val totalRows = gbc.maxByOrNull { it.gridY + it.gridHeight }?.let { it.gridY + it.gridHeight } ?: 1

            // Calculate column widths and row heights
            val columnWidths = IntArray(totalColumns) { 0 }
            val rowHeights = IntArray(totalRows) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val gridConstraint = gbc.getOrNull(index) ?: GridBagConstraints(0, 0)
                for (column in gridConstraint.gridX until (gridConstraint.gridX + gridConstraint.gridWidth)) {
                    columnWidths[column] = maxOf(columnWidths[column], placeable.width)
                }
                for (row in gridConstraint.gridY until (gridConstraint.gridY + gridConstraint.gridHeight)) {
                    rowHeights[row] = maxOf(rowHeights[row], placeable.height)
                }
            }

            val totalWidth = columnWidths.sum() + (totalColumns - 1) * 8 // Assuming 8dp spacing
            val totalHeight = rowHeights.sum() + (totalRows - 1) * 8 // Assuming 8dp spacing

            // Layout placement
            layout(totalWidth, totalHeight) {
                val columnPositions = columnWidths.runningFold(0) { sum, width -> sum + width + 8 }
                val rowPositions = rowHeights.runningFold(0) { sum, height -> sum + height + 8 }

                placeables.forEachIndexed { index, placeable ->
                    val gridConstraint = gbc.getOrNull(index) ?: GridBagConstraints(0, 0)

                    val x = columnPositions[gridConstraint.gridX]
                    val y = rowPositions[gridConstraint.gridY]

                    placeable.placeRelative(x, y)
                }
            }
        }
    )
}
