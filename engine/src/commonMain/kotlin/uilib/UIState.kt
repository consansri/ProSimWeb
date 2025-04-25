package uilib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import cengine.project.ProjectStateManager
import uilib.params.FontType
import uilib.resource.Icons
import uilib.scale.Scaling
import uilib.resource.BenIcons
import uilib.theme.ThemeDef

object UIState {

    val Theme = mutableStateOf(ThemeDef.all.first())
    val Icon = mutableStateOf<Icons>(BenIcons)
    val Scale = mutableStateOf(Scaling())

    val BaseStyle = staticCompositionLocalOf<TextStyle> { error("No BaseStyle provided") }
    val CodeStyle = staticCompositionLocalOf<TextStyle> { error("No BaseStyle provided") }
    val BaseSmallStyle = staticCompositionLocalOf<TextStyle> { error("No BaseStyle provided") }
    val BaseLargeStyle = staticCompositionLocalOf<TextStyle> { error("No BaseStyle provided") }
    val CodeSmallStyle = staticCompositionLocalOf<TextStyle> { error("No BaseStyle provided") }

    @Composable
    fun launch(content: @Composable () -> Unit) {
        CompositionLocalProvider(
            BaseStyle provides FontType.MEDIUM.getStyle(),
            CodeStyle provides FontType.CODE.getStyle(),
            BaseSmallStyle provides FontType.SMALL.getStyle(),
            BaseLargeStyle provides FontType.LARGE.getStyle(),
            CodeSmallStyle provides FontType.CODE_SMALL.getStyle()
        ) {
            content()
        }
    }

    @Composable
    fun StateUpdater() {
        ProjectStateManager.appState = ProjectStateManager.appState.copy(scale = Scale.value.scale, theme = Theme.value.name)
    }
}