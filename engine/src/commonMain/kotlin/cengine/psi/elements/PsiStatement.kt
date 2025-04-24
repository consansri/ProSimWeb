package cengine.psi.elements

import cengine.editor.annotation.Annotation
import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementTypeDef
import cengine.psi.feature.Named
import cengine.psi.feature.PsiReference
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.pratt.OpType
import cengine.util.collection.firstInstance

/**
 * A Statement can't be empty! Else it won't be created.
 */
open class PsiStatement(type: PsiStatementTypeDef, override var range: IntRange, vararg children: PsiElement) : PsiElement(type, *children) {

    override val annotations: MutableList<Annotation> = mutableListOf()

    interface PsiStatementTypeDef : PsiElementTypeDef {

    }

    open class Block(range: IntRange, vararg children: PsiElement) : PsiStatement(Block, range, *children) {

        val begin = children.filterIsInstance<PsiToken>().firstOrNull { it.type is PsiTokenType.PUNCTUATION }
        val statements = children.filterIsInstance<PsiStatement>()
        val end = children.filterIsInstance<PsiToken>().lastOrNull { it.type is PsiTokenType.PUNCTUATION }

        companion object : PsiStatementTypeDef {
            override val typeName: String = "Block"
            override val builder: NodeBuilderFn = { markerInfo, children, range ->
                if (children.size >= 2 && children.first().type == PsiTokenType.PUNCTUATION && children.last().type == PsiTokenType.PUNCTUATION) {
                    Block(range, *children)
                } else null
            }
        }
    }

    class Catch(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : PsiStatement(type, range, *children) {

    }

    class Import<T : PsiElement>(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : PsiStatement(type, range, *children), PsiReference<T> {
        override var reference: T? = null
    }

    class Flow(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : PsiStatement(type, range, *children) {

    }

    class Decl(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : PsiStatement(type, range, *children) {

    }

    sealed class Expr(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : PsiStatement(type, range, *children) {

        class Grouped(range: IntRange, vararg children: PsiElement) : Expr(Grouped, range, *children) {
            val lParen = children.first() as? PsiToken
            val operand = children.firstInstance<Expr>()
            val rParen = children.last() as? PsiToken

            init {
                if (rParen == null || rParen.type != PsiTokenType.PUNCTUATION) {
                    annotations.add(Annotation.error(this, "Terminating ')' is missing"))
                }
            }

            companion object : PsiStatementTypeDef {
                override val typeName = "Grouped"

                override val builder: NodeBuilderFn = { markerInfo, children, range ->
                    if (
                        children.size >= 1
                        && children.firstInstance<Expr>() != null
                    ) {
                        Grouped(range, *children)
                    } else null
                }
            }
        }

        class Identifier(range: IntRange, vararg children: PsiElement) : Expr(Identifier, range, *children), PsiReference<PsiElement>, Named {
            override var reference: PsiElement? = null
            override val name: String? = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.IDENTIFIER }?.value

            companion object : PsiStatementTypeDef {
                override val typeName = "Identifier"
                override val builder: NodeBuilderFn = { markerInfo, children, range ->
                    if (
                        children.size == 1
                        && children[0].type == PsiTokenType.IDENTIFIER
                    ) {
                        Identifier(range, *children)
                    } else null
                }
            }
        }

        sealed class Literal(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : Expr(type, range, *children) {

            open class Integer(range: IntRange, vararg children: PsiElement) : Literal(Integer, range, *children) {

                val literal = children.filterIsInstance<PsiToken>().firstOrNull { it.type is PsiTokenType.LITERAL.INTEGER }

                companion object : PsiStatementTypeDef {
                    override val builder: NodeBuilderFn = { markerInfo, children, range ->
                        if (
                            children.size == 1
                            && children[0].type is PsiTokenType.LITERAL.INTEGER
                        ) {
                            Integer(range, *children)
                        } else null
                    }
                    override val typeName = "Integer"
                }

            }

            open class Char(range: IntRange, vararg children: PsiElement) : Literal(Char, range, *children) {

                val literal = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.LITERAL.CHAR }

                companion object : PsiStatementTypeDef {
                    override val typeName = "Char"
                    override val builder: NodeBuilderFn = { markerInfo, children, range ->
                        if (
                            children.size == 1
                            && children[0].type is PsiTokenType.LITERAL.CHAR
                        ) {
                            Char(range, *children)
                        } else null
                    }
                }
            }

            class Bool(range: IntRange, vararg children: PsiElement) : Literal(Bool, range, *children) {

                val literal = children.filterIsInstance<PsiToken>().firstOrNull { it.type is PsiTokenType.KEYWORD }

                companion object : PsiStatementTypeDef {
                    override val typeName = "Bool"
                    override val builder: NodeBuilderFn = { markerInfo, children, range ->
                        if (
                            children.size == 1
                            && children[0].type is PsiTokenType.KEYWORD
                        ) {
                            Bool(range, *children)
                        } else null
                    }
                }
            }

            class Null(range: IntRange, vararg children: PsiElement) : Literal(Null, range, *children) {

                val literal = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.KEYWORD }

                companion object : PsiStatementTypeDef {
                    override val typeName = "Null"
                    override val builder: NodeBuilderFn = { markerInfo, children, range ->
                        if (
                            children.size == 1
                            && children[0].type is PsiTokenType.KEYWORD
                        ) {
                            Null(range, *children)
                        } else null
                    }
                }
            }

            sealed class FloatingPoint(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : Literal(type, range, *children) {

                open class Float(range: IntRange, vararg children: PsiElement) : FloatingPoint(Float, range, *children) {
                    val literal = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.LITERAL.FP.FLOAT }

                    companion object : PsiStatementTypeDef {
                        override val typeName = "Float"
                        override val builder: NodeBuilderFn = { markerInfo, children, range ->
                            if (
                                children.size == 1
                                && children[0].type is PsiTokenType.LITERAL.FP.FLOAT
                            ) {
                                Float(range, *children)
                            } else null
                        }
                    }
                }

                open class Double(range: IntRange, vararg children: PsiElement) : FloatingPoint(Double, range, *children) {

                    val literal = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.LITERAL.FP.DOUBLE }

                    companion object : PsiStatementTypeDef {
                        override val typeName = "Double"
                        override val builder: NodeBuilderFn = { markerInfo, children, range ->
                            if (
                                children.size == 1
                                && children[0].type is PsiTokenType.LITERAL.FP.DOUBLE
                            ) {
                                Double(range, *children)
                            } else null
                        }
                    }
                }
            }

            sealed class String(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : Literal(type, range, *children) {
                abstract val content: List<PsiStringElement>

                class MultiLine(range: IntRange, vararg children: PsiElement) : String(MultiLine, range, *children) {

                    val begin = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.LITERAL.STRING.MlStart }
                    val end = children.filterIsInstance<PsiToken>().lastOrNull { it.type == PsiTokenType.LITERAL.STRING.MlEnd }
                    override val content = children.filterIsInstance<PsiStringElement>()

                    companion object : PsiStatementTypeDef {
                        override val typeName = "MLString"
                        override val builder: NodeBuilderFn = { markerInfo, children, range ->
                            if (
                                children.size >= 2
                                && children.first().type == PsiTokenType.LITERAL.STRING.MlStart
                                && children.last().type == PsiTokenType.LITERAL.STRING.MlEnd
                                && children.drop(1).dropLast(1).all { it is PsiStringElement }
                            ) {
                                MultiLine(range, *children)
                            } else null
                        }
                    }
                }

                class SingleLine(range: IntRange, vararg children: PsiElement) : String(SingleLine, range, *children) {

                    val begin = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.LITERAL.STRING.SlStart }
                    val end = children.filterIsInstance<PsiToken>().lastOrNull { it.type == PsiTokenType.LITERAL.STRING.SlEnd }
                    override val content = children.filterIsInstance<PsiStringElement>()

                    companion object : PsiStatementTypeDef {
                        override val typeName = "SLString"
                        override val builder: NodeBuilderFn = { markerInfo, children, range ->
                            if (
                                children.size >= 2
                                && children.first().type == PsiTokenType.LITERAL.STRING.SlStart
                                && children.last().type == PsiTokenType.LITERAL.STRING.SlEnd
                                && children.drop(1).dropLast(1).all { it is PsiStringElement }
                            ) {
                                SingleLine(range, *children)
                            } else null
                        }
                    }
                }
            }
        }

        class OperationInfix(override val type: OperationInfixT, range: IntRange, vararg children: PsiElement) : Expr(type, range, *children) {

            val leftOperand = children[0] as Expr
            val operator = children[1] as PsiToken
            val rightOperand = children[2] as Expr

            class OperationInfixT(val opType: OpType) : PsiStatementTypeDef {
                override val typeName = opType.name.lowercase()
                override val builder: NodeBuilderFn = { markerInfo, children, range ->
                    if (
                        children.size == 3
                        && children[0] is Expr
                        && children[1].type == PsiTokenType.OPERATOR
                        && children[2] is Expr
                        && markerInfo.elementType is OperationInfixT
                    ) {
                        OperationInfix(markerInfo.elementType, range, *children)
                    } else null
                }
            }
        }

        class OperationPrefix(override val type: OperationPrefixT, range: IntRange, vararg children: PsiElement) : Expr(type, range, *children) {

            val operator = children[0] as PsiToken
            val operand = children[1] as Expr

            class OperationPrefixT(val opType: OpType) : PsiStatementTypeDef {
                override val typeName = opType.name.lowercase()
                override val builder: NodeBuilderFn = { markerInfo, children, range ->
                    if (
                        children.size == 2
                        && children[0].type == PsiTokenType.OPERATOR
                        && children[1] is Expr
                        && markerInfo.elementType is OperationPrefixT
                    ) {
                        OperationPrefix(markerInfo.elementType, range, *children)
                    } else null
                }
            }
        }

        class OperationPostfix(override val type: OperationPostFixT, range: IntRange, vararg children: PsiElement) : Expr(type, range, *children) {

            val operand = children[0] as Expr
            val operator = children[1] as PsiToken


            class OperationPostFixT(val opType: OpType) : PsiStatementTypeDef {
                override val typeName = opType.name.lowercase()
                override val builder: NodeBuilderFn = { markerInfo, children, range ->
                    if (
                        children.size == 2
                        && children[0] is Expr
                        && children[1].type == PsiTokenType.OPERATOR
                        && markerInfo.elementType is OperationPostFixT
                    ) {
                        OperationPostfix(markerInfo.elementType, range, *children)
                    } else null
                }
            }
        }

        class InfixFunctionCall(range: IntRange, vararg children: PsiElement) : Expr(InfixFunctionCall, range, *children) {

            val leftOperand = children[0] as Expr
            val funcIdentifier = children[1] as PsiToken
            val rightOperand = children[2] as Expr

            companion object : PsiStatementTypeDef {
                override val typeName = "InfixFunctionCall"
                override val builder: NodeBuilderFn = { markerInfo, children, range ->
                    if (
                        children.size == 3
                        && children[0] is Expr
                        && children[1].type == PsiTokenType.IDENTIFIER
                        && children[2] is Expr
                    ) {
                        InfixFunctionCall(range, *children)
                    } else null
                }
            }
        }

        class FunctionCall(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : Expr(type, range, *children) {

        }
    }

    sealed class PsiStringElement(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : PsiStatement(type, range, *children) {

        class Basic(range: IntRange, vararg children: PsiElement) : PsiStringElement(Basic, range, *children) {

            val value: String = (children[0] as PsiToken).value

            companion object : PsiStatementTypeDef {
                override val typeName = "Literal"
                override val builder: NodeBuilderFn = { markerInfo, children, range ->
                    if (
                        children.size == 1
                        && children[0].type == PsiTokenType.LITERAL.STRING.CONTENT.Basic
                    ) {
                        Basic(range, *children)
                    } else null
                }
            }

        }

        class Escaped(range: IntRange, vararg children: PsiElement) : PsiStringElement(Escaped, range, *children) {

            val value: String = (children[0] as PsiToken).value

            companion object : PsiStatementTypeDef {
                override val typeName = "Escaped"
                override val builder: NodeBuilderFn = { markerInfo, children, range ->
                    if (
                        children.size == 1
                        && children[0].type == PsiTokenType.LITERAL.STRING.CONTENT.Escaped
                    ) {
                        Escaped(range, *children)
                    } else null
                }
            }
        }

        sealed class Interpolated(type: PsiStatementTypeDef, range: IntRange, vararg children: PsiElement) : PsiStringElement(type, range, *children) {

            class InterpIdentifier(range: IntRange, vararg children: PsiElement) : Interpolated(InterpIdentifier, range, *children) {

                val interpStart = children[0] as PsiToken
                val identifier = children[1] as PsiToken

                companion object : PsiStatementTypeDef {
                    override val typeName = "Interpolated"
                    override val builder: NodeBuilderFn = { markerInfo, children, range ->
                        if (
                            children.size == 2
                            && children[0].type == PsiTokenType.LITERAL.STRING.INTERP.Single
                            && children[1].type == PsiTokenType.IDENTIFIER
                        ) {
                            Escaped(range, *children)
                        } else null
                    }
                }
            }

            class InterpBlock(range: IntRange, vararg children: PsiElement) : Interpolated(InterpBlock, range, *children) {

                val blockBegin = children[0] as PsiToken
                val statements = children.filterIsInstance<PsiStatement>()
                val blockEnd = children.last() as PsiToken

                companion object : PsiStatementTypeDef {
                    override val typeName = "Interpolated"
                    override val builder: NodeBuilderFn = { markerInfo, children, range ->
                        if (
                            children.size >= 2
                            && children[0].type == PsiTokenType.LITERAL.STRING.INTERP.BlockStart
                            && children.last().type == PsiTokenType.LITERAL.STRING.INTERP.BlockEnd
                            && children.drop(1).dropLast(1).all { it is PsiStatement }
                        ) {
                            InterpBlock(range, *children)
                        } else null
                    }
                }
            }
        }
    }
}
