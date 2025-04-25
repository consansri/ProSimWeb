package ikr.prosim.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cengine.lang.mif.MifLang
import cengine.lang.obj.ObjLang
import cengine.project.Project
import cengine.project.ProjectState
import cengine.project.ViewType


@Composable
fun ProjectViewScreen(state: ProjectState, close: () -> Unit) {

    val project = remember { Project(state, ObjLang, MifLang) }
    val architecture = remember { state.getTarget()?.emuLink?.load() }
    val viewType = remember { mutableStateOf(state.viewType) }

    when (viewType.value) {
        ViewType.IDE -> IDEView(project, viewType, close)
        ViewType.EMU -> EmulatorView(project, viewType, architecture, close)
    }

    LaunchedEffect(viewType.value) {
        state.viewType = viewType.value
    }
}



