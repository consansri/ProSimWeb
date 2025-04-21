package cengine.project

import cengine.console.SysOut
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

actual fun loadAppState(): AppState? {
    return try {
        val fileContent = Files.readString(Path.of(APPSTATE_NAME), Charset.defaultCharset())
        Json.decodeFromString<AppState>(fileContent)
    } catch (e: Exception) {
        SysOut.warn("Couldn't load app state!")
        null
    }
}

actual fun AppState.storeAppState() {
    val path = Path.of(APPSTATE_NAME)
    if (!Files.exists(path)) {
        Files.createFile(path)
    }

    Files.writeString(path, Json.encodeToString(this))
}