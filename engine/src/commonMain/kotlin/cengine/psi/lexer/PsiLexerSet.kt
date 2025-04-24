package cengine.psi.lexer

data class PsiLexerSet(
    val readNumberLiterals: Boolean,
    val keywordsLowerCase: Set<String> = emptySet(),
    val keywordsCaseSensitive: Set<String> = emptySet(),
    val symbolSpecialChars: Set<Char> = emptySet(),
    val punctuations: Set<String> = emptySet(), // additional to braces, brackets, curlybraces
    val operators: Set<String> = emptySet(),
    val commentSl: String? = null,
    val commentSlAlt: String? = null,
    val commentMl: Pair<String,String>? = null,
    val litChar: Pair<Char, Char>? = null,
    val litCharEscape: Char? = null,
    val litIntHexPrefix: String? = null,
    val litIntOctPrefix: String? = null,
    val litIntBinPrefix: String? = null,
    val litFloatPostfix: String? = null,
    val litStringSl: Pair<String, String>? = null,
    val litStringMl: Pair<String, String>? = null,
    val litStringInterpSingle: Char? = null,
    val litStringInterpBlock: Pair<String, String>? = null,
    val litStringEscape: Char? = null,
    val ignoreComments: Boolean = false,
    val ignoreWhitespace: Boolean = false,
) {
    companion object {
        val KOTLIN = PsiLexerSet(
            readNumberLiterals = true,
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
            commentMl = "/*" to "*/",
            litIntHexPrefix = "0x",
            litIntBinPrefix = "0b",
            litFloatPostfix = "f",
            litStringSl = "\"" to "\"",
            litChar = '\'' to '\'',
            litCharEscape = '\\',
            litStringMl = "\"\"\"" to "\"\"\"",
            litStringInterpSingle = '$',
            litStringInterpBlock = "\${" to "}",
            litStringEscape = '\\'
        )
    }

}
