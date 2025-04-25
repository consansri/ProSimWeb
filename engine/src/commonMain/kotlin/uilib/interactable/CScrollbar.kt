package uilib.interactable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch
import uilib.UIState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CVerticalScrollBar(scrollState: ScrollState, modifier: Modifier = Modifier, below: @Composable BoxScope.() -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()

    val containerHeight = scrollState.maxValue.toFloat() + scrollState.viewportSize.toFloat()
    val vScrollRatio = if (containerHeight == 0f) 0f else scrollState.value.toFloat() / containerHeight

    // Calculate scrollbar thumb height based on the content height and viewport
    val thumbHeightRatio = if (containerHeight == 0f) 1f else scrollState.viewportSize.toFloat() / containerHeight
    val thumbHeightPx = thumbHeightRatio * scrollState.viewportSize.toFloat()

    var scrollBarSize by remember { mutableStateOf(Size.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    with(LocalDensity.current) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .onGloballyPositioned {
                    scrollBarSize = it.size.toSize()
                }
        ) {

            below()

            Box(
                Modifier
                    .size(DpSize(UIState.Scale.value.SIZE_SCROLL_THUMB, thumbHeightPx.toDp()))
                    .offset(x = scrollBarSize.width.toDp() - UIState.Scale.value.SIZE_SCROLL_THUMB, y = vScrollRatio * scrollBarSize.height.toDp())
                    .onDrag { offset ->
                        // Calculate new scroll position based on drag amount
                        val newScroll = (scrollState.value + (offset.y / thumbHeightRatio)).toInt().coerceIn(0, scrollState.maxValue)
                        coroutineScope.launch {
                            scrollState.scrollTo(newScroll)
                        }
                    }
                    .hoverable(interactionSource)
                    .background(if(isHovered) UIState.Theme.value.COLOR_BORDER.copy(alpha = 0.6f) else UIState.Theme.value.COLOR_BORDER.copy(alpha = 0.4f))
            )
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CHorizontalScrollBar(scrollState: ScrollState, modifier: Modifier = Modifier, below: @Composable BoxScope.() -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()

    val containerWidth = scrollState.maxValue.toFloat() + scrollState.viewportSize.toFloat()
    val vScrollRatio = if (containerWidth == 0f) 0f else scrollState.value.toFloat() / containerWidth

    // Calculate scrollbar thumb height based on the content height and viewport
    val thumbWidthRatio = if (containerWidth == 0f) 1f else scrollState.viewportSize.toFloat() / containerWidth
    val thumbWidthPx = thumbWidthRatio * scrollState.viewportSize.toFloat()

    var scrollBarSize by remember { mutableStateOf(Size.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    with(LocalDensity.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    scrollBarSize = it.size.toSize()
                }
        ) {

            below()

            Box(
                Modifier
                    .size(DpSize(thumbWidthPx.toDp(), UIState.Scale.value.SIZE_SCROLL_THUMB))
                    .offset(vScrollRatio * scrollBarSize.width.toDp(), y = scrollBarSize.height.toDp() - UIState.Scale.value.SIZE_SCROLL_THUMB)
                    .onDrag { offset ->
                        val newScroll = (scrollState.value + (offset.x / thumbWidthRatio)).toInt().coerceIn(0, scrollState.maxValue)
                        coroutineScope.launch {
                            scrollState.scrollTo(newScroll)
                        }
                    }
                    .hoverable(interactionSource)
                    .background(if(isHovered) UIState.Theme.value.COLOR_BORDER.copy(alpha = 0.6f) else UIState.Theme.value.COLOR_BORDER.copy(alpha = 0.4f))

            )
        }
    }


}
