package cengine.lang.asm

import cengine.psi.parser.pratt.OpType
import cengine.psi.parser.pratt.Operator

object AsmLexer {

    val PUNCTUATIONS = setOf(":", ",", "@")
    val SPECIAL_CHARS = setOf('_', '.')

    const val ESCAPE_CHAR = '\\'

    val OPERATORS = setOf(
        Operator("+", OpType.ADD),
        Operator("-", OpType.SUB),
        Operator("*", OpType.MUL),
        Operator("/", OpType.DIV),
        Operator("%", OpType.MOD),

        Operator( "+", OpType.UNARY_PLUS),
        Operator("-", OpType.UNARY_MINUS),

        Operator("&", OpType.BITWISE_AND),
        Operator("|", OpType.BITWISE_OR),
        Operator("^", OpType.BITWISE_XOR),
        Operator("~", OpType.BITWISE_NOT),
        Operator("<<", OpType.SHIFT_LEFT),
        Operator(">>", OpType.SHIFT_RIGHT),

        Operator("==", OpType.EQUAL),
        Operator("!=", OpType.NOT_EQUAL),
        Operator("<", OpType.LESS_THAN),
        Operator(">", OpType.GREATER_THAN),
        Operator("<=", OpType.LESS_THAN),
        Operator(">=", OpType.GREATER_THAN),

        Operator("&&", OpType.LOGICAL_AND),
        Operator("||", OpType.LOGICAL_OR),
        Operator("!", OpType.LOGICAL_NOT),

        Operator("=", OpType.ASSIGN)
    )
}