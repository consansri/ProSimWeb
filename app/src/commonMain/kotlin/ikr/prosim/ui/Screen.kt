package ui

import cengine.project.ProjectState

sealed class Screen {
    data object ProjectSelection : Screen()
    data object CreateNewProject : Screen()
    data object About : Screen()
    data class ProjectView(val state: ProjectState) : Screen()
}