package ikr.prosim

import Constants
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import ikr.prosim.ui.AppLauncher

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    CanvasBasedWindow(title = Constants.TITLE, canvasElementId = "ComposeTarget") {
        AppLauncher()
    }

}