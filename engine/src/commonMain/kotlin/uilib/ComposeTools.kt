package uilib

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope

object ComposeTools {

    fun <T : Any> Collection<T>.sumDp(reducer: (T) -> Dp): Dp {
        var sum = 0.dp
        this.forEach {
            sum += reducer(it)
        }
        return sum
    }

    fun <T : Any> Collection<T>.sumOf(reducer: (T) -> Float): Float {
        var sum = 0f
        this.forEach {
            sum += reducer(it)
        }
        return sum
    }

    fun MutableList<AnnotatedString.Range<SpanStyle>>.insert(index: Int, length: Int) {
        forEachIndexed { i, range ->
            when {
                index > range.end -> {
                    // Nothing to change
                }

                index < range.start -> {
                    this[i] = range.copy(start = range.start + length, end = range.end + length)
                }

                else -> {
                    this[i] = range.copy(end = range.end + length)
                }
            }
        }
    }

    fun MutableList<AnnotatedString.Range<SpanStyle>>.delete(from: Int, to: Int) {
        val length = to - from
        val toRemove = mutableSetOf<AnnotatedString.Range<SpanStyle>>()
        forEachIndexed { i, range ->
            when {
                from > range.end -> {
                    // Nothing to change
                }

                to < range.start -> {
                    this[i] = range.copy(start = range.start - length, end = range.end - length)
                }

                else -> {
                    toRemove.add(range)
                }
            }
        }
        removeAll(toRemove)
    }

    @Composable
    fun <T> TrackStateChanges(
        state: T,
        onStateChanged: CoroutineScope.(oldValue: T, newValue: T) -> Unit
    ) {
        var previousState by remember { mutableStateOf(state) }

        LaunchedEffect(state) {
            if (previousState != state) {
                onStateChanged(previousState, state)
                previousState = state
            }
        }
    }

    @Composable
    fun Rotating(content: @Composable (rotation: Float) -> Unit) {
        val infiniteTransition = rememberInfiniteTransition()
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        content(rotation)
    }


}