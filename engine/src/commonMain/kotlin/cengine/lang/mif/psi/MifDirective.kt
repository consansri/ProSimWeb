package cengine.lang.mif.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType

/** Base class for MIF directives (WIDTH, DEPTH, etc.). */
sealed class MifDirective(
    type: MifDirectiveTypeDef, // Use specific type from companion
    range: IntRange,
    vararg children: PsiElement
) : PsiStatement(type, range, *children) {

    val keyword: PsiToken? = children.filterIsInstance<PsiToken>().firstOrNull()
    val equalsSign: PsiToken? = children.filterIsInstance<PsiToken>().firstOrNull { it.value == "=" }
    val semicolon: PsiToken? = children.filterIsInstance<PsiToken>().lastOrNull {it.value == ";"}

    /** Marker interface for MIF directive type definitions. */
    interface MifDirectiveTypeDef : PsiStatementTypeDef

    // Concrete Directive Implementations

    class Width(range: IntRange, vararg children: PsiElement) : MifDirective(Width, range, *children) {
        val value: MifNumericValue = children.filterIsInstance<MifNumericValue>().first()

        companion object : MifDirectiveTypeDef {
            override val typeName: String = "WidthDirective"
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.size == 4 &&
                    children[0].type == PsiTokenType.KEYWORD && (children[0] as PsiToken).value.equals("WIDTH", ignoreCase = true) &&
                    children[1].type == PsiTokenType.PUNCTUATION && (children[1] as PsiToken).value == "=" &&
                    children[2] is MifNumericValue && // Assumes parser builds MifNumericValue node first
                    children[3].type == PsiTokenType.PUNCTUATION && (children[3] as PsiToken).value == ";"
                ) {
                    Width(range, *children)
                } else null
            }
        }
    }

    class Depth(range: IntRange, vararg children: PsiElement) : MifDirective(Depth, range, *children) {
        val value: MifNumericValue = children.filterIsInstance<MifNumericValue>().first()

        companion object : MifDirectiveTypeDef {
            override val typeName: String = "DepthDirective"
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.size == 4 &&
                    children[0].type == PsiTokenType.KEYWORD && (children[0] as PsiToken).value.equals("DEPTH", ignoreCase = true) &&
                    children[1].type == PsiTokenType.PUNCTUATION && (children[1] as PsiToken).value == "=" &&
                    children[2] is MifNumericValue &&
                    children[3].type == PsiTokenType.PUNCTUATION && (children[3] as PsiToken).value == ";"
                ) {
                    Depth(range, *children)
                } else null
            }
        }
    }

    class AddressRadix(range: IntRange, vararg children: PsiElement) : MifDirective(AddressRadix, range, *children) {
        val value: MifRadixValue = children.filterIsInstance<MifRadixValue>().first()

        companion object : MifDirectiveTypeDef {
            override val typeName: String = "AddressRadixDirective"
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.size == 4 &&
                    children[0].type == PsiTokenType.KEYWORD && (children[0] as PsiToken).value.equals("ADDRESS_RADIX", ignoreCase = true) &&
                    children[1].type == PsiTokenType.PUNCTUATION && (children[1] as PsiToken).value == "=" &&
                    children[2] is MifRadixValue && // Assumes parser builds MifRadixValue node first
                    children[3].type == PsiTokenType.PUNCTUATION && (children[3] as PsiToken).value == ";"
                ) {
                    AddressRadix(range, *children)
                } else null
            }
        }
    }

    class DataRadix(range: IntRange, vararg children: PsiElement) : MifDirective(DataRadix, range, *children) {
        val value: MifRadixValue = children.filterIsInstance<MifRadixValue>().first()

        companion object : MifDirectiveTypeDef {
            override val typeName: String = "DataRadixDirective"
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.size == 4 &&
                    children[0].type == PsiTokenType.KEYWORD && (children[0] as PsiToken).value.equals("DATA_RADIX", ignoreCase = true) &&
                    children[1].type == PsiTokenType.PUNCTUATION && (children[1] as PsiToken).value == "=" &&
                    children[2] is MifRadixValue &&
                    children[3].type == PsiTokenType.PUNCTUATION && (children[3] as PsiToken).value == ";"
                ) {
                    DataRadix(range, *children)
                } else null
            }
        }
    }


}

