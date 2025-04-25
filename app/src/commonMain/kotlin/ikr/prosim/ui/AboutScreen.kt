package ikr.prosim.ui

import Constants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cengine.system.downloadDesktopApp
import cengine.system.presentDistributions
import uilib.interactable.CButton
import uilib.label.CLabel
import uilib.layout.BorderLayout
import uilib.params.IconType
import uilib.scale.Scaling
import uilib.theme.ThemeDef
import uilib.UIState

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
            ThemeDef.Switch()
            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
            CButton(onClick = {
                onCloseAbout()
            }, icon = UIState.Icon.value.close)
        },
        center = {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CLabel(icon = UIState.Icon.value.reportBug, iconType = IconType.LARGE)
                CLabel(text = Constants.NAME, textStyle = UIState.BaseLargeStyle.current, color = theme.COLOR_FG_0)
                CLabel(text = "v${Constants.VERSION}", textStyle = UIState.BaseStyle.current, color = theme.COLOR_FG_0)
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    presentDistributions().forEach { dist ->
                        CButton(text = "Download ${dist.name}", textStyle = UIState.BaseStyle.current, onClick = {
                            downloadDesktopApp(dist)
                        })
                    }
                }
                CLabel(text = Constants.COPYRIGHT, textStyle = UIState.BaseStyle.current, color = theme.COLOR_FG_0)
                CLabel(text = Constants.DEV_SIGN, textStyle = UIState.BaseSmallStyle.current, color = theme.COLOR_FG_0)
            }
        }
    )

}