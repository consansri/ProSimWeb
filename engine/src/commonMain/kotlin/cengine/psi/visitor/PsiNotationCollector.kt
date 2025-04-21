package cengine.psi.visitor

import cengine.editor.annotation.Annotation
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiFile

class PsiNotationCollector: PsiElementVisitor {
    val annotations = mutableListOf<Annotation>()
    override fun visitFile(file: PsiFile) {
        annotations.addAll(file.annotations)
        file.children.forEach {
            it.accept(this)
        }
    }

    override fun visitElement(element: PsiElement) {
        annotations.addAll(element.annotations)
        element.children.forEach {
            it.accept(this)
        }
    }
}