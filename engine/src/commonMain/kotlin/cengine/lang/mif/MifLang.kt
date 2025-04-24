package cengine.lang.mif

import cengine.lang.LanguageService
import cengine.lang.PsiSupport
import cengine.lang.Runner
import cengine.psi.PsiManager
import cengine.psi.lexer.PsiLexerSet
import cengine.vfs.VFileSystem

object MifLang : LanguageService() {

    override val name: String = "MIF"
    override val fileSuffix: String = ".mif"
    override val runConfig: Runner<MifLang> = MifRunner
    override val psiSupport: PsiSupport? = PsiSupport(
        PsiLexerSet(
            readNumberLiterals = false,
            keywordsLowerCase = setOf(
                // Directives
                "width", "depth", "address_radix", "data_radix",
                // Content block markers
                "content", "begin", "end",
                // Radix Values (Treating them as keywords for highlighting/parsing)
                "hex", "bin", "oct", "dec", "uns"
            ),
            keywordsCaseSensitive = emptySet(),
            symbolSpecialChars = setOf('_'),
            punctuations = setOf(
                "=",  // Directive assignment
                ";",  // Statement terminator
                ":",  // Address/Value separator
                "[",  // Range/List start
                "]",  // Range/List end
                ",",  // Address list separator
                ".."  // Range indicator
            ),
            operators = emptySet(),
            commentSl = "--",
            commentSlAlt = "%",
            ignoreWhitespace = true
        ),
        MifParser,
        MifPsiFile
    )

    override fun createManager(vfs: VFileSystem): PsiManager<*> = PsiManager<MifLang>(
        vfs,
        PsiManager.Mode.TEXT,
        this
    ) {

    }
}