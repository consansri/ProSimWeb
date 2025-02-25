package cengine.project

import cengine.vfs.FPath
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import config.BuildConfig
import nativeWarn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ProjectStateManager {

    private const val projectsFileName = "projects.json"

    private val vfs = VFileSystem(BuildConfig.NAME.lowercase())

    var appState: AppState = loadState()
        set(value) {
            field = value
            saveState(value)
        }

    var projects: List<ProjectState>
        set(value) {
            appState = appState.copy(projectStates = value)
        }
        get() = appState.projectStates

    // Function to save the project state to a file
    fun saveState(state: AppState): VirtualFile {
        // nativeLog("SaveProjectState")
        val file = vfs.findFile(FPath.of(vfs, projectsFileName)) ?: vfs.createFile(FPath.of(vfs, projectsFileName))
        // Serialize the ProjectState object to a JSON string
        val jsonString = Json.encodeToString(state)
        // Write the JSON string to the file
        file.setAsUTF8String(jsonString)
        return file
    }

    // Function to load the project state from a file
    fun loadState(): AppState {
        // nativeLog("LoadProjectState")
        val file = vfs.findFile(FPath.of(vfs, projectsFileName))

        if (file == null) {
            val createdFile = saveState(AppState.initial)

            // Read the JSON string from the file
            val jsonString = createdFile.getAsUTF8String()
            // Deserialize the JSON string to a ProjectState object
            val appState = try {
                Json.decodeFromString<AppState>(jsonString)
            } catch (e: Exception) {
                nativeWarn("Couldn't load state!")
                AppState()
            }

            return appState
        }

        // Read the JSON string from the file
        val jsonString = file.getAsUTF8String()
        // Deserialize the JSON string to a ProjectState object
        return Json.decodeFromString<AppState>(jsonString)
    }

    fun projectStateChanged(){
        saveState(appState)
    }

}