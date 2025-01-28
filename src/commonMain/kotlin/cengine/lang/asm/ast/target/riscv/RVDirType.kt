package cengine.lang.asm.ast.target.riscv

import cengine.editor.annotation.Annotation
import cengine.lang.asm.ast.*
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.util.integer.BigInt
import cengine.util.integer.BigInt.Companion.toBigInt
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.math.pow

enum class RVDirType(override val isSection: Boolean = false, override val rule: Rule? = null) : DirTypeInterface {
    ATTRIBUTE(rule = Rule {
        Component.Seq(
            Component.Specific(".attribute", ignoreCase = true),
            Component.InSpecific(AsmTokenType.SYMBOL),
            Component.Except(Component.Specific(",")),
            Component.Specific(","),
            Component.SpecNode(ASNodeType.ANY_EXPR)
        )
    }),
    ALIGN(rule = Rule {
        Component.Seq(
            Component.Specific(".align", ignoreCase = true),
            Component.SpecNode(ASNodeType.INT_EXPR),
        )
    }),
    DTPRELWORD(rule = Rule {
        Component.Seq(
            Component.Specific(".dtprelword", ignoreCase = true),
            Component.Optional {
                Component.Seq(
                    Component.SpecNode(ASNodeType.INT_EXPR),
                    Component.Repeatable {
                        Component.Seq(Component.Specific(","), Component.SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    DTPRELDWORD(rule = Rule {
        Component.Seq(
            Component.Specific(".dtpreldword", ignoreCase = true),
            Component.Optional {
                Component.Seq(
                    Component.SpecNode(ASNodeType.INT_EXPR),
                    Component.Repeatable {
                        Component.Seq(Component.Specific(","), Component.SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    DWORD(rule = Rule {
        Component.Seq(
            Component.Specific(".dword", ignoreCase = true),
            Component.Optional {
                Component.Seq(
                    Component.SpecNode(ASNodeType.INT_EXPR),
                    Component.Repeatable {
                        Component.Seq(Component.Specific(","), Component.SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    HALF(rule = Rule {
        Component.Seq(
            Component.Specific(".half", ignoreCase = true),
            Component.Optional {
                Component.Seq(
                    Component.SpecNode(ASNodeType.INT_EXPR),
                    Component.Repeatable {
                        Component.Seq(Component.Specific(","), Component.SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    OPTION(
        rule = Rule {
            Component.Seq(
                Component.Specific(".option", ignoreCase = true),
                Component.InSpecific(AsmTokenType.SYMBOL)
            )
        }
    ),


    ;

    override fun getDetectionString(): String = this.name

    override val typeName: String
        get() = name

    override fun buildDirectiveContent(lexer: AsmLexer, targetSpec: TargetSpec<*>): ASNode.Directive? {
        val initialPos = lexer.position
        val result = this.rule?.matchStart(lexer, targetSpec)

        if (result == null) {
            lexer.position = initialPos
            return null
        }

        if (result.matches) {
            //nativeLog("RuleResult: ${result} for $this")
            val identificationToken = result.matchingTokens.firstOrNull { it.type == AsmTokenType.DIRECTIVE }
            return if (identificationToken != null) {
                ASNode.Directive(this, identificationToken, result.matchingTokens - identificationToken, result.matchingNodes)
            } else {
                ASNode.Directive(this, identificationToken, result.matchingTokens, result.matchingNodes)
            }
        }

        lexer.position = initialPos
        return null
    }

    override suspend fun build(builder: AsmCodeGenerator<*>, dir: ASNode.Directive) {
        /**
         * Check Semantic
         */

        when (this) {
            ATTRIBUTE -> TODO()
            ALIGN -> {
                // Nothing to check for
            }

            DTPRELWORD -> TODO()
            DTPRELDWORD -> TODO()
            DWORD -> {
                dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                    val evaluated = it.evaluate(builder)
                    if (!evaluated.fitsInSignedOrUnsigned(64)) {
                        dir.annotations.add(Annotation.error(dir, "$evaluated exceeds 64 bits"))
                    }
                }
            }

            HALF -> TODO()
            OPTION -> TODO()
        }

        /**
         * Execute Directive
         */

        when (this) {
            ATTRIBUTE -> TODO()
            ALIGN -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()
                val amountExpr = exprs[0]
                val amount = amountExpr.evaluate(builder).toInt().toDouble()

                val alignment = 2.0.pow(amount).toBigInt()
                val addr = builder.currentSection.address + builder.currentSection.content.size
                if (addr % alignment != BigInt.ZERO) {
                    val padLength = alignment - addr % alignment
                    builder.currentSection.content.pad(padLength.toInt())
                }
            }
            DTPRELWORD -> TODO()
            DTPRELDWORD -> TODO()
            DWORD -> {
                dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                    val evaluated = it.evaluate(builder).toInt64()
                    builder.currentSection.content.put(evaluated)
                }
            }

            HALF -> TODO()
            OPTION -> TODO()
        }
    }
}