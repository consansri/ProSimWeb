package cengine.psi.semantic.expr

import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.pratt.OpType
import cengine.util.integer.BigInt

/**
 * Evaluates integer expressions represented by PSI nodes (`PsiStatement.Expr`),
 * resulting in a BigInt value. Extends the abstract ExpressionEvaluator.
 *
 * @param C The type of the optional context object passed to the identifier resolver.
 * @param resolveIdentifierLambda A lambda function to resolve an identifier's value to BigInt?.
 */
open class IntegerExpressionEvaluator<C>(
    processAssignment: (identifier: PsiStatement.Expr, value: BigInt, context: C) -> Unit = { _, _, _ -> },
    resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> BigInt?,
) : ExpressionEvaluator<BigInt, C>(processAssignment,resolveIdentifierLambda) { // Pass lambda to super constructor

    /**
     * Parses integer and boolean literals. Throws exceptions for other literal types.
     */
    override fun parseLiteral(expr: PsiStatement.Expr.Literal, context: C): BigInt {
        return when (expr) {
            is PsiStatement.Expr.Literal.Integer -> parseIntegerLiteralToken(expr)
            is PsiStatement.Expr.Literal.Bool -> { // Treat boolean literals as 1 or 0
                val boolVal = expr.literal?.value?.lowercase()
                when (boolVal) {
                    "true" -> BigInt.ONE
                    "false" -> BigInt.ZERO
                    else -> throw EvaluationException("Unsupported boolean literal value: ${expr.literal?.value}", expr)
                }
            }

            else -> throw EvaluationException("Unsupported literal type '${expr::class.simpleName}' for Integer evaluation", expr)
        }
    }

    /**
     * Implements infix operations for BigInt. Returns 1/0 for comparisons/logical ops.
     */
    override fun evaluateInfixOperation(op: OpType, opToken: PsiToken, left: BigInt, right: BigInt, context: C): BigInt {
        try {
            return when (op) {
                OpType.ADD, OpType.ADD_ASSIGN -> left + right
                OpType.SUB, OpType.SUB_ASSIGN -> left - right
                OpType.MUL, OpType.MUL_ASSIGN -> left * right
                OpType.DIV, OpType.DIV_ASSIGN -> {
                    if (right == BigInt.ZERO) throw EvaluationException("Division by zero", opToken)
                    left / right // Integer division
                }

                OpType.MOD, OpType.MOD_ASSIGN -> {
                    if (right == BigInt.ZERO) throw EvaluationException("Modulo by zero", opToken)
                    left % right
                }

                OpType.SHIFT_LEFT -> left shl right.toIntOrThrow(opToken, "Left shift amount")
                OpType.SHIFT_RIGHT -> left shr right.toIntOrThrow(opToken, "Right shift amount")
                OpType.BITWISE_AND -> left and right
                OpType.BITWISE_OR -> left or right
                OpType.BITWISE_XOR -> left xor right
                OpType.EQUAL -> if (left == right) BigInt.ONE else BigInt.ZERO
                OpType.NOT_EQUAL -> if (left != right) BigInt.ONE else BigInt.ZERO
                OpType.LESS_THAN -> if (left < right) BigInt.ONE else BigInt.ZERO
                OpType.LESS_EQUAL -> if (left <= right) BigInt.ONE else BigInt.ZERO
                OpType.GREATER_THAN -> if (left > right) BigInt.ONE else BigInt.ZERO
                OpType.GREATER_EQUAL -> if (left >= right) BigInt.ONE else BigInt.ZERO
                OpType.LOGICAL_AND -> if (left != BigInt.ZERO && right != BigInt.ZERO) BigInt.ONE else BigInt.ZERO
                OpType.LOGICAL_OR -> if (left != BigInt.ZERO || right != BigInt.ZERO) BigInt.ONE else BigInt.ZERO
                else -> throw EvaluationException("Unsupported infix operator for Integer: '$op'", opToken)
            }
        } catch (e: ArithmeticException) {
            throw EvaluationException("Arithmetic error during '$op': ${e.message}", opToken, e)
        }
    }

    /**
     * Implements prefix operations for BigInt.
     */
    override fun evaluatePrefixOperation(op: OpType, opToken: PsiToken, operand: BigInt, context: C): BigInt {
        return when (op) {
            OpType.UNARY_PLUS -> operand
            OpType.UNARY_MINUS -> -operand
            OpType.BITWISE_NOT -> operand.inv()
            OpType.LOGICAL_NOT -> if (operand == BigInt.ZERO) BigInt.ONE else BigInt.ZERO // Logical NOT
            else -> throw EvaluationException("Unsupported prefix operator for Integer: '$op'", opToken)
        }
    }

    // --- Helper Methods specific to Integer evaluation ---

    private fun parseIntegerLiteralToken(expr: PsiStatement.Expr.Literal.Integer): BigInt {
        val literal = expr.literal ?: throw EvaluationException("Invalid integer literal node (missing token)", expr)
        val value = literal.value
        try {
            return when (literal.type) {
                PsiTokenType.LITERAL.INTEGER.Hex -> BigInt.parse(value, 16)
                PsiTokenType.LITERAL.INTEGER.Bin -> BigInt.parse(value, 2)
                PsiTokenType.LITERAL.INTEGER.Oct -> BigInt.parse(value, 8)
                PsiTokenType.LITERAL.INTEGER.Dec -> BigInt.parse(value, 10)
                // Fallback to Decimal if no type specified (should never happen)
                else -> BigInt.parse(value, 10)
            }
        } catch (e: NumberFormatException) {
            throw EvaluationException("Invalid integer literal format for value: '$value' (type: ${literal.type})", expr, e)
        }
    }

    private fun BigInt.toIntOrThrow(element: PsiElement, purpose: String): Int {
        try {
            return this.toInt()
        } catch (e: ArithmeticException) {
            throw EvaluationException("$purpose value ($this) exceeds 32-bit integer range", element, e)
        }
    }
}