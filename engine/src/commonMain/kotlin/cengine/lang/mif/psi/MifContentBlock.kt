package cengine.lang.mif.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement

/** Represents the CONTENT BEGIN ... END; block. */
class MifContentBlock(range: IntRange, vararg children: PsiElement) : PsiStatement(MifContentBlock, range, *children) {

    val entries: List<MifContentEntry> = children.filterIsInstance<MifContentEntry>()

    companion object : PsiStatementTypeDef {
        override val typeName: String = "ContentBlock"
        override val builder: NodeBuilderFn = { _, children, range ->
            // CONTENT BEGIN [entries...] END ;
            MifContentBlock(range, *children)
        }
    }
}