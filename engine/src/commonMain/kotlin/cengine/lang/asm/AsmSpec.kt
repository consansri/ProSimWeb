package cengine.lang.asm

import cengine.lang.asm.AsmLexer.ESCAPE_CHAR
import cengine.lang.asm.AsmLexer.OPERATORS
import cengine.lang.asm.AsmLexer.PUNCTUATIONS
import cengine.lang.asm.AsmLexer.SPECIAL_CHARS
import cengine.lang.asm.gas.AsmCodeGenerator
import cengine.lang.asm.psi.AsmDirectiveT
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.asm.target.ikrmini.IkrMiniSpec
import cengine.lang.asm.target.ikrrisc2.IkrR2Spec
import cengine.lang.asm.target.riscv.Rv32Spec
import cengine.lang.asm.target.riscv.Rv64Spec
import cengine.psi.lexer.PsiLexerSet
import emulator.EmuLink

/**
 * Interface representing a defined assembly configuration.
 */
interface AsmSpec<T : AsmCodeGenerator<*>> {
    companion object {
        val specs = setOf(Rv32Spec, Rv64Spec, IkrR2Spec, IkrMiniSpec)
    }

    val name: String
    val shortName: String get() = name.replace("\\s".toRegex(), "").lowercase()
    val emuLink: EmuLink?

    val instrTypes: List<AsmInstructionT>
    val dirTypes: List<AsmDirectiveT>

    val commentSlAlt: String? get() = "#"
    val litIntHexPrefix: String get() = "0x"
    val litIntBinPrefix: String get() = "0b"
    val addPunctuations: Set<String> get() = emptySet()
    val addSymbolSpecialChars: Set<Char> get() = emptySet()

    val contentExample: String
        get() = """
            $commentSlAlt $name example
            
        """.trimIndent()

    fun createLexerSet(): PsiLexerSet {
        return PsiLexerSet(
            readNumberLiterals = true,
            keywordsLowerCase = instrTypes.map { it.keyWord.lowercase() }.toSet(),
            keywordsCaseSensitive = dirTypes.map { it.keyWord }.toSet() + "true" + "false",
            symbolSpecialChars = SPECIAL_CHARS + addSymbolSpecialChars,
            punctuations = (PUNCTUATIONS + addPunctuations).sortedBy { it.length }.reversed().toSet(),
            operators = OPERATORS.map { it.string }.sortedBy { it.length }.reversed().toSet(),
            commentSl = "//",
            commentSlAlt = commentSlAlt,
            commentMl = "/*" to "*/",
            litIntHexPrefix = litIntHexPrefix,
            litIntBinPrefix = litIntBinPrefix,
            litStringEscape = ESCAPE_CHAR,
            litCharEscape = ESCAPE_CHAR,
            litStringSl = "\"" to "\"",
            litChar = '\'' to '\''
        )
    }

    fun createParser(): AsmTreeParser = AsmTreeParser(
        instrTypes,
        dirTypes.associateBy { it.keyWord }
    )

    fun createGenerator(): T

    override fun toString(): String

}