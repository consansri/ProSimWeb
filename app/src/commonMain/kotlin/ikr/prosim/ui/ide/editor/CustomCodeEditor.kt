package ikr.prosim.ui.ide.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextGranularity.Companion.Character
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiFile
import cengine.psi.feature.Highlightable
import cengine.psi.style.CodeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skia.TextLine
import uilib.UIState

fun buildAnnotatedStringFromPsi(
    text: String, // The full text
    psiFile: PsiFile?,
    baseStyle: SpanStyle,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    // Always append the full text first.
    // The base style will apply where no other specific PSI style overrides it.
    builder.append(text)

    if (psiFile == null) {
        return builder.toAnnotatedString()
    }

    builder.addStyle(baseStyle, 0, text.length)

    // Use your existing Highlightable.Collector or a similar mechanism
    // This time, instead of returning List<Highlight>, it directly creates SpanStyles
    // or provides enough info to create them.

    val psiBasedStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()

    // Simplified example:
    // You'd typically use a visitor pattern on the psiFile
    fun collectPsiStylesRecursive(element: PsiElement) {
        if (element.range.isEmpty() || element.range.last > text.length) {
            // Skip empty or out-of-bounds elements
        } else if (element is Highlightable) { // Assuming Highlightable is your interface
            val highlightInfos = (element.annotations.map { it.severity.color } + element.style).filterNotNull() // Or whatever method gives styling info

            for (info in highlightInfos) { // Assuming info contains range and a style key
                val color = UIState.Theme.value.getColor(info) // Get color from your theme
                // Add other attributes like fontWeight if your theme/styleKey defines them
                val span = SpanStyle(color = color /*, fontWeight = ... */)
                psiBasedStyles.add(AnnotatedString.Range(span, element.range.first, element.range.last.coerceAtMost(text.length)))
            }
        }
        element.children.forEach { child ->
            collectPsiStylesRecursive(child)
        }
    }

    collectPsiStylesRecursive(psiFile)


    // Apply PSI-based styles. These will override the base style for their ranges.
    psiBasedStyles.forEach { range ->
        // Ensure start is not after end, and within text bounds for safety
        if (range.start < range.end && range.start >= 0 && range.end <= text.length) {
            builder.addStyle(range.item, range.start, range.end)
        }
    }

    // Add styles for annotations (errors, warnings) if they aren't part of Highlightable
    // This is similar to what you had in fetchStyledContent
    // globalAnnotations.filter { it.range.overlaps(0..text.length) }.forEach { annotation ->
    //     val annoCodeStyle = annotation.severity.color ?: CodeStyle.BASE0
    //     val style = SpanStyle(textDecoration = TextDecoration.Underline, color = theme.getColor(annoCodeStyle))
    //     if (!annotation.range.isEmpty()) {
    //         builder.addStyle(style,
    //             annotation.range.first.coerceIn(0, text.length),
    //             (annotation.range.last + 1).coerceIn(0, text.length)
    //         )
    //     }
    // }

    return builder.toAnnotatedString()
}

// For Undo/Redo
const val MAX_UNDO_STACK_SIZE = 100

@Composable
fun CustomCodeEditor(
    // Use TextFieldValue for richer state
    textFieldValueState: MutableState<TextFieldValue>,
    psiFileState: MutableState<PsiFile?>,
    baseTextStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    // For Undo/Redo
    undoStack: MutableList<TextFieldValue> = remember { mutableListOf() },
    redoStack: MutableList<TextFieldValue> = remember { mutableListOf() },
) {
    var tfv by textFieldValueState
    val psiFile by psiFileState
    val theme by UIState.Theme

    var paragraph by remember { mutableStateOf<Paragraph?>(null) }
    val density = LocalDensity.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val focusRequester = remember { FocusRequester() }
    val clipboardManager = LocalClipboardManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current // For mobile

    var showCursor by remember { mutableStateOf(true) }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState() // For horizontal scrolling
    var editorContainerWidthPx by remember { mutableStateOf(0f) } // Width of the composable holding the canvas
    var textLayoutWidthPx by remember { mutableStateOf(0f) } // Actual width of the laid out text

    // Line numbers
    var lineNumberAreaWidth by remember { mutableStateOf(0.dp) }
    val textMeasurer = rememberTextMeasurer()


    // --- Helper Functions ---
    fun recordUndo(currentTfv: TextFieldValue) {
        if (undoStack.lastOrNull() != currentTfv) { // Avoid duplicate entries
            undoStack.add(currentTfv)
            if (undoStack.size > MAX_UNDO_STACK_SIZE) {
                undoStack.removeFirst()
            }
            redoStack.clear() // Clear redo stack on new action
        }
    }

    fun applyTfvChange(newTfv: TextFieldValue, recordForUndo: Boolean = true) {
        if (recordForUndo && tfv != newTfv) { // Only record if it actually changed
            recordUndo(tfv) // Record current state before changing
        }
        tfv = newTfv
    }

    fun Char.isPrintable(): Boolean { // Basic check, can be improved
        return !isISOControl() && isDefined()
    }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = theme.COLOR_SELECTION,
        backgroundColor = theme.COLOR_SELECTION.copy(alpha = 0.4f)
    )

    // --- Effects ---
    LaunchedEffect(Unit) { // Cursor blink
        while (true) {
            delay(500)
            showCursor = !showCursor
        }
    }

    LaunchedEffect(tfv.text, psiFile, theme, baseTextStyle, editorContainerWidthPx) {
        if (editorContainerWidthPx <= 0f) {
            paragraph = null
            return@LaunchedEffect
        }

        val newAnnotatedString = withContext(Dispatchers.Default) {
            buildAnnotatedStringFromPsi(tfv.text, psiFile, baseTextStyle.toSpanStyle())
        }

        // For horizontal scrolling, lay out paragraph with potentially larger width
        // If you want lines to wrap at editorContainerWidthPx, use that as maxWidth.
        // If you want lines to be infinitely long and scroll horizontally, use a very large number or Constraints.Infinity if supported.
        // Let's assume for now we want horizontal scrolling for lines longer than container.
        val pIntrinsics = ParagraphIntrinsics(
            text = newAnnotatedString.text,
            style = baseTextStyle,
            spanStyles = newAnnotatedString.spanStyles,
            density = density,
            fontFamilyResolver = fontFamilyResolver
        )

        // Measure for max required width
        val tempParagraphForWidth = Paragraph(pIntrinsics, Constraints(), 1) // Max lines = 1 to get longest line intrinsic
        textLayoutWidthPx = tempParagraphForWidth.maxIntrinsicWidth // This is the intrinsic width of the longest line

        paragraph = Paragraph(
            paragraphIntrinsics = pIntrinsics,
            // Use the actual textLayoutWidth for laying out, or editorContainerWidth if you want wrapping and no H-scroll for text.
            // For H-scroll of text, the Canvas itself needs to be wider if textLayoutWidthPx > editorContainerWidthPx.
            // Let's use a large width for the paragraph, and the Canvas will clip/scroll.
            constraints = Constraints(maxWidth = Constraints.Infinity), // Lay out with unconstrained width
            maxLines = Int.MAX_VALUE
        )

        // Update tfv with the new AnnotatedString, preserving selection
        // This ensures that tfv.annotatedString is what's rendered.
        // Be careful to not cause a loop if tfv itself is a key to this effect.
        // Here, tfv.text is a key, so if only annotation changes, this should be fine.
        if (tfv.annotatedString != newAnnotatedString) {
            tfv = tfv.copy(annotatedString = newAnnotatedString)
        }
    }

    // Scroll to caret
    LaunchedEffect(tfv.selection, paragraph) {
        paragraph?.let { p ->
            val caretRect = p.getCursorRect(tfv.selection.start.coerceIn(0, tfv.text.length))

            // Vertical scroll
            val lineTop = caretRect.top
            val lineBottom = caretRect.bottom
            val viewportHeight = verticalScrollState.viewportSize // Needs Canvas to be sized first
            if (viewportHeight > 0) {
                val currentScrollY = verticalScrollState.value
                if (lineTop < currentScrollY) {
                    verticalScrollState.scrollTo(lineTop.toInt())
                } else if (lineBottom > currentScrollY + viewportHeight) {
                    verticalScrollState.scrollTo((lineBottom - viewportHeight).toInt().coerceAtLeast(0))
                }
            }

            // Horizontal scroll
            val caretX = caretRect.left
            val viewportWidth = horizontalScrollState.viewportSize // Needs Canvas to be sized first
            if (viewportWidth > 0) {
                val currentScrollX = horizontalScrollState.value
                if (caretX < currentScrollX) {
                    horizontalScrollState.scrollTo(caretX.toInt())
                } else if (caretX > currentScrollX + viewportWidth) {
                    horizontalScrollState.scrollTo((caretX - viewportWidth).toInt().coerceAtLeast(0))
                }
            }
        }
    }

    // Update line number area width
    LaunchedEffect(tfv.text, baseTextStyle) {
        paragraph?.let { p ->
            val lineCount = p.lineCount
            val maxLineNumText = lineCount.toString()
            val measuredWidth = textMeasurer.measure(maxLineNumText, style = baseTextStyle.copy(textAlign = TextAlign.End)).size.width
            lineNumberAreaWidth = (measuredWidth / density.density + 16).dp // Add some padding
        } ?: run {
            lineNumberAreaWidth = 30.dp // Default if no paragraph
        }
    }

    with(LocalDensity.current) {
        Row(modifier = modifier.onGloballyPositioned {
            editorContainerWidthPx = it.size.width - lineNumberAreaWidth.toPx().coerceAtLeast(0f)
        }) {
            // Line Number Column
            Box(
                modifier = Modifier
                    .width(lineNumberAreaWidth)
                    .fillMaxSize() // Fill height of the Row
                    .padding(end = 8.dp) // Padding between numbers and text
            ) {
                paragraph?.let { p ->
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        translate(top = -verticalScrollState.value.toFloat()) {
                            for (i in 0 until p.lineCount) {
                                val lineNumText = (i + 1).toString()
                                val yOffset = p.getLineTop(i) + p.getLineBaseline(i) - (textMeasurer.measure(lineNumText, baseTextStyle).size.height / 2)


                                // Simple Text draw, can be replaced with textMeasurer.measure & drawText(layoutResult)
                                // TODO
                            }
                        }
                    }
                }
            }


            // Code Area
            Box(modifier = Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState) // Scroll the Canvas itself horizontally
                        .focusRequester(focusRequester)
                        .focusable()
                        .onKeyEvent { keyEvent ->
                            var handled = false
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                val currentSelection = tfv.selection
                                var newText = tfv.text
                                var newSelection = tfv.selection

                                // Ctrl/Cmd detection (Cmd for macOS, Ctrl for others)
                                // val isCtrlPressed = keyEvent.isCtrlPressed || keyEvent.isMetaPressed // Simplified

                                when (keyEvent.key) {
                                    Key.Backspace -> {
                                        if (currentSelection.collapsed) {
                                            if (currentSelection.start > 0) {
                                                newText = newText.removeRange(currentSelection.start - 1, currentSelection.start)
                                                newSelection = TextRange(currentSelection.start - 1)
                                            }
                                        } else { // Selection active
                                            newText = newText.removeRange(currentSelection.min, currentSelection.max)
                                            newSelection = TextRange(currentSelection.min)
                                        }
                                        handled = true
                                    }

                                    Key.Delete -> {
                                        if (currentSelection.collapsed) {
                                            if (currentSelection.start < newText.length) {
                                                newText = newText.removeRange(currentSelection.start, currentSelection.start + 1)
                                                // newSelection remains at currentSelection.start
                                            }
                                        } else { // Selection active
                                            newText = newText.removeRange(currentSelection.min, currentSelection.max)
                                            newSelection = TextRange(currentSelection.min)
                                        }
                                        handled = true
                                    }

                                    Key.DirectionLeft -> {
                                        // TODO: Ctrl + Left for word-wise
                                        newSelection = TextRange((currentSelection.start - 1).coerceAtLeast(0))
                                        // TODO: Handle Shift for selection
                                        handled = true
                                    }

                                    Key.DirectionRight -> {
                                        // TODO: Ctrl + Right for word-wise
                                        newSelection = TextRange((currentSelection.start + 1).coerceAtMost(newText.length))
                                        // TODO: Handle Shift for selection
                                        handled = true
                                    }

                                    Key.DirectionUp -> {
                                        paragraph?.let { p ->
                                            val currentLine = p.getLineForOffset(currentSelection.start)
                                            if (currentLine > 0) {
                                                val xPos = p.getCursorRect(currentSelection.start).left
                                                newSelection = TextRange(p.getOffsetForPosition(Offset(xPos, p.getLineTop(currentLine - 1) + p.getLineHeight(currentLine - 1) / 2)))
                                            }
                                        }
                                        handled = true
                                    }

                                    Key.DirectionDown -> {
                                        paragraph?.let { p ->
                                            val currentLine = p.getLineForOffset(currentSelection.start)
                                            if (currentLine < p.lineCount - 1) {
                                                val xPos = p.getCursorRect(currentSelection.start).left
                                                newSelection = TextRange(p.getOffsetForPosition(Offset(xPos, p.getLineTop(currentLine + 1) + p.getLineHeight(currentLine + 1) / 2)))
                                            }
                                        }
                                        handled = true
                                    }

                                    Key.Enter -> {
                                        // TODO: Auto-indentation
                                        val toInsert = "\n"
                                        if (currentSelection.collapsed) {
                                            newText = newText.substring(0, currentSelection.start) + toInsert + newText.substring(currentSelection.start)
                                            newSelection = TextRange(currentSelection.start + toInsert.length)
                                        } else {
                                            newText = newText.substring(0, currentSelection.min) + toInsert + newText.substring(currentSelection.max)
                                            newSelection = TextRange(currentSelection.min + toInsert.length)
                                        }
                                        handled = true
                                    }

                                    Key.Tab -> {
                                        // TODO: Indent selection / Insert spaces
                                        val toInsert = "    " // Or use settings
                                        if (currentSelection.collapsed) {
                                            newText = newText.substring(0, currentSelection.start) + toInsert + newText.substring(currentSelection.start)
                                            newSelection = TextRange(currentSelection.start + toInsert.length)
                                        } else {
                                            // Naive: replace selection, better: indent selected lines
                                            newText = newText.substring(0, currentSelection.min) + toInsert + newText.substring(currentSelection.max)
                                            newSelection = TextRange(currentSelection.min + toInsert.length)
                                        }
                                        handled = true
                                    }
                                    // --- Clipboard & Undo/Redo ---
                                    Key.Z -> { // Assuming Ctrl/Cmd is checked outside or via keyEvent.isCtrlPressed
                                        // if (isCtrlPressed) {
                                        if (undoStack.isNotEmpty()) {
                                            redoStack.add(tfv)
                                            applyTfvChange(undoStack.removeLast(), recordForUndo = false)
                                        }
                                        handled = true;
                                        // }
                                    }

                                    Key.Y -> { // Ctrl/Cmd + Y or Ctrl/Cmd + Shift + Z
                                        // if (isCtrlPressed) {
                                        if (redoStack.isNotEmpty()) {
                                            undoStack.add(tfv)
                                            applyTfvChange(redoStack.removeLast(), recordForUndo = false)
                                        }
                                        handled = true;
                                        // }
                                    }

                                    Key.C -> { // Copy: if (isCtrlPressed)
                                        if (!currentSelection.collapsed) {
                                            clipboardManager.setText(AnnotatedString(tfv.text.substring(currentSelection.min, currentSelection.max)))
                                        }
                                        handled = true
                                    }

                                    Key.X -> { // Cut: if (isCtrlPressed)
                                        if (!currentSelection.collapsed) {
                                            clipboardManager.setText(AnnotatedString(tfv.text.substring(currentSelection.min, currentSelection.max)))
                                            newText = newText.removeRange(currentSelection.min, currentSelection.max)
                                            newSelection = TextRange(currentSelection.min)
                                        }
                                        handled = true
                                    }

                                    Key.V -> { // Paste: if (isCtrlPressed)
                                        clipboardManager.getText()?.let { clipText ->
                                            if (currentSelection.collapsed) {
                                                newText = newText.substring(0, currentSelection.start) + clipText.text + newText.substring(currentSelection.start)
                                                newSelection = TextRange(currentSelection.start + clipText.text.length)
                                            } else {
                                                newText = newText.substring(0, currentSelection.min) + clipText.text + newText.substring(currentSelection.max)
                                                newSelection = TextRange(currentSelection.min + clipText.text.length)
                                            }
                                        }
                                        handled = true
                                    }

                                    else -> {
                                        val char = keyEvent.utf16CodePoint.toChar()
                                        if (char.isPrintable()) { // Use your refined isPrintable
                                            if (currentSelection.collapsed) {
                                                newText = newText.substring(0, currentSelection.start) + char + newText.substring(currentSelection.start)
                                                newSelection = TextRange(currentSelection.start + 1)
                                            } else {
                                                newText = newText.substring(0, currentSelection.min) + char + newText.substring(currentSelection.max)
                                                newSelection = TextRange(currentSelection.min + 1)
                                            }
                                            handled = true
                                        } else {
                                            handled = false
                                        }
                                    }
                                }

                                if (handled) {
                                    applyTfvChange(TextFieldValue(text = newText, selection = newSelection.coerceIn(0, newText.length)))
                                }
                            }
                            handled // Return whether the event was consumed
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { clickOffset ->
                                    paragraph?.let { p ->
                                        val newCursorOffset = p.getOffsetForPosition(
                                            Offset(
                                                x = clickOffset.x + horizontalScrollState.value, // Adjust for H-scroll
                                                y = clickOffset.y + verticalScrollState.value   // Adjust for V-scroll
                                            )
                                        )
                                        applyTfvChange(tfv.copy(selection = TextRange(newCursorOffset.coerceIn(0, tfv.text.length))))
                                    }
                                    focusRequester.requestFocus()
                                    softwareKeyboardController?.show() // For mobile
                                }
                            )
                            // TODO: detectDragGestures for selection
                        }
                    // Set the width of the Canvas to be at least the container width,
                    // but potentially wider if the textLayoutWidthPx is greater (for H-scroll).
                    // This requires a different layout approach than just .fillMaxSize() if Canvas itself needs to be wider.
                    // One way: Parent Box with horizontalScroll, Canvas inside with dynamic width.
                    // For simplicity here, horizontalScroll modifier on Canvas handles it if Paragraph is wide.
                ) { // DrawScope
                    paragraph?.let { p ->
                        // The canvas drawing area is sized by its modifier.
                        // If paragraph 'p' was laid out wider than this Canvas, horizontalScroll will work.
                        translate(
                            top = -verticalScrollState.value.toFloat(),
                            left = -horizontalScrollState.value.toFloat() // Apply H-scroll
                        ) {
                            p.paint(drawContext.canvas)

                            // Draw selection
                            if (!tfv.selection.collapsed) {
                                val selectionPath = p.getPathForRange(tfv.selection.min, tfv.selection.max)
                                drawPath(selectionPath, color = customTextSelectionColors.backgroundColor)
                            }

                            // Draw cursor
                            if (showCursor && tfv.selection.collapsed) { // Only show for collapsed selection
                                val cursorRect = p.getCursorRect(tfv.selection.start.coerceIn(0, tfv.text.length))
                                drawLine(
                                    color = theme.getColor(CodeStyle.BASE0), // Use a distinct cursor color
                                    start = cursorRect.topLeft,
                                    end = cursorRect.bottomLeft,
                                    strokeWidth = 1.5.dp.toPx() // Slightly thicker cursor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun TextRange.coerceIn(minimumValue: Int, maximumValue: Int): TextRange {
    return TextRange(
        start = this.start.coerceIn(minimumValue, maximumValue),
        end = this.end.coerceIn(minimumValue, maximumValue)
    )
}
