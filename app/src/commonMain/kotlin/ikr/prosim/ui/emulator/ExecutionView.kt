package ikr.prosim.ui.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import cengine.console.SysOut
import cengine.editor.highlighting.HighlightProvider
import cengine.editor.highlighting.HighlightProvider.Companion.spanStyles
import cengine.lang.asm.AsmDisassembler
import cengine.util.integer.UnsignedFixedSizeIntNumber
import emulator.kit.Architecture
import kotlinx.coroutines.launch
import uilib.label.CLabel
import uilib.UIState
import uilib.interactable.CButton
import uilib.interactable.CToggle
import uilib.params.IconType
import kotlin.collections.get
import kotlin.math.roundToInt

@Composable
fun ExecutionView(architecture: Architecture<*, *>?, highlighter: HighlightProvider?, baseStyle: TextStyle, codeStyle: TextStyle) {

    val disassembler = remember { architecture?.disassembler }

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icons = UIState.Icon.value

    var decodedRenderingValues by remember { mutableStateOf<List<Pair<AsmDisassembler.DecodedSegment, AsmDisassembler.Decoded>>>(emptyList()) }
    var decodedRenderingLabels by remember { mutableStateOf<Map<UnsignedFixedSizeIntNumber<*>, AsmDisassembler.Label>>(emptyMap()) }
    var breakpointUpdateToggle by remember { mutableStateOf(false) } // State to trigger recomposition when breakpoints are toggled

    // Tooling States
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var followPCEnabled by remember { mutableStateOf(true) } // Default to true

    if (disassembler == null || architecture == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CLabel(text = "Disassembler missing!", textStyle = baseStyle)
        }
    } else {
        val targetLinks = disassembler.decodedContent.value.flatMap { srcSeg ->
            srcSeg.decodedContent.mapNotNull { src ->
                val destAddr = src.target ?: return@mapNotNull null
                var dest: AsmDisassembler.Decoded? = null
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

        // Effect for "Follow PC"
        LaunchedEffect(
            architecture.pcState.value,
            followPCEnabled,
            decodedRenderingValues
        ) {
            if (followPCEnabled) {
                val pcAddr = architecture.pcState.value
                val itemIndex = decodedRenderingValues.indexOfFirst { (segment, decoded) ->
                    (segment.addr + decoded.offset).toBigInt() == pcAddr.toBigInt()
                }
                if (itemIndex != -1) {
                    val viewportHeight = listState.layoutInfo.viewportSize.height
                    if (viewportHeight > 0) { // Ensure viewportHeight is available
                        val targetOffset = -(viewportHeight / 3F).roundToInt()
                        listState.animateScrollToItem(index = itemIndex, scrollOffset = targetOffset)
                    } else {
                        // Fallback if viewportHeight is not yet available (e.g., initial composition)
                        listState.animateScrollToItem(index = itemIndex) // Scroll to top as fallback
                    }
                }
            }
        }

        Column(Modifier.fillMaxSize()) {

            // --- Control Bar for Tooling ---
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(theme.COLOR_BG_0) // Optional: give it a slight background
                    .padding(horizontal = scale.SIZE_INSET_SMALL, vertical = scale.SIZE_INSET_SMALL),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Follow PC Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CToggle(
                        onClick = {
                            followPCEnabled = !followPCEnabled
                        },
                        followPCEnabled,
                        iconType = IconType.SMALL,
                        icon = UIState.Icon.value.autoscroll
                    )
                    Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                    CButton(
                        // "Go to PC" Button
                        onClick = {
                            scope.launch {
                                val pcAddr = architecture.pcState.value
                                val itemIndex = decodedRenderingValues.indexOfFirst { (segment, decoded) ->
                                    (segment.addr + decoded.offset).toBigInt() == pcAddr.toBigInt()
                                }
                                if (itemIndex != -1) {
                                    val viewportHeight = listState.layoutInfo.viewportSize.height
                                    if (viewportHeight > 0) {
                                        val targetOffset = -(viewportHeight / 3F).roundToInt()
                                        listState.animateScrollToItem(index = itemIndex, scrollOffset = targetOffset)
                                    } else {
                                        listState.animateScrollToItem(index = itemIndex) // Fallback
                                    }
                                }
                            }
                        },
                        text = "Go to PC"
                    )
                }

            }

            Box(Modifier.fillMaxWidth().height(scale.SIZE_BORDER_THICKNESS).background(theme.COLOR_BORDER)) // Separator

            // --- Header ---
            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.width(scale.SIZE_CONTROL_MEDIUM + scale.SIZE_INSET_MEDIUM), contentAlignment = Alignment.Center) { // Adjusted width for BP
                    Text("BP", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_1)
                }
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

            // --- Content ---
            LazyColumn(
                Modifier.fillMaxSize(),
                state = listState
            ) {

                items(decodedRenderingValues, key = {
                    /**
                     * @throws Exception (WebAssembly.Exception) if the key isn't unique!
                     */
                    "${it.first.addr.toString(16)}:${it.second.offset.toString(16)}"
                }) { (segment, decoded) ->
                    val address = segment.addr + decoded.offset
                    val pointsOn = targetLinks.firstOrNull { it.first.contains(decoded) }
                    val destOf = targetLinks.firstOrNull { it.second == decoded }
                    val pcPointsOn = architecture.pcState.value.toBigInt() == address

                    val interactionSourceRow = remember { MutableInteractionSource() }
                    val interactionSourceBreakpoint = remember { MutableInteractionSource() }

                    val isBreakpointSet = remember(address, breakpointUpdateToggle) {
                        architecture.isBreakpointSet(address)
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (pcPointsOn) theme.COLOR_SELECTION else Color.Transparent)
                            .hoverable(interactionSourceRow)
                            .clickable {
                                architecture.exeUntilAddress(address)
                            }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Breakpoint Indicator Column
                        Box(
                            modifier = Modifier
                                .width(scale.SIZE_CONTROL_MEDIUM + scale.SIZE_INSET_MEDIUM) // Ensure consistent width with header
                                .padding(end = scale.SIZE_INSET_MEDIUM) // Add padding to separate from address
                                .clickable(
                                    interactionSource = interactionSourceBreakpoint,
                                    indication = null, // No ripple for the breakpoint toggle itself
                                    onClick = {
                                        if (isBreakpointSet) {
                                            architecture.clearBreakpoint(address)
                                        } else {
                                            architecture.setBreakpoint(address)
                                        }
                                        breakpointUpdateToggle = !breakpointUpdateToggle // Trigger recomposition
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .size(scale.SIZE_CONTROL_SMALL) // Size of the dot
                                    .clip(CircleShape)
                                    .background(
                                        if (isBreakpointSet) theme.COLOR_RED else Color.Transparent
                                    )
                            )
                        }

                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(address.toString(16), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (pcPointsOn) theme.COLOR_GREEN else theme.COLOR_FG_0)
                        }
                        Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                        Box(Modifier.weight(0.75f), contentAlignment = Alignment.Center) {
                            Text(decoded.data.uPaddedHex(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (pcPointsOn) theme.COLOR_GREEN else theme.COLOR_FG_0)
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
                            val targetName = decodedRenderingLabels[decoded.target]?.name ?: decoded.target?.toString(16) ?: ""
                            pointsOn?.let {
                                Icon(icons.chevronRight, "src", Modifier.size(scale.SIZE_CONTROL_SMALL).background(it.third, RoundedCornerShape(scale.SIZE_CORNER_RADIUS)), tint = theme.COLOR_BG_0)
                            }
                            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                            Text(targetName, fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = pointsOn?.third ?: theme.COLOR_FG_1)
                        }
                    }

                }
            }
        }

        LaunchedEffect(disassembler.decodedContent.value) {
            decodedRenderingValues = disassembler.decodedContent.value.filter { it.decodedContent.isNotEmpty() }.flatMap { segment ->
                segment.decodedContent.map { segment to it }
            }

            decodedRenderingLabels = decodedRenderingValues.flatMap { (segment, _) ->
                segment.labels.map { label -> segment.addr + label.offset to label }
            }.toMap()
        }
    }
}