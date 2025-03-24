package cengine.lang

import cengine.project.Project
import cengine.project.ProjectStateManager
import cengine.vfs.FPath
import cengine.vfs.VirtualFile
import nativeError


/**
 * Abstract class representing a runner for a specific language service.
 *
 * @param T The type of the language service.
 * @property lang The language service instance.
 * @property name The name of the runner.
 */
abstract class Runner<T : LanguageService>(val lang: T, val name: String) {

    companion object {
        const val DEFAULT_FILEPATH_ATTR = "-f"
    }

    /**
     * Executes on the project.
     *
     * @param project The project to execute the action on.
     * @param attrs Optional attributes that affect the action.
     */
    abstract suspend fun run(project: Project, vararg attrs: String): Boolean

    suspend fun run(project: Project, file: VirtualFile, vararg attrs: String): Boolean = run(project, DEFAULT_FILEPATH_ATTR, file.path.toString(), *attrs)

    fun resolveFilePath(project: Project, filepath: FPath?): VirtualFile? {
        if (filepath == null) {
            nativeError("${this::class.simpleName} No filepath provided!")
            return null
        }

        val file = project.fileSystem.findFile(filepath)
        if (file == null) {
            nativeError("${this::class.simpleName} File ($filepath) not found!")
            return null
        }

        if (!file.name.endsWith(lang.fileSuffix)) {
            nativeError("${this::class.simpleName} File($filepath) is not of type ${lang.fileSuffix}!")
            return null
        }

        return file
    }

}