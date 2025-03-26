package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.lexer.AsmToken
import debug.DebugTools


/**
 * Class representing a rule for the assembler, which matches components based on specified criteria.
 *
 * @param comp A lambda that returns a [Component] which defines the matching logic.
 */
class Rule(comp: () -> Component = { Component.Nothing }) {

    companion object {
        /**
         * Creates a rule that matches a directive name.
         *
         * @param name The directive name to match.
         * @return A [Rule] that matches the specified directive.
         */
        fun dirNameRule(name: String) = Rule {
            Component.Specific(".${name}", ignoreCase = true)
        }
    }

    private val comp = comp()

    /**
     * Attempts to match the rule against the start of the provided token source.
     *
     * @return A [MatchResult] indicating whether the match was successful and the details of the match.
     */
    fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): MatchResult {
        val result = comp.matchStart(lexer, targetSpec)
        if (DebugTools.KIT_showRuleChecks) {
            SysOut.log(result.toString())
        }
        return result
    }

    /**
     * Returns a string representation of the rule.
     */
    override fun toString(): String = comp.print("")


    /**
     * Data class representing the result of a match attempt.
     *
     * @param matches Whether the match was successful.
     * @param matchingTokens The tokens that were matched.
     * @param matchingNodes The nodes that were matched.
     */
    data class MatchResult(val matches: Boolean, val matchingTokens: List<AsmToken> = listOf(), val matchingNodes: List<ASNode> = listOf()) {
        override fun toString(): String {
            return "Matches: $matches,${matchingTokens.joinToString("") { "\n\t${it::class.simpleName}" }}${matchingNodes.joinToString("") { it.getFormatted(4) }}"
        }
    }
}