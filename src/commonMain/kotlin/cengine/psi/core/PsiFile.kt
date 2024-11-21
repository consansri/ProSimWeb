package cengine.psi.core

import cengine.editor.annotation.Severity
import cengine.lang.LanguageService
import cengine.vfs.VirtualFile

/**
 * Represents a file in the PSI structure
 */
interface PsiFile : PsiElement {
    val lang: LanguageService?
    val file: VirtualFile

    override val pathName: String
        get() = file.name

    val content: String
        get() = file.getAsUTF8String()

    fun update()

    fun hasErrors() = annotations.any { it.severity == Severity.ERROR }

    fun printErrors(): String? {
        if (!hasErrors()) return null
        return annotations.joinToString("\n") {
            it.createConsoleMessage(this)
        }
    }
}