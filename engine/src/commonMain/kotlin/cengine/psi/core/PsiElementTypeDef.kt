package cengine.psi.core

import cengine.psi.parser.CompletedMarkerInfo
import cengine.psi.tree.PsiTreeBuilder

/**
 * Signature for functions responsible for building a specific PsiElement type.
 * Defined as an extension function on PsiTreeBuilder to provide implicit context.
 */
typealias NodeBuilderFn = PsiTreeBuilder.(
    markerInfo: CompletedMarkerInfo,
    children: Array<PsiElement>,
    range: IntRange,
) -> PsiElement? // Still returns PsiElement? (null signals error)



interface PsiElementTypeDef : PsiElementType {
    val builder: NodeBuilderFn
}
