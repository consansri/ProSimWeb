package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.lexer.AsmToken
import cengine.lang.asm.ast.lexer.AsmTokenType
import debug.DebugTools



/**
 * Lexing Position needs to be restored by the caller.
 */
sealed class Component {
    abstract fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult

    abstract fun print(prefix: String): String

    class Optional(comp: () -> Component) : Component() {
        private val comp = comp()
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val result = comp.matchStart(lexer, targetSpec)
            if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: Optional ${comp.print("")}")
            return Rule.MatchResult(true, result.matchingTokens, result.matchingNodes)
        }

        override fun print(prefix: String): String = "$prefix[${comp.print("")}]"
    }

    class XOR(private vararg val comps: Component) : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val initialPos = lexer.position
            for (comp in comps) {
                val result = comp.matchStart(lexer, targetSpec)
                if (result.matches) {
                    if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: XOR ${result.matchingTokens.joinToString { it.type.name }}")
                    return Rule.MatchResult(true, result.matchingTokens, result.matchingNodes)
                }
            }
            lexer.position = initialPos
            return Rule.MatchResult(false, listOf(), listOf())
        }

        override fun print(prefix: String): String = "$prefix${comps.joinToString("|") { it.print("") }}"
    }

    class Repeatable(private val maxLength: Int? = null, comp: () -> Component) : Component() {
        private val comp = comp()
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val matchingTokens = mutableListOf<AsmToken>()
            val matchingNodes = mutableListOf<ASNode>()

            var iteration = 0
            while (true) {
                val initialPos = lexer.position
                val result = comp.matchStart(lexer, targetSpec)
                if (!result.matches || (maxLength != null && iteration >= maxLength)) {
                    lexer.position = initialPos
                    break
                }

                matchingNodes.addAll(result.matchingNodes)
                matchingTokens.addAll(result.matchingTokens)
                iteration++
            }

            if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: Repeatable ${comp.print("")} iterations: $iteration")
            return Rule.MatchResult(true, matchingTokens, matchingNodes)
        }

        override fun print(prefix: String): String = "$prefix(vararg ${comp.print("")})"
    }

    class Seq(private vararg val comps: Component, private val print: Boolean = false) : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val initialPosition = lexer.position
            val matchingNodes = mutableListOf<ASNode>()
            val matchingTokens = mutableListOf<AsmToken>()

            for (comp in comps) {
                if (print) {
                    SysOut.info("${this::class.simpleName}: ${comp.print("")} == ${lexer.peek(true)}")
                }

                val result = comp.matchStart(lexer, targetSpec)

                if (!result.matches) {
                    lexer.position = initialPosition
                    return Rule.MatchResult(false, listOf(), listOf())
                }

                matchingNodes.addAll(result.matchingNodes)
                matchingTokens.addAll(result.matchingTokens)
            }

            if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: Seq ${comps.joinToString("") { it.print("") }}")
            return Rule.MatchResult(true, matchingTokens, matchingNodes)
        }

        override fun print(prefix: String): String = "$prefix${comps.joinToString(" ") { it.print("") }}"
    }

    class Except(private val comp: Component, private val ignoreSpace: Boolean = true) : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val initialPosition = lexer.position
            val result = comp.matchStart(lexer, targetSpec)
            lexer.position = initialPosition
            if (result.matches) {
                return Rule.MatchResult(false, listOf(), listOf())
            }

            val token = lexer.consume(ignoreSpace)
            if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: Except ${comp.print("")}")
            return Rule.MatchResult(true, listOf(token), listOf())
        }

        override fun print(prefix: String): String = "$prefix!${comp.print("")}"
    }

    class Specific(val content: String, private val ignoreCase: Boolean = false) : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val initialPos = lexer.position
            val token = lexer.consume(true)

            if (ignoreCase) {
                if (token.value.lowercase() != content.lowercase()) {
                    lexer.position = initialPos
                    return Rule.MatchResult(false)
                }
            } else {
                if (token.value != content) {
                    lexer.position = initialPos
                    return Rule.MatchResult(false)
                }
            }

            if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: Specific $content -> $token")
            return Rule.MatchResult(true, listOf(token))
        }

        override fun print(prefix: String): String = "$prefix${content}"
    }

    class Reg(private val isContainedBy: List<RegTypeInterface>? = null, private val isNotContainedBy: List<RegTypeInterface>? = null) : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val token = lexer.peek(true)
            if (token.type == AsmTokenType.EOF) return Rule.MatchResult(false, listOf(), listOf())

            if (token.type != AsmTokenType.REGISTER) return Rule.MatchResult(false, listOf(), listOf())

            if (isContainedBy != null && isContainedBy.firstOrNull { it.recognizable.contains(token.value.lowercase()) } == null) return Rule.MatchResult(false, listOf(), listOf())
            if (isNotContainedBy != null && isNotContainedBy.firstOrNull { it.recognizable.contains(token.value.lowercase()) } != null) return Rule.MatchResult(false, listOf(), listOf())

            lexer.consume(true)

            if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: Reg ${token.value} -> ${token.value}")
            return Rule.MatchResult(true, listOf(token), listOf())
        }

        override fun print(prefix: String): String = "$prefix reg${if (isContainedBy != null) "in $isContainedBy" else ""} and ${if (isNotContainedBy != null) "not in $isNotContainedBy" else ""}"
    }

    class Dir(private val dirName: String) : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val initialPos = lexer.position
            val token = lexer.consume(true)
            if (token.type == AsmTokenType.DIRECTIVE && ".${dirName.uppercase()}" == token.value.uppercase()) {
                if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: Dir $dirName")
                return Rule.MatchResult(true, listOf(token), listOf())
            }
            lexer.position = initialPos
            return Rule.MatchResult(false, listOf(), listOf())
        }

        override fun print(prefix: String): String = "$prefix.${dirName}"
    }

    class InSpecific(private val type: AsmTokenType, private val print: Boolean = false) : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val initialPos = lexer.position
            val token = lexer.consume(true)

            if (token.type != type) {
                if (print) {
                    SysOut.info("${this::class.simpleName} ${token.type} == $type -> false")
                }
                lexer.position = initialPos
                return Rule.MatchResult(false)
            }

            if (print) {
                SysOut.info("${this::class.simpleName} ${token.type} == $type -> true")
            }

            if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: InSpecific ${type.name}")
            return Rule.MatchResult(true, listOf(token))
        }

        override fun print(prefix: String): String = "$prefix${type.name}"
    }

    class SpecNode(private val type: ASNodeType, val condition: (node: ASNode) -> Boolean = { true }) : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            val initialPosition = lexer.position

            val node = ASNode.buildNode(type, lexer, targetSpec)
            if (node == null || !condition(node)) {
                if (DebugTools.KIT_showRuleChecks) SysOut.log("Mismatch: SpecNode ${type.name}")
                lexer.position = initialPosition
                return Rule.MatchResult(false, listOf(), listOf())
            }

            if (DebugTools.KIT_showRuleChecks) SysOut.log("Match: SpecNode ${type.name}")
            return Rule.MatchResult(true, listOf(), listOf(node))
        }

        override fun print(prefix: String): String = "$prefix${type.name}"
    }

    data object Nothing : Component() {
        override fun matchStart(lexer: AsmLexer, targetSpec: TargetSpec<*>): Rule.MatchResult {
            return Rule.MatchResult(true)
        }

        override fun print(prefix: String): String = "$prefix{}"
    }
}