package ui.uilib.filetree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import cengine.project.Project
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.launch
import ui.uilib.UIState
import ui.uilib.menu.Menu
import ui.uilib.menu.MenuItem

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

        val runConfig = project.getLang(file)?.runConfig
        runConfig?.let {
            MenuItem(UIState.Icon.value.build, it.name) {
                onDismiss()
                ioScope.launch {
                    it.onFile(project, file)
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
                nativeLog("Launch fileKitLauncher")
                onDismiss()
                onImport(file)
            }
            MenuItem(UIState.Icon.value.file, "Create New File") {
                onDismiss()
                onCreate(file, false)
            }
            MenuItem(UIState.Icon.value.folder, "Create New Folder") {
                nativeLog("Create New Folder")
                onDismiss()
                onCreate(file, true)
            }
        }
    }

}