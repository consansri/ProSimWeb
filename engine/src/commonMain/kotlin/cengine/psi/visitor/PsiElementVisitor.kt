package cengine.psi.visitor

import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiFile

interface PsiElementVisitor {
    fun visitFile(file: PsiFile)

    fun visitElement(element: PsiElement)
}