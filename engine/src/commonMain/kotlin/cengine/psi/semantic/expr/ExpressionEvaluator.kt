package cengine.psi.semantic.expr

import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken

/**
 * Abstract base class for evaluating expressions represented by PSI nodes (`PsiStatement.Expr`).
 * It handles the recursive structure, grouping, and identifier resolution, while delegating
 * type-specific literal parsing and operator implementation to subclasses.
 *
 * @param T The target result type of the evaluation (e.g., BigInt, Double, String).
 * @param C The type of the optional context object passed to the identifier resolver.
 * @property resolveIdentifierLambda The lambda function provided by the user to resolve identifiers.
 */
abstract class ExpressionEvaluator<T, C>(
    private val resolveIdentifierLambda: (name: String, element: PsiStatement.Expr.Identifier, context: C) -> T?
) {
// --- Public Entry Point ---

    /**
     * Evaluates the given expression PSI node.
     *
     * @param expr The expression node to evaluate.
     * @param context An optional context object to pass to the identifier resolver and other evaluation steps.
     * @return The result of the evaluation with type T.
     * @throws EvaluationException if any error occurs during evaluation.
     */
    fun evaluate(expr: PsiStatement.Expr, context: C): T {
        return evaluateRecursive(expr, context)
    }

    // --- Core Recursive Evaluation Logic ---

    /**
     * Handles the recursive descent through the expression tree.
     * Dispatches to abstract methods for type-specific logic.
     */
    protected open fun evaluateRecursive(expr: PsiStatement.Expr, context: C): T {
        return when (expr) {
            // Literals: Delegate parsing to abstract method
            is PsiStatement.Expr.Literal -> parseLiteral(expr, context)

            // Identifier: Use provided lambda via helper
            is PsiStatement.Expr.Identifier -> resolveIdentifierOrThrow(expr, context)

            // Operations: Recurse, then delegate calculation to abstract methods
            is PsiStatement.Expr.OperationInfix -> {
                val left = evaluateRecursive(expr.leftOperand, context)
                val right = evaluateRecursive(expr.rightOperand, context)
                evaluateInfixOperation(expr.operator.value, expr.operator, left, right, context)
            }
            is PsiStatement.Expr.OperationPrefix -> {
                val operand = evaluateRecursive(expr.operand, context)
                evaluatePrefixOperation(expr.operator.value, expr.operator, operand, context)
            }
            is PsiStatement.Expr.OperationPostfix -> {
                val operand = evaluateRecursive(expr.operand, context)
                evaluatePostfixOperation(expr.operator.value, expr.operator, operand, context)
            }

            // Grouping: Recurse directly
            is PsiStatement.Expr.Grouped -> {
                val innerExpr = expr.operand ?: throw EvaluationException("Grouped expression is empty", expr)
                evaluateRecursive(innerExpr, context)
            }

            // --- Handle other potentially common structures if needed ---
            // is PsiStatement.Expr.FunctionCall -> handleFunctionCall(expr, context) // Example extension point

            // Throw for unknown/unsupported types
            else -> throw EvaluationException("Unsupported expression type in evaluator base: ${expr::class.simpleName}", expr)
        }
    }

    // --- Abstract Methods for Subclasses to Implement ---

    /**
     * Parses any Literal expression node and converts it to the target type T.
     * Subclasses must implement this to handle all relevant literal types (Integer, String, etc.)
     * and throw EvaluationException for unsupported literal types.
     *
     * @param expr The literal expression node (e.g., Literal.Integer, Literal.String).
     * @param context The evaluation context.
     * @return The parsed value as type T.
     * @throws EvaluationException if the literal type is unsupported or parsing fails.
     */
    protected abstract fun parseLiteral(expr: PsiStatement.Expr.Literal, context: C): T

    /**
     * Performs an infix operation.
     * Subclasses must implement this based on the semantics of type T.
     *
     * @param op The operator symbol (e.g., "+", "==").
     * @param opToken The original operator token (for error reporting).
     * @param left The already evaluated left operand of type T.
     * @param right The already evaluated right operand of type T.
     * @param context The evaluation context.
     * @return The result of the operation as type T.
     * @throws EvaluationException if the operator is unsupported for type T or an error occurs.
     */
    protected abstract fun evaluateInfixOperation(op: String, opToken: PsiToken, left: T, right: T, context: C): T

    /**
     * Performs a prefix operation.
     * Subclasses must implement this based on the semantics of type T.
     *
     * @param op The operator symbol (e.g., "-", "!").
     * @param opToken The original operator token (for error reporting).
     * @param operand The already evaluated operand of type T.
     * @param context The evaluation context.
     * @return The result of the operation as type T.
     * @throws EvaluationException if the operator is unsupported for type T or an error occurs.
     */
    protected abstract fun evaluatePrefixOperation(op: String, opToken: PsiToken, operand: T, context: C): T

    /**
     * Performs a postfix operation.
     * Subclasses must implement this based on the semantics of type T.
     * Note: Postfix often implies side effects which may not fit pure evaluation.
     * Default implementation throws an exception. Override if needed.
     *
     * @param op The operator symbol (e.g., "++", "--").
     * @param opToken The original operator token (for error reporting).
     * @param operand The already evaluated operand of type T.
     * @param context The evaluation context.
     * @return The result of the operation as type T.
     * @throws EvaluationException if the operator is unsupported for type T or an error occurs.
     */
    protected open fun evaluatePostfixOperation(op: String, opToken: PsiToken, operand: T, context: C): T {
        throw EvaluationException("Postfix operator '$op' is not supported by this evaluator.", opToken)
    }


    // --- Helper Methods ---

    /**
     * Calls the provided identifier resolution lambda and throws a standard
     * EvaluationException if the lambda returns null (indicating undefined identifier).
     */
    protected fun resolveIdentifierOrThrow(identifier: PsiStatement.Expr.Identifier, context: C): T {
        val name = identifier.name ?: throw EvaluationException("Identifier node has no name", identifier)
        return resolveIdentifierLambda(name, identifier, context)
            ?: throw EvaluationException("Undefined identifier: '$name'", identifier)
    }
}