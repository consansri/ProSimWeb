package ui

import androidx.compose.runtime.Composable
import cengine.project.ProjectStateManager
import uilib.UIState

@Composable
fun AppLauncher(){
    UIState.Theme.value = ProjectStateManager.appState.getTheme()
    UIState.Scale.value = ProjectStateManager.appState.getScaling()

    UIState.StateUpdater()

    UIState.launch {
        MainScreen()
    }
}