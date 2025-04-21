package cengine.psi.parser.pratt

import cengine.psi.core.PsiElementTypeDef
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.PsiBuilder
import cengine.psi.parser.PsiBuilder.Marker

/**
 * Defines a contract for a Pratt-based (Top-Down Operator Precedence) expression parser.
 * Implementers configure the parser's behavior primarily by providing implementations for
 * the abstract [operatorConfig] and [atomParser].
 *
 * This interface implements [PrattParserSupport], providing default implementations for the core
 * Pratt logic methods (`getNudBindingPower`, `parseNud`, etc.) based on the configuration
 * supplied by the implementing class.
 *
 * **Usage:**
 * 1.  Define your language's operators (`Operator` list).
 * 2.  Implement this interface (`ConfigurableExpressionParser`).
 * 3.  Inside your implementation:
 * - Provide an `override val operatorConfig` by instantiating [OpConfig] with your operators.
 * - Provide an `override fun PsiBuilder.atomParser(): Marker?` with logic to parse
 * your language's atomic expressions (literals, identifiers, `()`, etc.).
 * 4.  Instantiate your implementing class.
 * 5.  Call the [parseExpression] extension function on a [PsiBuilder] instance, providing
 * the parser instance as the receiver (or calling it directly on the instance if preferred).
 *
 * **Note on PSI Type Mapping:** In this version, the mapping from an operator's type ([OpType])
 * to the resulting [PsiElementTypeDef] (e.g., for binary/prefix/postfix expressions) is handled
 * by the internal private function `opTypeToElementType`. This mapping is **not** directly
 * configurable via abstract properties. If custom PSI types are needed for specific operators
 * beyond the generic Prefix/Infix/Postfix distinction, the implementing class would need to
 * override the [parseNud] and/or [parseLed] methods.
 *
 * @see PrattParserSupport
 * @see OpConfig
 * @see Operator
 * @see OpType
 */
interface ConfigurableExpressionParser : PrattParserSupport {

    /**
     * Provides the operator configuration for this parser instance.
     * Implementers must override this property, typically by creating an instance
     * of [OpConfig] with the language-specific list of [Operator]s.
     * The default Pratt logic methods rely on this configuration.
     */
    val operatorConfig: OpConfig

    /**
     * Configuration for identifier-based infix operators (e.g., `a customInfix b`).
     * Override this property with a non-null binding power (precedence) to enable this feature.
     * Defaults to `null` (disabled).
     * The precedence value should typically fit within your [OpPrec] enum ordinals.
     */
    val identifierInfixBindingPower: Int?
        get() = null // Default implementation: feature disabled

    /**
     * Defines the configuration for operators used by a [ConfigurableExpressionParser].
     * It preprocesses a list of [Operator] definitions into efficient lookup maps
     * and determines the binding power for atomic expressions.
     *
     * @constructor Creates an operator configuration.
     * @param operators The variable number of [Operator] definitions for the language.
     * (typically using `PsiTokenType.OPERATOR`).
     */
    class OpConfig(
        vararg operators: Operator,
    ) {
        /**
         * The binding power (precedence) assigned to atomic expressions parsed by [atomParser].
         * Calculated automatically to be higher than any standard operator precedence.
         */
        val atomBindingPower: Int = OpPrec.entries.size

        /**
         * A map associating the string representation of prefix operators with their [OpType].
         * Derived from the input [operators]. Used for fast lookup in [getNudBindingPower] and [parseNud].
         */
        val prefixOps: Map<String, OpType> = operators.filter { it.type.loc == OpLoc.PREFIX }.associate { it.string to it.type }

        /**
         * A map associating the string representation of infix operators with their [OpType].
         * Derived from the input [operators]. Used for fast lookup in [getLedBindingPower] and [parseLed].
         */
        val infixOps: Map<String, OpType> = operators.filter { it.type.loc == OpLoc.INFIX }.associate { it.string to it.type }

        /**
         * A map associating the string representation of postfix operators with their [OpType].
         * Derived from the input [operators]. Used for fast lookup in [getLedBindingPower] and [parseLed].
         */
        val postfixOps: Map<String, OpType> = operators.filter { it.type.loc == OpLoc.POSTFIX }.associate { it.string to it.type }
    }

    // --- Abstract Method (To be implemented by concrete parser classes) ---

    /**
     * Parses an atomic expression (the base units of the expression grammar).
     * Examples: literals, identifiers, parenthesized expressions `()`, function calls (if parsed as atoms), etc.
     *
     * This function is called by the default [parseNud] implementation when the current token
     * is not recognized as a standard prefix operator.
     *
     * **Implementation Responsibilities:**
     * - Check the current token(s) using `this` [PsiBuilder].
     * - **Crucially:** If the current token *cannot* start a valid atom in the current context, return `null` immediately. Optionally report an error (`builder.error()`).
     * - If a recognized atom is found:
     * - Consume the corresponding tokens using `advance()`.
     * - Create a [Marker] spanning the consumed tokens using `mark()`.
     * - Complete the marker using `marker.done(YourAtomPsiElementType)`.
     * - Return the completed [Marker].
     *
     * @receiver The [PsiBuilder] instance providing the token stream and parsing context.
     * @return A completed [Marker] for the parsed atom, or `null` if no valid atom was recognized
     * at the current position.
     */
    fun PsiBuilder.atomParser(): Marker?

    // --- PrattParserSupport Implementation (Default implementations provided) ---

    /**
     * Determines the binding power of tokens that can start an expression (Null Denotation).
     * Checks for configured prefix operators (`PsiTokenType.OPERATOR`). If none match,
     * it defaults to [OpConfig.atomBindingPower], assuming the token *might* start an atom.
     * The final decision on whether it's a valid atom start rests with [atomParser].
     *
     * @receiver The [PsiBuilder] instance.
     * @param tokenType The [PsiTokenType] of the token at the current position.
     * @return The binding power (typically the [OpPrec] ordinal or `atomBindingPower`),
     * or -1 if the token type itself is inherently unable to start *any* expression part
     * (though this default implementation is generous).
     */
    override fun PsiBuilder.getNudBindingPower(tokenType: PsiTokenType): Int {
        if (tokenType == PsiTokenType.OPERATOR) {
            // Access prefixOps via the abstract operatorConfig property
            operatorConfig.prefixOps[getTokenText()]?.let { return it.prec.ordinal }
        }

        // If not a known prefix operator, assume it could be an atom.
        // The atomParser will ultimately decide if it's valid.
        // Return atomBindingPower to allow the Pratt loop to call parseNud -> atomParser.

        return operatorConfig.atomBindingPower
    }

    /**
     * Determines the binding power of tokens that can follow an expression (Left Denotation).
     * Checks for configured infix/postfix operators (`PsiTokenType.OPERATOR`) and optionally
     * for identifier-based infix operators (`PsiTokenType.IDENTIFIER`) if configured via
     * [identifierInfixBindingPower].
     *
     * @receiver The [PsiBuilder] instance.
     * @param tokenType The [PsiTokenType] of the token at the current position.
     * @return The binding power (typically the [OpPrec] ordinal or `identifierInfixBindingPower`),
     * or -1 if the token cannot follow an expression as an operator in this context.
     */
    override fun PsiBuilder.getLedBindingPower(tokenType: PsiTokenType): Int {
        val tokenText = getTokenText()

        // Check standard infix/postfix operators (`PsiTokenType.OPERATOR`)
        if (tokenType == PsiTokenType.OPERATOR && tokenText != null) {
            operatorConfig.infixOps[tokenText]?.let { return it.prec.ordinal }
            operatorConfig.postfixOps[tokenText]?.let { return it.prec.ordinal }
        }

        // Check for Identifier Infix Operator (`PsiTokenType.IDENTIFIER`)
        if (tokenType == PsiTokenType.IDENTIFIER) {
            identifierInfixBindingPower?.let { return it }
        }

        // TODO: Extend here if other tokens like '(' (function call) or '[' (array access) act as infix operators.
        // Example: if (tokenType == PsiTokenType.PUNCTUATION) return functionCallBindingPower

        return -1 // Cannot follow an expression in an operator context based on current config
    }

    /**
     * Parses atoms or prefix operator expressions (Null Denotation).
     * If the token is a recognized standard prefix operator, it parses the operator
     * and its operand recursively. Otherwise, it delegates to the abstract [atomParser].
     *
     * @receiver The [PsiBuilder] instance.
     * @param token The token potentially starting the expression.
     * @param bp The binding power context (precedence of the prefix operator, if applicable).
     * @return A completed [Marker] for the parsed expression part, or `null` on failure.
     */
    override fun PsiBuilder.parseNud(token: PsiToken, bp: Int): Marker? {
        // Handle standard prefix operators
        if (token.type == PsiTokenType.OPERATOR) {
            operatorConfig.prefixOps[token.value]?.let { opType ->
                val marker = mark()
                advance() // Consume the prefix operator token
                val operandMarker = parseExpression(opType.prec.ordinal) // Use operator's precedence for operand
                if (operandMarker == null) {
                    error("Expected operand after prefix operator '${token.value}'")
                    marker.drop()
                    return null
                }
                marker.done(opTypeToElementType(opType)) // Use default mapping
                return marker
            }
        }

        // If not a known standard prefix operator, delegate parsing to the implementation's atomParser.
        // atomParser is responsible for handling valid atoms AND reporting errors/returning null for invalid starts.
        return atomParser()
    }

    /**
     * Parses infix or postfix operator expressions (Left Denotation).
     * Handles standard operators (`PsiTokenType.OPERATOR`) and optionally identifier-based
     * infix operators (`PsiTokenType.IDENTIFIER`) based on configuration.
     * Uses operator precedence and assumes left-associativity by default for infix operators
     * (except for assignment, if defined with specific precedence/handling).
     *
     * @receiver The [PsiBuilder] instance.
     * @param operatorToken The infix or postfix operator token (`OPERATOR` or `IDENTIFIER`).
     * @param leftMarker The completed [Marker] for the expression parsed to the left of the operator.
     * @param bp The binding power (precedence) of the operator token.
     * @return A completed [Marker] for the combined expression, or `null` on failure.
     */
    override fun PsiBuilder.parseLed(operatorToken: PsiToken, leftMarker: Marker, bp: Int): Marker? {
        val opString = operatorToken.value

        // --- Handle Standard Operators (`PsiTokenType.OPERATOR`) ---
        if (operatorToken.type == PsiTokenType.OPERATOR) {
            // Check Infix
            operatorConfig.infixOps[opString]?.let { opType ->
                val operationMarker = leftMarker.precede()
                advance() // Consume the infix operator token

                // Simple left-associativity: parse RHS with slightly higher precedence demand (bp + 1)
                // Adjust if specific operators (like assignment) need right-associativity (use bp)
                val rightBp = bp + 1 // Default: Left-associative
                // Example for right-associativity (e.g., if OpPrec.ASSIGNMENT exists):
                // val rightBp = if (opType.prec == OpPrec.ASSIGNMENT) bp else bp + 1

                val rightMarker = parseExpression(rightBp)
                if (rightMarker == null) {
                    error("Expected operand after infix operator '$opString'")
                    operationMarker.drop()
                    return null
                }
                operationMarker.done(opTypeToElementType(opType)) // Use default mapping
                return operationMarker
            }

            // Check Postfix
            operatorConfig.postfixOps[opString]?.let { opType ->
                val operationMarker = leftMarker.precede()
                advance() // Consume the postfix operator token
                operationMarker.done(opTypeToElementType(opType)) // Use default mapping
                return operationMarker
            }
        }

        // --- Handle Identifier Infix Operator (`PsiTokenType.IDENTIFIER`) ---
        if (operatorToken.type == PsiTokenType.IDENTIFIER) {
            // Check if the feature is enabled AND the element type is provided

            if (identifierInfixBindingPower == bp) { // Check precedence matches expected bp
                val operationMarker = leftMarker.precede()
                advance() // Consume the identifier token (acting as operator)

                // Assume left-associativity for identifier infix too, parse RHS with bp + 1
                val rightBp = bp + 1
                val rightMarker = parseExpression(rightBp)
                if (rightMarker == null) {
                    error("Expected operand after identifier infix operator '$opString'")
                    operationMarker.drop()
                    return null
                }
                // Use the configured specific element type
                operationMarker.done(PsiStatement.Expr.InfixFunctionCall)
                return operationMarker
            }
        }

        // If we reach here, getLedBindingPower returned a value, but no parsing logic handled it.
        // This indicates an internal inconsistency or an unsupported operator type for parseLed.
        error("Internal Error: parseLed called with unhandled operator token '${operatorToken.value}' (type: ${operatorToken.type}) at binding power $bp")
        advance() // Consume the token to prevent potential loops
        return null // Indicate failure
    }

    // --- Private Helper Methods (Internal implementation details) ---

    /**
     * Internal private function mapping an operator type to a generic PSI element type based on its location (prefix/infix/postfix).
     * **Note:** This default mapping is basic. If language-specific PSI types are needed
     * for different operators (e.g., distinct types for `+` vs `*`), the implementing
     * class must override [parseNud] and/or [parseLed] to provide custom mapping logic.
     *
     * @param opType The [OpType] determined during parsing.
     * @return A generic [PsiElementTypeDef] (OperationPrefix, OperationInfix, OperationPostfix).
     */
    private fun opTypeToElementType(opType: OpType): PsiElementTypeDef {
        return when (opType.loc) {
            OpLoc.PREFIX -> PsiStatement.Expr.OperationPrefix
            OpLoc.INFIX -> PsiStatement.Expr.OperationInfix
            OpLoc.POSTFIX -> PsiStatement.Expr.OperationPostfix
        }
    }
}

