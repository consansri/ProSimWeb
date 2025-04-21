package cengine.psi.semantic.expr

import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.util.integer.BigInt

/**
 * Evaluates integer expressions represented by PSI nodes (`PsiStatement.Expr`),
 * resulting in a BigInt value. Extends the abstract ExpressionEvaluator.
 *
 * @param C The type of the optional context object passed to the identifier resolver.
 * @param resolveIdentifierLambda A lambda function to resolve an identifier's value to BigInt?.
 */
open class IntegerExpressionEvaluator<C>(
    resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> BigInt?,
) : ExpressionEvaluator<BigInt, C>(resolveIdentifierLambda) { // Pass lambda to super constructor

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
    override fun evaluateInfixOperation(op: String, opToken: PsiToken, left: BigInt, right: BigInt, context: C): BigInt {
        try {
            return when (op) {
                "+" -> left + right
                "-" -> left - right
                "*" -> left * right
                "/" -> {
                    if (right == BigInt.ZERO) throw EvaluationException("Division by zero", opToken)
                    left / right // Integer division
                }

                "%" -> {
                    if (right == BigInt.ZERO) throw EvaluationException("Modulo by zero", opToken)
                    left % right
                }

                "<<" -> left shl right.toIntOrThrow(opToken, "Left shift amount")
                ">>" -> left shr right.toIntOrThrow(opToken, "Right shift amount")
                "&" -> left and right
                "|" -> left or right
                "^" -> left xor right
                "==" -> if (left == right) BigInt.ONE else BigInt.ZERO
                "!=" -> if (left != right) BigInt.ONE else BigInt.ZERO
                "<" -> if (left < right) BigInt.ONE else BigInt.ZERO
                "<=" -> if (left <= right) BigInt.ONE else BigInt.ZERO
                ">" -> if (left > right) BigInt.ONE else BigInt.ZERO
                ">=" -> if (left >= right) BigInt.ONE else BigInt.ZERO
                "&&" -> if (left != BigInt.ZERO && right != BigInt.ZERO) BigInt.ONE else BigInt.ZERO
                "||" -> if (left != BigInt.ZERO || right != BigInt.ZERO) BigInt.ONE else BigInt.ZERO
                else -> throw EvaluationException("Unsupported infix operator for Integer: '$op'", opToken)
            }
        } catch (e: ArithmeticException) {
            throw EvaluationException("Arithmetic error during '$op': ${e.message}", opToken, e)
        }
    }

    /**
     * Implements prefix operations for BigInt.
     */
    override fun evaluatePrefixOperation(op: String, opToken: PsiToken, operand: BigInt, context: C): BigInt {
        return when (op) {
            "+" -> operand
            "-" -> -operand
            "~" -> operand.inv()
            "!" -> if (operand == BigInt.ZERO) BigInt.ONE else BigInt.ZERO // Logical NOT
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