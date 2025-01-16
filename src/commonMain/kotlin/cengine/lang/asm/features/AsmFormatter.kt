package cengine.lang.asm.features

import cengine.editor.formatting.Formatter
import cengine.lang.asm.ast.impl.AsmFile
import cengine.psi.core.PsiFile

class AsmFormatter : Formatter {
    override suspend fun formatted(psiFile: PsiFile): String? {
        if (psiFile !is AsmFile) return null
        return psiFile.getFormattedString(4)
    }
}