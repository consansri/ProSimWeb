package cengine.psi.semantic.expr

import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.parser.pratt.OpType

/**
 * Evaluates boolean expressions represented by PSI nodes (`PsiStatement.Expr`),
 * resulting in a Boolean value. Extends the abstract ExpressionEvaluator.
 * Supports logical operators and equality/inequality.
 * Only directly supports boolean literals; other literals cause errors.
 *
 * @param C The type of the optional context object passed to the identifier resolver.
 * @param resolveIdentifierLambda A lambda function to resolve an identifier's value to Boolean?.
 */
open class BooleanExpressionEvaluator<C>(
    processAssignment: (identifier: PsiStatement.Expr, value: Boolean, context: C) -> Unit = { _, _, _ -> },
    resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> Boolean?
) : ExpressionEvaluator<Boolean, C>(processAssignment,resolveIdentifierLambda) {

    /**
     * Parses boolean literals. Throws exceptions for other literal types.
     */
    override fun parseLiteral(expr: PsiStatement.Expr.Literal, context: C): Boolean {
        return when (expr) {
            is PsiStatement.Expr.Literal.Bool -> {
                val boolVal = expr.literal?.value?.lowercase()
                when (boolVal) {
                    "true" -> true
                    "false" -> false
                    else -> throw EvaluationException("Unsupported boolean literal value: ${expr.literal?.value}", expr)
                }
            }
            // Option: Could interpret Integer 0 as false, non-zero as true, etc.
            // But throwing is stricter and often safer.
            // is PsiStatement.Expr.Literal.Integer -> parseIntegerLiteralAsLong(expr) != 0L
            else -> throw EvaluationException("Unsupported literal type '${expr::class.simpleName}' for Boolean evaluation", expr)
        }
    }

    /**
     * Implements infix operations for Boolean: Logical operators and equality.
     */
    override fun evaluateInfixOperation(op: OpType, opToken: PsiToken, left: Boolean, right: Boolean, context: C): Boolean {
        return when (op) {
            // Logical
            OpType.LOGICAL_AND -> left && right
            OpType.LOGICAL_OR -> left || right
            // Use 'xor' if needed and available in your boolean logic
            OpType.BITWISE_XOR -> left xor right // Kotlin Boolean supports xor

            // Equality
            OpType.EQUAL -> left == right
            OpType.NOT_EQUAL -> left != right

            // Unsupported comparison/arithmetic/bitwise operators for Boolean
            else ->
                throw EvaluationException("Operator '$op' is not supported for Boolean evaluation", opToken)
        }
    }

    /**
     * Implements prefix operations for Boolean: Logical NOT.
     */
    override fun evaluatePrefixOperation(op: OpType, opToken: PsiToken, operand: Boolean, context: C): Boolean {
        return when (op) {
            // Logical NOT
            OpType.LOGICAL_NOT -> !operand

            // Unsupported
            else -> throw EvaluationException("Operator '$op' is not supported for Boolean evaluation", opToken)
        }
    }

}