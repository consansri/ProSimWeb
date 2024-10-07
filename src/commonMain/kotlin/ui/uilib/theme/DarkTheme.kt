package ui.uilib.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import cengine.lang.asm.CodeStyle
import emulator.kit.memory.Memory
import org.jetbrains.compose.resources.FontResource
import prosim.prosimweb_test.generated.resources.JetBrainsMono_Regular
import prosim.prosimweb_test.generated.resources.Poppins_Regular
import prosim.prosimweb_test.generated.resources.Res
import ui.uilib.UIState

@Immutable
data object DarkTheme: Theme {
    override val name: String = "dark"
    override val icon: ImageVector get() = UIState.Icon.value.darkmode
    override val dark: Boolean = true
    override val COLOR_BG_0: Color = Color(0xFF222222)
    override val COLOR_BG_1: Color = Color(0xFF373737)
    override val COLOR_BG_OVERLAY: Color = Color(0xFF222222)
    override val COLOR_FG_0: Color = Color(0xFFD5D5D5)
    override val COLOR_FG_1: Color = Color(0xFF777777)
    override val COLOR_BORDER: Color = Color(0xFF777777)
    override val COLOR_SELECTION: Color = Color(0xFF777777)
    override val COLOR_SEARCH_RESULT: Color = Color(0xFFB68B0F)
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

    override fun getColor(style: Memory.InstanceType): Color {
        val colInt = style.dark ?: style.light
        return Color(colInt)
    }
}