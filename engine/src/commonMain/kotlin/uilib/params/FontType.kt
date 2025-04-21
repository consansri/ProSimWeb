package ui.uilib.params

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import org.jetbrains.compose.resources.Font
import uilib.UIState

enum class FontType {
    CODE,
    CODE_SMALL,
    SMALL,
    MEDIUM,
    LARGE;

    fun getSize(): TextUnit {
        val scale = UIState.Scale.value
        return when (this) {
            SMALL -> scale.FONTSCALE_SMALL
            MEDIUM -> scale.FONTSCALE_MEDIUM
            LARGE -> scale.FONTSCALE_LARGE
            CODE -> scale.FONTSCALE_MEDIUM
            CODE_SMALL -> scale.FONTSCALE_SMALL
        }
    }

    @Composable
    fun getStyle(): TextStyle = TextStyle(
        fontSize = getSize(),
        fontFamily = getFamily(),
        //lineHeight = getSize() * UIState.Scale.value.FONTSCALE_LINE_HEIGHT_FACTOR
    )

    @Composable
    fun getFamily(): FontFamily {
        val theme = UIState.Theme.value
        return when (this) {
            CODE -> Font(theme.FONT_CODE).toFontFamily()
            CODE_SMALL -> Font(theme.FONT_CODE).toFontFamily()
            else -> Font(theme.FONT_BASIC).toFontFamily()
        }
    }

    @Composable
    fun measure(text: String, textMeasurer: TextMeasurer): DpSize {
        val (widthPx, heightPx) = textMeasurer.measure(text, getStyle()).size
        return with(LocalDensity.current) {
            DpSize(widthPx.toDp(), heightPx.toDp())
        }
    }

}