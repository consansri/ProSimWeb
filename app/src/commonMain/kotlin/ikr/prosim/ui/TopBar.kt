package ikr.prosim.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import cengine.project.Project
import cengine.project.ViewType
import uilib.interactable.CButton
import uilib.layout.AppBar
import uilib.scale.Scaling
import uilib.theme.ThemeDef
import uilib.UIState

@Composable
fun TopBar(
    project: Project,
    viewType: MutableState<ViewType>,
    onClose: () -> Unit,
    customContent: @Composable RowScope.() -> Unit = {},
) {

    UIState.Theme.value
    val scale = UIState.Scale.value
    val icons = UIState.Icon.value

    AppBar(
        icon = icons.appLogo,
        title = project.projectState.target,
        name = project.projectState.absRootPath.toString(),
        type = viewType.value.name,
        actions = {

            customContent()

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Scaling.Scaler()

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            ThemeDef.Switch()

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            CButton(onClick = {
                viewType.component2()(viewType.value.next())
            }, icon = viewType.value.next().icon)

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            // Close Button
            CButton(onClick = onClose, icon = icons.close)
        }
    )
}