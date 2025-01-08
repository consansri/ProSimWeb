package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cengine.system.AppTarget.WEB
import cengine.system.appTarget
import cengine.system.downloadDesktopApp
import config.BuildConfig
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.label.CLabel
import ui.uilib.layout.BorderLayout
import ui.uilib.params.IconType
import ui.uilib.scale.Scaling
import ui.uilib.theme.Theme

@Composable
fun AboutScreen(onCloseAbout: () -> Unit) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    BorderLayout(
        modifier = Modifier.background(theme.COLOR_BG_1),
        topBg = theme.COLOR_BG_0,
        top = {
            Spacer(Modifier.weight(2.0f))
            Scaling.Scaler()
            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
            Theme.Switch()
            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
            CButton(onClick = {
                onCloseAbout()
            }, icon = UIState.Icon.value.close)
        },
        center = {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CLabel(icon = UIState.Icon.value.reportBug, iconType = IconType.LARGE)
                CLabel(text = BuildConfig.NAME, textStyle = UIState.BaseLargeStyle.current, color = theme.COLOR_FG_0)
                CLabel(text = "v${BuildConfig.VERSION}", textStyle = UIState.BaseStyle.current, color = theme.COLOR_FG_0)
                if (appTarget() == WEB) {
                    CButton(text = "Download for Desktop", textStyle = UIState.BaseStyle.current, onClick = {
                        // Download Desktop Version from resources "${BuildConfig.FILENAME}.jar"
                        downloadDesktopApp(".jar")
                    })
                }
                CLabel(text = "Copyright @ ${BuildConfig.YEAR} ${BuildConfig.ORG}", textStyle = UIState.BaseStyle.current, color = theme.COLOR_FG_0)
                CLabel(text = "Developed by ${BuildConfig.DEV}", textStyle = UIState.BaseSmallStyle.current, color = theme.COLOR_FG_0)
            }
        }
    )

}