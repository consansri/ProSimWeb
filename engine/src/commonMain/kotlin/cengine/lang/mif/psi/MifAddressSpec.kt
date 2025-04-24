package cengine.lang.mif.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.feature.Highlightable
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.style.CodeStyle

/** Base class for address specifications (single, range, list). */
sealed class MifAddressSpec(
    type: MifAddressSpecTypeDef, // Use specific type from companion
    range: IntRange,
    vararg children: PsiElement,
) : PsiStatement(type, range, *children) {
    /** Marker interface for MIF address spec type definitions. */
    interface MifAddressSpecTypeDef : PsiStatementTypeDef

    class Single(range: IntRange, vararg children: PsiElement) : MifAddressSpec(Single, range, *children), Highlightable {
        val addressValue: PsiToken = children.filterIsInstance<PsiToken>()[0] // Contains the address token
        override val style: CodeStyle?
            get() = CodeStyle.identifier

        companion object : MifAddressSpecTypeDef {
            override val typeName: String = "SingleAddressSpec"
            override val builder: NodeBuilderFn = { _, children, range ->
                // Expects a single numeric value node
                if (children.size == 1 && children[0].type == PsiTokenType.IDENTIFIER) {
                    Single(range, *children)
                } else null
            }
        }
    }

    class Range(range: IntRange, vararg children: PsiElement) : MifAddressSpec(Range, range, *children) {
        val startAddressValue: Single? = children.filterIsInstance<Single>()[0]
        val endAddressValue: Single? = children.filterIsInstance<Single>()[1]

        companion object : MifAddressSpecTypeDef {
            override val typeName: String = "RangeAddressSpec"
            override val builder: NodeBuilderFn = { _, children, range ->
                // [ Start .. End ]
                if (children.size == 5 &&
                    children[0].type == PsiTokenType.PUNCTUATION && (children[0] as PsiToken).value == "[" &&
                    children[1] is Single &&
                    children[2].type == PsiTokenType.PUNCTUATION && (children[2] as PsiToken).value == ".." &&
                    children[3] is Single &&
                    children[4].type == PsiTokenType.PUNCTUATION && (children[4] as PsiToken).value == "]"
                ) {
                    Range(range, *children)
                } else null
            }
        }
    }

    class RangeList(range: IntRange, vararg children: PsiElement) : MifAddressSpec(RangeList, range, *children) {
        // List contains numeric values and comma punctuations alternately
        val addressValues: List<Single> = children.filterIsInstance<Single>()

        companion object : MifAddressSpecTypeDef {
            override val typeName: String = "ListAddressSpec"
            override val builder: NodeBuilderFn = { _, children, range ->
                // [ Val1 , Val2 , ... , ValN ]
                if (children.size >= 3 && // Need at least [, Value, ]
                    children.first().type == PsiTokenType.PUNCTUATION && (children.first() as PsiToken).value == "[" &&
                    children.last().type == PsiTokenType.PUNCTUATION && (children.last() as PsiToken).value == "]"
                ) {
                    RangeList(range, *children)
                } else null
            }
        }
    }
}

