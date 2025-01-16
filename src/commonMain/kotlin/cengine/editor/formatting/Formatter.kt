package cengine.editor.formatting

import cengine.psi.core.PsiFile

/**
 * This interface is used to format the code in the file.
 *
 * The most common use case is to format the code in the file when it is saved.
 */
interface Formatter {
    suspend fun formatted(psiFile: PsiFile): String?
}