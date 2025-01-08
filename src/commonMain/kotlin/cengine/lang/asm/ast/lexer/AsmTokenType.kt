package cengine.lang.asm.ast.lexer

import cengine.lang.asm.CodeStyle
import cengine.psi.lexer.core.TokenType

/**
 * Represents the type of token.
 */
enum class AsmTokenType(
    val regex: Regex? = null,
    override val style: CodeStyle? = null,
    val isOperator: Boolean = false,
    val couldBePrefix: Boolean = false,
    val isPunctuation: Boolean = false,
    val isOpeningBracket: Boolean = false,
    val isClosingBracket: Boolean = false,
    val isNumberLiteral: Boolean = false,
    val isStringLiteral: Boolean = false,
    val isCharLiteral: Boolean = false
) : TokenType {
    WHITESPACE(Regex("""[\t ]+""")),
    LINEBREAK(Regex("(\\n|\\r\\n)")),
    COMMENT_SL(Regex("//.*"), CodeStyle.comment),
    COMMENT_ML(Regex("""/\*([^*]|\*+[^*/])*\*/"""), CodeStyle.comment),
    COMMENT_NATIVE(Regex("#.+"), CodeStyle.comment),
    DIRECTIVE(style = CodeStyle.keyWordDir),
    REGISTER(style = CodeStyle.keyWordReg),
    INSTRNAME(style = CodeStyle.keyWordInstr),
    SYMBOL(Regex("""[a-zA-Z$._][a-zA-Z0-9$._]*""")),
    L_LABEL_REF(Regex("[0-9]+[bf](?![0-9a-f])", RegexOption.IGNORE_CASE)),
    INT_BIN(Regex("0b([01]+)", RegexOption.IGNORE_CASE), CodeStyle.altInt, isNumberLiteral = true),
    INT_HEX(Regex("0x([0-9a-f]+)", RegexOption.IGNORE_CASE), CodeStyle.altInt, isNumberLiteral = true),
    INT_OCT(Regex("0([0-7]+)", RegexOption.IGNORE_CASE), CodeStyle.altInt, isNumberLiteral = true),
    INT_DEC(Regex("([0-9]+)", RegexOption.IGNORE_CASE), CodeStyle.integer, isNumberLiteral = true),
    STRING_ML(Regex("\"\"\"(?:\\.|[^\"])*\"\"\""), CodeStyle.string, isStringLiteral = true),
    STRING_SL(Regex(""""(\\.|[^\\"])*""""), CodeStyle.string, isStringLiteral = true),
    CHAR(Regex("""'(\\.|[^\\'])"""), CodeStyle.char, isCharLiteral = true),
    ARG_REF(Regex("""\\[a-zA-Z$._][a-zA-Z0-9$._]*"""), CodeStyle.argument),
    ARG_SEPARATOR(Regex("""\\\(\)"""), CodeStyle.argument),
    ASSIGNMENT(Regex("=")),
    COMPLEMENT(Regex("~"), isOperator = true),
    MULT(Regex("\\*"), isOperator = true),
    DIV(Regex("/"), isOperator = true),
    REM(Regex("%"), isOperator = true),
    SHL(Regex("<<"), isOperator = true),
    SHR(Regex(">>"), isOperator = true),
    BITWISE_OR(Regex("\\|"), isOperator = true),
    BITWISE_AND(Regex("&"), isOperator = true),
    BITWISE_XOR(Regex("\\^"), isOperator = true),
    BITWISE_ORNOT(Regex("!"), isOperator = true),
    PLUS(Regex("\\+"), isOperator = true, couldBePrefix = true),
    MINUS(Regex("-"), isOperator = true, couldBePrefix = true),
    COMMA(Regex(","), isPunctuation = true),
    SEMICOLON(Regex(";"), isPunctuation = true),
    COLON(Regex(":"), isPunctuation = true),
    BRACKET_OPENING(Regex("\\("), isPunctuation = true, isOpeningBracket = true),
    BRACKET_CLOSING(Regex("\\)"), isPunctuation = true, isClosingBracket = true),
    CURLY_BRACKET_OPENING(Regex(Regex.escape("{")), isPunctuation = true, isOpeningBracket = true),
    CURLY_BRACKET_CLOSING(Regex(Regex.escape("}")), isPunctuation = true, isClosingBracket = true),
    SQUARE_BRACKET_OPENING(Regex("\\["), isPunctuation = true, isOpeningBracket = true),
    SQUARE_BRACKET_CLOSING(Regex(Regex.escape("]")), isPunctuation = true, isClosingBracket = true),
    UNDEFINED,
    EOF;

    fun isComment(): Boolean = this == COMMENT_SL || this == COMMENT_ML || this == COMMENT_NATIVE
    fun isLinkableSymbol(): Boolean = this == SYMBOL || this == L_LABEL_REF
    fun isLiteral(): Boolean = isStringLiteral || isNumberLiteral || isCharLiteral
    fun isBasicBracket(): Boolean = this == BRACKET_OPENING || this == BRACKET_CLOSING
    fun getRegex(prefices: AsmLexer.Prefices): Regex? {
        return when (this) {
            COMMENT_NATIVE -> Regex("${Regex.escape(prefices.comment)}.+")
            INT_BIN -> Regex("${Regex.escape(prefices.bin)}([01]+)")
            INT_HEX -> Regex("${Regex.escape(prefices.hex)}([0-9a-f]+)", RegexOption.IGNORE_CASE)
            INT_OCT -> Regex("${Regex.escape(prefices.oct)}([0-7]+)")
            INT_DEC -> Regex("${Regex.escape(prefices.dec)}([0-9]+)")
            SYMBOL -> prefices.symbol
            else -> this.regex
        }
    }
}