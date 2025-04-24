package cengine.lang.mif.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType

/** Represents a single entry within the CONTENT block. */
class MifContentEntry(range: IntRange, vararg children: PsiElement) : PsiStatement(MifContentEntry, range, *children) {

    val addressSpec: MifAddressSpec = children.filterIsInstance<MifAddressSpec>().first()
    // Data values are represented as MifNumericValue nodes by the parser
    val dataValues: List<MifNumericValue> = children.filterIsInstance<MifNumericValue>()

    companion object : PsiStatementTypeDef {
        override val typeName: String = "ContentEntry"
        override val builder: NodeBuilderFn = { _, children, range ->
            // AddressSpec : Value(s) ;
            if (children.size >= 4 && // Need at least AddressSpec, :, Value, ;
                children.first() is MifAddressSpec &&
                children[1].type == PsiTokenType.PUNCTUATION && (children[1] as PsiToken).value == ":" &&
                children.last().type == PsiTokenType.PUNCTUATION && (children.last() as PsiToken).value == ";" &&
                children.drop(2).dropLast(1).all { it is MifNumericValue } // Check middle elements are values
            ) {
                MifContentEntry(range, *children)
            } else null
        }
    }
}