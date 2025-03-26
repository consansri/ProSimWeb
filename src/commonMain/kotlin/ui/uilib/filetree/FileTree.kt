package ui.uilib.filetree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.FPath.Companion.toFPath
import cengine.vfs.FileChangeListener
import cengine.vfs.VirtualFile
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ui.uilib.UIState
import ui.uilib.dialog.ConfirmDialog
import ui.uilib.dialog.InputDialog
import ui.uilib.interactable.CButton
import ui.uilib.label.CLabel
import ui.uilib.params.IconType

@Composable
fun FileTree(
    project: Project,
    onDoubleClick: (VirtualFile) -> Unit,
) {
    val expandedItems = remember { mutableStateListOf<VirtualFile>() }
    val vfs by remember { mutableStateOf(project.fileSystem) }
    var root by remember { mutableStateOf(vfs.root) }

    var selectedFile by remember { mutableStateOf<VirtualFile?>(null) }

    // Context Menu
    var showMenu by remember { mutableStateOf(false) }
    var contextMenuPosition by remember { mutableStateOf(Offset.Zero) }

    // Input Dialog
    var showInputDialog by remember { mutableStateOf(false) }
    var inputDialogTitle by remember { mutableStateOf("") }
    var inputDialogInitText by remember { mutableStateOf("") }
    val onInputConfirm = remember { mutableStateOf<(String) -> Unit>({}) }

    // Delete Dialog
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteDialogTitle by remember { mutableStateOf("") }
    val onDeleteConfirm = remember { mutableStateOf<(Boolean) -> Unit>({}) }

    // Variables to keep track of clicks and timing
    var lastClickTime = remember { 0L }
    val doubleClickThreshold = 300L // milliseconds threshold for detecting double click
    val ioScope = rememberCoroutineScope()

    fun forceReload(file: VirtualFile) {
        expandedItems.remove(file)
        expandedItems.add(file)
    }

    fun forceReload() = forceReload(vfs.root)

    val fileKitLauncher = rememberFilePickerLauncher(mode = PickerMode.Multiple()) { files ->
        val currSelectedFile = selectedFile
        if (files != null && currSelectedFile != null) {
            files.forEach { file ->
                val vfile = vfs.createFile(currSelectedFile.path + file.name)
                ioScope.launch {
                    vfile.setContent(file.readBytes())
                }
            }
            forceReload(currSelectedFile)
        }
    }

    val currSelectedFile = selectedFile

    if (showMenu && currSelectedFile != null) {
        FileContextMenu(
            file = currSelectedFile,
            project = project,
            position = contextMenuPosition,
            onDismiss = { showMenu = false },
            onRename = { file ->
                inputDialogTitle = "Rename File"
                inputDialogInitText = file.name
                onInputConfirm.value = { newName ->
                    vfs.renameFile(file.path, newName)
                    forceReload()
                    showInputDialog = false
                }
                showInputDialog = true
            },
            onCreate = { file, isDirectory ->
                inputDialogTitle = if (isDirectory) "Create Directory" else "Create File"
                inputDialogInitText = "new"
                onInputConfirm.value = { newName ->
                    vfs.createFile(file.path + newName.toFPath(), isDirectory)
                    forceReload()
                    showInputDialog = false
                }
                showInputDialog = true
            },
            onDelete = { file ->
                deleteDialogTitle = "You are about to delete ${file.path}!"
                onDeleteConfirm.value = {
                    if (it) {
                        vfs.deleteFile(file.path)
                        forceReload()
                    }
                }
                showDeleteConfirm = true
                showMenu = false
            },
            onImport = { file ->
                fileKitLauncher.launch()
            }
        )
    }

    // Input Dialog for creating or renaming files
    if (showInputDialog) {
        InputDialog(
            inputDialogTitle,
            inputDialogInitText,
            onInputConfirm.value,
            onDismiss = {
                showInputDialog = false
            },
            valid = {
                val path = selectedFile!!.path + it
                vfs[path] != null
            }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            deleteDialogTitle
        ) {
            showDeleteConfirm = false
            onDeleteConfirm.value(it)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        node(
            root,
            expandedItems,
            selectedFile,
            toggleExpanded = {
                if (expandedItems.contains(it)) {
                    expandedItems.remove(it)
                } else {
                    expandedItems.add(it)
                }
            },
            onLeftClick = { file ->
                selectedFile = file
                val currentTime = Clock.System.now().toEpochMilliseconds()

                if (currentTime - lastClickTime < doubleClickThreshold) {
                    onDoubleClick(file)
                    lastClickTime = 0L
                } else {
                    lastClickTime = currentTime
                }
            },
            onRightClick = { file, offset ->
                selectedFile = file
                contextMenuPosition = offset
                showMenu = true
            },
            depth = 0.dp,
            expandWidth = IconType.SMALL.getSize() + UIState.Scale.value.SIZE_INSET_MEDIUM * 2
        )
    }

    LaunchedEffect(Unit) {
        vfs.addChangeListener(object : FileChangeListener {
            override fun onFileChanged(file: VirtualFile) {
                root = vfs.root
            }

            override fun onFileCreated(file: VirtualFile) {
                root = vfs.root
            }

            override fun onFileDeleted(file: VirtualFile) {
                root = vfs.root
            }
        })
    }
}

fun LazyListScope.nodes(
    nodes: List<VirtualFile>,
    expandedItems: List<VirtualFile>,
    selectedItem: VirtualFile?,
    toggleExpanded: (VirtualFile) -> Unit,
    onLeftClick: (VirtualFile) -> Unit,
    onRightClick: (VirtualFile, Offset) -> Unit,
    depth: Dp,
    expandWidth: Dp,
) {
    nodes.sortedBy { it.name }.forEach {
        node(
            it,
            expandedItems = expandedItems,
            selectedItem = selectedItem,
            toggleExpanded = toggleExpanded,
            onLeftClick = onLeftClick,
            onRightClick = onRightClick,
            depth = depth,
            expandWidth = expandWidth
        )
    }
}


fun LazyListScope.node(
    file: VirtualFile,
    expandedItems: List<VirtualFile>,
    selectedItem: VirtualFile?,
    toggleExpanded: (VirtualFile) -> Unit,
    onLeftClick: (VirtualFile) -> Unit,
    onRightClick: (VirtualFile, Offset) -> Unit,
    depth: Dp,
    expandWidth: Dp,
) {
    val icon = UIState.Icon.value

    item(file.path) {
        var itemPosition by remember { mutableStateOf(Rect.Zero) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selectedItem == file) UIState.Theme.value.COLOR_SELECTION else Color.Transparent, RoundedCornerShape(UIState.Scale.value.SIZE_CORNER_RADIUS))
                .onGloballyPositioned {
                    itemPosition = it.boundsInParent()
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)

                            when {
                                event.type == PointerEventType.Press && event.buttons.isSecondaryPressed -> {
                                    val localOffset = event.changes.firstOrNull()?.position ?: Offset.Zero
                                    val globalOffset = itemPosition.topLeft + localOffset
                                    event.changes.forEach { it.consume() }
                                    onRightClick(file, globalOffset)
                                }

                                event.type == PointerEventType.Press && event.buttons.isPrimaryPressed -> {
                                    val offset = event.changes.firstOrNull()?.position ?: Offset.Zero

                                    event.changes.forEach { it.consume() }
                                    onLeftClick(file)
                                }
                            }
                        }
                    }
                },
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(depth))

            if (file.isDirectory) {
                CButton(onClick = {
                    toggleExpanded(file)
                }, icon = if (expandedItems.contains(file)) icon.chevronDown else icon.chevronRight, iconType = IconType.SMALL, withPressedBg = false, withHoverBg = false)
            } else {
                Spacer(Modifier.width(expandWidth))
            }

            CLabel(
                icon = when {
                    file.isDirectory -> icon.folder
                    file.name.endsWith(".s") || file.name.endsWith(".S") -> icon.asmFile
                    else -> icon.file
                },
                iconType = IconType.SMALL,
                text = file.name,
                textAlign = TextAlign.Left
            )
        }
    }
    if (expandedItems.contains(file)) {
        nodes(
            file.getChildren(),
            expandedItems,
            selectedItem,
            toggleExpanded,
            onLeftClick,
            onRightClick,
            depth + expandWidth,
            expandWidth = expandWidth
        )
    }
}

