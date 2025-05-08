package ikr.prosim.ui.ide.editor

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import cengine.console.SysOut

object TextEditing {

    fun TextFieldValue.toggleLineComments(
        layoutResult: TextLayoutResult?,
        lineCommentPrefix: String,
    ): TextFieldValue {
        if (layoutResult == null) return this

        val originalText = this.text
        val originalSelection = this.selection

        val newTextBuilder = StringBuilder(originalText)

        val startLineIndex = layoutResult.getLineForOffset(originalSelection.min)
        val endLineIndex = layoutResult.getLineForOffset(originalSelection.max)

        val linesToProcessInfo = (startLineIndex..endLineIndex).map { lineIdx ->
            val lineStart = layoutResult.getLineStart(lineIdx)
            val lineEnd = layoutResult.getLineEnd(lineIdx) // End of content, excluding newline
            val lineContent = originalText.substring(lineStart, lineEnd)
            val leadingWhitespaceLength = lineContent.takeWhile { it.isWhitespace() }.length

            object {
                val lineStart = lineStart
                val isEffectivelyEmpty = lineContent.trim().isEmpty()
                val startsWithCommentPrefix = lineContent.startsWith(lineCommentPrefix, leadingWhitespaceLength)
                val origContentStartOff = lineStart + leadingWhitespaceLength
            }
        }

        val shouldUncomment = linesToProcessInfo.filterNot { it.isEffectivelyEmpty }
            .all { it.startsWithCommentPrefix }

        var editoffset = 0
        for (line in linesToProcessInfo) {
            if (line.isEffectivelyEmpty) continue
            if (shouldUncomment) {
                if (line.startsWithCommentPrefix) {
                    newTextBuilder.deleteRange(line.lineStart + editoffset, line.origContentStartOff + editoffset + lineCommentPrefix.length)
                    editoffset -= lineCommentPrefix.length + line.origContentStartOff - line.lineStart
                }
            } else {
                newTextBuilder.insert(line.origContentStartOff + editoffset, "$lineCommentPrefix ")
                editoffset += lineCommentPrefix.length + 1
            }
        }

        val adjustedSelectionMin = originalSelection.min
        val adjustedSelectionMax = originalSelection.max + editoffset

        return this.copy(
            text = newTextBuilder.toString(),
            selection = TextRange(adjustedSelectionMin, adjustedSelectionMax)
        )
    }

    fun TextFieldValue.duplicateSelectionOrLine(
        layoutResult: TextLayoutResult?,
    ): TextFieldValue {
        // This function's logic appears mostly sound.
        // Clarification: When selection is not collapsed and layoutResult is null,
        // it duplicates the selected text and places the selection AFTER the duplicated part.
        if (layoutResult == null && !selection.collapsed) {
            val selectedTextToDuplicate = text.substring(selection.min, selection.max)
            val newTextContent = StringBuilder(text).insert(selection.max, selectedTextToDuplicate).toString()
            return TextFieldValue(
                text = newTextContent,
                // Selection is placed after the newly inserted duplicated text,
                // effectively at the end of the (original selection + duplicated part).
                selection = TextRange(selection.max + selectedTextToDuplicate.length)
            )
        }
        if (layoutResult == null) return this // No layout and collapsed selection, do nothing

        val textContent = this.text
        val currentSelection = this.selection
        val newTextBuilder = StringBuilder(textContent)

        val textToDuplicate: String
        val finalSelection: TextRange

        if (currentSelection.collapsed) {
            val currentLineNum = layoutResult.getLineForOffset(currentSelection.start)
            val lineStartOffset = layoutResult.getLineStart(currentLineNum)
            // Get line including its newline character for a full line duplication
            val lineEndOffsetWithNewline = layoutResult.getLineEnd(currentLineNum)

            textToDuplicate = "\n" + textContent.substring(lineStartOffset, lineEndOffsetWithNewline)
            newTextBuilder.insert(lineEndOffsetWithNewline, textToDuplicate)

            // Place cursor at the same relative position in the newly duplicated line
            val newCursorPos = currentSelection.start + textToDuplicate.length
            finalSelection = TextRange(newCursorPos)
        } else {
            textToDuplicate = textContent.substring(currentSelection.min, currentSelection.max)

            newTextBuilder.insert(currentSelection.max, textToDuplicate)
            // Select the newly inserted duplicated text
            finalSelection = TextRange(currentSelection.max, currentSelection.max + textToDuplicate.length)
        }

        return TextFieldValue(
            text = newTextBuilder.toString(),
            selection = finalSelection
        )
    }

    enum class Direction { UP, DOWN }

    fun TextFieldValue.moveLines(
        layoutResult: TextLayoutResult?,
        direction: Direction,
    ): TextFieldValue {
        if (layoutResult == null) return this

        val originalFullText = this.text
        val currentSelection = this.selection

        val startLineNum = layoutResult.getLineForOffset(currentSelection.min)
        val endLineNum = layoutResult.getLineForOffset(currentSelection.max)

        // Determine the block of selected lines, including the newline of the last line in the block
        val selectedBlockStartOffset = layoutResult.getLineStart(startLineNum)
        val selectedBlockEndOffset = layoutResult.getLineEnd(endLineNum)
        val selectedBlockText = originalFullText.substring(selectedBlockStartOffset, selectedBlockEndOffset + 1)

        val newTextBuilder = StringBuilder(originalFullText)
        var newSelectionStart = currentSelection.start
        var newSelectionEnd = currentSelection.end

        if (direction == Direction.UP) {
            if (startLineNum == 0) return this // Already at the first line

            val prevLineNum = startLineNum - 1
            val prevLineStartOffset = layoutResult.getLineStart(prevLineNum)
            val prevLineEndOffset = layoutResult.getLineEnd(prevLineNum)
            val prevLineText = originalFullText.substring(prevLineStartOffset, prevLineEndOffset + 1)
            val prevLine = if (startLineNum == layoutResult.lineCount - 1) prevLineText.removeSuffix("\n") else prevLineText
            val selectedBlock = if (startLineNum == layoutResult.lineCount - 1) selectedBlockText + "\n" else selectedBlockText
            SysOut.info("Line: ${selectedBlock.replace("\n", "\\n")}")
            SysOut.info("PrevLine: ${prevLine.replace("\n", "\\n")}")

            // Order of removal is important for StringBuilder if offsets are not pre-adjusted
            // Remove selected block first (it's below the prevLine block)
            newTextBuilder.deleteRange(selectedBlockStartOffset, selectedBlockEndOffset + 1)
            // Then remove the previous line block (its original offset is still valid)
            newTextBuilder.deleteRange(prevLineStartOffset, prevLineEndOffset + 1)

            // Insert selected block at the original start of the previous line
            newTextBuilder.insert(prevLineStartOffset, selectedBlock)
            // Then insert the previous line block after the moved selected block
            newTextBuilder.insert(prevLineStartOffset + selectedBlock.length, prevLine)

            val distanceMoved = prevLineText.length
            newSelectionStart -= distanceMoved
            newSelectionEnd -= distanceMoved

        } else { // Direction.DOWN
            if (endLineNum >= layoutResult.lineCount - 1) return this // Already at the last line

            val nextLineNum = endLineNum + 1
            val nextLineStartOffset = layoutResult.getLineStart(nextLineNum)
            val nextLineEndOffset = layoutResult.getLineEnd(nextLineNum)
            val nextLineText = originalFullText.substring(nextLineStartOffset, nextLineEndOffset + 1)
            val nextLine = if (nextLineNum == layoutResult.lineCount - 1) nextLineText + "\n" else nextLineText
            val selectedBlock = if (nextLineNum == layoutResult.lineCount - 1) selectedBlockText.removeSuffix("\n") else selectedBlockText
            SysOut.info("Line: ${selectedBlock.replace("\n", "\\n")}")
            SysOut.info("NextLine: ${nextLine.replace("\n", "\\n")}")

            // Order of removal for moving down:
            // Remove the next line block first (it's below the selected block)
            newTextBuilder.deleteRange(nextLineStartOffset, nextLineEndOffset + 1)
            // Then remove the selected block (its original offset is still valid)
            newTextBuilder.deleteRange(selectedBlockStartOffset, selectedBlockEndOffset + 1)

            // Insert the next line block at the original start of the selected lines
            newTextBuilder.insert(selectedBlockStartOffset, nextLine)
            // Then insert the selected block after the moved next line
            newTextBuilder.insert(selectedBlockStartOffset + nextLine.length, selectedBlock)

            val distanceMoved = nextLine.length
            newSelectionStart += distanceMoved
            newSelectionEnd += distanceMoved
        }

        val finalNewText = newTextBuilder.toString()
        // Coerce selection to be within bounds of the new text
        newSelectionStart = newSelectionStart.coerceIn(0, finalNewText.length)
        newSelectionEnd = newSelectionEnd.coerceIn(0, finalNewText.length)
        // Ensure start <= end
        if (newSelectionStart > newSelectionEnd) {
            if (currentSelection.collapsed) newSelectionEnd = newSelectionStart
            else newSelectionStart = newSelectionEnd // or any other policy for inverted selection
        }


        return TextFieldValue(
            text = finalNewText,
            selection = TextRange(newSelectionStart, newSelectionEnd)
        )
    }
}