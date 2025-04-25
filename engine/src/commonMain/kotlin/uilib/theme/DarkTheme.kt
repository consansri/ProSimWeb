package uilib.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import cengine.psi.style.CodeStyle
import org.jetbrains.compose.resources.FontResource
import prosim.engine.generated.resources.JetBrainsMono_Regular
import prosim.engine.generated.resources.Poppins_Regular
import prosim.engine.generated.resources.Res
import uilib.UIState

data object DarkTheme : ThemeDef() {

    override val name: String = "dark"
    override val icon: ImageVector get() = UIState.Icon.value.darkmode
    override val dark: Boolean = true
    override val randomBrightness: Float = 0.7f
    override val randomSaturation: Float = 0.7f
    override val COLOR_BG_0: Color = Color(0xFF222222)
    override val COLOR_BG_1: Color = Color(0xFF373737)
    override val COLOR_BG_OVERLAY: Color = Color(0xFF222222)
    override val COLOR_FG_0: Color = Color(0xFFD5D5D5)
    override val COLOR_FG_1: Color = Color(0xFF777777)
    override val COLOR_BORDER: Color = Color(0xFF777777)
    override val COLOR_SELECTION: Color = Color(0xFF777777)
    override val COLOR_SEARCH_RESULT: Color = Color(0x33B68B0F)
    override val COLOR_GREEN_LIGHT: Color = Color(0xFF98D8AA)
    override val COLOR_GREEN: Color = Color(0xFF58CC79)
    override val COLOR_YELLOW: Color = Color(0xFFE2B124)
    override val COLOR_BLUE: Color = Color(0xFF549FD8)
    override val COLOR_ORANGE: Color = Color(0xFFEE9955)
    override val COLOR_RED: Color = Color(0xFFEE2222)
    override val COLOR_ICON_FG_0: Color = Color(0xFFD5D5D5)
    override val COLOR_ICON_FG_1: Color = Color(0xFFAAAAAA)
    override val COLOR_ICON_FG_INACTIVE: Color = Color(0x77777733)
    override val COLOR_ICON_BG: Color = Color(0, 0, 0, 0)
    override val COLOR_ICON_BG_HOVER: Color = Color(0x30777777)
    override val COLOR_ICON_BG_ACTIVE: Color = Color(0x50777777)

    override val FONT_BASIC: FontResource = Res.font.Poppins_Regular
    override val FONT_CODE: FontResource = Res.font.JetBrainsMono_Regular

    override fun getColor(style: CodeStyle?): Color {
        if (style == null) return Color(CodeStyle.baseColor.getDarkElseLight() or 0xFF000000.toInt())
        return Color(style.getDarkElseLight() or 0xFF000000.toInt())
    }
}