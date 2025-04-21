package cengine.project

import cengine.console.SysOut
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual fun loadAppState(): AppState? {
    return try {
        val fileContent = localStorage.getItem(APPSTATE_NAME) ?: return null
        Json.decodeFromString<AppState>(fileContent)
    } catch (e: Exception) {
        SysOut.warn("Couldn't load app state!")
        null
    }
}

actual fun AppState.storeAppState() {
    localStorage.setItem(APPSTATE_NAME, Json.encodeToString(this))
}