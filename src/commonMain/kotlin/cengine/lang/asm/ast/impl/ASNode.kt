package cengine.lang.asm.ast.impl

import cengine.editor.CodeEditor
import cengine.editor.annotation.Annotation
import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.*
import cengine.lang.asm.ast.Component.*
import cengine.lang.asm.ast.impl.ASNode.*
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.lexer.AsmToken
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.psi.core.*
import cengine.psi.feature.Highlightable
import cengine.psi.lexer.core.Token
import cengine.util.integer.BigInt
import cengine.util.integer.BigInt.Companion.toBigInt
import com.ionspin.kotlin.bignum.integer.BigInteger
import debug.DebugTools
import nativeLog

/**
 * Node Relatives
 * --------------------
 * 1. [Program]
 *   - [Statement]
 * --------------------
 * 2. [Statement]
 *   Each Statement can contain an optional [Label]
 *
 *   - [Statement.Empty] No Content
 *   - [Statement.Dir] Content determined by [DirTypeInterface]
 *   - [Statement.Instr] Content determined by [InstrTypeInterface]
 *   - [Statement.Error] Unresolved Content
 * --------------------
 */
sealed class ASNode(override var range: IntRange, vararg children: PsiElement) : PsiElement, PsiFormatter {
    override var parent: PsiElement? = null

    final override val children: MutableList<PsiElement> = mutableListOf(*children)
    final override val annotations: MutableList<Annotation> = mutableListOf()

    override val additionalInfo: String
        get() = ""

    fun addError(message: String, execute: (CodeEditor) -> Unit = {}) {
        annotations.add(Annotation.error(this, message, execute))
    }

    fun addWarn(message: String, execute: (CodeEditor) -> Unit = {}) {
        annotations.add(Annotation.warn(this, message, execute))
    }

    fun addInfo(message: String, execute: (CodeEditor) -> Unit = {}) {
        annotations.add(Annotation.info(this, message, execute))
    }

    fun addErrorNode(node: SyntaxError) {
        children.add(node)
        if (node.range.first < range.first) {
            range = node.range.first..range.last
        }
        if (node.range.last > range.last) {
            range = range.first..node.range.last
        }
    }

    override fun accept(visitor: PsiElementVisitor) {
        //nativeLog("${visitor::class.simpleName} at ${this::class.simpleName}")
        visitor.visitElement(this)
    }

    class Comment(val token: AsmToken) : ASNode(token.range), Highlightable {
        override val style: CodeStyle
            get() = CodeStyle.comment
        override val pathName: String
            get() = PATHNAME

        companion object {
            const val PATHNAME = "Comment"
        }

        override fun getFormatted(identSize: Int): String = token.value
    }

    class SyntaxError(val message: String, private vararg val tokens: AsmToken) : ASNode(tokens.minOf { it.range.first }..tokens.maxOf { it.range.last }), Highlightable {
        override val pathName: String
            get() = PATHNAME

        override val style: CodeStyle
            get() = CodeStyle.error

        companion object {
            const val PATHNAME = "Syntax Error"
        }

        init {
            addError(message)
        }

        fun catchIf(lexer: AsmLexer, possibleCatcher: AsmLexer.() -> ASNode?): ASNode {
            val startPos = lexer.position

            val catchingParent = lexer.possibleCatcher()

            if (catchingParent != null) {
                return catchingParent.apply { addErrorNode(this@SyntaxError) }
            } else {
                lexer.position = startPos
                return this
            }
        }

        override fun getFormatted(identSize: Int): String = tokens.joinToString(" ") { it.value }
    }

    companion object {

        /**
         * Severities will be set by the Lowest Node, which is actually checking the token.
         */
        fun buildNode(gasNodeType: ASNodeType, lexer: AsmLexer, targetSpec: TargetSpec<*>): ASNode? {
            val initialPos = lexer.position

            when (gasNodeType) {
                ASNodeType.PROGRAM -> {
                    val statements = mutableListOf<Statement>()
                    val syntaxErrors = mutableListOf<SyntaxError>()
                    val annotations = mutableListOf<Annotation>()

                    while (lexer.hasMoreTokens()) {
                        val node = buildNode(ASNodeType.STATEMENT, lexer, targetSpec) ?: break

                        if (node is SyntaxError) {
                            syntaxErrors.add(node)
                            continue
                        }

                        if (node is Statement.Error && node.lineTokens.isEmpty() && node.label == null) {
                            // node is empty
                            continue
                        }

                        if (node !is Statement) {
                            throw PsiParser.NodeException(node, "Didn't get a statement node for GASNode.buildNode(${ASNodeType.STATEMENT})!")
                        }

                        statements.add(node)
                    }

                    val node = Program(statements, lexer.ignored.map { Comment(it as AsmToken) }, syntaxErrors)
                    node.annotations.addAll(annotations)
                    return node
                }

                ASNodeType.STATEMENT -> {
                    val label = buildNode(ASNodeType.LABEL, lexer, targetSpec) as? Label

                    val directive = buildNode(ASNodeType.DIRECTIVE, lexer, targetSpec)
                    if (directive is Directive) {
                        val lineBreak = lexer.consume(true)
                        if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                            val node = Statement.Dir(label, directive, lineBreak)
                            node.annotations.add(Annotation.error(lineBreak, "Linebreak is missing!"))
                            return node
                        }

                        return Statement.Dir(label, directive, lineBreak)
                    }

                    if (directive is SyntaxError) {
                        return directive.catchIf(lexer) {
                            val lb = lexer.consume(true)
                            if (lb.type != AsmTokenType.LINEBREAK) return@catchIf null
                            Statement.Empty(label, lb)
                        }
                    }

                    val instruction = buildNode(ASNodeType.INSTRUCTION, lexer, targetSpec)
                    if (instruction is Instruction) {
                        val lineBreak = lexer.consume(true)
                        if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                            val node = Statement.Instr(label, instruction, lineBreak)
                            node.annotations.add(Annotation.error(lineBreak, "Linebreak is missing!"))
                            return node
                        }

                        return Statement.Instr(label, instruction, lineBreak)
                    }

                    if (instruction is SyntaxError) {
                        return instruction.catchIf(lexer) {
                            val lb = lexer.consume(true)
                            if (lb.type != AsmTokenType.LINEBREAK) return@catchIf null
                            Statement.Empty(label, lb)
                        }
                    }

                    val lineBreak = lexer.consume(true)

                    if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                        val unresolvedTokens = mutableListOf(lineBreak)
                        var token: AsmToken

                        while (true) {
                            token = lexer.consume(false)

                            if (token.type == AsmTokenType.LINEBREAK || token.type == AsmTokenType.EOF) {
                                break
                            }

                            if (!lexer.hasMoreTokens()) {
                                return SyntaxError("Linebreak is missing!", *unresolvedTokens.toTypedArray())
                            }

                            unresolvedTokens.add(token)
                        }

                        return Statement.Empty(label, token).apply {
                            addErrorNode(SyntaxError("Invalid Statement!", *unresolvedTokens.toTypedArray()))
                        }
                    }

                    return Statement.Empty(label, lineBreak)
                }

                ASNodeType.DIRECTIVE -> {
                    targetSpec.allDirs.forEach {
                        val node = it.buildDirectiveContent(lexer, targetSpec)

                        if (node != null) {
                            //nativeLog("Found directive ${it.getDetectionString()} ${node::class.simpleName}")
                            return node
                        }

                        lexer.position = initialPos
                    }
                    return null
                }

                ASNodeType.INSTRUCTION -> {
                    val first = lexer.consume(true)
                    if (first.type != AsmTokenType.INSTRNAME) {
                        lexer.position = initialPos
                        return null
                    }

                    val validTypes = targetSpec.allInstrs.filter { it.detectionName.lowercase() == first.value.lowercase() }
                    if (validTypes.isEmpty()) {
                        return SyntaxError("No valid instruction type found for $first.", first)
                    }

                    validTypes.forEach {
                        val rule = it.paramRule ?: return Instruction(it, first, emptyList(), emptyList())
                        val result = rule.matchStart(lexer, targetSpec)
                        if (result.matches) {
                            return Instruction(it, first, result.matchingTokens, result.matchingNodes)
                        }
                    }

                    return SyntaxError("Invalid Arguments for Instruction ${first.value}!", first)
                }

                ASNodeType.INT_EXPR -> {
                    return NumericExpr.parse(lexer)
                }

                ASNodeType.ANY_EXPR -> {
                    val stringExpr = StringExpr.parse(lexer)
                    if (stringExpr != null) return stringExpr

                    val numericExpr = NumericExpr.parse(lexer)
                    return numericExpr
                }

                ASNodeType.STRING_EXPR -> {
                    return StringExpr.parse(lexer)
                }

                ASNodeType.LABEL -> {
                    val first = lexer.consume(true)
                    if (first.type != AsmTokenType.SYMBOL && first.type != AsmTokenType.INT_DEC) {
                        lexer.position = initialPos
                        return null
                    }

                    val second = lexer.consume(false)
                    if (second.value != ":") {
                        lexer.position = initialPos
                        return null
                    }
                    return Label(first, second)
                }

                ASNodeType.ARG -> {
                    val first = lexer.consume(true)
                    if (first.type != AsmTokenType.SYMBOL) {
                        lexer.position = initialPos
                        return null
                    }

                    val second = lexer.consume(true)
                    if (second.value != "=") {
                        lexer.position = second.range.first
                        return Argument.Basic(first)
                    }

                    val third = buildNode(ASNodeType.ANY_EXPR, lexer, targetSpec)
                    if (third != null) {
                        return Argument.DefaultValue(first, second, third)
                    }

                    val thirdNotExpr = lexer.consume(true)
                    return Argument.DefaultValue(first, second, TokenExpr(thirdNotExpr))
                }

                ASNodeType.ARG_DEF -> {
                    val named = ArgDef.Named.rule.matchStart(lexer, targetSpec)
                    if (named.matches) {
                        return ArgDef.Named(named.matchingTokens[0], named.matchingTokens[1], named.matchingTokens.drop(2))
                    }

                    val pos = ArgDef.Positional.rule.matchStart(lexer, targetSpec)
                    if (pos.matches && pos.matchingTokens.isNotEmpty()) {
                        return ArgDef.Positional(pos.matchingTokens)
                    }

                    lexer.position = initialPos
                    return null
                }
            }
        }
    }

    /**
     * [Program]
     * A RootNode only contains several [Statement]s.
     */
    class Program(statements: List<Statement>, comments: List<Comment>, syntaxErrors: List<SyntaxError>) : ASNode(
        (statements.minByOrNull { it.range.first }?.range?.start ?: 0)..(statements.maxByOrNull { it.range.last }?.range?.last ?: 0),
        *(statements + comments + syntaxErrors).sortedBy { it.range.first }.toTypedArray()
    ) {
        override val pathName: String = this::class.simpleName.toString()

        override fun getFormatted(identSize: Int): String = children.joinToString("") { (it as? ASNode)?.getFormatted(identSize) ?: it.pathName }

        /*private fun removeEmptyStatements() {
            ArrayList(children).filterIsInstance<Statement.Empty>().forEach {
                if (it.label == null) children.remove(it)
            }
        }*/

        fun getAllStatements(): List<Statement> = children.filterIsInstance<Statement>()
    }

    /**
     * [Statement]
     * Each Statement can contain an optional [Label].
     *
     *  - [Statement.Empty] No Content
     *  - [Statement.Dir] Content determined by [Directive]
     *  - [Statement.Instr] Content determined by [Instruction]
     *  - [Statement.Error] Unresolved Content
     */
    sealed class Statement(val label: Label?, val lineBreak: AsmToken, range: IntRange? = null, vararg childs: ASNode) : ASNode(
        range ?: (label?.range?.first ?: childs.firstOrNull()?.range?.first ?: lineBreak.range.first)..<(lineBreak.range.last + 1)
    ) {
        override val pathName: String get() = PATHNAME

        init {
            if (label != null) {
                children.add(label)
            }
            children.addAll(childs)
        }

        companion object {
            const val PATHNAME = "Statement"
        }

        class Dir(label: Label?, val dir: Directive, lineBreak: AsmToken) : Statement(label, lineBreak, null, dir) {
            override fun getFormatted(identSize: Int): String {
                return if (label != null) {
                    label.getFormatted(identSize) + " ".repeat(identSize - label.range.count() % identSize) + dir.getFormatted(identSize) + lineBreak.value
                } else {
                    dir.getFormatted(identSize) + lineBreak.value
                }
            }
        }

        class Instr(label: Label?, val instruction: Instruction, lineBreak: AsmToken) : Statement(label, lineBreak, null, instruction) {
            override fun getFormatted(identSize: Int): String {
                return if (label != null) {
                    label.getFormatted(identSize) + " ".repeat(identSize - label.range.count() % identSize) + instruction.getFormatted(identSize) + lineBreak.value
                } else {
                    " ".repeat(identSize) + instruction.getFormatted(identSize) + lineBreak.value
                }
            }
        }

        class Error(label: Label?, val lineTokens: List<AsmToken>, val nodes: List<ASNode>, lineBreak: AsmToken, message: String) : Statement(
            label, lineBreak,
            (label?.range?.start ?: lineTokens.firstOrNull()?.range?.start ?: lineBreak.range.first)..lineBreak.range.last
        ) {
            val content = lineTokens.joinToString("") { it.value } + lineBreak.value

            init {
                addError(message)
            }

            override fun getFormatted(identSize: Int): String = (label?.getFormatted(identSize) ?: "") + lineTokens.joinToString("") { it.value } + lineBreak.value
        }

        class Empty(label: Label?, lineBreak: AsmToken) : Statement(label, lineBreak) {

            override fun getFormatted(identSize: Int): String {
                return if (label != null) {
                    label.getFormatted(identSize) + lineBreak.value
                } else {
                    lineBreak.value
                }
            }
        }
    }

    class Label(private val nameToken: AsmToken, private val colon: AsmToken) : ASNode(nameToken.start..<colon.end), Highlightable {
        override val pathName get() = nameToken.value + colon.value
        val type = if (nameToken.type == AsmTokenType.INT_DEC) Type.NUMERIC else Type.ALPHANUMERIC
        val identifier = nameToken.value
        override val style: CodeStyle
            get() = CodeStyle.label

        init {
            children.add(nameToken)
            children.add(colon)
        }

        override fun getFormatted(identSize: Int): String = "${nameToken.value}${colon.value}"

        enum class Type {
            NUMERIC,
            ALPHANUMERIC
        }
    }

    /**
     * Directive
     */
    class Directive(val type: DirTypeInterface, private val optionalIdentificationToken: AsmToken?, val allTokens: List<AsmToken> = listOf(), val additionalNodes: List<ASNode> = listOf()) : ASNode(
        (optionalIdentificationToken?.range?.start ?: allTokens.first().range.first)..maxOf(allTokens.lastOrNull()?.range?.start ?: 0, additionalNodes.lastOrNull()?.range?.last ?: 0),
        *additionalNodes.toTypedArray()
    ) {
        override val pathName: String get() = type.typeName
        override val additionalInfo: String
            get() = type.typeName + " " + optionalIdentificationToken

        private val sortedContent = (allTokens + additionalNodes).sortedBy { it.range.first }

        init {
            optionalIdentificationToken?.let {
                children.add(it)
            }
            children.addAll(allTokens)
        }

        override fun getFormatted(identSize: Int): String = (optionalIdentificationToken?.value ?: "") + sortedContent.joinToString("", " ") {
            when (it) {
                is ASNode -> it.getFormatted(identSize)
                is Token -> it.value
                else -> ""
            }
        }
    }

    sealed class Argument(val argName: AsmToken, range: IntRange) : ASNode(range), Highlightable {
        override val pathName: String
            get() = argName.value

        override val style: CodeStyle
            get() = CodeStyle.argument

        abstract fun getDefaultValue(): String

        class Basic(argName: AsmToken) : Argument(argName, argName.range) {
            override fun getDefaultValue(): String = ""

            override fun getFormatted(identSize: Int): String = argName.value
        }

        class DefaultValue(argName: AsmToken, private val assignment: AsmToken, private val expression: ASNode? = null) : Argument(argName, argName.range.first..(expression?.range?.last ?: assignment.range.last)) {
            init {
                expression?.let {
                    children.add(expression)
                }
            }

            override fun getDefaultValue(): String = expression?.getFormatted(identSize = 4) ?: ""

            override fun getFormatted(identSize: Int): String = "${argName.value} ${assignment.value}${if (expression != null) " ${expression.getFormatted(identSize)}" else ""}"
        }
    }

    sealed class ArgDef(val content: List<AsmToken>, range: IntRange) : ASNode(range), Highlightable {

        override val style: CodeStyle
            get() = CodeStyle.argument

        class Positional(content: List<AsmToken>) : ArgDef(content, content.first().range.first..content.last().range.last) {
            override val pathName: String
                get() = PATHNAME

            companion object {
                const val PATHNAME = "Positional Argument"
                val rule = Rule {
                    Seq(
                        Repeatable {
                            Except(XOR(Specific(","), InSpecific(AsmTokenType.LINEBREAK)))
                        }
                    )
                }
            }

            override fun getFormatted(identSize: Int): String = content.joinToString("") { it.value }
        }

        class Named(private val nameToken: AsmToken, private val assignment: AsmToken, content: List<AsmToken>) : ArgDef(content, nameToken.range.first..(content.lastOrNull()?.range?.last ?: assignment.range.last)) {
            override val pathName: String
                get() = nameToken.value

            companion object {
                val rule = Rule {
                    Seq(
                        InSpecific(AsmTokenType.SYMBOL),
                        Specific("="),
                        Repeatable {
                            Except(XOR(Specific(","), InSpecific(AsmTokenType.LINEBREAK)))
                        }
                    )
                }
            }

            override fun getFormatted(identSize: Int): String = "${nameToken.value} ${assignment.value} ${content.joinToString("") { it.value }}"
        }
    }

    class Instruction(val type: InstrTypeInterface, private val instrName: AsmToken, val tokens: List<AsmToken>, val nodes: List<ASNode>) : ASNode(instrName.range.first..maxOf(tokens.lastOrNull()?.range?.last ?: 0, instrName.range.last, nodes.lastOrNull()?.range?.last ?: 0), *nodes.toTypedArray()) {
        override val pathName: String
            get() = instrName.value

        init {
            children.add(instrName)
            children.addAll(tokens)
        }

        override fun getFormatted(identSize: Int): String = "${instrName.value} ${
            (tokens + nodes).sortedBy { it.range.first }.joinToString("") {
                when (it) {
                    is AsmToken -> it.value
                    is ASNode -> it.getFormatted(identSize)
                    else -> ""
                }
            }
        }"
    }

    class TokenExpr(val token: AsmToken) : ASNode(token.range) {
        companion object {
            const val PATHNAME = "Token"
        }

        override val pathName: String
            get() = PATHNAME

        override fun getFormatted(identSize: Int): String = token.value
    }

    sealed class StringExpr(range: IntRange, vararg operands: StringExpr) : ASNode(range, *operands), Highlightable {

        override val style: CodeStyle
            get() = CodeStyle.string

        abstract fun evaluate(printErrors: Boolean): String

        class Concatenation(private val exprs: Array<StringExpr>) : StringExpr(exprs.first().range.first..exprs.last().range.last, *exprs) {
            override val pathName: String
                get() = "Concatenation"

            override fun evaluate(printErrors: Boolean): String = exprs.joinToString("") { it.evaluate(printErrors) }

            override fun getFormatted(identSize: Int): String = exprs.joinToString(" ") { it.getFormatted(identSize) }
        }

        sealed class Operand(val token: AsmToken, range: IntRange) : StringExpr(range) {

            class Identifier(val symbol: AsmToken) : Operand(symbol, symbol.range), PsiReference {
                private var expr: StringExpr? = null
                override val pathName: String
                    get() = symbol.value

                override val element: PsiElement = this
                override var referencedElement: PsiElement? = null

                override val style: CodeStyle
                    get() = CodeStyle.symbol

                override fun evaluate(printErrors: Boolean): String {
                    val currExpr = expr

                    if (currExpr != null) {
                        return currExpr.evaluate(printErrors)
                    }

                    if (printErrors) throw PsiParser.NodeException(this, "Unknown String Identifier!")

                    return ""
                }

                override fun getFormatted(identSize: Int): String = symbol.value

                override fun resolve(): PsiElement? = referencedElement

                override fun isReferenceTo(element: PsiElement): Boolean = referencedElement == element
            }

            class StringLiteral(val string: AsmToken) : Operand(string, string.range) {
                private val stringContent: String = token.getContentAsString()
                override val pathName: String
                    get() = string.value

                override fun evaluate(printErrors: Boolean): String = stringContent

                override fun getFormatted(identSize: Int): String = string.value
            }
        }

        companion object {
            fun parse(lexer: AsmLexer, allowSymbolsAsOperands: Boolean = true): StringExpr? {
                val initialPos = lexer.position

                val operands = mutableListOf<Operand>()
                while (true) {
                    val token = lexer.consume(true)

                    when {
                        token.type.isStringLiteral -> operands.add(Operand.StringLiteral(token))
                        allowSymbolsAsOperands && token.type == AsmTokenType.SYMBOL -> operands.add(Operand.Identifier(token))
                        else -> {
                            lexer.position = token.start
                            break
                        }
                    }
                }

                if (operands.isEmpty()) {
                    lexer.position = initialPos
                    return null
                }

                val expr = if (operands.size == 1) {
                    operands.first()
                } else {
                    Concatenation(operands.toTypedArray())
                }

                return expr
            }
        }
    }

    /**
     * [NumericExpr]
     *
     */
    sealed class NumericExpr(val brackets: List<AsmToken>, range: IntRange, vararg operands: NumericExpr) : ASNode(range, *operands) {
        abstract var eval: BigInt?

        companion object {
            fun parse(lexer: AsmLexer, allowSymbolsAsOperands: Boolean = true): ASNode? {
                val initialPos = lexer.position
                val relevantTokens = mutableListOf<AsmToken>()

                var openingBracketCount = 0
                var closingBracketCount = 0

                while (true) {
                    val token = lexer.consume(true)
                    when {
                        token.type.isOperator -> relevantTokens.add(token)
                        token.type.isNumberLiteral -> relevantTokens.add(token)
                        token.type.isCharLiteral -> relevantTokens.add(token)
                        token.type.isBasicBracket() -> {
                            if (token.type.isOpeningBracket) {
                                openingBracketCount++
                                relevantTokens.add(token)
                            } else {
                                if (openingBracketCount <= closingBracketCount) {
                                    lexer.position = token.start
                                    break
                                }
                                closingBracketCount++
                                relevantTokens.add(token)
                            }
                        }

                        allowSymbolsAsOperands && token.type.isLinkableSymbol() -> relevantTokens.add(token)
                        else -> {
                            lexer.position = token.start
                            break
                        }
                    }
                }

                if (relevantTokens.lastOrNull()?.type?.isOpeningBracket == true) {
                    lexer.position = relevantTokens.removeLast().range.first
                }

                if (DebugTools.KIT_showPostFixExpressions) nativeLog("NumericExpr.validTokens: $relevantTokens")

                if (relevantTokens.isEmpty()) {
                    lexer.position = initialPos
                    return null
                }

                try {
                    val markedAsPrefix = computePrefixList(relevantTokens)

                    // Convert tokens to postfix notation
                    val postFixTokens = convertToPostfix(relevantTokens, markedAsPrefix).toSet()

                    val expression = buildExpressionFromPostfixNotation(postFixTokens.toMutableList(), relevantTokens - postFixTokens, markedAsPrefix)

                    if (expression != null) {
                        val unusedTokens = (relevantTokens - postFixTokens).filter { !it.type.isBasicBracket() }
                        if (unusedTokens.isNotEmpty()) {
                            unusedTokens.forEach {
                                expression.annotations.add(Annotation.error(it, "Invalid Token $it for Numeric Expression!"))
                            }
                        }
                    }

                    if (DebugTools.KIT_showPostFixExpressions) nativeLog(
                        "NumericExpr:" +
                                "\n\tRelevantTokens: ${relevantTokens.joinToString(" ") { it.value }}" +
                                "\n\tPostFixNotation: ${postFixTokens.joinToString(" ") { it.value }}" +
                                "\n\tExpression: ${expression?.getFormatted(4)}"
                    )

                    return expression
                } catch (e: PsiParser.TokenException) {
                    return SyntaxError("Invalid Numeric Expression.", *relevantTokens.toTypedArray())
                }
            }

            private fun computePrefixList(tokens: List<AsmToken>): List<AsmToken> {
                return tokens.filterIndexed { i, currentToken ->
                    if (currentToken.type.isOperator && currentToken.type.couldBePrefix) {
                        // Check if '-' is a negation prefix
                        val previousToken = tokens.getOrNull(i - 1)
                        val nextToken = tokens.getOrNull(i + 1)

                        /**
                         * Is Prefix if previous is:
                         * - null
                         * - not a literal or a symbol
                         */
                        val prevMatchesPrefix: Boolean = previousToken == null || !(previousToken.type.isLiteral() || previousToken.type.isLinkableSymbol())

                        /**
                         * Is Prefix if next is:
                         * - not null
                         * - a literal or a symbol or an opening bracket
                         */
                        val nextMatchesPrefix = nextToken != null && (nextToken.type.isLiteral() || nextToken.type.isLinkableSymbol() || nextToken.value == "(")

                        val isPrefix = prevMatchesPrefix && nextMatchesPrefix

                        isPrefix
                    } else {
                        false
                    }
                }
            }

            private fun buildExpressionFromPostfixNotation(tokens: MutableList<AsmToken>, brackets: List<AsmToken>, prefixTokens: List<AsmToken>): NumericExpr? {
                val uncheckedLast1 = tokens.removeLastOrNull() ?: return null
                val operator = when {
                    uncheckedLast1.type.isOperator -> {
                        uncheckedLast1
                    }

                    uncheckedLast1.type.isLinkableSymbol() -> {
                        return Operand.Identifier(uncheckedLast1)
                    }

                    uncheckedLast1.type.isNumberLiteral -> {
                        return Operand.Number(uncheckedLast1)
                    }

                    uncheckedLast1.type.isCharLiteral -> {
                        return Operand.Char(uncheckedLast1)
                    }

                    else -> {
                        throw PsiParser.TokenException(uncheckedLast1, "Token is not an operator!")
                    }
                }
                val uncheckedLast2 = tokens.lastOrNull() ?: return null
                val operandA = when {
                    uncheckedLast2.type.isOperator -> buildExpressionFromPostfixNotation(tokens, brackets, prefixTokens)
                    uncheckedLast2.type.isLinkableSymbol() -> Operand.Identifier(tokens.removeLast())
                    uncheckedLast2.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast2.type.isCharLiteral -> Operand.Char(tokens.removeLast())

                    else -> {
                        throw PsiParser.TokenException(uncheckedLast2, "Token is not an operator!")
                    }
                } ?: return null

                if (prefixTokens.contains(operator)) {
                    return Prefix(operator, operandA, brackets)
                }

                val uncheckedLast3 = tokens.lastOrNull()
                val operandB = when {
                    uncheckedLast3 == null -> null
                    uncheckedLast3.type.isOperator -> buildExpressionFromPostfixNotation(tokens, brackets, prefixTokens)
                    uncheckedLast3.type.isLinkableSymbol() -> Operand.Identifier(tokens.removeLast())
                    uncheckedLast3.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast3.type.isCharLiteral -> Operand.Char(tokens.removeLast())
                    else -> null
                }

                return if (operandB != null) {
                    Classic(operandB, operator, operandA, brackets)
                } else null
            }

            /**
             * [convertToPostfix] converts infix notated expression to postfix notated expression
             *
             * Operators: [AsmTokenType.isOperator] -> Precedence through [AsmToken.getPrecedence] which contains [AsmToken.Precedence]
             * Operands: [AsmTokenType.isLiteral]
             * Brackets: [AsmTokenType.isBasicBracket]
             *
             */
            private fun convertToPostfix(infix: List<AsmToken>, markedAsPrefix: List<AsmToken>): List<AsmToken> {
                val output = mutableListOf<AsmToken>()
                val operatorStack = mutableListOf<AsmToken>()

                for (token in infix) {
                    if (DebugTools.KIT_showPostFixExpressions) nativeLog(
                        "PostFixIteration: for ${token.type}:${token.getPrecedence(markedAsPrefix)} -> ${token.value}" +
                                "\n\tOutput: ${output.joinToString(" ") { it.value }}" +
                                "\n\tOperatorStack: ${operatorStack.joinToString("") { it.value }}"
                    )
                    if (token.type.isLiteral() || token.type.isLinkableSymbol()) {
                        output.add(token)
                        continue
                    }

                    if (token.type.isOperator) {
                        val higherOrEqualPrecedence = mutableSetOf<Token>()
                        for (op in operatorStack) {
                            if (op.type.isOperator && op.higherOrEqualPrecedenceAs(token, markedAsPrefix)) {
                                output.add(op)
                                higherOrEqualPrecedence.add(op)
                            }
                            if (op.type.isPunctuation) break
                        }
                        operatorStack.removeAll(higherOrEqualPrecedence)
                        operatorStack.add(token)
                        continue
                    }

                    if (token.type.isPunctuation) {
                        if (token.type.isOpeningBracket) {
                            operatorStack.add(token)
                            continue
                        }
                        if (token.type.isClosingBracket) {
                            var peekedOpToken = operatorStack.lastOrNull()
                            while (peekedOpToken != null) {
                                if (peekedOpToken.type.isOpeningBracket) {
                                    operatorStack.removeLast()
                                    break
                                }
                                output.add(operatorStack.removeLast())
                                peekedOpToken = operatorStack.lastOrNull()
                            }
                            continue
                        }
                    }

                    throw PsiParser.TokenException(token, "Token (${token::class.simpleName}: ${token.value}) is not valid in a numeric expression!")
                }

                while (operatorStack.isNotEmpty()) {
                    output.add(operatorStack.removeLast())
                }

                return output
            }
        }

        abstract fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit = {}): BigInt

        abstract fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt)

        /**
         * [Prefix]
         * - [operator] [Operand]
         *
         */
        class Prefix(val operator: AsmToken, val operand: NumericExpr, brackets: List<AsmToken>) : NumericExpr(
            brackets,
            operator.range.first..(brackets.lastOrNull()?.range?.last ?: operand.range.last),
            operand
        ) {

            override val pathName: String
                get() = operator.value

            override var eval: BigInt? = null

            override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): BigInt {
                return when (operator.type) {
                    AsmTokenType.COMPLEMENT -> operand.evaluate(builder, createRelocations).inv()
                    AsmTokenType.MINUS -> operand.evaluate(builder, createRelocations).unaryMinus()
                    AsmTokenType.PLUS -> operand.evaluate(builder, createRelocations)
                    else -> {
                        throw PsiParser.NodeException(this, "$operator is not defined for this type of expression!")
                    }
                }.also { eval = it }
            }

            override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {
                operand.assign(symbols, section, offset)
            }

            override fun getFormatted(identSize: Int): String = if (brackets.isEmpty()) "${operator.value}${operand.getFormatted(identSize)}" else "${operator.value}(${operand.getFormatted(identSize)})"
        }

        /**
         * [Classic]
         * - [operandA] [operator] [operandB]
         */
        class Classic(private val operandA: NumericExpr, val operator: AsmToken, private val operandB: NumericExpr, brackets: List<AsmToken>) : NumericExpr(
            brackets,
            (brackets.firstOrNull()?.range?.first ?: operandA.range.first)..(brackets.lastOrNull()?.range?.last ?: operandB.range.last), operandA,
            operandB
        ) {
            override var eval: BigInt? = null

            override val pathName: String
                get() = operator.value

            override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): BigInt {
                return (when (operator.type) {
                    AsmTokenType.MULT -> operandA.evaluate(builder, createRelocations) * operandB.evaluate(builder, createRelocations)
                    AsmTokenType.DIV -> operandA.evaluate(builder, createRelocations) / operandB.evaluate(builder, createRelocations)
                    AsmTokenType.REM -> operandA.evaluate(builder, createRelocations) % operandB.evaluate(builder, createRelocations)
                    AsmTokenType.SHL -> operandA.evaluate(builder, createRelocations) shl operandB.evaluate(builder, createRelocations)
                    AsmTokenType.SHR -> operandA.evaluate(builder, createRelocations) shl operandB.evaluate(builder, createRelocations)
                    AsmTokenType.BITWISE_OR -> operandA.evaluate(builder, createRelocations) or operandB.evaluate(builder, createRelocations)
                    AsmTokenType.BITWISE_AND -> operandA.evaluate(builder, createRelocations) and operandB.evaluate(builder, createRelocations)
                    AsmTokenType.BITWISE_XOR -> operandA.evaluate(builder, createRelocations) xor operandB.evaluate(builder, createRelocations)
                    AsmTokenType.BITWISE_ORNOT -> operandA.evaluate(builder, createRelocations) or operandB.evaluate(builder, createRelocations).inv()
                    AsmTokenType.PLUS -> operandA.evaluate(builder, createRelocations) + operandB.evaluate(builder, createRelocations)
                    AsmTokenType.MINUS -> operandA.evaluate(builder, createRelocations) - operandB.evaluate(builder, createRelocations)
                    else -> {
                        throw PsiParser.NodeException(this, "$operator is not defined for this type of expression!")
                    }
                }).also { eval = it }
            }

            override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {
                operandA.assign(symbols, section, offset)
                operandB.assign(symbols, section, offset)
            }

            override fun getFormatted(identSize: Int): String = if (brackets.isEmpty()) "${operandA.getFormatted(identSize)} ${operator.value} ${operandB.getFormatted(identSize)}" else "(${operandA.getFormatted(identSize)} ${operator.value} ${operandB.getFormatted(identSize)})"
        }

        sealed class Operand(val token: AsmToken, range: IntRange) : NumericExpr(listOf(), range) {
            override val pathName: String
                get() = additionalInfo

            class Identifier(val symToken: AsmToken) : Operand(symToken, symToken.range), PsiReference, Highlightable {
                private var symbol: AsmCodeGenerator.Symbol<*>? = null

                override val additionalInfo: String
                    get() = token.value
                override val element: PsiElement = this
                override var referencedElement: ASNode? = null
                override var eval: BigInt? = null

                override val style: CodeStyle
                    get() = when (symbol) {
                        is AsmCodeGenerator.Symbol.Abs -> CodeStyle.symbol
                        is AsmCodeGenerator.Symbol.Label -> CodeStyle.label
                        null -> CodeStyle.BASE0
                    }

                override fun getFormatted(identSize: Int): String = symToken.value

                override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): BigInt {
                    return when (val currSymbol = symbol) {
                        is AsmCodeGenerator.Symbol.Abs -> currSymbol.value.also { eval = it }
                        is AsmCodeGenerator.Symbol.Label -> currSymbol.address().also { eval = it }
                        null -> {
                            createRelocations(symToken.value)
                            BigInt.ZERO.also { eval = it }
                        }
                    }
                }

                override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {
                    val nameAssociated = symbols.firstOrNull {
                        it.name == symToken.value
                    }
                    if (nameAssociated != null) {
                        symbol = nameAssociated
                        return
                    }

                    if (symToken.value.endsWith("f")) {
                        val nextLocal = symbols.filterIsInstance<AsmCodeGenerator.Symbol.Label<*>>().firstOrNull {
                            it.local && it.section == section && it.offset >= offset.toBigInt()
                        }
                        if (nextLocal != null) {
                            symbol = nextLocal
                            return
                        }
                    }

                    if (symToken.value.endsWith("b")) {
                        val lastLocal = symbols.filterIsInstance<AsmCodeGenerator.Symbol.Label<*>>().firstOrNull {
                            it.local && it.section == section && it.offset <= offset.toBigInt()
                        }
                        if (lastLocal != null) {
                            symbol = lastLocal
                            return
                        }
                    }
                }
            }

            class Number(val number: AsmToken) : Operand(number, number.range), Highlightable {
                override val additionalInfo: String
                    get() = number.value

                override var eval: BigInt? = null

                override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): BigInt {
                    return (when (number.type) {
                        AsmTokenType.INT_DEC -> BigInt(BigInteger.parseString(number.asNumber, 10))
                        AsmTokenType.INT_HEX -> BigInt(BigInteger.parseString(number.asNumber, 16))
                        AsmTokenType.INT_OCT -> BigInt(BigInteger.parseString(number.asNumber, 8))
                        AsmTokenType.INT_BIN -> BigInt(BigInteger.parseString(number.asNumber, 2))

                        else -> throw PsiParser.NodeException(this, "$number is not a valid Int Literal!")
                    }).also { eval = it }
                }

                override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {}

                override fun getFormatted(identSize: Int): String = number.value

                override val style: CodeStyle
                    get() = number.type.style ?: CodeStyle.integer
            }

            class Char(val char: AsmToken) : Operand(char, char.range), Highlightable {

                val value: Byte = char.getContentAsString().encodeToByteArray().first()
                override val additionalInfo: String
                    get() = char.value

                override var eval: BigInt? = null
                override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {}

                override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): BigInt {
                    return char.getContentAsString().first().code.toBigInt().also { eval = it }
                }

                override fun getFormatted(identSize: Int): String = char.value

                override val style: CodeStyle
                    get() = CodeStyle.char
            }
        }
    }


}







