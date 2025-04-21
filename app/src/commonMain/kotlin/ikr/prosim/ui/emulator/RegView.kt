package ui.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import cengine.util.integer.Format
import emulator.kit.Architecture
import emulator.kit.MicroSetup
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile
import ui.uilib.interactable.CButton
import ui.uilib.interactable.CToggle
import ui.uilib.layout.TabItem
import ui.uilib.layout.TabbedPane
import ui.uilib.params.IconType
import uilib.UIState

@Composable
fun RegView(arch: Architecture<*, *>) {

    val regFiles = remember { MicroSetup.regFiles }
    val tabs = remember { regFiles.map { TabItem(it, title = it.name) } }

    TabbedPane(tabs, closeable = false, content = { tabIndex ->

        val tab = tabs[tabIndex]

        key(tab.value.name) {
            RegTable(tab.value)
        }

    }, baseStyle = UIState.BaseStyle.current)
}

@Composable
fun RegTable(regFile: RegFile<*>) {
    val scale = UIState.Scale.value
    val theme = UIState.Theme.value

    var sortedBy: FieldProvider? by remember { mutableStateOf(null) }
    val hScrollState = rememberScrollState()

    var numberFormat by remember { mutableStateOf(Format.HEX) }

    var showDescription by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(theme.COLOR_BG_1),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Box(Modifier.weight(0.1f), contentAlignment = Alignment.Center){
                CToggle(onClick = {
                    showDescription = !showDescription
                }, showDescription, icon = UIState.Icon.value.info, iconType = IconType.SMALL)
            }

            Row(
                Modifier.weight(0.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                regFile.indentificators.forEach { provider ->
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CButton(
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = UIState.BaseStyle.current,
                            text = if (sortedBy == provider) {
                                "${provider.name} ⌄"
                            } else {
                                provider.name
                            }, onClick = {
                                sortedBy = if (sortedBy == provider) {
                                    null
                                } else {
                                    provider
                                }
                            },
                            softWrap = false
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.weight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                CButton(text = numberFormat.name, modifier = Modifier.fillMaxWidth(), softWrap = false, onClick = {
                    numberFormat = numberFormat.next()
                })
            }

            if (showDescription) {
                Row(
                    Modifier.weight(0.4f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    regFile.descriptors.forEach { provider ->
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            CButton(
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = UIState.BaseSmallStyle.current,
                                text = if (sortedBy == provider) {
                                    "${provider.name} ⌄"
                                } else {
                                    provider.name
                                }, onClick = {
                                    sortedBy = if (sortedBy == provider) {
                                        null
                                    } else {
                                        provider
                                    }
                                },
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            Modifier.fillMaxSize()
        ) {
            val indexedRegs = regFile.regValues.mapIndexed { index, intNumber -> index to intNumber }.filter { regFile.isVisible(it.first) }

            val sortedRegs = sortedBy?.let { provider ->
                indexedRegs.sortedBy { provider.get(it.first) }
            } ?: indexedRegs

            items(sortedRegs, key = {
                "${it.first}:${it.second}"
            }) { (id, reg) ->
                // Display Reg

                val interactionSource = remember { MutableInteractionSource() }
                var regValue by remember { mutableStateOf(numberFormat.format(reg)) }
                val focused by interactionSource.collectIsFocusedAsState()

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        id.toString(16),
                        Modifier.weight(0.1f),
                        softWrap = false,
                        fontFamily = UIState.CodeStyle.current.fontFamily,
                        color = UIState.Theme.value.COLOR_FG_0,
                        fontSize = UIState.CodeStyle.current.fontSize,
                        textAlign = TextAlign.Right
                    )

                    Row(
                        Modifier.weight(0.2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        regFile.indentificators.forEach { provider ->
                            Text(
                                provider.get(id),
                                Modifier.fillMaxWidth(),
                                softWrap = false,
                                fontFamily = UIState.BaseStyle.current.fontFamily,
                                fontSize = UIState.BaseStyle.current.fontSize,
                                textAlign = TextAlign.Center,
                                color = theme.COLOR_FG_1
                            )
                        }
                    }

                    Box(Modifier.weight(0.3f).horizontalScroll(hScrollState), contentAlignment = Alignment.Center) {
                        BasicTextField(
                            value = regValue,
                            onValueChange = { regValue = it },
                            textStyle = UIState.CodeStyle.current.copy(theme.COLOR_FG_0, textAlign = TextAlign.Center),
                            visualTransformation = { annotatedString ->
                                TransformedText(AnnotatedString(numberFormat.filter(annotatedString.text)), OffsetMapping.Identity)
                            },
                            interactionSource = interactionSource
                        )
                    }

                    if (showDescription) {
                        Row(Modifier.weight(0.4f)) {
                            regFile.descriptors.forEach { provider ->
                                Text(
                                    provider.get(id),
                                    Modifier.fillMaxWidth(),
                                    softWrap = false,
                                    fontFamily = UIState.BaseSmallStyle.current.fontFamily,
                                    fontSize = UIState.BaseSmallStyle.current.fontSize,
                                    textAlign = TextAlign.Center,
                                    color = theme.COLOR_FG_1
                                )
                            }
                        }
                    }
                }

                LaunchedEffect(numberFormat) {
                    regValue = numberFormat.format(regFile[id])
                }

                LaunchedEffect(focused) {
                    if (!focused) {
                        regFile[id] = numberFormat.parse(regValue)
                        regValue = numberFormat.format(regFile[id])
                    }
                }
            }
        }
    }
}

