package cengine.editor.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

interface HighlightProvider {

    /**
     * Should only use a fast lexical analysis to determine the highlighting.
     */
    fun fastHighlight(text: String, inRange: IntRange = text.indices): List<HLInfo>

    companion object {
        fun List<HLInfo>.spanStyles() = mapNotNull {
            if (!it.range.isEmpty()) {
                AnnotatedString.Range(SpanStyle(color = Color(it.color or 0xFF000000.toInt())), it.range.first, it.range.last + 1)
            } else null
        }
    }

}