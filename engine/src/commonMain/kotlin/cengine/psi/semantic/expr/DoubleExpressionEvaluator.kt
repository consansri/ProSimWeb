package cengine.psi.semantic.expr

import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
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
    resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> Double?
) : ExpressionEvaluator<Double, C>(resolveIdentifierLambda) {

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
    override fun evaluateInfixOperation(op: String, opToken: PsiToken, left: Double, right: Double, context: C): Double {
        return when (op) {
            // Arithmetic
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> {
                if (right == 0.0) throw EvaluationException("Division by zero", opToken)
                left / right
            }
            // "%" -> left % right // Remainder
            // "**" -> left.pow(right) // Power

            // Comparison (return 1.0 for true, 0.0 for false)
            // Using epsilon comparison for robust float equality check
            "==" -> if (abs(left - right) < doubleEpsilon) 1.0 else 0.0
            "!=" -> if (abs(left - right) >= doubleEpsilon) 1.0 else 0.0
            "<" -> if (left < right) 1.0 else 0.0
            "<=" -> if (left <= right) 1.0 else 0.0
            ">" -> if (left > right) 1.0 else 0.0
            ">=" -> if (left >= right) 1.0 else 0.0

            // Logical (treat non-zero as true, return 1.0 for true, 0.0 for false)
            "&&" -> if (left != 0.0 && right != 0.0) 1.0 else 0.0
            "||" -> if (left != 0.0 || right != 0.0) 1.0 else 0.0

            // Unsupported Operators
            "%", "&", "|", "^", "<<", ">>", "~", "!" ->
                throw EvaluationException("Operator '$op' is not supported for Double evaluation", opToken)

            else -> throw EvaluationException("Unknown infix operator: '$op'", opToken)
        }
    }

    /**
     * Implements prefix operations for Double.
     */
    override fun evaluatePrefixOperation(op: String, opToken: PsiToken, operand: Double, context: C): Double {
        return when (op) {
            "+" -> operand // Unary plus
            "-" -> -operand // Unary minus

            // Unsupported Operators
            "~", "!" -> throw EvaluationException("Operator '$op' is not supported for Double evaluation", opToken)

            else -> throw EvaluationException("Unsupported prefix operator: '$op'", opToken)
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