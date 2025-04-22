package ui.uilib.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import cengine.console.SysOut
import cengine.psi.style.CodeStyle
import org.jetbrains.compose.resources.FontResource
import ui.uilib.interactable.CButton
import uilib.UIState
import kotlin.random.Random

abstract class Theme {

    companion object {

        val all = setOf(LightTheme, DarkTheme)

        @Composable
        fun Switch() {
            val theme = UIState.Theme.value

            CButton(onClick = {
                if (theme == LightTheme) {
                    UIState.Theme.value = DarkTheme
                } else {
                    UIState.Theme.value = LightTheme
                }
                SysOut.debug { "Theme switched to ${theme.name}" }
            }, icon = theme.icon)
        }
    }

    abstract val name: String
    abstract val icon: ImageVector
    abstract val dark: Boolean
    abstract val randomBrightness: Float
    abstract val randomSaturation: Float

    // COLORS (BASIC)

    abstract val COLOR_BG_0: Color
    abstract val COLOR_BG_1: Color
    abstract val COLOR_BG_OVERLAY: Color

    abstract val COLOR_FG_0: Color
    abstract val COLOR_FG_1: Color

    abstract val COLOR_BORDER: Color

    abstract val COLOR_SELECTION: Color
    abstract val COLOR_SEARCH_RESULT: Color

    // COLORS (SIMPLE)

    abstract val COLOR_GREEN_LIGHT: Color
    abstract val COLOR_GREEN: Color
    abstract val COLOR_YELLOW: Color
    abstract val COLOR_BLUE: Color
    abstract val COLOR_ORANGE: Color
    abstract val COLOR_RED: Color

    // COLORS (ICON)

    abstract val COLOR_ICON_FG_0: Color
    abstract val COLOR_ICON_FG_1: Color
    abstract val COLOR_ICON_FG_INACTIVE: Color
    abstract val COLOR_ICON_BG: Color
    abstract val COLOR_ICON_BG_HOVER: Color
    abstract val COLOR_ICON_BG_ACTIVE: Color

    // FONTS

    abstract val FONT_BASIC: FontResource
    abstract val FONT_CODE: FontResource


    fun getRandom(): Color {
        val hue = Random.nextFloat() * 360f  // Vary hue for different colors
        return Color.hsl(hue, randomSaturation, randomBrightness)
    }

    abstract fun getColor(style: CodeStyle?): Color
}