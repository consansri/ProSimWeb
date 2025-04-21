package cengine.psi.feature

import cengine.psi.elements.PsiFile

interface FileReference: PsiReference<PsiFile> {

    override var reference: PsiFile?

}