package ui.ide.editor

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun insertIndentationForSelectedLines(value: TextFieldValue, layoutResult: TextLayoutResult?): TextFieldValue {
    val indent = "    " // 4 spaces or use "\t" for tab-based indentation
    val startOffset = value.selection.min
    val endOffset = value.selection.max

    val startLine = layoutResult?.getLineForOffset(startOffset) ?: 0
    val endLine = layoutResult?.getLineForOffset(endOffset) ?: 0

    val lines = value.text.lines().toMutableList()

    for (lineIndex in startLine..endLine) {
        val lineStart = layoutResult?.getLineStart(lineIndex) ?: 0
        lines[lineIndex] = indent + lines[lineIndex]
    }

    val newText = lines.joinToString("\n")
    val newSelection = TextRange(
        start = startOffset + indent.length,
        end = endOffset + (endLine - startLine + 1) * indent.length
    )

    return value.copy(text = newText, selection = newSelection)
}

fun removeIndentationForSelectedLines(value: TextFieldValue, layoutResult: TextLayoutResult?): TextFieldValue {
    val indent = "    " // 4 spaces or "\t"
    val startOffset = value.selection.min
    val endOffset = value.selection.max

    val startLine = layoutResult?.getLineForOffset(startOffset) ?: 0
    val endLine = layoutResult?.getLineForOffset(endOffset) ?: 0

    val lines = value.text.lines().toMutableList()

    for (lineIndex in startLine..endLine) {
        val lineStart = layoutResult?.getLineStart(lineIndex) ?: 0
        if (lines[lineIndex].startsWith(indent)) {
            lines[lineIndex] = lines[lineIndex].removePrefix(indent)
        }
    }

    val newText = lines.joinToString("\n")
    val newSelection = TextRange(
        start = startOffset - indent.length.coerceAtLeast(0),
        end = endOffset - ((endLine - startLine + 1) * indent.length).coerceAtLeast(0)
    )

    return value.copy(text = newText, selection = newSelection)
}

fun insertIndentation(value: TextFieldValue): TextFieldValue {
    // Insert 4 spaces (or tab character, depending on your preference)
    val indent = "    "  // You can replace with "\t" for tabs
    val newText = value.text.substring(0, value.selection.start) +
            indent +
            value.text.substring(value.selection.start)

    // Move the caret after the indent
    val newSelection = TextRange(start = value.selection.start + indent.length, end = value.selection.end + indent.length)

    return value.copy(text = newText, selection = newSelection)
}

fun insertNewlineAndIndent(value: TextFieldValue, layoutResult: TextLayoutResult?): TextFieldValue {
    // Find the line the caret is on and replicate its indentation
    val caretPosition = value.selection.start
    val currentLineIndex = layoutResult?.getLineForOffset(caretPosition) ?: 0
    val currentLineStart = layoutResult?.getLineStart(currentLineIndex) ?: 0
    val currentLineText = value.text.substring(currentLineStart, caretPosition)

    // Extract the leading whitespace (indentation) from the current line
    val leadingWhitespace = currentLineText.takeWhile { it.isWhitespace() }

    // Insert newline + leading whitespace
    val newText = value.text.substring(0, caretPosition) +
            "\n" +
            leadingWhitespace +
            value.text.substring(caretPosition)

    // Move the caret after the newline and indentation
    val newCaretPosition = caretPosition + 1 + leadingWhitespace.length

    val newSelection = TextRange(newCaretPosition, newCaretPosition)

    return value.copy(text = newText, selection = newSelection)
}

fun removeIndentation(value: TextFieldValue, layoutResult: TextLayoutResult?): TextFieldValue {
    val caretPosition = value.selection.start
    val currentLineIndex = layoutResult?.getLineForOffset(caretPosition) ?: 0
    val currentLineStart = layoutResult?.getLineStart(currentLineIndex) ?: 0
    val currentLineText = value.text.substring(currentLineStart, caretPosition)

    // Detect leading whitespace and remove 4 spaces or a tab
    val indent = "    " // or "\t"
    val leadingWhitespace = currentLineText.takeWhile { it.isWhitespace() }

    val newText = if (leadingWhitespace.endsWith(indent)) {
        value.text.substring(0, currentLineStart + leadingWhitespace.length - indent.length) +
                value.text.substring(currentLineStart + leadingWhitespace.length)
    } else {
        value.text
    }

    // Adjust caret position
    val newCaretPosition = caretPosition - indent.length
    val newSelection = TextRange(start = newCaretPosition.coerceAtLeast(currentLineStart), end = newCaretPosition.coerceAtLeast(currentLineStart))

    return value.copy(text = newText, selection = newSelection)
}