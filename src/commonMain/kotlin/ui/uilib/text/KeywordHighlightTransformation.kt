package ui.uilib.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A custom VisualTransformation that highlights keywords.
 * It builds an AnnotatedString where keywords are styled with [keywordStyle].
 */
class KeywordHighlightTransformation(
    private val keywords: List<String>,
    private val keywordStyle: SpanStyle = SpanStyle()
) : VisualTransformation {

    private val regex = "\\b(${keywords.joinToString("|")})\\b".toRegex()

    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = buildAnnotatedString {
            // Split text into words using a simple regex. This can be improved for more robust matching.
            var lastIndex = 0
            regex.findAll(text.text).forEach { result ->
                // Append text before the keyword.
                append(text.text.substring(lastIndex, result.range.first))
                // Append the keyword with highlighting.
                pushStyle(keywordStyle)
                append(result.value)
                pop()
                lastIndex = result.range.last + 1
            }
            // Append any remaining text.
            if (lastIndex < text.length) {
                append(text.text.substring(lastIndex, text.length))
            }
        }
        // IdentityOffsetMapping because we don't change text length.
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}