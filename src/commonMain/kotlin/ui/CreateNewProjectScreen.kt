package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.target.riscv.rv32.RV32Spec
import cengine.project.ProjectState
import cengine.project.ProjectStateManager
import cengine.system.AppTarget.DESKTOP
import cengine.system.AppTarget.WEB
import cengine.system.appTarget
import cengine.system.isAbsolutePathValid
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.launch
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.interactable.Selector
import ui.uilib.label.CLabel
import ui.uilib.layout.BorderLayout
import ui.uilib.scale.Scaling
import ui.uilib.text.CTextField
import ui.uilib.theme.Theme

@Composable
fun CreateNewProjectScreen(onProjectCreated: (ProjectState) -> Unit, onCancel: () -> Unit) {
    val pickerScope = rememberCoroutineScope()
    var pathField by remember { mutableStateOf("new-project") }
    var target by remember { mutableStateOf<TargetSpec<*>>(RV32Spec) }

    var invalidProjectPath by remember { mutableStateOf(false) }

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
        },
        center = {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .border(scale.SIZE_BORDER_THICKNESS, theme.COLOR_BORDER, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                    .padding(scale.SIZE_INSET_MEDIUM)
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                ) {
                    CLabel(text = "Create New Project", softWrap = false, modifier = Modifier.fillMaxWidth(), textStyle = UIState.BaseStyle.current)

                    Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                    Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_BORDER_THICKNESS).background(theme.COLOR_BORDER))

                    Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                    Row(Modifier) {
                        CLabel(text = "Project Path:", softWrap = false, modifier = Modifier.weight(1.0f), textAlign = TextAlign.Left, textStyle = UIState.BaseStyle.current)

                        Spacer(Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))


                        when (appTarget()) {
                            WEB -> {
                                CTextField(
                                    value = pathField,
                                    modifier = Modifier.weight(1.0f),
                                    onValueChange = {
                                        pathField = it
                                        invalidProjectPath = !isAbsolutePathValid(it)
                                    },
                                    singleLine = true,
                                    error = invalidProjectPath,
                                    textColor = theme.COLOR_FG_0
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = UIState.Scale.value.SIZE_INSET_MEDIUM)) {
                                        it()
                                    }
                                }
                            }

                            DESKTOP -> {
                                CButton(text = pathField, onClick = {
                                    pickerScope.launch {
                                        val path = FileKit.pickDirectory()?.path
                                        if (path != null) {
                                            pathField = path
                                        }
                                    }
                                })
                            }
                        }

                    }

                    Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                    Selector(TargetSpec.specs, onSelectionChanged = {
                        target = it
                    }, itemContent = { isSelected, value ->
                        CLabel(text = value.name, textStyle = UIState.BaseStyle.current)
                    })

                    Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        CButton(
                            onClick = {
                                if (!invalidProjectPath) {
                                    val state = ProjectState(pathField, target.name)
                                    ProjectStateManager.appState = ProjectStateManager.appState.copy(
                                        projectStates = listOf(state) + ProjectStateManager.projects,
                                        currentlyOpened = 0
                                    )
                                    onProjectCreated(state)
                                }
                            }, text = "Create",
                            active = !invalidProjectPath,
                            modifier = Modifier.weight(1.0f)
                        )

                        Spacer(modifier = Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        CButton(onClick = onCancel, text = "Cancel", modifier = Modifier.weight(1.0f))
                    }
                }
            }
        }
    )
}