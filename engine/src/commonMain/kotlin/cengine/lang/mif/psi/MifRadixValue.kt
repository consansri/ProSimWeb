package cengine.lang.mif.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType

/** Represents a radix keyword value (HEX, BIN, etc.). */
class MifRadixValue(range: IntRange, vararg children: PsiElement) : PsiStatement(Type, range, *children) {
    val radixToken: PsiToken? = children.filterIsInstance<PsiToken>().first() // Should be the keyword token

    companion object Type : PsiStatementTypeDef {
        override val typeName: String = "RadixValue"
        override val builder: NodeBuilderFn = { _, children, range ->
            // Check if the single child is a keyword token matching known radix values
            if (children.size == 1 && children[0] is PsiToken && children[0].type == PsiTokenType.KEYWORD) {
                val tokenValue = (children[0] as PsiToken).value.uppercase()
                if (tokenValue in setOf("HEX", "DEC", "BIN", "OCT", "UNS")) {
                    MifRadixValue(range, *children)
                } else null
            } else null
        }
    }
}