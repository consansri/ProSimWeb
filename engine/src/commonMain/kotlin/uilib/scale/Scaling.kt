package uilib.scale

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uilib.interactable.CButton
import uilib.UIState
import kotlin.math.roundToInt

@Immutable
class Scaling(val scale: Float = 1f) {

    companion object {

        val BOUNDS = 0.25f..2.0f

        @Composable
        fun Scaler() {
            var currentScale by remember { mutableStateOf(UIState.Scale.value.scale) }
            val scope = rememberCoroutineScope()
            var currJob: Job? = null

            var accumulatedScale by remember { mutableStateOf(currentScale) }

            fun scale(delta: Float) {
                val scaleOffset = delta  / 10000
                if (accumulatedScale + scaleOffset in BOUNDS) {
                    accumulatedScale += scaleOffset
                }

                currJob?.cancel()
                currJob =  scope.launch {
                    delay(500)
                    currentScale = accumulatedScale
                }
            }

            CButton(
                text = "${(accumulatedScale * 100).roundToInt()}%", onClick = {
                    currentScale = 1f
                }, modifier = Modifier
                    .scrollable(orientation = Orientation.Vertical,
                        state = rememberScrollableState { delta ->
                            scale(delta)
                            delta
                        })
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            // Adjust stepCount based on the dragAmount
                            scale(dragAmount)
                        }
                    }
            )

            LaunchedEffect(currentScale) {
                UIState.Scale.value = Scaling(currentScale)
                accumulatedScale = currentScale
            }
        }
    }

    val name: String = "${(scale * 100).toInt()}%"

    // FONT SCALING
    val FONTSCALE_SMALL: TextUnit = 0.7.em
        get() = (field * scale)
    val FONTSCALE_MEDIUM: TextUnit = 0.9.em
        get() = (field * scale)
    val FONTSCALE_LARGE: TextUnit = 1.1.em
        get() = (field * scale)

    val FONTSCALE_LINE_HEIGHT_FACTOR: Double = 22.0

    // SIZES

    val SIZE_CONTROL_SMALL: Dp = 18.dp
        get() = field * scale

    val SIZE_CONTROL_MEDIUM: Dp = 24.dp
        get() = field * scale

    val SIZE_CONTROL_LARGE: Dp = 30.dp
        get() = field * scale

    val SIZE_INSET_SMALL: Dp = 2.dp
        get() = field * scale
    val SIZE_INSET_MEDIUM: Dp = 4.dp
        get() = field * scale
    val SIZE_INSET_LARGE: Dp = 6.dp
        get() = field * scale
    val SIZE_CORNER_RADIUS: Dp = 2.dp
        get() = field * scale
    val SIZE_BORDER_THICKNESS: Dp = 1.dp
        get() = field * scale
    val SIZE_DIVIDER_THICKNESS: Dp = 4.dp
        get() = field * scale
    val SIZE_SCROLL_THUMB: Dp = 10.dp
        get() = field * scale

    val SIZE_BORDER_THICKNESS_MARKED: Dp
        get() = SIZE_BORDER_THICKNESS * 4

    // BORDERS

}