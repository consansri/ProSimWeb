package uilib.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uilib.UIState

@Composable
fun ResizableBorderPanels(
    modifier: Modifier = Modifier,
    leftContent: (@Composable BoxScope.() -> Unit)? = null,
    centerContent: @Composable BoxScope.() -> Unit,
    rightContent: (@Composable BoxScope.() -> Unit)? = null,
    bottomContent: (@Composable BoxScope.() -> Unit)? = null,
    initialLeftWidth: Dp = 0.dp,
    initialRightWidth: Dp = 0.dp,
    initialBottomHeight: Dp = 0.dp,
    onLeftWidthChange: (Dp) -> Unit = {},
    onRightWidthChange: (Dp) -> Unit = {},
    onBottomHeightChange: (Dp) -> Unit = {}
) {
    val scale = UIState.Scale.value

    var leftWidth by remember { mutableStateOf(initialLeftWidth) }
    var rightWidth by remember { mutableStateOf(initialRightWidth) }
    var bottomHeight by remember { mutableStateOf(initialBottomHeight) }

    var layoutWidth by remember { mutableStateOf(0.dp) }
    var layoutHeight by remember { mutableStateOf(0.dp) }

    Column(modifier = modifier.onGloballyPositioned { layoutCoordinates ->
        // Get the layout size in dp
        layoutWidth = layoutCoordinates.size.width.dp
        layoutHeight = layoutCoordinates.size.height.dp
    }) {
        Row(modifier = Modifier.weight(1f)) {
            leftContent?.let {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(leftWidth)
                ) {
                    it()
                }
                DragHandle(
                    isVertical = true,
                    onResize = { delta ->
                        val maxLeftWidth = layoutWidth - rightWidth - scale.SIZE_DIVIDER_THICKNESS * 2
                        leftWidth = (leftWidth + delta).coerceIn(0.dp, maxLeftWidth)
                        onLeftWidthChange(leftWidth)
                    }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                centerContent()
            }

            rightContent?.let {
                DragHandle(
                    isVertical = true,
                    onResize = { delta ->
                        val maxRightWidth = layoutWidth - leftWidth - scale.SIZE_DIVIDER_THICKNESS * 2
                        rightWidth = (rightWidth - delta).coerceIn(0.dp, maxRightWidth)
                        onRightWidthChange(rightWidth)
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(rightWidth)
                ) {
                    it()
                }
            }
        }

        bottomContent?.let {
            DragHandle(
                isVertical = false,
                onResize = { delta ->
                    val maxBottomHeight = layoutHeight - scale.SIZE_DIVIDER_THICKNESS * 2
                    bottomHeight = (bottomHeight - delta).coerceIn(0.dp, maxBottomHeight)
                    onBottomHeightChange(bottomHeight)
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomHeight)
            ) {
                it()
            }
        }
    }
}

@Composable
fun DragHandle(
    isVertical: Boolean,
    onResize: (Dp) -> Unit
) {
    val scale = UIState.Scale.value
    Box(
        modifier = Modifier
            .let { if (isVertical) it.width(scale.SIZE_DIVIDER_THICKNESS).fillMaxHeight() else it.height(scale.SIZE_DIVIDER_THICKNESS).fillMaxWidth() }
            .background(UIState.Theme.value.COLOR_BORDER)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = if (isVertical) dragAmount.x.toDp() else dragAmount.y.toDp()
                    onResize(delta)
                }
            }
            .pointerHoverIcon(PointerIcon.Hand)
    )
}