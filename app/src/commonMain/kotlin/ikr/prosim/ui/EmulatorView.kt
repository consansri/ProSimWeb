package ikr.prosim.ui


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
import cengine.console.SysOut
import cengine.lang.asm.AsmBinaryProvider
import cengine.lang.mif.MifLang
import cengine.lang.mif.MifPsiFile
import cengine.lang.obj.ObjLang
import cengine.lang.obj.ObjPsiFile
import cengine.project.EmulatorContentView
import cengine.project.Project
import cengine.project.ViewType
import emulator.kit.Architecture
import ikr.prosim.ui.emulator.ArchitectureOverview
import ikr.prosim.ui.emulator.ExecutionView
import ikr.prosim.ui.emulator.MemView
import ikr.prosim.ui.emulator.RegView
import uilib.UIState
import uilib.filetree.FileTree
import uilib.interactable.CButton
import uilib.interactable.CToggle
import uilib.label.CLabel
import uilib.layout.BorderLayout
import uilib.layout.HorizontalToolBar
import uilib.layout.ResizableBorderPanels
import uilib.layout.VerticalToolBar

@Composable
fun EmulatorView(project: Project, viewType: MutableState<ViewType>, architecture: Architecture<*, *>?, close: () -> Unit) {

    val theme = UIState.Theme.value
    val icons = UIState.Icon.value
    val projectState = project.projectState
    val emuState = projectState.emu

    val asmHighlighter = project.getAsmLang()?.psiSupport?.highlightProvider

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

    fun buildInitializer(onFinish: (AsmBinaryProvider?) -> Unit) {
        emuState.initFilePath = emuInitFilePath

        val initFilePath = emuInitFilePath ?: return onFinish(null)
        val file = project.fileSystem[initFilePath] ?: return onFinish(null)
        val manager = project.getManager(file) ?: return onFinish(null)
        val lang = project.getLang(file) ?: return onFinish(null)

        when (lang) {
            MifLang -> {
                manager.queueUpdate(file) {
                    if (it.valid) onFinish(it as MifPsiFile)
                }
            }

            ObjLang -> {
                manager.queueUpdate(file) {
                    if (it.valid) onFinish(it as ObjPsiFile)
                }
            }

            else -> {}
        }
        onFinish(null)
    }

    var initializer: AsmBinaryProvider? by remember { mutableStateOf(null) }

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
                Text("PC: ${architecture?.pcState?.value?.uPaddedHex() ?: "N/A"}", fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_0)
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
                    })
                    CButton(
                        modifier = Modifier
                            .scrollable(
                                orientation = Orientation.Vertical,
                                state = rememberScrollableState { delta ->
                                    accumulatedScroll += delta

                                    // Increment stepCount when a scroll threshold is crossed
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
                    CLabel(textStyle = UIState.BaseSmallStyle.current, text = "execution limit: ${Performance.MAX_INSTR_EXE_AMOUNT}")
                }
            )
        },
        leftBg = theme.COLOR_BG_1,
        rightBg = theme.COLOR_BG_1,
        bottomBg = theme.COLOR_BG_1
    )

    LaunchedEffect(initializer) {
        SysOut.debug { "updated initializer!" }
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
        SysOut.debug { "emuInitFilePath changed!" }
        buildInitializer {
            initializer = it
        }
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


