package ikr.prosim.ui.ide.editor


import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.toSize
import cengine.console.SysOut
import cengine.editor.completion.Completion
import cengine.vfs.VirtualFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uilib.UIState
import uilib.interactable.CHorizontalScrollBar
import uilib.interactable.CVerticalScrollBar
import uilib.layout.CornerLayout
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.measureTime

@Composable
fun TextEditor(
    file: VirtualFile,
    codeStyle: TextStyle,
    codeSmallStyle: TextStyle,
    modifier: Modifier = Modifier,
    onInputLag: (Duration) -> Unit = {},
) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    UIState.Icon.value

    val textMeasurer = rememberTextMeasurer()
    val coroutineScope = rememberCoroutineScope()
    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()
    val scrollPadding by remember { mutableStateOf(30) }

    // Content State

    var textFieldValue by remember { mutableStateOf(TextFieldValue(file.getAsUTF8String())) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

    var lineNumberLabelingBounds by remember { mutableStateOf(Size.Zero) }
    val (lineCount, setLineCount) = remember { mutableStateOf(0) }
    val (lineHeight, setLineHeight) = remember { mutableStateOf(0f) }
    var visibleIndexRange by remember { mutableStateOf(0..<textFieldValue.text.length) }
    var highlightJob by remember { mutableStateOf<Job?>(null) }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var rowHeaderWidth by remember { mutableStateOf(0f) }

    var caretOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    fun scrollToIndex(index: Int) {
        textLayout?.let { layout ->
            val line = layout.getLineForOffset(index)
            val scrollDestinationUpper = layout.getLineBottom(line).roundToInt()
            val scrollDestinationLower = layout.getLineTop(line).roundToInt()
            val lowerBound = (scrollVertical.value + scrollPadding)
            val upperBound = scrollVertical.value + scrollVertical.viewportSize - scrollPadding

            when {
                scrollDestinationLower > upperBound -> {
                    // Scroll To Upper Bound
                    val dest = (scrollDestinationUpper - scrollVertical.viewportSize + scrollPadding).coerceAtLeast(0)
                    coroutineScope.launch {
                        scrollVertical.scrollTo(dest)
                    }
                }

                scrollDestinationLower < lowerBound -> {
                    // Scroll To Lower Bound
                    val dest = (scrollDestinationLower - scrollPadding).coerceAtLeast(0)
                    coroutineScope.launch {
                        scrollVertical.scrollTo(dest)
                    }
                }

                else -> {}
            }
        }
    }

    fun onTextChange(new: TextFieldValue) {
        val time = measureTime {
            val caretIndex = new.selection.start
            scrollToIndex(caretIndex)

            textFieldValue = new
        }
        if (time.inWholeNanoseconds > 0) onInputLag(time)
    }

    fun insertCompletion(completion: Completion) {
        val start = textFieldValue.selection.start
        val newText = textFieldValue.annotatedString.subSequence(0, start) + AnnotatedString(completion.insertion) + textFieldValue.annotatedString.subSequence(start, textFieldValue.annotatedString.length)

        onTextChange(textFieldValue.copy(annotatedString = newText, selection = TextRange(start + completion.insertion.length)))
    }

    with(LocalDensity.current) {
        BasicTextField(
            modifier = Modifier
                .onGloballyPositioned {
                    textFieldSize = it.size.toSize()
                }
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when {
                            !keyEvent.isShiftPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed -> {
                                when (keyEvent.key) {
                                    Key.Tab -> {
                                        onTextChange(
                                            if (keyEvent.isShiftPressed) {
                                                if (textFieldValue.selection.length == 0) {
                                                    removeIndentation(textFieldValue, textLayout)
                                                } else {
                                                    removeIndentationForSelectedLines(textFieldValue, textLayout)
                                                }
                                            } else {
                                                if (textFieldValue.selection.length == 0) {
                                                    insertIndentation(textFieldValue)
                                                } else {
                                                    insertIndentationForSelectedLines(textFieldValue, textLayout)
                                                }
                                            }
                                        )
                                        true
                                    }


                                    else -> false
                                }
                            }

                            keyEvent.isShiftPressed && keyEvent.isCtrlPressed && !keyEvent.isAltPressed -> {
                                when (keyEvent.key) {
                                    Key.S -> {
                                        SysOut.warn("Couldn't run a file which has no language service attached.")
                                        true
                                    }

                                    else -> false
                                }
                            }

                            else -> false
                        }

                    } else {
                        false
                    }
                },
            value = textFieldValue,
            cursorBrush = SolidColor(theme.COLOR_FG_0),
            textStyle = codeStyle.copy(
                color = theme.COLOR_FG_0
            ),
            onValueChange = { newValue ->
                onTextChange(newValue)
            },
            onTextLayout = { result ->
                textLayout = result
                setLineCount(result.lineCount)
                setLineHeight(result.multiParagraph.height / result.lineCount)
            }
        ) { textField ->

            Box(
                modifier
                    .fillMaxSize()
            ) {
                Box(
                    Modifier.matchParentSize()
                        .verticalScroll(scrollVertical)
                ) {

                    // Add a light blue background for the current line
                    textLayout?.let { layout ->
                        val lineTop: Float
                        val lineBottom: Float

                        if (textFieldValue.selection.collapsed) {
                            val currentLine = layout.getLineForOffset(textFieldValue.selection.start)
                            lineTop = layout.getLineTop(currentLine)
                            lineBottom = layout.getLineBottom(currentLine)

                        } else {
                            val minLine = layout.getLineForOffset(textFieldValue.selection.min)
                            val maxLine = layout.getLineForOffset(textFieldValue.selection.max)
                            lineTop = layout.getLineTop(minLine)
                            lineBottom = layout.getLineBottom(maxLine)
                        }

                        Box(
                            modifier = Modifier
                                .offset(y = lineTop.toDp())
                                .height((lineBottom - lineTop).toDp())
                                .fillMaxWidth()
                                .background(theme.COLOR_SELECTION.copy(alpha = 0.10f))  // Light blue with 10% opacity
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth()
                    ) {
                        // Scroll Container

                        Box(
                            modifier = Modifier
                                .padding(horizontal = scale.SIZE_INSET_MEDIUM)
                                .height(textFieldSize.height.toDp())
                                .width(lineNumberLabelingBounds.width.toDp())
                                .onGloballyPositioned {
                                    rowHeaderWidth = it.size.toSize().width
                                }
                        ) {
                            repeat(lineCount) { line ->
                                val thisLineContent = (line + 1).toString()
                                val thisLineTop = textLayout?.multiParagraph?.getLineTop(line) ?: (lineHeight * line)
                                Text(
                                    modifier = Modifier
                                        .width(lineNumberLabelingBounds.width.toDp())
                                        .height(lineHeight.toDp()).offset(y = (thisLineTop).toDp() + (lineHeight.toDp() - lineNumberLabelingBounds.height.toDp()) / 2),
                                    textAlign = TextAlign.Right,
                                    text = thisLineContent,
                                    color = theme.COLOR_FG_1,
                                    style = codeSmallStyle
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .height(textFieldSize.height.toDp())
                                .width(scale.SIZE_BORDER_THICKNESS)
                        )

                        Box(
                            Modifier.fillMaxWidth()
                                .horizontalScroll(scrollHorizontal)
                        ) {

                            Box(
                                Modifier
                            ) {

                                textField()

                            }
                        }
                    }
                }

                CornerLayout(
                    north = {
                        Row(
                            Modifier.padding(scale.SIZE_INSET_SMALL).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Controls
                        }
                    },
                    east = {
                        // Draw Vertical ScrollBar
                        CVerticalScrollBar(scrollVertical) {

                        }
                    },
                    south = {

                        // Draw Horizontal ScrollBar
                        CHorizontalScrollBar(scrollHorizontal) {
                            Column {
                                Row(Modifier.padding(scale.SIZE_INSET_SMALL).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                    val selection = textFieldValue.selection
                                    val line = textLayout?.getLineForOffset(selection.start) ?: 0
                                    val column = selection.start - (textLayout?.getLineStart(line) ?: 0)

                                    Text("${line + 1}:${column + 1}", fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_1)
                                }

                                Spacer(Modifier.height(scale.SIZE_SCROLL_THUMB))
                            }
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(textFieldValue) {
        coroutineScope.launch {
            val time = measureTime {
                caretOffset = textLayout?.getCursorRect(textFieldValue.selection.start)?.bottomCenter ?: Offset(0f, 0f)
                // Update the current line when the text changes

                file.setAsUTF8String(textFieldValue.text)
            }
            if (time.inWholeMilliseconds > 5) SysOut.log("LaunchedEffect(textFieldValue) took ${time.inWholeMilliseconds}ms")
        }
    }

    LaunchedEffect(scrollVertical.value, textLayout) {
        textLayout?.let { layout ->
            val preload = scrollVertical.viewportSize / 2
            val first = layout.getOffsetForPosition(Offset(0f, (scrollVertical.value - preload).coerceAtLeast(0).toFloat()))
            val last = layout.getOffsetForPosition(Offset(0f, (scrollVertical.value + scrollVertical.viewportSize + preload).toFloat()))
            visibleIndexRange = first..<last
        }
    }

    LaunchedEffect(visibleIndexRange) {
        highlightJob?.cancel()
        highlightJob = coroutineScope.launch {
            textFieldValue = textFieldValue.copy(textFieldValue.text)
        }
    }

    LaunchedEffect(lineCount) {
        val time = measureTime {
            lineNumberLabelingBounds = textMeasurer.measure(lineCount.toString(), codeSmallStyle).size.toSize()
        }
        if (time.inWholeMilliseconds > 5) SysOut.log("LaunchedEffect(lineCount) took ${time.inWholeMilliseconds}ms")
    }
}


