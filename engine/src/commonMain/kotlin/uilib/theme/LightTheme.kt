package uilib.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import cengine.psi.style.CodeStyle
import org.jetbrains.compose.resources.FontResource
import prosim.engine.generated.resources.JetBrainsMono_Regular
import prosim.engine.generated.resources.Poppins_Regular
import prosim.engine.generated.resources.Res
import uilib.UIState

data object LightTheme : ThemeDef() {

    override val name: String = "light"
    override val icon: ImageVector get() = UIState.Icon.value.lightmode
    override val dark: Boolean = false
    override val randomBrightness: Float = 0.3f
    override val randomSaturation: Float = 0.9f
    override val COLOR_BG_0: Color = Color(0xFFFFFFFF)
    override val COLOR_BG_1: Color = Color(0xFFF0F2F5)
    override val COLOR_BG_OVERLAY: Color = Color(0xFFFFFFFF)
    override val COLOR_FG_0: Color = Color(0xFF454545)
    override val COLOR_FG_1: Color = Color(0xFF999999)
    override val COLOR_BORDER: Color = Color(0xFFCDCDCD)
    override val COLOR_SELECTION: Color = Color(0x334d90fe) // old one
    override val COLOR_SEARCH_RESULT: Color = Color(0x33E2B124)
    override val COLOR_GREEN_LIGHT: Color = Color(0xFF41A05A)
    override val COLOR_GREEN: Color = Color(0xFF19A744)
    override val COLOR_YELLOW: Color = Color(0xFFB68B0F)
    override val COLOR_BLUE: Color = Color(0xFF126EB4)
    override val COLOR_ORANGE: Color = Color(0xFFAC5916)
    override val COLOR_RED: Color = Color(0xFF9A0000)
    override val COLOR_ICON_FG_0: Color = Color(0xFF333333)
    override val COLOR_ICON_FG_1: Color = Color(0xFF666666)
    override val COLOR_ICON_FG_INACTIVE: Color = Color(0x77777733)
    override val COLOR_ICON_BG: Color = Color(0x00EFEFEF)
    override val COLOR_ICON_BG_HOVER: Color = Color(0x20777777)
    override val COLOR_ICON_BG_ACTIVE: Color = Color(0x50777777)
    override val FONT_BASIC: FontResource = Res.font.Poppins_Regular
    override val FONT_CODE: FontResource = Res.font.JetBrainsMono_Regular

    override fun getColor(style: CodeStyle?): Color {
        if (style == null) return Color(CodeStyle.baseColor.getDarkElseLight() or 0xFF333333.toInt())
        return Color(style.lightHexColor or 0xFF000000.toInt())
    }

}