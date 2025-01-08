package cengine.lang

import cengine.project.Project
import cengine.vfs.VirtualFile


/**
 * Abstract class representing a runner for a specific language service.
 *
 * @param T The type of the language service.
 * @property lang The language service instance.
 * @property name The name of the runner.
 */
abstract class Runner<T : LanguageService>(val lang: T, val name: String) {

    /**
     * Executes a global action on the project.
     *
     * @param project The project to execute the action on.
     * @param attrs Optional attributes that affect the action.
     */
    abstract suspend fun global(project: Project, vararg attrs: String)

    /**
     * Executes an action on a specific file within the project.
     *
     * @param project The project containing the file.
     * @param file The file to execute the action on.
     * @param attrs Optional attributes that affect the action.
     */
    abstract suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String)
}