package ikr.prosim

import Constants
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.*
import ikr.prosim.ui.AppLauncher
import uilib.UIState

fun main(){
    application {

        val iconPainter = rememberVectorPainter(UIState.Icon.value.appLogo)
        val windowState = rememberWindowState(WindowPlacement.Maximized,
            position = WindowPosition.Aligned(Alignment.Center)
        )

        Window(::exitApplication, windowState, title = Constants.TITLE, icon = iconPainter){
            AppLauncher()
        }

    }
}