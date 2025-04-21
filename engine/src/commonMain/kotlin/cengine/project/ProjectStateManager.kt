package cengine.project

object ProjectStateManager {

    var appState: AppState = loadAppState() ?: AppState()
        set(value) {
            field = value
            appState.storeAppState()
        }

    var projects: List<ProjectState>
        set(value) {
            appState = appState.copy(projectStates = value)
        }
        get() = appState.projectStates

    fun projectStateChanged() {
        appState.storeAppState()
    }

}