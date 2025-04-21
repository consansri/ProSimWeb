package ui.uilib.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class FlexDirection {
    ROW, COLUMN
}

enum class JustifyContent {
    FLEX_START, CENTER, FLEX_END, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY
}

enum class AlignItems {
    FLEX_START, CENTER, FLEX_END, STRETCH
}

enum class FlexWrap {
    NO_WRAP, WRAP
}

@Composable
fun FlexboxLayout(
    modifier: Modifier = Modifier,
    flexDirection: FlexDirection = FlexDirection.ROW,
    justifyContent: JustifyContent = JustifyContent.FLEX_START,
    alignItems: AlignItems = AlignItems.STRETCH,
    flexWrap: FlexWrap = FlexWrap.NO_WRAP,
    itemSpacing: Dp = 0.dp,
    lineSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content,
        measurePolicy = { measurables, constraints ->
            // Convert spacing from Dp to pixels
            val itemSpacingPx = itemSpacing.roundToPx()
            val lineSpacingPx = lineSpacing.roundToPx()

            // Measure all children
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            // Initialize lines and tracking variables
            val lines = mutableListOf<MutableList<Placeable>>()
            var currentLine = mutableListOf<Placeable>()
            var currentLineMainAxisSize = 0
            var crossAxisMaxSize = 0

            val maxMainAxisSize = if (flexDirection == FlexDirection.ROW) constraints.maxWidth else constraints.maxHeight

            placeables.forEach { placeable ->
                val placeableMainAxisSize = if (flexDirection == FlexDirection.ROW) placeable.width else placeable.height

                if (flexWrap == FlexWrap.WRAP &&
                    currentLineMainAxisSize + placeableMainAxisSize + (if (currentLine.isNotEmpty()) itemSpacingPx else 0) > maxMainAxisSize) {
                    // Wrap to next line if enabled and item does not fit
                    lines.add(currentLine)
                    crossAxisMaxSize += if (flexDirection == FlexDirection.ROW) {
                        currentLine.maxOf { it.height } + lineSpacingPx
                    } else {
                        currentLine.maxOf { it.width } + lineSpacingPx
                    }
                    currentLine = mutableListOf(placeable)
                    currentLineMainAxisSize = placeableMainAxisSize
                } else {
                    // Add item to current line
                    if (currentLine.isNotEmpty()) {
                        currentLineMainAxisSize += itemSpacingPx
                    }
                    currentLine.add(placeable)
                    currentLineMainAxisSize += placeableMainAxisSize
                }
            }

            // Add the last line
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
                crossAxisMaxSize += if (flexDirection == FlexDirection.ROW) {
                    currentLine.maxOf { it.height }
                } else {
                    currentLine.maxOf { it.width }
                }
            }

            // Determine layout size based on lines
            val layoutWidth = if (flexDirection == FlexDirection.ROW) constraints.maxWidth else crossAxisMaxSize
            val layoutHeight = if (flexDirection == FlexDirection.ROW) crossAxisMaxSize else constraints.maxHeight

            layout(layoutWidth, layoutHeight) {
                var crossAxisPosition = 0

                lines.forEach { line ->
                    val lineMainAxisSize = line.sumOf { if (flexDirection == FlexDirection.ROW) it.width else it.height } +
                            (line.size - 1) * itemSpacingPx

                    var mainAxisPosition = when (justifyContent) {
                        JustifyContent.FLEX_START -> 0
                        JustifyContent.CENTER -> (maxMainAxisSize - lineMainAxisSize) / 2
                        JustifyContent.FLEX_END -> maxMainAxisSize - lineMainAxisSize
                        JustifyContent.SPACE_BETWEEN -> 0
                        JustifyContent.SPACE_AROUND -> itemSpacingPx / 2
                        JustifyContent.SPACE_EVENLY -> itemSpacingPx
                    }

                    val spacing = when (justifyContent) {
                        JustifyContent.SPACE_BETWEEN -> if (line.size > 1) (maxMainAxisSize - lineMainAxisSize) / (line.size - 1) else 0
                        JustifyContent.SPACE_AROUND -> if (line.size > 1) (maxMainAxisSize - lineMainAxisSize) / line.size else 0
                        JustifyContent.SPACE_EVENLY -> if (line.size > 1) (maxMainAxisSize - lineMainAxisSize) / (line.size + 1) else 0
                        else -> itemSpacingPx
                    }

                    line.forEach { placeable ->
                        val crossAxisOffset = when (alignItems) {
                            AlignItems.FLEX_START -> 0
                            AlignItems.CENTER -> (line.maxOf { if (flexDirection == FlexDirection.ROW) it.height else it.width } -
                                    if (flexDirection == FlexDirection.ROW) placeable.height else placeable.width) / 2
                            AlignItems.FLEX_END -> line.maxOf { if (flexDirection == FlexDirection.ROW) it.height else it.width } -
                                    if (flexDirection == FlexDirection.ROW) placeable.height else placeable.width
                            AlignItems.STRETCH -> 0
                        }

                        if (flexDirection == FlexDirection.ROW) {
                            placeable.placeRelative(mainAxisPosition, crossAxisPosition + crossAxisOffset)
                            mainAxisPosition += placeable.width + spacing
                        } else {
                            placeable.placeRelative(crossAxisPosition + crossAxisOffset, mainAxisPosition)
                            mainAxisPosition += placeable.height + spacing
                        }
                    }

                    crossAxisPosition += if (flexDirection == FlexDirection.ROW) {
                        line.maxOf { it.height } + lineSpacingPx
                    } else {
                        line.maxOf { it.width } + lineSpacingPx
                    }
                }
            }
        }
    )
}

