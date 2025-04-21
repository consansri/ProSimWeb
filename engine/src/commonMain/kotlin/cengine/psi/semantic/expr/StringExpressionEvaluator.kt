package cengine.psi.semantic.expr

import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.util.integer.BigInt

/**
 * Evaluates string expressions represented by PSI nodes (`PsiStatement.Expr`),
 * resulting in a String value. Extends the abstract ExpressionEvaluator.
 * Supports concatenation and comparisons. Other literals are converted to string representations.
 *
 * **Note:** String interpolation within literals is currently NOT supported.
 *
 * @param C The type of the optional context object passed to the identifier resolver.
 * @param resolveIdentifierLambda A lambda function to resolve an identifier's value to String?.
 */
open class StringExpressionEvaluator<C>(
    resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> String?
) : ExpressionEvaluator<String, C>(resolveIdentifierLambda) {

    /**
     * Parses string, char, and other basic literals into their string representation.
     */
    override fun parseLiteral(expr: PsiStatement.Expr.Literal, context: C): String {
        return when (expr) {
            is PsiStatement.Expr.Literal.String -> parseStringLiteralToken(expr, context)
            is PsiStatement.Expr.Literal.Char -> parseCharLiteralToken(expr)
            // Convert other known literals to their string representation
            is PsiStatement.Expr.Literal.Integer -> parseIntegerLiteralToken(expr).toString() // Reuse BigInt parse for accuracy
            is PsiStatement.Expr.Literal.FloatingPoint -> parseFloatLiteralToken(expr).toString() // Reuse Double parse
            is PsiStatement.Expr.Literal.Bool -> expr.literal?.value ?: "false" // "true" or "false"
            is PsiStatement.Expr.Literal.Null -> "null"
        }
    }

    /**
     * Implements infix operations for String. '+' is concatenation. Returns "1"/"0" for comparisons.
     */
    override fun evaluateInfixOperation(op: String, opToken: PsiToken, left: String, right: String, context: C): String {
        return when (op) {
            // Concatenation
            "+" -> left + right

            // Comparison (lexicographical, return "1" for true, "0" for false)
            "==" -> if (left == right) "1" else "0"
            "!=" -> if (left != right) "1" else "0"
            "<" -> if (left < right) "1" else "0"
            "<=" -> if (left <= right) "1" else "0"
            ">" -> if (left > right) "1" else "0"
            ">=" -> if (left >= right) "1" else "0"

            // Unsupported Operators
            "-", "*", "/", "%", "&", "|", "^", "<<", ">>", "&&", "||" ->
                throw EvaluationException("Operator '$op' is not supported for String evaluation", opToken)

            else -> throw EvaluationException("Unknown infix operator: '$op'", opToken)
        }
    }

    /**
     * Prefix operations are generally not supported for String evaluation.
     */
    override fun evaluatePrefixOperation(op: String, opToken: PsiToken, operand: String, context: C): String {
        throw EvaluationException("Prefix operator '$op' is not supported for String evaluation", opToken)
    }

    // --- Helper Methods ---

    private fun parseStringLiteralToken(expr: PsiStatement.Expr.Literal.String, context: C): String {
        val sb = StringBuilder()
        expr.content.forEach { element ->
            when (element) {
                is PsiStatement.PsiStringElement.Basic -> sb.append(element.value)
                is PsiStatement.PsiStringElement.Escaped -> sb.append(element.value)
                is PsiStatement.PsiStringElement.Interpolated -> throw EvaluationException(
                    "String interpolation ('${element.type}'') is not supported by this basic evaluator", element
                )
            }
        }
        return sb.toString()
    }

    private fun parseCharLiteralToken(expr: PsiStatement.Expr.Literal.Char): String {
        return expr.literal?.value ?: throw EvaluationException("Invalid char literal node (missing token)", expr)
    }

    // Helpers to parse other literals just for their string representation
    // Using simple/potentially lossy conversions here - adjust if precise string form needed
    private fun parseIntegerLiteralToken(expr: PsiStatement.Expr.Literal.Integer): BigInt {
        val literal = expr.literal ?: return BigInt.ZERO // Default on missing token
        val value = literal.value
        return try {
            return when(literal.type) {
                PsiTokenType.LITERAL.INTEGER.Hex -> BigInt.parse(value, 16)
                PsiTokenType.LITERAL.INTEGER.Bin -> BigInt.parse(value, 2)
                PsiTokenType.LITERAL.INTEGER.Oct -> BigInt.parse(value, 8)
                PsiTokenType.LITERAL.INTEGER.Dec -> BigInt.parse(value, 10)
                // Fallback to Decimal if no type specified (should never happen)
                else -> BigInt.parse(value, 10)
            }
        } catch (e: Exception) { BigInt.ZERO } // Default on parse error
    }

    private fun parseFloatLiteralToken(expr: PsiStatement.Expr.Literal.FloatingPoint): Double {
        val literal = when(expr) {
            is PsiStatement.Expr.Literal.FloatingPoint.Float -> expr.literal
            is PsiStatement.Expr.Literal.FloatingPoint.Double -> expr.literal
        } ?: return 0.0
        return try {
            literal.value.replace("_", "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) { 0.0 }
    }
}