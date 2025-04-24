package cengine.psi.lexer

import kotlin.math.min

/**
 * A generic lexer that tokenizes a source string based on a provided [PsiLexerSet].
 * This lexer supports various token types, including keywords, identifiers, literals (numbers, characters, strings),
 * operators, punctuations, and comments. It also handles string interpolation and escape sequences.
 *
 * @param source The input source string to be tokenized.
 * @param set The [PsiLexerSet] configuration that defines the language-specific rules for tokenization.
 */
class PsiLexer(private val source: String, private val set: PsiLexerSet, private val inRange: IntRange? = null) {

    private var position: Int = inRange?.first ?: 0   // Current position in the source string
    private var line: Int = 0       // Current line number.
    private var column: Int = 0     // Current column number.

    /**
     * Returns the character at the current [position] or null if the position
     * is outside the source length or the specified [inRange].
     */
    private fun peek(): Char? {
        // Determine the maximum valid index we are allowed to peek at.
        // It's the minimum of the source's last index and the range's last index (if specified).
        val effectiveLastIndex = if (inRange != null) {
            // Cannot peek beyond the source string's actual end, even if range is larger.
            // Also respect the range's upper bound.
            min(inRange.last, source.length - 1)
        } else {
            // No specific range, just limited by the source string length.
            source.length - 1
        }

        // Determine the minimum valid index we are allowed to peek at.
        val effectiveFirstIndex = inRange?.first ?: 0

        // Check if the current position is within the allowed bounds [effectiveFirstIndex, effectiveLastIndex].
        // Also ensure position is non-negative as a safety measure.
        return if (position in effectiveFirstIndex..effectiveLastIndex) {
            // Position is valid and within allowed bounds. Access is safe.
            source[position] // This should correspond to the previous line 22
        } else {
            // Position is out of bounds (either < first, > last, or >= source.length).
            null
        }
    }

    /**
     * Extension function to check if the next characters in the source match a given [CharSequence].
     */
    private fun CharSequence?.isNext(ignoreCase: Boolean = false): Boolean {
        if (this == null || peek() == null) return false
        val maxPos = inRange?.last ?: (source.length - 1)
        // Ensure we don't check beyond the allowed range or source length
        return if (position + this.length > (maxPos + 1)) false
        else source.startsWith(this, position, ignoreCase)
    }

    /**
     * Extension function to check if the next character in the source matches a given [Char].
     */
    private fun Char?.isNext(): Boolean = if (this == null) false else peek() == this


    /**
     * Extension function to check if any of the [CharSequence]s in a [Set] match the next characters in the source.
     */
    private fun Set<String>.isNext(ignoreCase: Boolean = false): Boolean = if (peek() == null) false else any { it.isNext(ignoreCase) } // Use the range-aware isNext

    /**
     * Extension function to find the first [CharSequence] in a [Set] that matches the next characters in the source.
     * Sorts by length descending to prioritize longer matches (e.g., "==" over "=").
     */
    private fun Set<String>.firstMatch(ignoreCase: Boolean = false): String? =
        if (peek() == null) null
        else this.sortedByDescending { it.length } // Prioritize longer matches
            .firstOrNull { it.isNext(ignoreCase) } // Use the range-aware isNext

    // ------------------------------------------------------------- CONTROL

    /**
     * Advances the current [position] by one character and updates the [line] and [column] accordingly.
     */
    private fun advance(): Char? {
        val char = peek()
        if (char != null) {
            position++
            if (char == '\n') {
                line++
                column = 0
            } else {
                column++
            }
        }
        return char
    }

    /**
     * Advances the current [position] by a specified [length].
     */
    private fun advance(length: Int) {
        repeat(length) {
            advance() // Use the main advance to update line/column correctly
        }
    }

    // ------------------------------------------------------------- CHECK

    private fun Char?.isDigit(): Boolean = this in '0'..'9'
    private fun Char?.isHexDigit(): Boolean = isDigit() || this in 'a'..'f' || this in 'A'..'F'
    private fun Char?.isBinDigit(): Boolean = this == '0' || this == '1'
    private fun Char?.isOctDigit(): Boolean = this in '0'..'7'
    private fun Char?.isAlpha(): Boolean = this != null && (this in 'a'..'z' || this in 'A'..'Z' || set.symbolSpecialChars.contains(this))
    private fun Char?.isAlphaNumeric(): Boolean = isAlpha() || isDigit()

    private fun String.isKeyword(): Boolean = set.keywordsCaseSensitive.contains(this) || set.keywordsLowerCase.contains(lowercase())

    // ------------------------------------------------------------- CONSUME

    /** Reads whitespace and returns a WHITESPACE token, or null if no whitespace found. */
    private fun readWhitespace(): PsiToken? {
        val start = position
        while (peek()?.isWhitespace() == true && peek() != '\n') {
            advance()
        }
        return if (start != position) {
            PsiToken(source.substring(start, position), PsiTokenType.WHITESPACE, start..<position)
        } else null
    }

    /** Reads a comment and returns a COMMENT token, or null if no comment found. */
    private fun readComment(): PsiToken? {
        val start = position
        when {
            set.commentSl != null && set.commentSl.isNext() -> {
                advance(set.commentSl.length)
                while (peek() != null && peek() != '\n') {
                    advance()
                }
                // Don't consume the newline, let the main loop handle it
            }

            set.commentSlAlt != null && set.commentSlAlt.isNext() -> {
                advance(set.commentSlAlt.length) // null check handled by isNext
                while (peek() != null && peek() != '\n') {
                    advance()
                }
                // Don't consume the newline
            }

            set.commentMl != null && set.commentMl.first.isNext() -> {
                advance(set.commentMl.first.length)
                while (peek() != null && !set.commentMl.second.isNext()) {
                    advance()
                }
                if (set.commentMl.second.isNext()) {
                    advance(set.commentMl.second.length)
                } // else: unterminated comment, error handled implicitly by EOF or next token
            }

            else -> return null // No comment found
        }

        return if (start != position) {
            PsiToken(source.substring(start, position), PsiTokenType.COMMENT, start..<position)
        } else null // Should not happen if isNext was true, but safe fallback
    }

    /** Reads a number literal (Hex, Bin, Dec, Float, Double). */
    private fun readNumber(): PsiToken {
        val start = position

        when {
            // Hexadecimal (Check before decimal because of '0')
            set.litIntHexPrefix != null && set.litIntHexPrefix.isNotEmpty() && set.litIntHexPrefix.isNext() -> {
                advance(set.litIntHexPrefix.length)
                val afterPrefix = position

                if (!peek().isHexDigit()) { // Must have at least one hex digit after prefix
                    return PsiToken("Invalid hex literal: missing digits after ${set.litIntHexPrefix}", PsiTokenType.ERROR, start..<position)
                }
                while (peek().isHexDigit()) advance()
                return PsiToken(source.substring(afterPrefix, position), PsiTokenType.LITERAL.INTEGER.Hex, start..<position)
            }

            set.litIntOctPrefix != null && set.litIntOctPrefix.isNotEmpty() && set.litIntOctPrefix.isNext() -> {
                advance(set.litIntOctPrefix.length)

                val afterPrefix = position
                if (!peek().isOctDigit()) {
                    return PsiToken("Invalid oct literal: missing digits after ${set.litIntOctPrefix}", PsiTokenType.ERROR, start..<position)
                }
                while (peek().isOctDigit()) advance()
                return PsiToken(source.substring(afterPrefix, position), PsiTokenType.LITERAL.INTEGER.Oct, start..<position)
            }

            // Binary (Check before decimal because of '0')
            set.litIntBinPrefix != null && set.litIntBinPrefix.isNotEmpty() && set.litIntBinPrefix.isNext() -> {
                advance(set.litIntBinPrefix.length)

                val afterPrefix = position
                if (!peek().isBinDigit()) { // Must have at least one bin digit after prefix
                    return PsiToken("Invalid bin literal: missing digits after ${set.litIntBinPrefix}", PsiTokenType.ERROR, start..<position)
                }
                while (peek().isBinDigit()) advance()
                return PsiToken(source.substring(afterPrefix, position), PsiTokenType.LITERAL.INTEGER.Bin, start..<position)
            }

            // Decimal, Float, Double
            peek().isDigit() -> {
                while (peek().isDigit()) advance()

                var isFloat = false
                var isDouble = false

                // Check for fractional part
                if ('.'.isNext()) {
                    // Peek ahead: is there a digit after the dot?
                    val posAfterDot = position + 1
                    val nextCharIsDigit = if (inRange != null) {
                        posAfterDot <= inRange.last && posAfterDot < source.length && source[posAfterDot].isDigit()
                    } else {
                        posAfterDot < source.length && source[posAfterDot].isDigit()
                    }

                    // Only consume dot if followed by a digit
                    if (nextCharIsDigit) {
                        advance() // Consume '.'
                        isDouble = true // It's at least a double now
                        while (peek().isDigit()) advance()
                    } else {
                        // Dot not followed by digit, treat number so far as integer
                        return PsiToken(source.substring(start, position), PsiTokenType.LITERAL.INTEGER.Dec, start..<position)
                    }
                }

                // Check for float suffix (only if it was potentially a float/double)
                if (isDouble && set.litFloatPostfix != null && set.litFloatPostfix.isNotEmpty() && set.litFloatPostfix.isNext()) {
                    advance(set.litFloatPostfix.length)
                    isFloat = true
                    isDouble = false
                }

                // Determine token type
                return when {
                    isFloat -> PsiToken(source.substring(start, position), PsiTokenType.LITERAL.FP.FLOAT, start..<position)
                    isDouble -> PsiToken(source.substring(start, position), PsiTokenType.LITERAL.FP.DOUBLE, start..<position)
                    else -> PsiToken(source.substring(start, position), PsiTokenType.LITERAL.INTEGER.Dec, start..<position)
                }
            }
        }

        // Should not be reached if called correctly (i.e., after peek().isDigit() check)
        return PsiToken("Invalid number literal", PsiTokenType.ERROR, start..<position)
    }

    /** Reads an escaped character sequence. */
    private fun readEscaped(): Char? { // Return Char and number of characters consumed after '\'
        return when (val content = advance()) {
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'b' -> '\b'
            'f' -> '\u000c' // Form Feed
            // 'v' -> '\u000b' // Vertical Tab - Less common, omit?
            '0' -> '\u0000' // Null char
            '\'' -> '\''
            '"' -> '"'
            '\\' -> '\\'
            '$' -> '$' // Often needs escaping in interpolated strings
            'u' -> { // Unicode escape sequence \uXXXX
                var codeValue = 0
                var digitsConsumed = 0
                for (i in 0..3) {
                    val digitChar = peek()
                    if (digitChar.isHexDigit()) {
                        advance()
                        codeValue = (codeValue shl 4) + digitChar.toString().toInt(16)
                        digitsConsumed++
                    } else {
                        // Invalid Unicode escape
                        return null // Indicate error
                    }
                }
                if (digitsConsumed == 4) {
                    codeValue.toChar() // 'u' + 4 hex digits
                } else {
                    null // Error case already handled, but for safety
                }
            }

            '\n' -> null // Escaped newline - often ignored or handled specially, returning null for now
            null -> null // EOF after escape character
            else -> content // Treat as literal character (e.g., \z -> z)
        }
    }

    /** Reads a character literal. */
    private fun readChar(): PsiToken {
        val start = position
        advance() // Consume opening litCharPrefix

        var charValue: Char? = null

        if (set.litCharEscape != null && set.litCharEscape.isNext()) {
            advance() // Consume escape char
            val escaped = readEscaped()
            if (escaped != null) {
                charValue = escaped
            } else {
                // Error: invalid escape sequence
                // Consume until postfix or error
                while (peek() != null && !set.litChar?.second.isNext() && peek() != '\n') {
                    advance()
                }
                if (set.litChar?.second.isNext()) advance() // Consume postfix if present
                return PsiToken("Invalid escape sequence in char literal", PsiTokenType.ERROR, start..<position)
            }
        } else {
            // Regular character
            charValue = advance()
            if (charValue != null) {
                if (charValue == set.litChar?.second && set.litCharEscape == null) {
                    // Empty char literal '' and no escape defined -> error
                    return PsiToken("Empty character literal", PsiTokenType.ERROR, start..<position)
                }
                if (charValue == '\n') {
                    return PsiToken("Illegal newline in char literal", PsiTokenType.ERROR, start..<position)
                }
            } else {
                // EOF after opening quote
                return PsiToken("Unterminated character literal", PsiTokenType.ERROR, start..<position)
            }
        }

        // Check for postfix
        if (set.litChar != null) {
            if (set.litChar.second.isNext()) {
                advance() // Consume postfix
            } else {
                // Error: postfix missing
                // backtrack? No, just report error with current span
                return PsiToken("Character literal missing postfix '${set.litChar.second}'", PsiTokenType.ERROR, start..<position)
            }
        }


        return PsiToken(charValue.toString(), PsiTokenType.LITERAL.CHAR, start..<position)
    }

    /** Reads an identifier or keyword. */
    private fun readIdentifierOrKeyword(): PsiToken {
        val start = position
        while (peek().isAlphaNumeric()) { // Continue consuming alpha-numeric
            advance()
        }
        val value = source.substring(start, position)
        val type = if (value.isKeyword()) PsiTokenType.KEYWORD else PsiTokenType.IDENTIFIER
        return PsiToken(value, type, start..<position)
    }

    /** Reads basic content within a multi-line string until a delimiter or special char. */
    private fun readStringContent(multiLine: Boolean): PsiToken? {
        val start = position
        while (true) {
            when {
                peek() == null -> break // EOF
                !multiLine && peek() == '\n' && !multiLine -> break
                !multiLine && set.litStringSl?.second.isNext() -> break
                multiLine && set.litStringMl != null && set.litStringMl.second.isNext() -> break

                set.litStringInterpSingle.isNext() -> break
                set.litStringInterpBlock != null && set.litStringInterpBlock.first.isNext() -> break
                set.litStringEscape.isNext() -> break
                else -> advance() // Consume character
            }
        }
        return if (start != position) {
            PsiToken(source.substring(start, position), PsiTokenType.LITERAL.STRING.CONTENT.Basic, start..<position)
        } else null
    }

    /** Reads a multi-line string literal, handling escapes and interpolation. */
    private fun readString(multiLine: Boolean): List<PsiToken> {
        // Assume prefixes/postfixes are non-null and non-empty if this is called

        val tokens = mutableListOf<PsiToken>()
        val stringStartPos = position

        if (multiLine && set.litStringMl != null) {
            advance(set.litStringMl.first.length)
            tokens.add(PsiToken(set.litStringMl.first, PsiTokenType.LITERAL.STRING.MlStart, stringStartPos..<position))
        } else if(set.litStringSl != null) {
            advance(set.litStringSl.first.length)
            tokens.add(PsiToken(set.litStringSl.first, PsiTokenType.LITERAL.STRING.SlStart, stringStartPos..<position))
        }

        while (true) {
            val loopStartPos = position
            when {
                // --- Termination conditions ---
                peek() == null -> {
                    tokens.add(PsiToken("Unterminated string literal", PsiTokenType.ERROR, loopStartPos..<position))
                    break
                }

                !multiLine && peek() == '\n' -> {
                    tokens.add(PsiToken("Missing terminating ${set.litStringSl?.second}", PsiTokenType.ERROR, loopStartPos..<position))
                    break
                }

                multiLine && set.litStringMl != null && set.litStringMl.second.isNext() -> {
                    advance(set.litStringMl.second.length)
                    tokens.add(PsiToken(set.litStringMl.second, PsiTokenType.LITERAL.STRING.MlEnd, loopStartPos..<position))
                    break // End of string
                }

                !multiLine && set.litStringSl != null && set.litStringSl.second.isNext() -> {
                    advance(set.litStringSl.second.length)
                    tokens.add(PsiToken(set.litStringSl.second, PsiTokenType.LITERAL.STRING.SlEnd, loopStartPos..<position))
                    break // End of string
                }

                // --- Special sequences (Similar to SLString) ---
                set.litStringEscape != null && set.litStringEscape.isNext() -> {
                    advance() // Consume escape char
                    val escaped = readEscaped()
                    if (escaped != null) {
                        tokens.add(PsiToken(escaped.toString(), PsiTokenType.LITERAL.STRING.CONTENT.Escaped, loopStartPos..<position))
                    } else {
                        tokens.add(PsiToken("Invalid escape sequence", PsiTokenType.ERROR, loopStartPos..<position))
                    }
                }

                set.litStringInterpBlock != null && set.litStringInterpBlock.first.isNext() -> {
                    advance(set.litStringInterpBlock.first.length)
                    tokens.add(PsiToken(set.litStringInterpBlock.first, PsiTokenType.LITERAL.STRING.INTERP.BlockStart, loopStartPos..<position))

                    tokens.addAll(tokenize(inString = true, stopAtStringEnd = true)) // Recursive call

                    if (set.litStringInterpBlock.second.isNext()) {
                        val blockEndStart = position
                        advance(set.litStringInterpBlock.second.length) // *** FIX 4: Advance past BlockEnd ***
                        tokens.add(PsiToken(set.litStringInterpBlock.second, PsiTokenType.LITERAL.STRING.INTERP.BlockEnd, blockEndStart..<position))
                    } else {
                        tokens.add(PsiToken("Unterminated string interpolation block, expected '${set.litStringInterpBlock.second}'", PsiTokenType.ERROR, position..<position))
                    }
                }

                set.litStringInterpSingle != null && set.litStringInterpSingle.isNext() -> {
                    advance() // Consume '$'
                    tokens.add(PsiToken(set.litStringInterpSingle.toString(), PsiTokenType.LITERAL.STRING.INTERP.Single, loopStartPos..<position))
                    if (peek().isAlpha()) {
                        tokens.add(readIdentifierOrKeyword())
                    } else {
                        tokens.add(PsiToken("Invalid target for single variable string interpolation", PsiTokenType.ERROR, position..<position))
                    }
                }

                // --- Basic Content ---
                else -> {
                    val contentToken = readStringContent(multiLine)
                    if (contentToken != null) {
                        tokens.add(contentToken)
                    }
                }
            }
            // Safety break
            if (position == loopStartPos) {
                tokens.add(PsiToken("Lexer stuck in string", PsiTokenType.ERROR, position..position))
                advance()
            }
        }
        return tokens
    }

    /**
     * Tokenizes the source string according to the PsiLexerSet rules.
     *
     * @param inString Internal flag indicating if currently lexing inside a string interpolation block.
     * @param stopAtStringEnd Internal flag for interpolation: should stop at BlockEnd?
     * @return A list of [PsiToken]s.
     */
    private fun tokenize(inString: Boolean = false, stopAtStringEnd: Boolean = true): List<PsiToken> {
        val tokens = mutableListOf<PsiToken>()

        // Simple brace counting for interpolation exit (can be enhanced for full syntax awareness if needed)
        var curlyBraceDepth = 0 // Only track curly braces for typical ${} interpolation exit

        mainLoop@ while (true) {
            // Handle whitespace and comments first
            val wsToken = readWhitespace()
            if (wsToken != null) {
                if (!set.ignoreWhitespace) {
                    tokens.add(wsToken)
                }
                continue@mainLoop // Continue to next token after whitespace
            }

            val commentToken = readComment()
            if (commentToken != null) {
                if (!set.ignoreComments) {
                    tokens.add(commentToken)
                }
                continue@mainLoop // Continue to next token after comment
            }

            // Store position before reading the next token
            val tokenStartPos = position
            val next = peek()

            // --- Check for end of input ---
            if (next == null) {
                if (!inString) {
                    // Only add EOF if not inside an interpolation expecting an end
                    tokens.add(PsiToken("", PsiTokenType.EOF, tokenStartPos..<position))
                } else {
                    // If EOF reached inside string interpolation, it's an error
                    tokens.add(PsiToken("Unterminated string interpolation block (EOF reached)", PsiTokenType.ERROR, tokenStartPos..<position))
                }
                break@mainLoop // Exit loop
            }

            // --- Check for interpolation end (only if stopAtStringEnd is true) ---
            // Check if we are existing an interpolation block like ${...} or just }
            if (inString && stopAtStringEnd && curlyBraceDepth == 0 && set.litStringInterpBlock != null && set.litStringInterpBlock.second.isNext()) {
                // Don't consume '}', let the caller handle it by finding it via isNext()
                break@mainLoop
            }

            // Longest Match Logic for Operator/Punctuations
            val matchedOperator = set.operators.firstMatch()
            val matchedPunctuation = set.punctuations.firstMatch()

            // --- Tokenize based on the next character(s) ---
            when {
                // Braces/Brackets/Curlies (Handle nesting for interpolation exit)
                // These are often single characters but might overlap with operators/punctuations (e.g., '<', '>')
                // We handle them explicitly first for clarity and potential frequency,
                // but the longest match logic below might also catch them if they are in the sets.

                next == '(' -> {
                    advance(); tokens.add(PsiToken("(", PsiTokenType.PUNCTUATION, tokenStartPos..<position))
                }

                next == '[' -> {
                    advance(); tokens.add(PsiToken("[", PsiTokenType.PUNCTUATION, tokenStartPos..<position))
                }

                next == '{' -> {
                    if (inString) curlyBraceDepth++ // Track for interpolation exit
                    advance(); tokens.add(PsiToken("{", PsiTokenType.PUNCTUATION, tokenStartPos..<position))
                }

                next == ')' -> {
                    advance(); tokens.add(PsiToken(")", PsiTokenType.PUNCTUATION, tokenStartPos..<position))
                }

                next == ']' -> {
                    advance(); tokens.add(PsiToken("]", PsiTokenType.PUNCTUATION, tokenStartPos..<position))
                }

                next == '}' -> {
                    // We only decrement depth if inside interpolation AND it's the closing curly
                    // Note: If litStringInterpBlock.second is '}', the break@mainLoop above handles exit.
                    // This handles nested curlies *within* an interpolation.
                    if (inString && curlyBraceDepth > 0) curlyBraceDepth-- // Track for interpolation exit
                    advance(); tokens.add(PsiToken("}", PsiTokenType.PUNCTUATION, tokenStartPos..<position))
                    // Check again immediately if this closing brace ends the interpolation block
                    if (inString && stopAtStringEnd && curlyBraceDepth == 0 && set.litStringInterpBlock != null && set.litStringInterpBlock.second == "}") {
                        break@mainLoop // Exit loop, let caller handle BlockEnd
                    }
                }

                // LONGEST MATCH: Operators vs Punctuations
                matchedOperator != null && matchedPunctuation != null -> {
                    if (matchedOperator.length >= matchedPunctuation.length) {
                        advance(matchedOperator.length)
                        tokens.add(PsiToken(matchedOperator, PsiTokenType.OPERATOR, tokenStartPos..<position))
                    } else {
                        advance(matchedPunctuation.length)
                        tokens.add(PsiToken(matchedPunctuation, PsiTokenType.PUNCTUATION, tokenStartPos..<position))
                    }
                }

                matchedOperator != null -> {
                    advance(matchedOperator.length)
                    tokens.add(PsiToken(matchedOperator, PsiTokenType.OPERATOR, tokenStartPos..<position))
                }

                matchedPunctuation != null -> {
                    advance(matchedPunctuation.length)
                    tokens.add(PsiToken(matchedPunctuation, PsiTokenType.PUNCTUATION, tokenStartPos..<position))
                }

                // Linebreak
                next == '\n' -> {
                    advance()
                    tokens.add(PsiToken("\n", PsiTokenType.LINEBREAK, tokenStartPos..<position))
                }

                // --- Literals ---

                // String literals
                set.litStringMl != null && set.litStringMl.first.isNext() -> tokens.addAll(readString(true))
                set.litStringSl?.first.isNext() -> tokens.addAll(readString(false)) // Check SL after ML

                // Char literal
                set.litChar?.first.isNext() -> tokens.add(readChar())

                // Number literals (Handles Hex, Bin, Dec, Float, Double prefixes)
                set.readNumberLiterals && next.isDigit() || (next == '.' && peekAheadIsDigit()) -> tokens.add(readNumber()) // Handle starting with '.' like .5
                // Hex/Bin checks also needed if prefixes don't start with digit/dot (e.g., #...)
                set.readNumberLiterals && set.litIntHexPrefix != null && set.litIntHexPrefix.isNotEmpty() && set.litIntHexPrefix.isNext() -> tokens.add(readNumber())
                set.readNumberLiterals && set.litIntBinPrefix != null && set.litIntBinPrefix.isNotEmpty() && set.litIntBinPrefix.isNext() -> tokens.add(readNumber())
                set.readNumberLiterals && set.litIntOctPrefix != null && set.litIntOctPrefix.isNotEmpty() && set.litIntOctPrefix.isNext() -> tokens.add(readNumber())

                // Keywords and Identifiers (Must come after checking specific literal keywords)
                next.isAlphaNumeric() -> tokens.add(readIdentifierOrKeyword())

                // --- Error / Unknown Token ---
                else -> {
                    advance() // Consume the unknown character
                    tokens.add(PsiToken("Invalid or unrecognized token", PsiTokenType.ERROR, tokenStartPos..<position))
                }
            }

            // Safety check: If position didn't advance, force it to avoid infinite loops
            if (position == tokenStartPos && peek() != null) {
                val stuckChar = advance() // Consume the char causing the stall
                tokens.add(PsiToken("Lexer stalled at this character ($stuckChar)", PsiTokenType.ERROR, tokenStartPos..<position))
            }
        }

        return tokens
    }

    /** Helper to check if the character immediately following the current position is a digit. */
    private fun peekAheadIsDigit(): Boolean {
        val aheadPos = position + 1
        val maxPos = inRange?.last ?: (source.length - 1)
        return if (aheadPos <= maxPos && aheadPos < source.length) source[aheadPos].isDigit() else false
    }

    /**
     * Tokenizes the entire source string (or the specified range) and returns a list of [PsiToken]s.
     *
     * @return A list of [PsiToken]s representing the tokenized source.
     */
    fun tokenize(): List<PsiToken> = tokenize(inString = false, stopAtStringEnd = false) // Start tokenization normally

}