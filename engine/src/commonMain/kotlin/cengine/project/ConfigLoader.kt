package cengine.project

import cengine.console.SysOut
import cengine.vfs.ActualFileSystem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun loadAppState(): AppState? {
    val appStatePath = ActualFileSystem.getAppStateDir() + AppState.APPSTATE_NAME
    if (!ActualFileSystem.exists(appStatePath)) {
        AppState.initial.storeAppState()
    }

    val path = ActualFileSystem.getAppStateDir() + AppState.APPSTATE_NAME
    return try {
        val fileContent = ActualFileSystem.readFile(path).decodeToString()
        SysOut.log("Loaded app state from $path!")
        Json.decodeFromString<AppState>(fileContent)
    } catch (e: Exception) {
        SysOut.error("Couldn't load/create app state from $path!")
        null
    }
}

fun AppState.storeAppState() {
    val path = ActualFileSystem.getAppStateDir() + AppState.APPSTATE_NAME
    try {
        if (!ActualFileSystem.exists(path)) {
            ActualFileSystem.createFile(path, false)
        }

        ActualFileSystem.writeFile(path, Json.encodeToString(this).encodeToByteArray())
        SysOut.log("Stored app state to $path!")
    } catch (e: Exception) {
        SysOut.error("Couldn't store app state to $path!")
    }
}
