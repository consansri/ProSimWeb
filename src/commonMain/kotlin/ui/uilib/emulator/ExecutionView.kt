package ui.uilib.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.highlighting.HighlightProvider.Companion.spanStyles
import cengine.lang.asm.Disassembler
import cengine.util.integer.toValue
import emulator.kit.Architecture
import ui.uilib.UIState
import ui.uilib.label.CLabel

@Composable
fun ExecutionView(architecture: Architecture?, highlighter: HighlightProvider?, baseStyle: TextStyle, codeStyle: TextStyle) {

    val disassembler = remember { architecture?.disassembler }

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icons = UIState.Icon.value
    val vScrollState = rememberScrollState()

    if (disassembler == null || architecture == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CLabel(text = "Disassembler missing!", textStyle = baseStyle)
        }
    } else {
        val targetLinks = disassembler.decoded.value.flatMap { srcSeg ->
            srcSeg.decodedContent.mapNotNull { src ->
                val destAddr = src.target ?: return@mapNotNull null
                var dest: Disassembler.Decoded? = null
                disassembler.decoded.value.forEach { destSeg ->
                    val found = destSeg.decodedContent.firstOrNull { dest ->
                        val rowAddr = destSeg.addr + dest.offset.toValue(destSeg.addr.size)
                        rowAddr == destAddr
                    }
                    if (found != null) dest = found
                }
                if (dest == null) return@mapNotNull null

                Triple(src, dest, theme.getRandom())
            }
        }

        Row(Modifier.fillMaxSize()) {

            Column(Modifier.fillMaxSize()) {

                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("ADDR", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                    }
                    Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("DATA", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                    }
                    Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("LABEL", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                    }
                    Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                    Box(Modifier.weight(0.5f))
                    Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("DECODED", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                    }
                    Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                    Box(Modifier.weight(0.3f))
                    Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("TARGET", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                    }
                }

                Box(Modifier.fillMaxWidth().height(scale.SIZE_BORDER_THICKNESS).background(theme.COLOR_BORDER))

                val values = disassembler.decoded.value.filter { it.decodedContent.isNotEmpty() }.flatMap { segment ->
                    segment.decodedContent.map { segment to it }
                }
                val labels = values.flatMap {(segment, decoded) ->
                    segment.labels.map { label -> segment.addr + label.offset.toValue(segment.addr.size) to label }
                }.toMap()

                LazyColumn(Modifier.fillMaxSize()) {

                    items(values, key = {
                        it.first.addr.rawInput + it.second.offset
                    }) { (segment, decoded) ->
                        val address = (segment.addr + decoded.offset.toValue(segment.addr.size)).toHex()
                        val destOf = targetLinks.firstOrNull { it.first == decoded }
                        val pcPointsOn = architecture.regContainer.pc.variable.state.value == address

                        val interactionSource = remember { MutableInteractionSource() }
                        val isHovered by interactionSource.collectIsHoveredAsState()

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(if (pcPointsOn) theme.COLOR_SELECTION else Color.Transparent)
                                .hoverable(interactionSource)
                                .clickable {
                                    architecture.exeUntilAddress(address)
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(address.rawInput, fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (pcPointsOn) theme.COLOR_GREEN else theme.COLOR_FG_0)
                            }
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(decoded.data.rawInput, fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (pcPointsOn) theme.COLOR_GREEN else theme.COLOR_FG_0)
                            }
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                                Text(segment.labels.filter { it.offset == decoded.offset }.joinToString(", ") { it.name + ":" }, fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_0)
                            }
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Row(Modifier.weight(0.5f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                targetLinks.filter { it.second == decoded }.forEach { (source, dest, color) ->
                                    Icon(icons.chevronRight, "dest", Modifier.size(scale.SIZE_CONTROL_SMALL).background(color, RoundedCornerShape(scale.SIZE_CORNER_RADIUS)), tint = theme.COLOR_BG_0)
                                }
                            }
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                val hls = highlighter?.fastHighlight(decoded.disassembled) ?: emptyList()
                                Text(AnnotatedString(decoded.disassembled, hls.spanStyles()), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_0)
                            }
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Row(Modifier.weight(0.3f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                targetLinks.filter { it.first == decoded }.forEach { (source, dest, color) ->
                                    Icon(icons.chevronRight, "src", Modifier.size(scale.SIZE_CONTROL_SMALL).background(color, RoundedCornerShape(scale.SIZE_CORNER_RADIUS)), tint = theme.COLOR_BG_0)
                                }
                            }
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                val targetName = labels[decoded.target]?.name ?: decoded.target?.rawInput ?: ""
                                Text(targetName, fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = destOf?.third ?: theme.COLOR_FG_1)
                            }
                        }
                    }
                }
            }
        }
    }
}