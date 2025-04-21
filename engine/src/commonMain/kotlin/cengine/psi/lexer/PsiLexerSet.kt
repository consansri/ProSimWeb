package cengine.psi.lexer

data class PsiLexerSet(
    val keywordsLowerCase: Set<String> = emptySet(),
    val keywordsCaseSensitive: Set<String> = emptySet(),
    val symbolSpecialChars: Set<Char> = emptySet(),
    val punctuations: Set<String> = emptySet(), // additional to braces, brackets, curlybraces
    val operators: Set<String> = emptySet(),
    val commentSl: String = "//",
    val commentSlAlt: String? = null, // #,
    val commentMlStart: String = "/*",
    val commentMlEnd: String = "*/",
    val litCharPrefix: Char = '\'',
    val litCharPostfix: Char? = litCharPrefix,
    val litCharEscape: Char? = null,
    val litIntHexPrefix: String = "0x",
    val litIntBinPrefix: String = "0b",
    val litFloatPostfix: String = "f",
    val litStringSlPrefix: String = "\"",
    val litStringSlPostfix: String = "\"",
    val litStringMlPrefix: String? = null,
    val litStringMlPostfix: String? = null,
    val litStringInterpSingle: Char? = null,
    val litStringInterpBlockStart: String? = null,
    val litStringInterpBlockEnd: String? = null,
    val litStringEscape: Char? = null,
    val ignoreComments: Boolean = false,
    val ignoreWhitespace: Boolean = false,
) {
    companion object {
        val KOTLIN = PsiLexerSet(
            keywordsLowerCase = emptySet(),
            keywordsCaseSensitive = setOf(
                "abstract", "as", "break", "by", "case", "catch", "class", "companion",
                "const", "continue", "do", "dynamic", "else", "enum", "external", "false",
                "final", "finally", "for", "fun", "if", "import", "in", "interface",
                "internal", "is", "object", "open", "out", "override", "package", "private",
                "protected", "public", "return", "sealed", "super", "then", "this", "throw",
                "true", "try", "typealias", "val", "var", "when", "where", "while"
            ),
            symbolSpecialChars = setOf('_'),
            punctuations = setOf("->", ";", ":", "@", "?", ","),
            operators = setOf("+=", "-=", "*=", "/=", "%=", "++", "--", "==", "!=", ">=", "<=", "&&", "||", "?:", "..", "?.", "::", ".", "+", "-", "*", "/", "%", "=", ">", "<", "!"),
            commentSl = "//",
            commentSlAlt = null,
            commentMlStart = "/*",
            commentMlEnd = "*/",
            litIntHexPrefix = "0x",
            litIntBinPrefix = "0b",
            litFloatPostfix = "f",
            litStringSlPrefix = "\"",
            litStringSlPostfix = "\"",
            litCharPrefix = '\'',
            litCharEscape = '\\',
            litCharPostfix = '\'',
            litStringMlPrefix = "\"\"\"",
            litStringMlPostfix = "\"\"\"",
            litStringInterpSingle = '$',
            litStringInterpBlockStart = "\${",
            litStringInterpBlockEnd = "}",
            litStringEscape = '\\'
        )
    }

}
