package cengine.psi.semantic.expr

import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.util.integer.BigInt
import kotlin.math.abs

/**
 * Evaluates floating-point expressions represented by PSI nodes (`PsiStatement.Expr`),
 * resulting in a Float value. Extends the abstract ExpressionEvaluator.
 *
 * @param C The type of the optional context object passed to the identifier resolver.
 * @param resolveIdentifierLambda A lambda function to resolve an identifier's value to Float?.
 */
open class FloatExpressionEvaluator<C>(
    resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> Float?
) : ExpressionEvaluator<Float, C>(resolveIdentifierLambda) {

    private val floatEpsilon = 1e-6f // Epsilon for float comparisons

    /**
     * Parses integer and floating-point literals. Throws exceptions for others.
     */
    override fun parseLiteral(expr: PsiStatement.Expr.Literal, context: C): Float {
        return when (expr) {
            is PsiStatement.Expr.Literal.Integer -> parseIntegerLiteralAsFloat(expr)
            is PsiStatement.Expr.Literal.FloatingPoint -> parseFloatLiteralToken(expr)
            else -> throw EvaluationException("Unsupported literal type '${expr::class.simpleName}' for Float evaluation", expr)
        }
    }

    /**
     * Implements infix operations for Float. Returns 1.0f/0.0f for comparisons/logical ops.
     */
    override fun evaluateInfixOperation(op: String, opToken: PsiToken, left: Float, right: Float, context: C): Float {
        return when (op) {
            // Arithmetic
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> {
                if (right == 0.0f) throw EvaluationException("Division by zero", opToken)
                left / right
            }
            // "%" -> left % right // Remainder
            // "**" -> left.pow(right) // Power

            // Comparison (return 1.0f for true, 0.0f for false)
            "==" -> if (abs(left - right) < floatEpsilon) 1.0f else 0.0f
            "!=" -> if (abs(left - right) >= floatEpsilon) 1.0f else 0.0f
            "<" -> if (left < right) 1.0f else 0.0f
            "<=" -> if (left <= right) 1.0f else 0.0f
            ">" -> if (left > right) 1.0f else 0.0f
            ">=" -> if (left >= right) 1.0f else 0.0f

            // Logical (treat non-zero as true, return 1.0f for true, 0.0f for false)
            "&&" -> if (left != 0.0f && right != 0.0f) 1.0f else 0.0f
            "||" -> if (left != 0.0f || right != 0.0f) 1.0f else 0.0f

            // Unsupported Operators
            "%", "&", "|", "^", "<<", ">>", "~", "!" ->
                throw EvaluationException("Operator '$op' is not supported for Float evaluation", opToken)

            else -> throw EvaluationException("Unknown infix operator: '$op'", opToken)
        }
    }

    /**
     * Implements prefix operations for Float.
     */
    override fun evaluatePrefixOperation(op: String, opToken: PsiToken, operand: Float, context: C): Float {
        return when (op) {
            "+" -> operand // Unary plus
            "-" -> -operand // Unary minus

            // Unsupported Operators
            "~", "!" -> throw EvaluationException("Operator '$op' is not supported for Float evaluation", opToken)

            else -> throw EvaluationException("Unsupported prefix operator: '$op'", opToken)
        }
    }

    // --- Helper Methods ---

    private fun parseIntegerLiteralAsFloat(expr: PsiStatement.Expr.Literal.Integer): Float {
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
            }).value.floatValue()
        } catch (e: NumberFormatException) {
            throw EvaluationException("Invalid integer literal format for value: '$value'", expr, e)
        }
    }

    private fun parseFloatLiteralToken(expr: PsiStatement.Expr.Literal.FloatingPoint): Float {
        val literal = when(expr) {
            is PsiStatement.Expr.Literal.FloatingPoint.Float -> expr.literal
            is PsiStatement.Expr.Literal.FloatingPoint.Double -> expr.literal
        } ?: throw EvaluationException("Invalid floating point literal node (missing token)", expr)

        val value = literal.value.replace("_", "")
        val floatValue = if (value.endsWith('f', ignoreCase = true)) {
            value.substring(0, value.length - 1)
        } else {
            value // Assume parsable as float/double
        }

        try {
            // Using toDouble().toFloat() can sometimes be more robust for various formats
            return floatValue.toDouble().toFloat()
        } catch (e: NumberFormatException) {
            throw EvaluationException("Invalid floating point literal format: '$value'", expr, e)
        }
    }
}