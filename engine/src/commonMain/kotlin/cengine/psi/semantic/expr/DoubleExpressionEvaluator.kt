package cengine.psi.semantic.expr

import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.pratt.OpType
import cengine.util.integer.BigInt
import kotlin.math.abs

/**
 * Evaluates floating-point expressions represented by PSI nodes (`PsiStatement.Expr`),
 * resulting in a Double value. Extends the abstract ExpressionEvaluator.
 *
 * @param C The type of the optional context object passed to the identifier resolver.
 * @param resolveIdentifierLambda A lambda function to resolve an identifier's value to Double?.
 */
open class DoubleExpressionEvaluator<C>(
    processAssignment: (identifier: PsiStatement.Expr, value: Double, context: C) -> Unit = { _, _, _ -> },
    resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> Double?
) : ExpressionEvaluator<Double, C>(processAssignment,resolveIdentifierLambda) {

    // Using a small epsilon for double comparisons if needed, direct comparison often okay
    private val doubleEpsilon = 1e-9

    /**
     * Parses integer and floating-point literals. Throws exceptions for others.
     */
    override fun parseLiteral(expr: PsiStatement.Expr.Literal, context: C): Double {
        return when (expr) {
            is PsiStatement.Expr.Literal.Integer -> parseIntegerLiteralAsDouble(expr)
            is PsiStatement.Expr.Literal.FloatingPoint -> parseDoubleLiteralToken(expr)
            else -> throw EvaluationException("Unsupported literal type '${expr::class.simpleName}' for Double evaluation", expr)
        }
    }

    /**
     * Implements infix operations for Double. Returns 1.0/0.0 for comparisons/logical ops.
     */
    override fun evaluateInfixOperation(op: OpType, opToken: PsiToken, left: Double, right: Double, context: C): Double {
        return when (op) {
            // Arithmetic
            OpType.ADD, OpType.ADD_ASSIGN -> left + right
            OpType.SUB, OpType.SUB_ASSIGN -> left - right
            OpType.MUL, OpType.MUL_ASSIGN -> left * right
            OpType.DIV, OpType.DIV_ASSIGN -> {
                if (right == 0.0) throw EvaluationException("Division by zero", opToken)
                left / right
            }

            // "%" -> left % right // Remainder
            // "**" -> left.pow(right) // Power

            // Comparison (return 1.0f for true, 0.0f for false)
            OpType.EQUAL -> if (abs(left - right) < doubleEpsilon) 1.0 else 0.0
            OpType.NOT_EQUAL -> if (abs(left - right) >= doubleEpsilon) 1.0 else 0.0
            OpType.LESS_THAN -> if (left < right) 1.0 else 0.0
            OpType.LESS_EQUAL -> if (left <= right) 1.0 else 0.0
            OpType.GREATER_THAN -> if (left > right) 1.0 else 0.0
            OpType.GREATER_EQUAL -> if (left >= right) 1.0 else 0.0

            // Logical (treat non-zero as true, return 1.0f for true, 0.0f for false)
            OpType.LOGICAL_AND -> if (left != 0.0 && right != 0.0) 1.0 else 0.0
            OpType.LOGICAL_OR -> if (left != 0.0 || right != 0.0) 1.0 else 0.0

            // Unsupported Operators
            else ->
                throw EvaluationException("Operator '$op' is not supported for Float evaluation", opToken)
        }
    }

    /**
     * Implements prefix operations for Double.
     */
    override fun evaluatePrefixOperation(op: OpType, opToken: PsiToken, operand: Double, context: C): Double {
        return when (op) {
            OpType.UNARY_PLUS -> operand // Unary plus
            OpType.UNARY_MINUS -> -operand // Unary minus

            // Unsupported Operators
            else -> throw EvaluationException("Operator '$op' is not supported for Double evaluation", opToken)
        }
    }

    // --- Helper Methods ---

    private fun parseIntegerLiteralAsDouble(expr: PsiStatement.Expr.Literal.Integer): Double {
        val literal = expr.literal ?: throw EvaluationException("Invalid integer literal node (missing token)", expr)
        val value = literal.value
        try {
            return (when(literal.type) {
                PsiTokenType.LITERAL.INTEGER.Hex -> BigInt.parse(value, 16)
                PsiTokenType.LITERAL.INTEGER.Bin -> BigInt.parse(value, 2)
                PsiTokenType.LITERAL.INTEGER.Oct -> BigInt.parse(value, 8)
                PsiTokenType.LITERAL.INTEGER.Dec -> BigInt.parse(value, 10)
                // Fallback to Decimal if no type specified (should never happen)
                else -> BigInt.parse(value, 10)
            }).value.doubleValue()
        } catch (e: NumberFormatException) {
            throw EvaluationException("Invalid integer literal format for value: '$value'", expr, e)
        }
    }

    private fun parseDoubleLiteralToken(expr: PsiStatement.Expr.Literal.FloatingPoint): Double {
        val literal = when(expr) {
            is PsiStatement.Expr.Literal.FloatingPoint.Float -> expr.literal
            is PsiStatement.Expr.Literal.FloatingPoint.Double -> expr.literal
        } ?: throw EvaluationException("Invalid floating point literal node (missing token)", expr)

        val value = literal.value.replace("_", "")
        val doubleValue = if (value.endsWith('f', ignoreCase = true)) {
            value.substring(0, value.length - 1)
        } else {
            value
        }

        try {
            return doubleValue.toDouble()
        } catch (e: NumberFormatException) {
            throw EvaluationException("Invalid floating point literal format: '$value'", expr, e)
        }
    }
}