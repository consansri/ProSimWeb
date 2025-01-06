package ui

import Performance
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import cengine.lang.asm.Initializer
import cengine.lang.mif.MifLang
import cengine.lang.obj.ObjLang
import cengine.lang.obj.elf.ELFFile
import cengine.project.Project
import emulator.kit.Architecture
import emulator.kit.nativeError
import kotlinx.serialization.Serializable
import ui.emulator.ArchitectureOverview
import ui.emulator.ExecutionView
import ui.emulator.MemView
import ui.emulator.RegView
import ui.uilib.UIState
import ui.uilib.filetree.FileTree
import ui.uilib.interactable.CButton
import ui.uilib.interactable.CToggle
import ui.uilib.label.CLabel
import ui.uilib.layout.BorderLayout
import ui.uilib.layout.HorizontalToolBar
import ui.uilib.layout.ResizableBorderPanels
import ui.uilib.layout.VerticalToolBar

@Composable
fun EmulatorView(project: Project, viewType: MutableState<ViewType>, architecture: Architecture<*,*>?, close: () -> Unit) {

    val theme = UIState.Theme.value
    val icons = UIState.Icon.value
    val projectState = project.projectState
    val emuState = projectState.emu

    val asmHighlighter = project.getAsmLang()?.highlightProvider

    val baseStyle = UIState.BaseStyle.current
    val baseLargeStyle = UIState.BaseLargeStyle.current
    val codeStyle = UIState.CodeStyle.current

    var stepCount by remember { mutableStateOf(4U) }
    var accumulatedScroll by remember { mutableStateOf(0f) }
    val scrollThreshold = 100f
    var leftContentType by remember { mutableStateOf(emuState.leftContent) }
    var rightContentType by remember { mutableStateOf(emuState.rightContent) }
    var bottomContentType by remember { mutableStateOf(emuState.bottomContent) }

    var emuInitFilePath by remember { mutableStateOf(project.projectState.emu.initFilePath) }

    fun buildInitializer(): Initializer? {
        emuState.initFilePath = emuInitFilePath

        val initFilePath = emuInitFilePath ?: return null
        val file = project.fileSystem.findFile(initFilePath) ?: return null
        val lang = project.getLang(file)
        return try {
            when (lang) {
                MifLang -> {
                    val mifPsiFile = MifLang.psiParser.parse(file)
                    val errors = mifPsiFile.printErrors()
                    if (errors != null) {
                        nativeError(errors)
                        return null
                    }
                    mifPsiFile
                }

                ObjLang -> {
                    ELFFile.parse(file.name, file.getContent())
                }

                else -> null
            }
        } catch (e: Throwable) {
            nativeError(e.message.toString())
            null
        }
    }

    var initializer: Initializer? by remember { mutableStateOf(buildInitializer()) }

    val archOverview: (@Composable BoxScope.() -> Unit) = {
        ArchitectureOverview(architecture, baseStyle, baseLargeStyle)
    }

    val memView: (@Composable BoxScope.() -> Unit) = {
        if (architecture != null) {
            MemView(architecture)
        } else {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "No Architecture Selected!", textStyle = baseStyle)
            }
        }
    }

    val regView: (@Composable BoxScope.() -> Unit) = {
        if (architecture != null) {
            RegView(architecture)
        } else {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "No Architecture Selected!", textStyle = baseStyle)
            }
        }
    }

    val objFileSelector: (@Composable BoxScope.() -> Unit) = {
        val leftVScrollState = rememberScrollState()
        val leftHScrollState = rememberScrollState()

        Column {
            CButton(modifier = Modifier.fillMaxWidth().background(theme.COLOR_BG_1), text = initializer?.id ?: "[not selected]", icon = icons.fileCompiled, softWrap = false, onClick = {
                emuInitFilePath = null
            })
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
                    emuInitFilePath = file.path
                }
            }
        }
    }

    BorderLayout(
        Modifier.fillMaxSize().background(theme.COLOR_BG_0),
        top = {
            TopBar(project, viewType, onClose = { close() }) {
                Text("PC: ${architecture?.pcState?.value?.zeroPaddedHex() ?: "N/A"}", fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_0)
                //CLabel(text = "PC: ${pcState.value?.toHex() ?: "N/A"}", fontType = FontType.CODE) // ISSUE: PC doesn't seem to automatically update its value!
            }
        },
        center = {

            with(LocalDensity.current) {
                ResizableBorderPanels(
                    Modifier.fillMaxSize(),
                    initialLeftWidth = emuState.leftWidth.toDp(),
                    initialBottomHeight = emuState.bottomHeight.toDp(),
                    initialRightWidth = emuState.rightWidth.toDp(),
                    leftContent = when (leftContentType) {
                        EmulatorContentView.ObjFileSelection -> objFileSelector
                        EmulatorContentView.ArchOverview -> archOverview
                        EmulatorContentView.RegView -> regView
                        EmulatorContentView.MemView -> memView
                        null -> null
                    },
                    centerContent = {
                        ExecutionView(architecture, asmHighlighter, baseStyle, codeStyle)
                    },
                    rightContent = when (rightContentType) {
                        EmulatorContentView.ObjFileSelection -> objFileSelector
                        EmulatorContentView.ArchOverview -> archOverview
                        EmulatorContentView.RegView -> regView
                        EmulatorContentView.MemView -> memView
                        null -> null
                    },
                    bottomContent = when (bottomContentType) {
                        EmulatorContentView.ObjFileSelection -> objFileSelector
                        EmulatorContentView.ArchOverview -> archOverview
                        EmulatorContentView.RegView -> regView
                        EmulatorContentView.MemView -> memView
                        null -> null
                    },
                    onBottomHeightChange = {
                        emuState.bottomHeight = it.value
                    },
                    onLeftWidthChange = {
                        emuState.leftWidth = it.value
                    },
                    onRightWidthChange = {
                        emuState.rightWidth = it.value
                    }
                )
            }

        },
        left = {
            VerticalToolBar(
                upper = {
                    CToggle(onClick = {
                        leftContentType = if (leftContentType != EmulatorContentView.ObjFileSelection) {
                            EmulatorContentView.ObjFileSelection
                        } else null
                    }, value = bottomContentType == EmulatorContentView.ObjFileSelection, icon = icons.folder)
                },
                lower = {
                    CToggle(onClick = {
                        bottomContentType = if (it && bottomContentType != EmulatorContentView.ArchOverview) {
                            EmulatorContentView.ArchOverview
                        } else null
                    }, value = bottomContentType == EmulatorContentView.ArchOverview, icon = icons.settings)
                    CToggle(onClick = {
                        bottomContentType = if (it && bottomContentType != EmulatorContentView.MemView) {
                            EmulatorContentView.MemView
                        } else null
                    }, value = bottomContentType == EmulatorContentView.MemView, icon = icons.bars)
                }
            )
        },
        right = {
            VerticalToolBar(
                upper = {
                    CButton(icon = icons.singleExe, onClick = {
                        architecture?.exeSingleStep()
                    })
                    CButton(icon = icons.continuousExe, onClick = {
                        architecture?.exeContinuous()
                    }, tooltip = "${Performance.MAX_INSTR_EXE_AMOUNT}")
                    CButton(modifier = Modifier
                        .scrollable(orientation = Orientation.Vertical,
                            state = rememberScrollableState { delta ->
                                accumulatedScroll += delta

                                // Increment stepCount when scroll threshold is crossed
                                if (accumulatedScroll <= -scrollThreshold) {
                                    stepCount = stepCount.dec().coerceAtLeast(1U)
                                    accumulatedScroll = 0f // Reset after increment
                                } else if (accumulatedScroll >= scrollThreshold) {
                                    stepCount = stepCount.inc()
                                    accumulatedScroll = 0f // Reset after decrement
                                }
                                delta
                            })
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                // Adjust stepCount based on the dragAmount
                                if (dragAmount < 0) {
                                    stepCount = stepCount.inc() // Scroll up to increase
                                } else if (dragAmount > 0) {
                                    stepCount = stepCount.dec().coerceAtLeast(1U) // Scroll down to decrease, ensure it's >= 1
                                }
                            }
                        }, icon = icons.stepMultiple, tooltip = stepCount.toString(), onClick = {
                        architecture?.exeMultiStep(stepCount.toLong())
                    })
                    CButton(icon = icons.stepOver, onClick = {
                        architecture?.exeSkipSubroutine()
                    })
                    CButton(icon = icons.stepOut, onClick = {
                        architecture?.exeReturnFromSubroutine()
                    })
                    CButton(icon = icons.refresh, onClick = {
                        architecture?.exeReset()
                    })
                },
                lower = {
                    CToggle(onClick = {
                        rightContentType = if (it && rightContentType != EmulatorContentView.RegView) {
                            EmulatorContentView.RegView
                        } else null
                    }, value = rightContentType == EmulatorContentView.RegView, icon = icons.processorBold)
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

    LaunchedEffect(initializer) {
        architecture ?: return@LaunchedEffect
        architecture.initializer = initializer
        architecture.disassembler?.decodedContent?.value = emptyList()

        initializer?.let { initializer ->
            architecture.disassembler?.let {
                it.decodedContent.value = it.disassemble(initializer)
            }
        }

        architecture.exeReset()
    }

    LaunchedEffect(emuInitFilePath) {
        initializer = buildInitializer()
    }

    LaunchedEffect(leftContentType) {
        emuState.leftContent = leftContentType
    }
    LaunchedEffect(rightContentType) {
        emuState.rightContent = rightContentType
    }
    LaunchedEffect(bottomContentType) {
        emuState.bottomContent = bottomContentType
    }
}

@Serializable
enum class EmulatorContentView {
    ObjFileSelection,
    ArchOverview,
    RegView,
    MemView
}
