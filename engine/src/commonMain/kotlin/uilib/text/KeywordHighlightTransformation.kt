package uilib.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle

/**
 * A custom VisualTransformation that highlights keywords efficiently using a single combined regex.
 * It resolves overlaps by prioritizing longer matches.
 *
 * This version is optimized for speed compared to applying regex patterns sequentially.
 */
class KeywordHighlightTransformation(
    vararg keywordStyleMap: Pair<List<String>, SpanStyle>
) : VisualTransformation {

    private val combinedRegex: Regex
    private val groupIndexToStyleMap: Map<Int, SpanStyle>

    init {
        val groupStyles = mutableListOf<SpanStyle>()
        var groupIndex = 1 // Regex group indices start from 1
        val patternParts = mutableListOf<String>()
        val tempGroupIndexToStyleMap = mutableMapOf<Int, SpanStyle>()

        keywordStyleMap.forEach { (keywords, style) ->
            if (keywords.isNotEmpty()) {
                // Escape keywords and join with |
                val groupPattern = keywords
                    .filter { it.isNotEmpty() } // Avoid empty keywords
                    .joinToString("|") { Regex.escape(it) }

                if (groupPattern.isNotEmpty()) {
                    // Add capturing parentheses for this group
                    patternParts.add("($groupPattern)")
                    // Map the current group index to its style
                    tempGroupIndexToStyleMap[groupIndex] = style
                    groupStyles.add(style) // Keep track of styles in order (alternative mapping)
                    groupIndex++
                }
            }
        }

        groupIndexToStyleMap = tempGroupIndexToStyleMap

        // Combine all group patterns with | inside a non-capturing group for word boundaries
        // Using \b ensures we match whole words. Remove if partial matches are desired.
        val combinedPattern = if (patternParts.isNotEmpty()) {
            "\\b(?:${patternParts.joinToString("|")})\\b"
        } else {
            // Create a regex that never matches if there are no keywords
            "$^" // Matches absolutely nothing
        }

        // Consider RegexOption.IGNORE_CASE if case-insensitivity is needed
        combinedRegex = combinedPattern.toRegex()
    }


    // Data class to hold match information (same as before)
    private data class MatchInfo(val start: Int, val end: Int, val style: SpanStyle) : Comparable<MatchInfo> {
        val length: Int get() = end - start

        override fun compareTo(other: MatchInfo): Int {
            val startCompare = start.compareTo(other.start)
            return if (startCompare == 0) {
                other.length.compareTo(length) // Descending length for same start
            } else {
                startCompare // Ascending start index
            }
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        // Early exit for empty text or if no keywords were provided
        if (text.text.isEmpty() || groupIndexToStyleMap.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val sourceText = text.text
        // Use ArrayList for potential minor performance gains, though List is fine
        val allMatches = ArrayList<MatchInfo>()

        // 1. Find all matches using the single combined regex
        combinedRegex.findAll(sourceText).forEach { matchResult ->
            // Find which capturing group matched to determine the style
            var matchedGroupIndex = -1
            // Iterate group indices (start from 1)
            for (i in 1 until matchResult.groups.size) {
                // Check if group exists and matched something non-null
                // Note: matchResult.groups[i] can be null even if the group index is valid
                if (matchResult.groups[i] != null) {
                    // Check if this index corresponds to one of our keyword groups
                    if (groupIndexToStyleMap.containsKey(i)) {
                        matchedGroupIndex = i
                        break // Found the matching group
                    }
                }
            }

            if (matchedGroupIndex != -1) {
                // Retrieve the style associated with the matched group
                val style = groupIndexToStyleMap.getValue(matchedGroupIndex)
                allMatches.add(
                    MatchInfo(
                        start = matchResult.range.first,
                        end = matchResult.range.last + 1,
                        style = style
                    )
                )
            }
        }

        // If no matches found, return original text transformed
        if (allMatches.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // 2. Sort matches: by start index ascending, then by length descending (same as before)
        allMatches.sort()

        // 3. Filter out overlapping matches (same logic as before)
        val filteredMatches = ArrayList<MatchInfo>(allMatches.size) // Preallocate capacity
        var lastStyledEnd = -1

        for (match in allMatches) {
            if (match.start >= lastStyledEnd) {
                filteredMatches.add(match)
                lastStyledEnd = match.end
            }
        }

        // 4. Build the AnnotatedString (same logic as before)
        val annotated = buildAnnotatedString {
            var currentIndex = 0
            filteredMatches.forEach { match ->
                if (match.start > currentIndex) {
                    append(sourceText.substring(currentIndex, match.start))
                }
                withStyle(style = match.style) {
                    append(sourceText.substring(match.start, match.end))
                }
                currentIndex = match.end
            }
            if (currentIndex < sourceText.length) {
                append(sourceText.substring(currentIndex))
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }
}