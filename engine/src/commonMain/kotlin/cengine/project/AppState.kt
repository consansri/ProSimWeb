package cengine.project

import kotlinx.serialization.Serializable
import uilib.scale.Scaling
import uilib.theme.LightTheme
import uilib.theme.ThemeDef

@Serializable
data class AppState(
    val scale: Float = 1.0f,
    val theme: String = LightTheme.name,
    val currentlyOpened: Int? = null,
    val projectStates: List<ProjectState> = listOf()
) {

    companion object {
        const val APPSTATE_NAME = "appstate.json"
        val initial = AppState()
    }

    fun getTheme(): ThemeDef = ThemeDef.all.firstOrNull { it.name == theme } ?: LightTheme

    fun getScaling(): Scaling = Scaling(scale)

}
