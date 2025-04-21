package ui.uilib.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout

@Composable
fun CornerLayout(
    modifier: Modifier = Modifier,
    north: @Composable (BoxScope.() -> Unit)? = null,
    east: @Composable (BoxScope.() -> Unit)? = null,
    south: @Composable (BoxScope.() -> Unit)? = null,
    west: @Composable (BoxScope.() -> Unit)? = null,
) {
    Layout(modifier = modifier, content = {
        if (north != null) Box(content = north)
        if (east != null) Box(content = east)
        if (south != null) Box(content = south)
        if (west != null) Box(content = west)
    }) { measurables, constraints ->
        // Measure edges
        val eastPlaceable = east?.let { measurables[1].measure(constraints.copy(minWidth = 0)) }
        val westPlaceable = west?.let { measurables[3].measure(constraints.copy(minWidth = 0)) }

        val leftWidth = westPlaceable?.width ?: 0
        val rightWidth = eastPlaceable?.width ?: 0

        val northPlaceable = north?.let { measurables[0].measure(constraints.copy(minHeight = 0, maxWidth = constraints.maxWidth - leftWidth - rightWidth)) }
        val southPlaceable = south?.let { measurables[2].measure(constraints.copy(minHeight = 0, maxWidth = constraints.maxWidth - leftWidth - rightWidth)) }

        val southHeight = southPlaceable?.height ?: 0

        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place north edge
            northPlaceable?.placeRelative(
                x = leftWidth,
                0
            )

            // Place east edge
            eastPlaceable?.placeRelative(
                x = constraints.maxWidth - rightWidth,
                y = 0
            )

            // Place south edge
            southPlaceable?.placeRelative(
                x = leftWidth,
                y = constraints.maxHeight - southHeight
            )

            // Place west edge
            westPlaceable?.placeRelative(
                x = 0,
                y = 0
            )
        }


    }

}