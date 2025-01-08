package ui

import androidx.compose.runtime.*
import cengine.project.ProjectStateManager

@Composable
fun MainScreen(){
    val currentlyOpened = ProjectStateManager.appState.currentlyOpened
    val initialScreen = if (currentlyOpened != null) {
        Screen.ProjectView(ProjectStateManager.appState.projectStates[currentlyOpened])
    } else {
        Screen.ProjectSelection
    }

    var currentScreen by remember { mutableStateOf(initialScreen) }

    when (val screen = currentScreen) {
        is Screen.ProjectSelection -> ProjectSelectionScreen(
            onProjectSelected = { selectedProject ->
                ProjectStateManager.appState = ProjectStateManager.appState.copy(currentlyOpened = ProjectStateManager.projects.indexOf(selectedProject))
                currentScreen = Screen.ProjectView(selectedProject)
            },
            onCreateNewProject = {
                currentScreen = Screen.CreateNewProject
            },
            onShowAbout = {
                currentScreen = Screen.About
            }
        )

        is Screen.About -> {
            AboutScreen {
                currentScreen = Screen.ProjectSelection
            }
        }

        is Screen.ProjectView -> {
            ProjectViewScreen(screen.state) {
                ProjectStateManager.appState = ProjectStateManager.appState.copy(currentlyOpened = null)
                currentScreen = Screen.ProjectSelection
            }
        }

        is Screen.CreateNewProject -> CreateNewProjectScreen(
            onProjectCreated = { newProjectPath ->
                currentScreen = Screen.ProjectView(newProjectPath)
            },
            onCancel = {
                currentScreen = Screen.ProjectSelection
            }
        )
    }
}