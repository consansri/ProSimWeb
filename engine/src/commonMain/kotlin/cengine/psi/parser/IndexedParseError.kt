package cengine.psi.parser

/**
 * Data class to hold processed error information, including the starting token index.
 * Assumes this information is available from the ParseResult or can be derived.
 */
data class IndexedParseError(
    val message: String,
    val characterRange: IntRange,
    val startTokenIndex: Int // The token index where the error condition begins
)