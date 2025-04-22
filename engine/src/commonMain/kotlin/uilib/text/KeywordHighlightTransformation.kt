package uilib.text

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
    vararg keywordStyleMap: Pair<List<String>, SpanStyle>
) : VisualTransformation {
    private val regexStyles: List<Pair<Regex, SpanStyle>> = keywordStyleMap.map {
        val pattern = "\\b${it.first.joinToString("|")}\\b"
         pattern.toRegex() to it.second
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = buildAnnotatedString {

            append(text) // Start by adding all the text

            regexStyles.forEach { (regex, spanStyle) ->
                regex.findAll(text.text).forEach { matchResult ->
                    // Add a style span for each match
                    addStyle(
                        style = spanStyle,
                        start = matchResult.range.first,
                        end = matchResult.range.last + 1,
                    )
                }
            }
        }

        // IdentityOffsetMapping because we don't change text length.
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}