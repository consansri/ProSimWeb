package cengine.psi.core

import cengine.psi.PsiManager
import cengine.psi.lexer.core.Token
import cengine.vfs.VirtualFile

/**
 * Language-specific parser interface
 */
interface PsiParser<T : PsiFile> {

    suspend fun parse(file: VirtualFile, manager: PsiManager<*, *>): T

    class NodeException(element: PsiElement, message: String) : Exception("${element.range}: $message")
    class TokenException(element: Token, message: String) : Exception("${element.range}: $message")

    class ParentLinker : PsiElementVisitor {
        override fun visitFile(file: PsiFile) {
            file.children.forEach {
                it.parent = file
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            element.children.forEach {
                it.parent = element
                it.accept(this)
            }
        }
    }
}