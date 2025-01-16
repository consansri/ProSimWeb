package cengine.lang.mif

import cengine.lang.mif.ast.MifLexer
import cengine.lang.mif.ast.MifNode
import cengine.lang.mif.ast.MifPsiFile
import cengine.psi.PsiManager
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

object MifParser : PsiParser<MifPsiFile> {
    override suspend fun parse(file: VirtualFile, manager: PsiManager<*, *>): MifPsiFile {
        val content = file.getAsUTF8String()
        val lexer = MifLexer(content)
        val program = MifNode.Program.parse(lexer)

        program.accept(PsiParser.ParentLinker())

        return MifPsiFile(file, manager, program)
    }
}