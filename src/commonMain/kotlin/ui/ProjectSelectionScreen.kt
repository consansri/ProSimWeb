package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cengine.project.ProjectState
import cengine.project.ProjectStateManager
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.label.CLabel
import ui.uilib.layout.BorderLayout
import ui.uilib.params.IconType
import ui.uilib.scale.Scaling
import ui.uilib.theme.Theme


@Composable
fun ProjectSelectionScreen(onProjectSelected: (ProjectState) -> Unit, onCreateNewProject: () -> Unit, onShowAbout: () -> Unit) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    var currentProjects by remember { mutableStateOf(ProjectStateManager.projects) }

    BorderLayout(
        modifier = Modifier.background(theme.COLOR_BG_1),
        topBg = theme.COLOR_BG_0,
        top = {
            Spacer(Modifier.weight(2.0f))
            CButton(onClick = {
                onShowAbout()
            }, icon = UIState.Icon.value.info)
            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
            Scaling.Scaler()
            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
            Theme.Switch()
        },
        center = {
            Box(modifier = Modifier.align(Alignment.Center).width(500.dp)) {
                Column(
                    modifier = Modifier
                        .padding(UIState.Scale.value.SIZE_INSET_MEDIUM),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CLabel(text = "Select a Project:", modifier = Modifier, textStyle = UIState.BaseStyle.current)
                    Spacer(modifier = Modifier.height(8.dp))

                    currentProjects.forEach {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isHovered by interactionSource.collectIsHoveredAsState()

                        Row(
                            Modifier
                                /*.background(if (isHovered) theme.COLOR_SELECTION else Color.Transparent, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))*/
                                .fillMaxWidth()
                                .clickable {
                                    onProjectSelected(it)
                                }
                                .hoverable(interactionSource),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(UIState.Icon.value.chevronRight, ">", Modifier.size(scale.SIZE_CONTROL_MEDIUM), tint = theme.COLOR_FG_1)

                            CLabel(
                                modifier = Modifier.weight(1.0f),
                                text = it.absRootPath.toString(),
                                textAlign = TextAlign.Left,
                                horizontalArrangement = Arrangement.Start,
                                softWrap = false
                            )

                            CLabel(
                                modifier = Modifier.weight(0.5f),
                                icon = UIState.Icon.value.processor,
                                text = it.target,
                                textAlign = TextAlign.Left,
                                textStyle = UIState.BaseSmallStyle.current,
                                horizontalArrangement = Arrangement.Start,
                                softWrap = false
                            )

                            CButton(
                                icon = UIState.Icon.value.close,
                                iconType = IconType.SMALL,
                                onClick = {
                                    currentProjects -= it
                                },
                                iconTint = theme.COLOR_FG_1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CButton(onClick = onCreateNewProject, text = "Create New Project", modifier = Modifier)
                }
            }
        }
    )

    LaunchedEffect(currentProjects) {
        ProjectStateManager.projects = currentProjects
    }
}