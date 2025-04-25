package uilib.filetree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import cengine.lang.Runner
import cengine.project.Project
import cengine.vfs.VirtualFile
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.launch
import uilib.menu.Menu
import uilib.menu.MenuItem
import uilib.menu.MenuItemWithAttrs
import uilib.UIState

@Composable
fun FileContextMenu(
    file: VirtualFile,
    project: Project,
    position: Offset,
    onDismiss: () -> Unit,
    onRename: (VirtualFile) -> Unit,
    onDelete: (VirtualFile) -> Unit,
    onCreate: (VirtualFile, isDirectory: Boolean) -> Unit,
    onImport: (VirtualFile) -> Unit,
) {
    val ioScope = rememberCoroutineScope()

    Menu(
        position,
        onDismiss = onDismiss,
    ) {
        MenuItem(UIState.Icon.value.edit, "Rename") {
            onDismiss()
            onRename(file)
        }

        MenuItem(UIState.Icon.value.deleteBlack, "Delete") {
            onDismiss()
            onDelete(file)
        }

        val runner = project.getLang(file)?.runConfig
        runner?.let {
            MenuItemWithAttrs(UIState.Icon.value.chevronRight, it.name, project.projectState.ide.runnerAttrs[it.name] ?: emptyList()) { attrs ->
                onDismiss()
                project.projectState.ide.replaceRunnerAttrs(runner, attrs)
                ioScope.launch {
                    val console = project.getOrCreateConsole()
                    // Print executed command
                    console.replaceCommand(runner.name + " ${Runner.DEFAULT_FILEPATH_ATTR} ${file.path.relativeTo(console.directory)} " + attrs.joinToString(" "))
                    console.streamln()
                    // Run command
                    runner.run(console, project, file, *attrs.toTypedArray())
                    // Stream Prompt
                    console.streamprompt()
                }
            }
        }

        if (file.isFile) {
            MenuItem(UIState.Icon.value.file, "Export") {
                onDismiss()
                ioScope.launch {
                    val baseName = file.name.substringBeforeLast('.')
                    val ext = file.name.substringAfterLast('.')
                    FileKit.saveFile(file.getContent(), baseName, ext)
                }
            }
        }

        if (file.isDirectory) {
            MenuItem(UIState.Icon.value.import, "Import") {
                onDismiss()
                onImport(file)
            }
            MenuItem(UIState.Icon.value.file, "Create New File") {
                onDismiss()
                onCreate(file, false)
            }
            MenuItem(UIState.Icon.value.folder, "Create New Folder") {
                onDismiss()
                onCreate(file, true)
            }
        }
    }

}