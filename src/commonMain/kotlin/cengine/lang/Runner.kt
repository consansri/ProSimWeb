package cengine.lang

import ConsoleContext
import SysOut
import cengine.project.Project
import cengine.vfs.VirtualFile


/**
 * Abstract class representing a runner for a specific language service.
 *
 * @param T The type of the language service.
 * @property lang The language service instance.
 * @property name The name of the runner.
 */
abstract class Runner<T: LanguageService>(val name: String) {

    abstract val lang: T

    companion object {
        const val DEFAULT_FILEPATH_ATTR = "-f"
    }

    /**
     * Executes on a [ConsoleContext]
     *
     * @param project The project to execute the action on.
     * @param attrs Optional attributes that affect the action.
     */
    protected abstract suspend fun ConsoleContext.runWithContext(project: Project, vararg attrs: String): Boolean

    suspend fun run(context: ConsoleContext, project: Project, vararg attrs: String): Boolean = context.runWithContext(project, *attrs)

    suspend fun run(ioContext: ConsoleContext, project: Project, file: VirtualFile, vararg attrs: String): Boolean = ioContext.runWithContext(project, DEFAULT_FILEPATH_ATTR, file.path.relativeTo(ioContext.directory).toString(), *attrs)

}