package ikr.prosim.ui.ide.editor


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.toSize
import cengine.console.SysOut
import cengine.editor.annotation.Annotation
import cengine.editor.annotation.Severity
import cengine.editor.completion.Completion
import cengine.editor.highlighting.HighlightProvider.Companion.spanStyles
import cengine.lang.LanguageService
import cengine.lang.Runner
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiService
import cengine.psi.elements.PsiFile
import cengine.psi.feature.Highlightable
import cengine.psi.feature.PsiReference
import cengine.psi.style.CodeStyle
import cengine.util.integer.IntNumberUtils.overlaps
import cengine.vfs.ActualFileSystem
import cengine.vfs.VirtualFile
import ikr.prosim.ui.ide.editor.TextEditing.duplicateSelectionOrLine
import ikr.prosim.ui.ide.editor.TextEditing.moveLines
import ikr.prosim.ui.ide.editor.TextEditing.toggleLineComments
import ikr.prosim.ui.ide.editor.state.TextStateModelImpl
import ikr.prosim.ui.ide.editor.state.UndoRedoState
import kotlinx.coroutines.*
import uilib.interactable.CButton
import uilib.interactable.CHorizontalScrollBar
import uilib.interactable.CVerticalScrollBar
import uilib.layout.CornerLayout
import uilib.params.IconType
import uilib.ComposeTools
import uilib.UIState
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.measureTime

// Debounced save utility
private var saveJob: Job? = null
private const val SAVE_DEBOUNCE_MILLIS = 1000L

private fun scheduleSave(textToSave: String, currentFile: VirtualFile, scope: CoroutineScope) {
    saveJob?.cancel()
    saveJob = scope.launch(Dispatchers.Default) {
        delay(SAVE_DEBOUNCE_MILLIS)
        try {
            currentFile.setAsUTF8String(textToSave)
            SysOut.log("File saved: ${currentFile.path}")
        } catch (e: Exception) {
            SysOut.error("Error saving file ${currentFile.path}: ${e.message}")
        }
    }
}

@Composable
fun CodeEditor(
    file: VirtualFile,
    lang: LanguageService,
    manager: PsiManager<*>,
    project: Project,
    codeStyle: TextStyle,
    codeSmallStyle: TextStyle,
    baseSmallStyle: TextStyle,
    modifier: Modifier = Modifier,
    onElementSelected: (VirtualFile, List<PsiElement>) -> Unit = { _, _ -> },
    onInputLag: (Duration) -> Unit = {},
) {

    if (!ActualFileSystem.exists(file.path)) {
        Text("Die Datei existiert nicht mehr.", style = codeStyle)
        return
    }

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icon = UIState.Icon.value

    val textMeasurer = rememberTextMeasurer()
    val coroutineScope = rememberCoroutineScope()
    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()
    val scrollPadding by remember { mutableStateOf(30) }

    // Content State
    var textFieldValue by remember(file) {
        mutableStateOf(TextFieldValue(file.getAsUTF8String()))
    }

    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

    // State Model
    val textStateModel = remember(file) {
        TextStateModelImpl(textFieldValue)
    }

    var lineNumberLabelingBounds by remember { mutableStateOf(Size.Zero) }
    val (lineCount, setLineCount) = remember { mutableStateOf(0) }
    val (lineHeight, setLineHeight) = remember { mutableStateOf(0f) }
    var visibleIndexRange by remember { mutableStateOf(0..<textFieldValue.text.length) }
    var highlightJob by remember { mutableStateOf<Job?>(null) }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var rowHeaderWidth by remember { mutableStateOf(0f) }

    var hoverPosition by remember { mutableStateOf<Offset?>(null) }
    var caretOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var currentElement by remember { mutableStateOf<PsiElement?>(null) }
    var references by remember { mutableStateOf<List<PsiReference<*>>>(emptyList()) }

    var analyticsAreUpToDate by remember { mutableStateOf(false) }
    var annotationOverlayJob by remember { mutableStateOf<Job?>(null) }
    var globalAnnotations by remember { mutableStateOf<Set<Annotation>>(emptySet()) }
    var localAnnotations by remember { mutableStateOf<Set<Annotation>>(emptySet()) }
    var isAnnotationVisible by remember { mutableStateOf(false) }
    var psiVersion by remember { mutableStateOf(0) }

    var completionOverlayJob by remember { mutableStateOf<Job?>(null) }
    var completions by remember { mutableStateOf<List<Completion>>(emptyList()) }
    val isCompletionVisible by remember { derivedStateOf { completions.isNotEmpty() } }
    var selectedCompletionIndex by remember { mutableStateOf(0) }

    // Feature States

    fun scrollToIndex(index: Int) {
        textLayout?.let { layout ->
            val line = layout.getLineForOffset(index)
            val scrollDestinationUpper = layout.getLineBottom(line).roundToInt()
            val scrollDestinationLower = layout.getLineTop(line).roundToInt()
            val lowerBound = (scrollVertical.value + scrollPadding)
            val upperBound = scrollVertical.value + scrollVertical.viewportSize - scrollPadding

            when {
                scrollDestinationLower > upperBound -> {
                    coroutineScope.launch {
                        scrollVertical.scrollTo((scrollDestinationUpper - scrollVertical.viewportSize + scrollPadding).coerceAtLeast(0))
                    }
                }

                scrollDestinationLower < lowerBound -> {
                    coroutineScope.launch {
                        scrollVertical.scrollTo((scrollDestinationLower - scrollPadding).coerceAtLeast(0))
                    }
                }
            }
        }
    }

    fun fetchStyledContent(
        code: String,
        psiFileForHighlighting: PsiFile?,
        currentVisibleIndexRange: IntRange,
    ): AnnotatedString {
        val spanStylesList = mutableListOf<AnnotatedString.Range<SpanStyle>>()

        // 1. Fast Lexical Highlighting (hopefully efficient for its range)
        val hls = lang.psiSupport?.highlightProvider?.fastHighlight(code, currentVisibleIndexRange) ?: emptyList()
        spanStylesList.addAll(hls.spanStyles())

        val psiFile = psiFileForHighlighting ?: manager.getPsiFile(file)
        if (psiFile == null) {
            // No PSI file, return basic lexical highlighting
            return AnnotatedString(code, spanStylesList)
        }

        // 2. PSI-based Semantic Highlighting (for visible range)
        Highlightable.Collector(currentVisibleIndexRange).apply {
            visitFile(psiFile) // Processes only elements intersecting currentVisibleIndexRange
            spanStylesList.addAll(styles)
        }

        // 3. Annotation Styles (for text decoration in visible range)
        // Filter globalAnnotations for those overlapping the visible range.
        val annotationsInVisibleRange = globalAnnotations.filter { it.range.overlaps(currentVisibleIndexRange) }
        spanStylesList.addAll(annotationsInVisibleRange.mapNotNull { annotation ->
            val annoCodeStyle = annotation.severity.color ?: CodeStyle.BASE0
            val style = SpanStyle(textDecoration = TextDecoration.Underline, color = theme.getColor(annoCodeStyle))
            if (!annotation.range.isEmpty()) {
                AnnotatedString.Range(
                    style,
                    annotation.range.first.coerceIn(0, code.length),
                    (annotation.range.last + 1).coerceIn(0, code.length)
                )
            } else null
        })
        return AnnotatedString(code, spanStylesList)
    }

    // Extracted common logic for text processing, PSI updates, and highlighting
    fun processTextUpdate(newTfv: TextFieldValue, isUndoRedoOperation: Boolean = false) {
        val localTime = measureTime { // Renamed to avoid conflict with 'time' in calling scope
            val oldTfv = textFieldValue // Capture current state before any changes

            if (!isUndoRedoOperation && oldTfv.text != newTfv.text) {
                textStateModel.recordChange(oldTfv, newTfv)
            }

            // Update TextFieldValue state (plain text for now, styling comes later)
            textFieldValue = newTfv.copy(annotatedString = AnnotatedString(newTfv.text))

            if (oldTfv.text != newTfv.text) {
                analyticsAreUpToDate = false

                val sOld = oldTfv.text
                val sNew = newTfv.text
                val commonPrefixLen = StringUtils.commonPrefixLength(sOld, sNew)
                val commonSuffixLen = StringUtils.commonSuffixLength(sOld, sNew)

                val oldChangeRegionEnd = sOld.length - commonSuffixLen
                val deletedTextLength = oldChangeRegionEnd - commonPrefixLen

                val newChangeRegionEnd = sNew.length - commonSuffixLen
                val insertedTextLength = newChangeRegionEnd - commonPrefixLen

                // Order of operations for PSI manager might matter: typically delete then insert for replacements
                if (deletedTextLength > 0) {
                    manager.deleted(file, commonPrefixLen, commonPrefixLen + deletedTextLength)
                }
                if (insertedTextLength > 0) {
                    manager.inserted(file, commonPrefixLen, insertedTextLength)
                }
            }

            // Highlighting job (async)
            val currentPsiFile = manager.getPsiFile(file)
            highlightJob?.cancel()
            highlightJob = coroutineScope.launch {
                // Ensure to use the text and selection from newTfv for styling
                val styledContent = fetchStyledContent(newTfv.text, currentPsiFile, visibleIndexRange)
                if (isActive && textFieldValue.text == newTfv.text) { // Check if still relevant
                    textFieldValue = TextFieldValue(
                        annotatedString = styledContent,
                        selection = newTfv.selection // Crucially, restore selection from newTfv
                    )
                }
                scrollToIndex(newTfv.selection.start)
            }
        }
        if (localTime.inWholeNanoseconds > 0) onInputLag(localTime)
    }

    suspend fun locatePSIElement() {
        val time = measureTime {
            val caretPosition = textFieldValue.selection.start
            currentElement = manager.getPsiFile(file)?.let {
                PsiService.findElementAt(it, caretPosition)
            }
        }
        if (time.inWholeMilliseconds > 5) SysOut.log("locatePSIElement took ${time.inWholeMilliseconds}ms")
    }

    suspend fun psiHasChanged(psiFile: PsiFile) {
        val newTextFromPsi = psiFile.file.getAsUTF8String()
        if (newTextFromPsi != textFieldValue.text) {
            // If text changed (e.g. by formatter in analyze), call onTextChange
            // which will handle styling synchronously.
            processTextUpdate(
                TextFieldValue(
                    text = newTextFromPsi, // Use text from PsiFile
                    selection = TextRange(textFieldValue.selection.start.coerceAtMost(newTextFromPsi.length))
                )
            )
        }
        // If only annotations/structure changed, psiVersion bump in analyze() + LaunchedEffect will handle re-styling.

        locatePSIElement()
        analyticsAreUpToDate = true
    }

    fun run() {
        coroutineScope.launch {
            // Ensure analysis is done and annotations are up-to-date before running
            if (!analyticsAreUpToDate) {
                // Perform a quick analysis or ensure PSI is current if needed
                // This depends on whether 'run' implies needing the absolute latest PSI state
                // For now, we assume 'analyze' button is the way to get latest PSI for globalAnnotations
                SysOut.log("Run called, analytics might be stale. Consider analyzing first.")
            }

            withContext(Dispatchers.Default) {
                val console = project.getOrCreateConsole()
                console.replaceCommand(lang.runConfig.name + " ${Runner.DEFAULT_FILEPATH_ATTR} ${file.path.relativeTo(console.directory)}")
                console.streamln()
                lang.runConfig.run(console, project, file)
                console.streamprompt()

                // After run, PSI might have changed (e.g. if runner modifies files or state)
                // but typically 'analyze' is for code structure. This might not be needed here.
                val psiFile = manager.getPsiFile(file) ?: return@withContext
                withContext(Dispatchers.Main) { psiHasChanged(psiFile) }
            }
        }
    }

    fun analyze() {
        coroutineScope.launch {
            lateinit var psiFile: PsiFile
            val analysisTime = measureTime {
                withContext(Dispatchers.Default) {
                    psiFile = manager.updatePsi(file, SysOut) // Full re-parse/analysis
                    globalAnnotations = PsiService.collectNotations(psiFile) // Update global annotations
                    psiVersion++ // Signal significant PSI structural change
                }
            }
            SysOut.log("Full analysis and global annotation update took $analysisTime")

            withContext(Dispatchers.Main) {
                psiHasChanged(psiFile) // Update UI based on new PSI state
            }
        }
    }

    fun insertCompletion(completion: Completion) { // Largely unchanged
        val currentText = textFieldValue.text
        val selectionStart = textFieldValue.selection.start

        val newTextString = currentText.substring(0, selectionStart) +
                completion.insertion +
                currentText.substring(selectionStart)

        val newSelection = TextRange(selectionStart + completion.insertion.length)

        processTextUpdate(TextFieldValue(text = newTextString, selection = newSelection))
    }

    fun fetchCompletions(showIfPrefixIsEmpty: Boolean = false, onlyHide: Boolean = false) { // Unchanged
        completionOverlayJob?.cancel()
        if (onlyHide) {
            completions = emptyList()
            return
        }
        completionOverlayJob = coroutineScope.launch {
            delay(200)
            val time = measureTime {
                textLayout?.let { layout ->
                    try {
                        val lineIndex = layout.getLineForOffset(textFieldValue.selection.start)
                        val lineStart = layout.getLineStart(lineIndex)
                        val lineContentBefore = textFieldValue.text.substring(lineStart, textFieldValue.selection.start)

                        completions = if (showIfPrefixIsEmpty || lineContentBefore.isNotEmpty()) {
                            lang.psiSupport?.completionProvider?.fetchCompletions(
                                lineContentBefore,
                                currentElement,
                                manager.getPsiFile(file)
                            ) ?: emptyList()
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {
                        SysOut.warn("Completion fetching canceled or failed: ${e.message}")
                        completions = emptyList()
                    }
                } ?: run { completions = emptyList() }
            }
            if (time.inWholeMilliseconds > 5) SysOut.log("fetchCompletions took ${time.inWholeMilliseconds}ms")
        }
    }



    with(LocalDensity.current) {
        BasicTextField(
            modifier = Modifier
                .onGloballyPositioned { textFieldSize = it.size.toSize() }
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {

                        when {
                            !keyEvent.isShiftPressed && !keyEvent.isCtrlPressed && !keyEvent.isAltPressed -> {
                                when (keyEvent.key) {
                                    Key.Tab -> {
                                        val newTfv = if (keyEvent.isShiftPressed) {
                                            if (textFieldValue.selection.length == 0) removeIndentation(textFieldValue, textLayout)
                                            else removeIndentationForSelectedLines(textFieldValue, textLayout)
                                        } else {
                                            if (textFieldValue.selection.length == 0) insertIndentation(textFieldValue)
                                            else insertIndentationForSelectedLines(textFieldValue, textLayout)
                                        }
                                        processTextUpdate(newTfv)
                                        true
                                    }

                                    Key.Enter -> {
                                        val completion = completions.getOrNull(selectedCompletionIndex)
                                        if (isCompletionVisible && completion != null) {
                                            insertCompletion(completion)
                                        } else {
                                            processTextUpdate(insertNewlineAndIndent(textFieldValue, textLayout))
                                        }
                                        true
                                    }

                                    Key.DirectionDown -> {
                                        if (isCompletionVisible && selectedCompletionIndex < completions.size - 1) {
                                            selectedCompletionIndex++; true
                                        } else false
                                    }

                                    Key.DirectionUp -> {
                                        if (isCompletionVisible && selectedCompletionIndex > 0) {
                                            selectedCompletionIndex--; true
                                        } else false
                                    }

                                    else -> false
                                }
                            }

                            keyEvent.isAltPressed && !keyEvent.isCtrlPressed -> {
                                when {
                                    keyEvent.isShiftPressed && keyEvent.key == Key.DirectionUp -> {
                                        processTextUpdate(textFieldValue.moveLines(textLayout, TextEditing.Direction.UP))
                                        true
                                    }

                                    keyEvent.isShiftPressed && keyEvent.key == Key.DirectionDown -> {
                                        processTextUpdate(textFieldValue.moveLines(textLayout, TextEditing.Direction.DOWN))
                                        true
                                    }

                                    else -> false
                                }
                            }

                            keyEvent.isCtrlPressed && !keyEvent.isAltPressed -> { // Simpler Ctrl check
                                when {
                                    keyEvent.key.keyCode == 89L -> { // Cause QWERTY Layout
                                        if (keyEvent.isShiftPressed) { // Ctrl+Shift+Z for Redo
                                            if (textStateModel.canRedo) textStateModel.redo()
                                        } else { // Ctrl+Z for Undo
                                            if (textStateModel.canUndo) textStateModel.undo()
                                        }
                                        true
                                    }

                                    !keyEvent.isShiftPressed && keyEvent.key.keyCode == 90L -> { // Ctrl+Y for Redo
                                        if (textStateModel.canRedo) textStateModel.redo()
                                        true
                                    }

                                    !keyEvent.isShiftPressed && keyEvent.key == Key.Seven -> {
                                        val prefix = lang.psiSupport?.lexerSet?.commentSl
                                        if (prefix != null) {
                                            processTextUpdate(textFieldValue.toggleLineComments(textLayout, prefix))
                                            true
                                        } else false
                                    }

                                    !keyEvent.isShiftPressed && keyEvent.key == Key.D -> {
                                        SysOut.log("Key.D: ${keyEvent.key}")
                                        processTextUpdate(textFieldValue.duplicateSelectionOrLine(textLayout))
                                        true
                                    }

                                    !keyEvent.isShiftPressed && keyEvent.key == Key.S -> {
                                        analyze(); true
                                    }

                                    else -> false
                                }
                            }

                            else -> false
                        }
                    } else false
                },
            value = textFieldValue,
            cursorBrush = SolidColor(theme.COLOR_FG_0),
            textStyle = codeStyle.copy(color = theme.COLOR_FG_0),
            onValueChange = { newTextFieldValue -> processTextUpdate(newTextFieldValue) },
            onTextLayout = { result ->
                textLayout = result
                setLineCount(result.lineCount)
                if (result.lineCount > 0) {
                    setLineHeight(result.multiParagraph.height / result.lineCount)
                } else {
                    setLineHeight(textMeasurer.measure(" ", codeStyle).size.height.toFloat()) // Fallback line height
                }
            }
        ) { textField ->
            Box(modifier.fillMaxSize()) {
                Box(Modifier.matchParentSize().verticalScroll(scrollVertical)) {
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
                                .background(theme.COLOR_SELECTION.copy(alpha = 0.10f))
                        )
                    }

                    Row(Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = scale.SIZE_INSET_MEDIUM)
                                .height(textFieldSize.height.toDp())
                                .width(lineNumberLabelingBounds.width.toDp())
                                .onGloballyPositioned { rowHeaderWidth = it.size.toSize().width }
                        ) {
                            if (lineHeight > 0f) { // Ensure lineHeight is valid
                                repeat(lineCount) { line ->
                                    val thisLineContent = (line + 1).toString()
                                    val thisLineTop = textLayout?.multiParagraph?.getLineTop(line) ?: (lineHeight * line)
                                    Text(
                                        modifier = Modifier
                                            .width(lineNumberLabelingBounds.width.toDp())
                                            .height(lineHeight.toDp())
                                            .offset(y = thisLineTop.toDp() + (lineHeight.toDp() - lineNumberLabelingBounds.height.toDp()) / 2),
                                        textAlign = TextAlign.Right,
                                        text = thisLineContent,
                                        color = theme.COLOR_FG_1,
                                        style = codeSmallStyle
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(textFieldSize.height.toDp()).width(scale.SIZE_BORDER_THICKNESS))
                        Box(Modifier.fillMaxWidth().horizontalScroll(scrollHorizontal)) {
                            textLayout?.let { layout ->

                                Canvas(Modifier.size(layout.size.toSize().toDpSize())) {
                                    if (references.isNotEmpty()) {
                                        references.firstOrNull()?.reference?.let { rootRef ->
                                            val rootRange = rootRef.range
                                            val rootPath = layout.getPathForRange(rootRange.first, rootRange.last + 1)
                                            drawPath(rootPath, theme.COLOR_SEARCH_RESULT, style = Fill)
                                            references.mapNotNull { it.reference }.forEach { ref ->
                                                val range = ref.range
                                                val path = layout.getPathForRange(range.first, range.last + 1)
                                                drawPath(path, theme.COLOR_SEARCH_RESULT, style = Fill)
                                            }
                                        }
                                    }

                                    currentElement?.let { element ->
                                        val elementRange = element.range
                                        val elementPath = layout.getPathForRange(elementRange.first, elementRange.last + 1)
                                        drawPath(elementPath, theme.COLOR_FG_1, style = Stroke(0.5f))
                                    }
                                }
                            }
                            Box(
                                Modifier.pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.type == PointerEventType.Move) {
                                                val position = event.changes.first().position
                                                hoverPosition = null // Clear first
                                                annotationOverlayJob?.cancel()
                                                annotationOverlayJob = coroutineScope.launch {
                                                    delay(500)
                                                    hoverPosition = position
                                                }
                                            } else if (event.type == PointerEventType.Exit) {
                                                annotationOverlayJob?.cancel()
                                                hoverPosition = null
                                            }
                                        }
                                    }
                                }
                            ) {
                                textField()
                            }
                            if (isCompletionVisible) {
                                CompletionOverlay(
                                    Modifier.offset(x = caretOffset.x.toDp(), y = caretOffset.y.toDp()),
                                    codeStyle, completions, selectedCompletionIndex
                                )
                            }
                            if (isAnnotationVisible) {
                                hoverPosition?.let {
                                    AnnotationOverlay(
                                        Modifier.offset(it.x.toDp(), it.y.toDp()),
                                        codeStyle, localAnnotations
                                    )
                                }
                            }
                        }
                    }
                }

                CornerLayout(
                    north = {
                        Row(
                            Modifier.padding(scale.SIZE_INSET_SMALL).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (analyticsAreUpToDate) {
                                CButton(icon = icon.chevronRight, iconType = IconType.SMALL, onClick = { run() })
                                Spacer(Modifier.width(scale.SIZE_INSET_SMALL))
                                val errors = globalAnnotations.count { it.severity == Severity.ERROR }
                                val warnings = globalAnnotations.count { it.severity == Severity.WARNING }
                                val infos = globalAnnotations.count { it.severity == Severity.INFO }
                                CButton(icon = icon.statusFine, text = "$infos", onClick = {
                                    globalAnnotations.firstOrNull { it.severity == Severity.INFO }?.let { scrollToIndex(it.range.first) }
                                }, iconType = IconType.SMALL, textStyle = baseSmallStyle, iconTint = theme.COLOR_GREEN)
                                Spacer(Modifier.width(scale.SIZE_INSET_SMALL))
                                CButton(icon = icon.info, text = "$warnings", onClick = {
                                    globalAnnotations.firstOrNull { it.severity == Severity.WARNING }?.let { scrollToIndex(it.range.first) }
                                }, iconType = IconType.SMALL, textStyle = baseSmallStyle, iconTint = theme.COLOR_YELLOW)
                                Spacer(Modifier.width(scale.SIZE_INSET_SMALL))
                                CButton(icon = icon.statusError, text = "$errors", onClick = {
                                    globalAnnotations.firstOrNull { it.severity == Severity.ERROR }?.let { scrollToIndex(it.range.first) }
                                }, iconType = IconType.SMALL, textStyle = baseSmallStyle, iconTint = theme.COLOR_RED)
                            } else {
                                ComposeTools.Rotating { rotation ->
                                    CButton(icon = icon.chevronRight, iconType = IconType.SMALL, onClick = { run() })
                                    Spacer(Modifier.width(scale.SIZE_INSET_SMALL))
                                    Text("CTRL+SHIFT+S to analyze", style = baseSmallStyle, color = theme.COLOR_FG_0)
                                    Icon(icon.statusLoading, "loading", Modifier.size(scale.SIZE_CONTROL_SMALL).rotate(rotation), tint = theme.COLOR_FG_0)
                                    Spacer(Modifier.width(scale.SIZE_INSET_SMALL))
                                    CButton(icon = icon.build, iconType = IconType.SMALL, onClick = { analyze() })
                                }
                            }
                        }
                    },
                    east = {
                        CVerticalScrollBar(scrollVertical) {
                            Canvas(modifier.fillMaxHeight().width(UIState.Scale.value.SIZE_CONTROL_SMALL)) {
                                textLayout?.let { layout ->
                                    val totalHeight = layout.size.height.toFloat()
                                    if (totalHeight == 0f) return@Canvas

                                    globalAnnotations.forEach { annotation ->
                                        val range = annotation.range
                                        val line = layout.getLineForOffset(range.first)
                                        val ratioTop = layout.getLineTop(line) / totalHeight
                                        val ratioBottom = layout.getLineBottom(line) / totalHeight
                                        val ratioHeight = (ratioBottom - ratioTop).coerceAtLeast(0.001f) // Min height for visibility

                                        drawRect(
                                            theme.getColor(annotation.severity.color),
                                            topLeft = Offset(0f, ratioTop * size.height),
                                            size = Size(size.width, ratioHeight * size.height), style = Fill
                                        )
                                    }
                                    references.firstOrNull()?.reference?.let { rootRef ->
                                        val rootRange = rootRef.range
                                        val rootLine = layout.getLineForOffset(rootRange.first)
                                        val rootRatioTop = layout.getLineTop(rootLine) / totalHeight
                                        val rootRatioBottom = layout.getLineBottom(rootLine) / totalHeight
                                        val rootRatioHeight = (rootRatioBottom - rootRatioTop).coerceAtLeast(0.001f)
                                        drawRect(theme.COLOR_SEARCH_RESULT, topLeft = Offset(0f, rootRatioTop * size.height), size = Size(size.width, rootRatioHeight * size.height), style = Fill)

                                        references.mapNotNull { it.reference }.forEach { ref ->
                                            val range = ref.range
                                            val line = layout.getLineForOffset(range.first)
                                            val ratioTop = layout.getLineTop(line) / totalHeight
                                            val ratioBottom = layout.getLineBottom(line) / totalHeight
                                            val ratioHeight = (ratioBottom - ratioTop).coerceAtLeast(0.001f)
                                            drawRect(theme.COLOR_SEARCH_RESULT, topLeft = Offset(0f, ratioTop * size.height), size = Size(size.width, ratioHeight * size.height), style = Fill)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    south = {
                        CHorizontalScrollBar(scrollHorizontal) {
                            Column {
                                Row(
                                    Modifier.padding(scale.SIZE_INSET_SMALL).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    val selection = textFieldValue.selection
                                    val line = textLayout?.getLineForOffset(selection.start)?.plus(1) ?: 0
                                    val colOffset = textLayout?.getLineStart(line - 1) ?: 0
                                    val column = selection.start - colOffset + 1


                                    Text("$line:$column (${selection.start})", style = codeStyle.copy(fontSize = codeStyle.fontSize * 0.8f, color = theme.COLOR_FG_1))
                                }
                                Spacer(Modifier.height(scale.SIZE_SCROLL_THUMB))
                            }
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(textStateModel) {
        textStateModel.onStateUpdate = {
            processTextUpdate(it, isUndoRedoOperation = true)
        }
    }

    // Debounced file save
    LaunchedEffect(textFieldValue.text, file) {
        scheduleSave(textFieldValue.text, file, coroutineScope)
    }

    // Update caret offset, locate PSI element, and fetch completions when text field value changes
    LaunchedEffect(textFieldValue) {
        val time = measureTime {
            textLayout?.let { layout ->
                caretOffset = layout.getCursorRect(textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)).bottomCenter
            }
            locatePSIElement() // Depends on selection and PSI state
            fetchCompletions() // Depends on text and PSI element
        }
        if (time.inWholeMilliseconds > 5) SysOut.log("LaunchedEffect(textFieldValue) for caret/PSI/completion took ${time.inWholeMilliseconds}ms")
    }


    // Update visibleIndexRange based on scroll
    LaunchedEffect(scrollVertical.value, textLayout, scrollVertical.viewportSize) {
        textLayout?.let { layout ->
            val preload = scrollVertical.viewportSize / 2
            val first = layout.getOffsetForPosition(Offset(0f, (scrollVertical.value - preload).coerceAtLeast(0).toFloat()))
            val last = layout.getOffsetForPosition(Offset(0f, (scrollVertical.value + scrollVertical.viewportSize + preload).toFloat()))
            val newRange = first..<last.coerceAtMost(textFieldValue.text.length)
            if (newRange != visibleIndexRange && !newRange.isEmpty()) {
                visibleIndexRange = newRange
            } else if (visibleIndexRange.last > textFieldValue.text.length) { // Adjust if text got shorter
                visibleIndexRange = visibleIndexRange.first..<textFieldValue.text.length
            }
        }
    }

    // This LaunchedEffect handles re-highlighting when scrolling (visibleIndexRange changes)
    // or after a full analysis (psiVersion changes), assuming text content hasn't changed.
    LaunchedEffect(visibleIndexRange, psiVersion) {
        // Don't run if onTextChange is likely already handling it due to text also changing.
        // This effect is for when ONLY visible range or PSI version changed.
        if (textFieldValue.text.isEmpty() || textLayout == null) return@LaunchedEffect

        highlightJob?.cancel()
        highlightJob = coroutineScope.launch { // Still use a coroutine for cancellation logic
            val currentText = textFieldValue.text // Capture current text

            // Potentially heavy work, can be on Default dispatcher if fetchStyledContent is not trivial
            val styledAnnotatedString = withContext(Dispatchers.Default) {
                val currentPsiFile = manager.getPsiFile(file)
                fetchStyledContent(currentText, currentPsiFile, visibleIndexRange)
            }

            if (isActive) { // Ensure job wasn't cancelled
                withContext(Dispatchers.Main) {
                    // Only update if the text is still the same as when we started.
                    // This avoids race conditions with onTextChange.
                    if (currentText == textFieldValue.text) {
                        textFieldValue = textFieldValue.copy(annotatedString = styledAnnotatedString)
                    } else {
                        SysOut.log("Highlighting (scroll/analyze) is stale, skipping update.")
                    }
                }
            }
        }
    }


    LaunchedEffect(lineCount) {
        val time = measureTime {
            if (lineCount > 0) {
                lineNumberLabelingBounds = textMeasurer.measure(lineCount.toString(), codeSmallStyle).size.toSize()
            } else {
                lineNumberLabelingBounds = textMeasurer.measure("1", codeSmallStyle).size.toSize()
            }
        }
        if (time.inWholeMilliseconds > 5) SysOut.log("LaunchedEffect(lineCount) for bounds took ${time.inWholeMilliseconds}ms")
    }

    LaunchedEffect(completions) {
        selectedCompletionIndex = 0
    }

    LaunchedEffect(hoverPosition) {
        val time = measureTime {
            isAnnotationVisible = false // Hide first
            localAnnotations = emptySet()

            hoverPosition?.let { currentHoverPos ->
                val inCodePosition = Offset(currentHoverPos.x - rowHeaderWidth, currentHoverPos.y + scrollVertical.value) // Adjust for scroll
                textLayout?.getOffsetForPosition(inCodePosition)?.let { index ->
                    manager.getPsiFile(file)?.let { psiFile ->
                        localAnnotations = PsiService.collectNotations(psiFile, index..index)
                    }
                }
            }
        }
        // isAnnotationVisible will update automatically via derivedStateOf if you choose to implement it,
        // otherwise this manual update is fine if localAnnotations directly controls visibility.
        // For now, let's make it explicit, similar to completions.
        isAnnotationVisible = localAnnotations.isNotEmpty()
        if (time.inWholeMilliseconds > 5) SysOut.log("LaunchedEffect(hoverPosition) for local annotations took ${time.inWholeMilliseconds}ms")
    }

    // Log global annotations when they change (e.g., after analysis)
    LaunchedEffect(globalAnnotations) {
        val time = measureTime {
            manager.getPsiFile(file)?.let { psiFile ->
                globalAnnotations.forEach { annotation ->
                    SysOut.log(annotation.createConsoleMessage(psiFile))
                }
            }
        }
        if (time.inWholeMilliseconds > 5) SysOut.log("LaunchedEffect(globalAnnotations) for logging took ${time.inWholeMilliseconds}ms")
    }

    LaunchedEffect(currentElement) {
        val element = currentElement
        if (element == null) {
            references = emptyList()
        } else {
            val root = if (element is PsiReference<*>) element.reference else element
            references = if (root == null) emptyList() else PsiService.findReferences(root)
            onElementSelected(file, PsiService.path(element))
        }
    }

    LaunchedEffect(file, manager) { // Initial load (largely unchanged)
        coroutineScope.launch(Dispatchers.Default) {
            var initialPsiVersionBump = false
            manager.getPsiFile(file)?.let { psiFile ->
                val initialAnnotations = PsiService.collectNotations(psiFile)
                if (initialAnnotations.isNotEmpty()) {
                    globalAnnotations = initialAnnotations
                    initialPsiVersionBump = true // Mark that psiVersion should be incremented
                    analyticsAreUpToDate = true
                }
            }
            // Always do an initial style pass
            val currentPsiFile = manager.getPsiFile(file) // Re-fetch, might have been created by getPsiFile above
            val initialStyledText = fetchStyledContent(textFieldValue.text, currentPsiFile, visibleIndexRange)

            withContext(Dispatchers.Main) {
                textFieldValue = textFieldValue.copy(annotatedString = initialStyledText)
                if (initialPsiVersionBump) {
                    psiVersion++ // Increment after UI update
                }
            }
        }
    }
}



