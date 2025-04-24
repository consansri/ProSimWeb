package cengine.psi.core

import cengine.psi.elements.PsiFile
import cengine.psi.parser.CompletedMarkerInfo
import cengine.psi.tree.PsiTreeBuilder
import cengine.vfs.VirtualFile

/**
 * Signature for functions responsible for building a specific PsiFile type.
 * Defined as an extension function on PsiTreeBuilder to provide implicit context.
 */
typealias FileBuilderFn = PsiTreeBuilder.(
    file: VirtualFile,
    valid: Boolean,
    children: Array<PsiElement>
) -> PsiFile


interface PsiFileTypeDef : PsiElementType {
    val builder: FileBuilderFn
}