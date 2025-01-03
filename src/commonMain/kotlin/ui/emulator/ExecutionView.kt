package ui.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.highlighting.HighlightProvider.Companion.spanStyles
import cengine.lang.asm.Disassembler
import cengine.util.integer.BigInt
import emulator.kit.Architecture
import ui.uilib.UIState
import ui.uilib.label.CLabel

@Composable
fun ExecutionView(architecture: Architecture<*, *>?, highlighter: HighlightProvider?, baseStyle: TextStyle, codeStyle: TextStyle) {

    val disassembler = remember { architecture?.disassembler }

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icons = UIState.Icon.value

    var decodedRenderingValues by remember { mutableStateOf<List<Pair<Disassembler.DecodedSegment, Disassembler.Decoded>>>(emptyList()) }
    var decodedRenderingLabels by remember { mutableStateOf<Map<BigInt, Disassembler.Label>>(emptyMap()) }

    if (disassembler == null || architecture == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CLabel(text = "Disassembler missing!", textStyle = baseStyle)
        }
    } else {
        val targetLinks = disassembler.decodedContent.value.flatMap { srcSeg ->
            srcSeg.decodedContent.mapNotNull { src ->
                val destAddr = src.target ?: return@mapNotNull null
                var dest: Disassembler.Decoded? = null
                disassembler.decodedContent.value.forEach { destSeg ->
                    val found = destSeg.decodedContent.firstOrNull { dest ->
                        val rowAddr = destSeg.addr + dest.offset
                        rowAddr == destAddr
                    }
                    if (found != null) dest = found
                }
                if (dest == null) return@mapNotNull null

                Pair(src, dest)
            }
        }.groupBy { it.second }.map { targetGroup ->
            Triple(targetGroup.value.map { it.first }, targetGroup.key, theme.getRandom())
        }

        Column(Modifier.fillMaxSize()) {

            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("ADDR", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                }
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                Box(Modifier.weight(0.75f), contentAlignment = Alignment.Center) {
                    Text("DATA", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                }
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("LABEL", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                }
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("DECODED", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                }
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("TARGET", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                }
            }

            Box(Modifier.fillMaxWidth().height(scale.SIZE_BORDER_THICKNESS).background(theme.COLOR_BORDER))

            LazyColumn(Modifier.fillMaxSize()) {

                items(decodedRenderingValues, key = {
                    /**
                     * @throws Exception (WebAssembly.Exception) if key isn't unique!
                     */
                    "${it.first.addr.toString(16)}:${it.second.offset.toString(16)}"
                }) { (segment, decoded) ->
                    val address = segment.addr + decoded.offset
                    val targets = targetLinks.firstOrNull { it.first.contains(decoded) }
                    val destOf = targetLinks.firstOrNull { it.second == decoded }
                    val pcPointsOn = architecture.pcState.value.toBigInt() == address

                    val interactionSource = remember { MutableInteractionSource() }

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
                            Text(address.toString(16), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (pcPointsOn) theme.COLOR_GREEN else theme.COLOR_FG_0)
                        }
                        Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                        Box(Modifier.weight(0.75f), contentAlignment = Alignment.Center) {
                            Text(decoded.data.zeroPaddedHex(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (pcPointsOn) theme.COLOR_GREEN else theme.COLOR_FG_0)
                        }
                        Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            Text(segment.labels.filter { it.offset == decoded.offset }.joinToString(", ") { it.name + ":" }, fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = destOf?.third ?: theme.COLOR_FG_0)
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            destOf?.let {
                                Icon(icons.chevronRight, "dest", Modifier.size(scale.SIZE_CONTROL_SMALL).background(it.third, RoundedCornerShape(scale.SIZE_CORNER_RADIUS)), tint = theme.COLOR_BG_0)
                            }
                        }
                        Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            val hls = highlighter?.fastHighlight(decoded.disassembled) ?: emptyList()
                            Text(AnnotatedString(decoded.disassembled, hls.spanStyles()), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_0)
                        }
                        Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            val targetName = decodedRenderingLabels[decoded.target]?.name ?: decoded.target?.zeroPaddedHex() ?: ""
                            targets?.let {
                                Icon(icons.chevronRight, "src", Modifier.size(scale.SIZE_CONTROL_SMALL).background(it.third, RoundedCornerShape(scale.SIZE_CORNER_RADIUS)), tint = theme.COLOR_BG_0)
                            }
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Text(targetName, fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = targets?.third ?: theme.COLOR_FG_1)
                        }
                    }

                }
            }
        }

        LaunchedEffect(disassembler.decodedContent.value) {
            decodedRenderingValues = disassembler.decodedContent.value.filter { it.decodedContent.isNotEmpty() }.flatMap { segment ->
                segment.decodedContent.map { segment to it }
            }

            decodedRenderingLabels = decodedRenderingValues.flatMap { (segment, decoded) ->
                segment.labels.map { label -> segment.addr + label.offset to label }
            }.toMap()
        }
    }
}