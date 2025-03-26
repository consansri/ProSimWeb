package cengine.project

const val APPSTATE_NAME = "appstate.json"

expect fun loadAppState(): AppState?

expect fun AppState.storeAppState()
