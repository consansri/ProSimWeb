package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cengine.project.Project
import cengine.vfs.VirtualFile
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ui.uilib.UIState
import ui.uilib.filetree.FileTree
import ui.uilib.ide.analyze.PsiAnalyzerView
import ui.uilib.ide.editor.CodeEditor
import ui.uilib.ide.editor.ObjectEditor
import ui.uilib.interactable.CButton
import ui.uilib.interactable.CToggle
import ui.uilib.label.CLabel
import ui.uilib.layout.*

@Composable
fun IDEView(
    project: Project,
    viewType: MutableState<ViewType>,
    close: () -> Unit,
) {
    val theme = UIState.Theme.value
    val icons = UIState.Icon.value

    val projectState = project.projectState
    val ideState = projectState.ide

    val baseStyle = UIState.BaseStyle.current
    val codeStyle = UIState.CodeStyle.current
    val baseLargeStyle = UIState.BaseLargeStyle.current
    val codeSmallStyle = UIState.CodeSmallStyle.current
    val baseSmallStyle = UIState.BaseSmallStyle.current

    val coroutineScope = rememberCoroutineScope()

    val fileEditors = remember {
        mutableStateListOf<TabItem<VirtualFile>>(*ideState.openFiles.mapNotNull { path ->
            project.fileSystem.findFile(path)
        }.map { file ->
            TabItem(file, icons.file, file.name)
        }.toTypedArray())
    }
    var fileEditorSelectedIndex by remember { mutableStateOf(0) }

    var leftContentType by remember { mutableStateOf<ToolContentType?>(ideState.leftContent) }
    var rightContentType by remember { mutableStateOf<ToolContentType?>(ideState.rightContent) }
    var bottomContentType by remember { mutableStateOf<ToolContentType?>(ideState.bottomContent) }

    val fileTree: (@Composable BoxScope.() -> Unit) = {
        val leftVScrollState = rememberScrollState()
        val leftHScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIState.Theme.value.COLOR_BG_1)
                .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
                .scrollable(leftHScrollState, Orientation.Horizontal)
                .scrollable(leftVScrollState, Orientation.Vertical)
        ) {
            // Left content
            FileTree(project) { file ->
                if (file.isFile && !fileEditors.any { it.value == file }) {
                    fileEditors.add(TabItem(file, icons.file, file.name))
                    ideState.openFiles = fileEditors.map { it.value.path }
                }
            }
        }
    }

    val psiAnalyzer: (@Composable BoxScope.() -> Unit) = {
        val psiManagers = project.psiManagers.map { TabItem(it, title = it.lang.name) }
        TabbedPane(psiManagers, content = {
            PsiAnalyzerView(psiManagers[it].value) { psiFile, index ->
                val editorIndex = fileEditors.indexOfFirst { it.value == psiFile.file }
                if (editorIndex == -1) {
                    fileEditors.add(TabItem(psiFile.file, icons.file, psiFile.file.name))
                    fileEditorSelectedIndex = fileEditors.size - 1
                    ideState.openFiles = fileEditors.map { it.value.path }
                }else{
                    fileEditorSelectedIndex = editorIndex
                }
            }
        })
    }

    BorderLayout(
        Modifier.fillMaxSize().background(theme.COLOR_BG_0),
        top = {
            TopBar(project, viewType, onClose = { close() }) {
                // Run Configurations Menu
                val runConfigs = project.services.map { it.runConfig }
                var runConfigExpanded by remember { mutableStateOf(false) }
                CButton(onClick = {
                    runConfigExpanded = true
                }, icon = icons.build)

                DropdownMenu(
                    expanded = runConfigExpanded,
                    onDismissRequest = { runConfigExpanded = false },
                    modifier = Modifier.padding(0.dp)
                ) {
                    runConfigs.forEach { config ->
                        DropdownMenuItem(
                            onClick = {
                                coroutineScope.launch {
                                    config.global(project)
                                }
                                runConfigExpanded = false
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            CLabel(text = config.name, textStyle = baseStyle)
                        }
                    }
                }
            }
        },
        center = {
            with(LocalDensity.current) {
                ResizableBorderPanels(
                    Modifier.fillMaxSize(),
                    initialLeftWidth = ideState.leftWidth.toDp(),
                    initialBottomHeight = ideState.bottomHeight.toDp(),
                    initialRightWidth = ideState.rightWidth.toDp(),
                    leftContent = when (leftContentType) {
                        ToolContentType.FileTree -> fileTree
                        ToolContentType.PsiAnalyzer -> psiAnalyzer
                        null -> null
                    },
                    centerContent = {
                        Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_0)) {
                            // Center content
                            TabbedPane(fileEditors, closeable = true, content = { index ->
                                // Display File Content
                                key(fileEditors[index].value.path) {
                                    when {
                                        fileEditors[index].value.name.endsWith(".o") -> {
                                            ObjectEditor(
                                                fileEditors[index].value,
                                                project,
                                                codeStyle,
                                                baseLargeStyle,
                                                baseStyle
                                            )
                                        }

                                        else -> {
                                            CodeEditor(
                                                fileEditors[index].value,
                                                project,
                                                codeStyle,
                                                codeSmallStyle,
                                                baseSmallStyle
                                            )
                                        }
                                    }
                                }

                            }, selectedTabIndex = fileEditorSelectedIndex, baseStyle = baseStyle) {
                                fileEditors.remove(it)
                                ideState.openFiles = fileEditors.map { it.value.path }
                            }
                        }
                    },
                    rightContent = when (rightContentType) {
                        ToolContentType.FileTree -> fileTree
                        ToolContentType.PsiAnalyzer -> psiAnalyzer
                        null -> null
                    },
                    bottomContent = when (bottomContentType) {
                        ToolContentType.FileTree -> fileTree
                        ToolContentType.PsiAnalyzer -> psiAnalyzer
                        null -> null
                    },
                    onLeftWidthChange = {
                        ideState.leftWidth = it.value
                    },
                    onBottomHeightChange = {
                        ideState.bottomHeight = it.value
                    },
                    onRightWidthChange = {
                        ideState.rightWidth = it.value
                    }
                )
            }

        },
        left = {
            VerticalToolBar(
                upper = {
                    CToggle(onClick = {
                        leftContentType = if (leftContentType != ToolContentType.FileTree) {
                            ToolContentType.FileTree
                        } else null
                    }, value = leftContentType == ToolContentType.FileTree, icon = icons.folder)
                },
                lower = {
                    CToggle(onClick = {
                        bottomContentType = if (bottomContentType != ToolContentType.PsiAnalyzer) {
                            ToolContentType.PsiAnalyzer
                        } else null
                    }, value = false, icon = icons.statusError)

                    CToggle(onClick = {

                    }, value = false, icon = icons.console)

                }
            )
        },
        right = {
            VerticalToolBar(
                upper = {

                },
                lower = {

                }
            )
        },
        bottom = {
            HorizontalToolBar(
                left = {

                },
                right = {

                }
            )
        },
        leftBg = theme.COLOR_BG_1,
        rightBg = theme.COLOR_BG_1,
        bottomBg = theme.COLOR_BG_1
    )

    LaunchedEffect(leftContentType) {
        ideState.leftContent = leftContentType
    }

    LaunchedEffect(rightContentType) {
        ideState.rightContent = rightContentType
    }

    LaunchedEffect(bottomContentType) {
        ideState.bottomContent = bottomContentType
    }
}

@Serializable
enum class ToolContentType {
    FileTree,
    PsiAnalyzer;
}