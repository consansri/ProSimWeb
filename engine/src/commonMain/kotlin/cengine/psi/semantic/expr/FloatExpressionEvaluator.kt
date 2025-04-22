package cengine.psi.semantic.expr

import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.pratt.OpType
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
    processAssignment: (identifier: PsiStatement.Expr, value: Float, context: C) -> Unit = { _, _, _ -> },
    resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> Float?
) : ExpressionEvaluator<Float, C>(processAssignment,resolveIdentifierLambda) {

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
    override fun evaluateInfixOperation(op: OpType, opToken: PsiToken, left: Float, right: Float, context: C): Float {
        return when (op) {
            // Arithmetic
            OpType.ADD, OpType.ADD_ASSIGN -> left + right
            OpType.SUB, OpType.SUB_ASSIGN -> left - right
            OpType.MUL, OpType.MUL_ASSIGN -> left * right
            OpType.DIV, OpType.DIV_ASSIGN -> {
                if (right == 0.0f) throw EvaluationException("Division by zero", opToken)
                left / right
            }

            // "%" -> left % right // Remainder
            // "**" -> left.pow(right) // Power

            // Comparison (return 1.0f for true, 0.0f for false)
            OpType.EQUAL -> if (abs(left - right) < floatEpsilon) 1.0f else 0.0f
            OpType.NOT_EQUAL -> if (abs(left - right) >= floatEpsilon) 1.0f else 0.0f
            OpType.LESS_THAN -> if (left < right) 1.0f else 0.0f
            OpType.LESS_EQUAL -> if (left <= right) 1.0f else 0.0f
            OpType.GREATER_THAN -> if (left > right) 1.0f else 0.0f
            OpType.GREATER_EQUAL -> if (left >= right) 1.0f else 0.0f

            // Logical (treat non-zero as true, return 1.0f for true, 0.0f for false)
            OpType.LOGICAL_AND -> if (left != 0.0f && right != 0.0f) 1.0f else 0.0f
            OpType.LOGICAL_OR -> if (left != 0.0f || right != 0.0f) 1.0f else 0.0f

            // Unsupported Operators
            else ->
                throw EvaluationException("Operator '$op' is not supported for Float evaluation", opToken)
        }
    }

    /**
     * Implements prefix operations for Float.
     */
    override fun evaluatePrefixOperation(op: OpType, opToken: PsiToken, operand: Float, context: C): Float {
        return when (op) {
            OpType.UNARY_PLUS -> operand // Unary plus
            OpType.UNARY_MINUS -> -operand // Unary minus

            // Unsupported Operators
            OpType.BITWISE_NOT, OpType.LOGICAL_NOT -> throw EvaluationException("Operator '$op' is not supported for Float evaluation", opToken)

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