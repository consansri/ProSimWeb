package cengine.lang.cown.psi

import cengine.lang.cown.CownLang
import cengine.psi.PsiManager
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

class CownPsiParser: PsiParser<CownPsiFile> {
    override suspend fun parse(file: VirtualFile, manager: PsiManager<*, *>): CownPsiFile {
        return CownPsiFile(file, manager)
    }
}