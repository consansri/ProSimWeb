package cengine.lang.asm.parser

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.ast.gas.GASNodeType
import cengine.lang.asm.lexer.AsmLexer
import cengine.lang.asm.lexer.AsmToken
import cengine.lang.asm.lexer.AsmTokenType
import debug.DebugTools
import emulator.kit.common.RegContainer
import emulator.kit.nativeLog

/**
 * Lexing Position needs to be restored by the caller.
 */
sealed class Component {
    abstract fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult

    abstract fun print(prefix: String): String

    class Optional(comp: () -> Component) : Component() {
        private val comp = comp()
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val result = comp.matchStart(lexer,  asmSpec)
            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Optional ${comp.print("")}")
            return Rule.MatchResult(true, result.matchingTokens, result.matchingNodes)
        }

        override fun print(prefix: String): String = "$prefix[${comp.print("")}]"
    }

    class XOR(private vararg val comps: Component) : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val initialPos = lexer.position
            for (comp in comps) {
                val result = comp.matchStart(lexer, asmSpec)
                if (result.matches) {
                    if (DebugTools.KIT_showRuleChecks) nativeLog("Match: XOR ${result.matchingTokens.joinToString { it.type.name }}")
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
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val matchingTokens = mutableListOf<AsmToken>()
            val matchingNodes = mutableListOf<GASNode>()

            var iteration = 0
            while (true) {
                val initialPos = lexer.position
                val result = comp.matchStart(lexer,  asmSpec)
                if (!result.matches || (maxLength != null && iteration >= maxLength)) {
                    lexer.position = initialPos
                    break
                }

                matchingNodes.addAll(result.matchingNodes)
                matchingTokens.addAll(result.matchingTokens)
                iteration++
            }

            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Repeatable ${comp.print("")} iterations: $iteration")
            return Rule.MatchResult(true, matchingTokens, matchingNodes)
        }

        override fun print(prefix: String): String = "$prefix(vararg ${comp.print("")})"
    }

    class Seq(vararg val comps: Component) : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val initialPosition = lexer.position
            val matchingNodes = mutableListOf<GASNode>()
            val matchingTokens = mutableListOf<AsmToken>()

            for (comp in comps) {
                val result = comp.matchStart(lexer, asmSpec)

                if (!result.matches) {
                    lexer.position = initialPosition
                    return Rule.MatchResult(false, listOf(), listOf())
                }

                matchingNodes.addAll(result.matchingNodes)
                matchingTokens.addAll(result.matchingTokens)
            }

            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Seq ${comps.joinToString("") { it.print("") }}")
            return Rule.MatchResult(true, matchingTokens, matchingNodes)
        }

        override fun print(prefix: String): String = "$prefix${comps.joinToString(" ") { it.print("") }}"
    }

    class Except(private val comp: Component, private val ignoreSpace: Boolean = true) : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val initialPosition = lexer.position
            val result = comp.matchStart(lexer, asmSpec)
            lexer.position = initialPosition
            if (result.matches) {
                return Rule.MatchResult(false, listOf(), listOf())
            }

            val token = lexer.consume(ignoreSpace)
            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Except ${comp.print("")}")
            return Rule.MatchResult(true, listOf(token), listOf())
        }

        override fun print(prefix: String): String = "$prefix!${comp.print("")}"
    }

    class Specific(val content: String, private val ignoreCase: Boolean = false) : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val initialPos = lexer.position
            val token = lexer.consume(true)

            if (ignoreCase) {
                if (token.value.uppercase() != content.uppercase()) {
                    lexer.position = initialPos
                    return Rule.MatchResult(false)
                }
            } else {
                if (token.value != content) {
                    lexer.position = initialPos
                    return Rule.MatchResult(false)
                }
            }

            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Specific $content -> $token")
            return Rule.MatchResult(true, listOf(token))
        }

        override fun print(prefix: String): String = "$prefix${content}"
    }

    class Reg(private val inRegFile: RegContainer.RegisterFile? = null, private val notInRegFile: RegContainer.RegisterFile? = null) : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val token = lexer.peek(true)
            if (token.type == AsmTokenType.EOF) return Rule.MatchResult(false, listOf(), listOf())

            if (token.type != AsmTokenType.REGISTER) return Rule.MatchResult(false, listOf(), listOf())

            if (inRegFile != null && inRegFile.search(token.value) == null) return Rule.MatchResult(false, listOf(), listOf())
            if (notInRegFile != null && notInRegFile.search(token.value) != null) return Rule.MatchResult(false, listOf(), listOf())

            lexer.consume(true)

            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Reg ${token.value} -> ${token.value}")
            return Rule.MatchResult(true, listOf(token), listOf())
        }

        override fun print(prefix: String): String = "$prefix reg${if (inRegFile != null) "in ${inRegFile.name}" else ""} and ${if (notInRegFile != null) "not in ${notInRegFile.name}" else ""}"
    }

    class Dir(private val dirName: String) : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val initialPos = lexer.position
            val token = lexer.consume(true)
            if (token.type == AsmTokenType.DIRECTIVE && ".${dirName.uppercase()}" == token.value.uppercase()) {
                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Dir ${dirName}")
                return Rule.MatchResult(true, listOf(token), listOf())
            }
            lexer.position = initialPos
            return Rule.MatchResult(false, listOf(), listOf())
        }

        override fun print(prefix: String): String = "$prefix.${dirName}"
    }

    class InSpecific(private val type: AsmTokenType) : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val initialPos = lexer.position
            val token = lexer.consume(false)
            if (token.type != type) {
                lexer.position = initialPos
                return Rule.MatchResult(false)
            }
            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: InSpecific ${type.name}")
            return Rule.MatchResult(true, listOf(token))
        }

        override fun print(prefix: String): String = "$prefix${type.name}"
    }

    class SpecNode(private val type: GASNodeType) : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            val initialPosition = lexer.position

            val node = GASNode.buildNode(type, lexer, asmSpec)
            if (node == null) {
                if (DebugTools.KIT_showRuleChecks) nativeLog("Mismatch: SpecNode ${type.name}")
                lexer.position = initialPosition
                return Rule.MatchResult(false, listOf(), listOf())
            }

            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: SpecNode ${type.name}")
            return Rule.MatchResult(true, listOf(), listOf(node))
        }

        override fun print(prefix: String): String = "$prefix${type.name}"
    }

    object Nothing : Component() {
        override fun matchStart(lexer: AsmLexer, asmSpec: AsmSpec): Rule.MatchResult {
            return Rule.MatchResult(true)
        }

        override fun print(prefix: String): String = "$prefix{}"
    }
}