package cengine.lang.asm.psi

import cengine.lang.asm.AsmParser
import cengine.psi.core.NodeBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiFile
import cengine.psi.elements.PsiStatement
import cengine.psi.feature.FileReference
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.PsiBuilder
import cengine.util.collection.firstInstance

/**
 * Represents an assembler directive (command starting with '.').
 * E.g., ".globl main", ".byte 0x42"
 */
sealed class AsmDirective(
    type: PsiStatementTypeDef,
    range: IntRange,
    vararg children: PsiElement, // Arguments can be diverse: literals, identifiers, etc. Use PsiElement or maybe Expr
) : PsiStatement(type, range, *children) {

    companion object {
        val all = Emissive.EmissiveT.entries + SectionControl.SectionControlT.entries + Include
    }

    val keyWord = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.KEYWORD }

    class Include(range: IntRange, vararg children: PsiElement) : AsmDirective(Include, range, *children), FileReference {

        val pathExpr = children.firstInstance<Expr.Literal.String>() ?: error("Include directive is missing its string literal path argument")

        override var reference: PsiFile? = null

        companion object : AsmDirectiveT {
            override val keyWord: String = ".include"
            override val builder: NodeBuilderFn = { markerInfo, children, range ->
                if (
                    children.size >= 2
                ) {
                    Include(range, *children)
                } else null
            }

            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword already consumed by caller
                skipWhitespaceAndComments()
                val stringParsed = with(asmParser) { parseString() } // Pass builder explicitly
                if (stringParsed == null) {
                    error("Expected string literal path after .include")
                    // Allow empty or error node creation
                    marker.drop()
                    return false
                }
                marker.done(Include)
                return true
            }
        }
    }

    class Emissive(override val type: EmissiveT, range: IntRange, vararg children: PsiElement) : AsmDirective(type, range, *children) {

        val arguments = children.filterIsInstance<Expr>()

        enum class EmissiveT : AsmDirectiveT {
            BYTE,
            SHORT,
            HALF,
            WORD,
            LONG,
            QUAD,
            FLOAT,
            DOUBLE,
            STRING,
            ASCII,
            ASCIZ;

            override val keyWord: String = ".${name.lowercase()}"
            override val builder: NodeBuilderFn = { markerInfo, children, range ->
                if (children.isNotEmpty()) {
                    Emissive(this@EmissiveT, range, *children)
                } else null
            }

            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword consumed by caller
                var firstArgument = true
                while (true) {
                    skipWhitespaceAndComments()
                    if (isAtEnd() || currentIs(PsiTokenType.LINEBREAK)) break // End of arguments

                    // Check for keyword starting next statement (heuristic)
                    if (getTokenType() == PsiTokenType.KEYWORD && peek()?.value?.startsWith(".") == true) break

                    if (!firstArgument) {
                        if (!expect(",")) {
                            // error("Expected ',' separating arguments for $keyWord") // Optional error
                            break // Assume end of arguments if no comma
                        }
                        skipWhitespaceAndComments()
                    }
                    firstArgument = false

                    val argParsed: PsiBuilder.Marker? = when (this@EmissiveT) {
                        STRING, ASCII, ASCIZ -> with(asmParser) { parseString() }
                        else -> with(asmParser) { parseExpression() } // Pass builder
                    }

                    if (argParsed == null) {
                        error("Expected ${if (this@EmissiveT in listOf(STRING, ASCII, ASCIZ)) "string literal" else "expression"} argument for $keyWord directive")
                        // Attempt recovery? Maybe advance past the problematic token?
                        // advance() // Be careful with recovery
                        break // Stop parsing args for this directive on error
                    }
                }
                marker.done(this@EmissiveT)
                return true
            }
        }
    }

    class SectionControl(override val type: SectionControlT, range: IntRange, vararg children: PsiElement) : AsmDirective(type, range, *children) {
        val arguments = children.drop(1)
        val sectionName: PsiElement? = if (type == SectionControlT.SECTION) arguments.firstOrNull() else null
        val sectionFlags: Expr.Literal.String? = if (type == SectionControlT.SECTION) arguments.getOrNull(1) as? PsiStatement.Expr.Literal.String else null // Adjust type
        val sectionType: PsiElement? = if (type == SectionControlT.SECTION) arguments.getOrNull(2) else null // Type descriptor node

        enum class SectionControlT : AsmDirectiveT {
            TEXT,
            DATA,
            RODATA,
            BSS,
            SECTION
            ;

            override val keyWord: String = ".${name.lowercase()}"
            override val builder: NodeBuilderFn = { markerInfo, children, range ->
                if (children.isNotEmpty()) {
                    SectionControl(this@SectionControlT, range, *children)
                } else null
            }

            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword consumed by caller
                if (this@SectionControlT == SECTION) {
                    skipWhitespaceAndComments()
                    // Use the specific helper for section names
                    if (!expect(PsiTokenType.IDENTIFIER, PsiTokenType.KEYWORD)) {
                        marker.drop()
                        return false
                    }

                    // Parse optional flags and type descriptor
                    skipWhitespaceAndComments()
                    if (currentIs(",")) {
                        advance() // Consume ',' before flags
                        skipWhitespaceAndComments()
                        // Flags are typically a string literal
                        val flagsParsed = with(asmParser) { parseString() }
                        if (flagsParsed == null) {
                            // Maybe wasn't a string, could be something else?
                            // Or just an error if flags were expected after comma
                            error("Expected string literal for section flags after ','")
                        }

                        // Check for another comma before type descriptor
                        skipWhitespaceAndComments()
                        if (currentIs(",")) {
                            advance() // Consume ',' before type
                            skipWhitespaceAndComments()
                            // Type descriptor starts with '@'
                            val typeParsed = with(asmParser) { parseTypeDescriptor() }
                            if (typeParsed == null) {
                                error("Expected section type descriptor (e.g., '@progbits') after ','")
                            }
                            // TODO: Parse optional flag-specific arguments after type? (Advanced)
                        }
                    }

                } else {
                    // .text, .data, .rodata, .bss : Optional subsection (expression)
                    skipWhitespaceAndComments()
                    if (!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) {
                        with(asmParser) { parseExpression() }
                        // No error if expression not found, it's optional
                    }
                }
                marker.done(this@SectionControlT)
                return true
            }
        }
    }

    // --- Symbol Management Directives ---
    class SymbolManagement(override val type: SymbolManagementT, range: IntRange, vararg children: PsiElement) : AsmDirective(type, range, *children) {
        val symbols: List<PsiToken> = children.filterIsInstance<PsiToken>().filter { it.type == PsiTokenType.IDENTIFIER }
        val expressionArg: Expr? = children.firstInstance<PsiStatement.Expr>() // Find first actual expression node
        val typeArg: AsmTypeDescriptor? = children.firstInstance<AsmTypeDescriptor>() // Example check

        enum class SymbolManagementT : AsmDirectiveT {
            GLOBL, LOCAL, WEAK, COMM, LCOMM, TYPE, SIZE;

            override val keyWord: String by lazy { ".${name.lowercase()}" }
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.isNotEmpty()) {
                    SymbolManagement(this@SymbolManagementT, range, *children)
                } else null
            }

            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword consumed by caller
                when (this@SymbolManagementT) {
                    GLOBL, LOCAL, WEAK -> {
                        var first = true
                        while (true) {
                            skipWhitespaceAndComments()
                            if (isAtEnd() || currentIs(PsiTokenType.LINEBREAK)) break
                            if (!first) {
                                if (!expect(",")) break
                            }
                            first = false
                            skipWhitespaceAndComments()
                            // **MODIFIED**: Expect identifier token directly
                            if (!expect(PsiTokenType.IDENTIFIER, "Expected symbol name (identifier token) ${if (!first) "after ','" else ""} for $keyWord")) {
                                break // Error reported by expect
                            }
                        }
                    }
                    COMM, LCOMM -> {
                        skipWhitespaceAndComments()
                        // **MODIFIED**: Expect identifier token
                        if (!expect(PsiTokenType.IDENTIFIER, "Expected symbol name (identifier token) for $keyWord")) { marker.done(this@SymbolManagementT); return true }
                        skipWhitespaceAndComments()
                        if (!expect(",")) { error("Expected ',' after symbol name in $keyWord"); marker.done(this@SymbolManagementT); return true }
                        skipWhitespaceAndComments()
                        // Parse expression for size
                        if (with(asmParser) { parseExpression() } == null) { error("Expected size expression for $keyWord"); marker.done(this@SymbolManagementT); return true }
                        // Optional align (expression)
                        skipWhitespaceAndComments()
                        if (currentIs(",")) {
                            advance()
                            skipWhitespaceAndComments()
                            if (with(asmParser) { parseExpression() } == null) { error("Expected alignment expression after ',' for $keyWord") }
                        }
                    }
                    TYPE -> {
                        skipWhitespaceAndComments()
                        // **MODIFIED**: Expect identifier token
                        if (!expect(PsiTokenType.IDENTIFIER, "Expected symbol name (identifier token) for $keyWord")) { marker.done(this@SymbolManagementT); return true }
                        skipWhitespaceAndComments()
                        if (!expect(",")) { error("Expected ',' after symbol name in $keyWord"); marker.done(this@SymbolManagementT); return true }
                        skipWhitespaceAndComments()
                        // Parse type descriptor
                        if (with(asmParser) { parseTypeDescriptor() } == null) {
                            error("Expected type descriptor (e.g., '@function') for $keyWord")
                        }
                    }
                    SIZE -> {
                        skipWhitespaceAndComments()
                        // **MODIFIED**: Expect identifier token
                        if (!expect(PsiTokenType.IDENTIFIER, "Expected symbol name (identifier token) for $keyWord")) { marker.done(this@SymbolManagementT); return true }
                        skipWhitespaceAndComments()
                        if (!expect(",")) { error("Expected ',' after symbol name in $keyWord"); marker.done(this@SymbolManagementT); return true }
                        skipWhitespaceAndComments()
                        // Parse expression for size
                        if (with(asmParser) { parseExpression() } == null) { error("Expected size expression for $keyWord") }
                    }
                }
                marker.done(this@SymbolManagementT)
                return true
            }
        }
    }

    // --- Alignment and Padding Directives ---
    class Alignment(override val type: AlignmentT, range: IntRange, vararg children: PsiElement) : AsmDirective(type, range, *children) {
        val arguments = children.filterIsInstance<PsiStatement.Expr>() // Adjust type

        enum class AlignmentT : AsmDirectiveT {
            ALIGN, P2ALIGN, FILL, SKIP, SPACE, ZERO;

            override val keyWord: String by lazy { ".${name.lowercase()}" }
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.isNotEmpty()) {
                    Alignment(this@AlignmentT, range, *children)
                } else null
            }

            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword consumed by caller
                skipWhitespaceAndComments()
                if (with(asmParser) { parseExpression() } == null) {
                    // Only .zero requires an argument unconditionally based on common usage.
                    if (this@AlignmentT == ZERO) error("Expected size argument for $keyWord")
                    // Others might technically allow zero args, but often expect at least one.
                    // Add error if needed based on specific assembler behavior.
                    // else { error("Expected first argument (expression) for $keyWord") }
                } else {
                    // Parse optional subsequent arguments
                    var argIndex = 1
                    while (currentIs(",")) {
                        advance()
                        skipWhitespaceAndComments()
                        if (with(asmParser) { parseExpression() } == null) {
                            error("Expected expression after ',' for $keyWord argument ${argIndex + 1}")
                            break
                        }
                        argIndex++
                        // Argument count limits
                        if ((this@AlignmentT == ALIGN || this@AlignmentT == P2ALIGN || this@AlignmentT == FILL) && argIndex >= 3) break
                        if ((this@AlignmentT == SKIP || this@AlignmentT == SPACE) && argIndex >= 2) break
                        // .zero only takes one arg, loop shouldn't continue if first arg parsed
                    }
                }
                marker.done(this@AlignmentT)
                return true
            }
        }
    }

    // --- Assembler Control Directives ---
    class AssemblyControl(override val type: AssemblyControlT, range: IntRange, vararg children: PsiElement) : AsmDirective(type, range, *children) {
        val symbol: PsiToken? = children.firstInstance<PsiToken> { it.type == PsiTokenType.IDENTIFIER }
        val expression: Expr? = children.firstInstance<Expr>()
        val syntaxOption : PsiElement? = if (type == AssemblyControlT.INTEL_SYNTAX || type == AssemblyControlT.ATT_SYNTAX) children.getOrNull(1) else null

        enum class AssemblyControlT : AsmDirectiveT {
            EQU, EQUIV, SET, INTEL_SYNTAX, ATT_SYNTAX;

            override val keyWord: String by lazy { ".${name.lowercase()}" }
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.isNotEmpty()) {
                    AssemblyControl(this@AssemblyControlT, range, *children)
                } else null
            }

            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword consumed by caller
                when (this@AssemblyControlT) {
                    EQU, EQUIV, SET -> {
                        skipWhitespaceAndComments()
                        // **MODIFIED**: Expect identifier token
                        if (!expect(PsiTokenType.IDENTIFIER, "Expected symbol name (identifier token) for $keyWord")) { marker.done(this@AssemblyControlT); return true }
                        skipWhitespaceAndComments()
                        if (!expect(",")) { error("Expected ',' after symbol name in $keyWord"); marker.done(this@AssemblyControlT); return true }
                        skipWhitespaceAndComments()
                        // Parse expression
                        if (with(asmParser) { parseExpression() } == null) { error("Expected expression for $keyWord") }
                    }
                    INTEL_SYNTAX, ATT_SYNTAX -> {
                        skipWhitespaceAndComments()
                        if (!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) {
                            // **MODIFIED**: Expect identifier token ('prefix' or 'noprefix')
                            if (currentIs("prefix") || currentIs("noprefix")) {
                                // Use expect for the identifier token
                                expect(PsiTokenType.IDENTIFIER, "Internal Error: Expected 'prefix' or 'noprefix' identifier")
                            } else {
                                error("Expected 'prefix' or 'noprefix' for $keyWord, found '${peek()?.value}'")
                            }
                        }
                        // No argument is also valid
                    }
                }
                marker.done(this@AssemblyControlT)
                return true
            }
        }
    }

    // --- Macro Definition Directives ---
    class MacroDefinition(override val type: MacroDefinitionT, range: IntRange, vararg children: PsiElement) : AsmDirective(type, range, *children) {
        val macroNameToken: PsiToken? = if (type == MacroDefinitionT.MACRO) children.firstInstance<PsiToken> { it.type == PsiTokenType.IDENTIFIER } else null
        val parameterTokens: List<PsiToken> = if (type == MacroDefinitionT.MACRO) children.filterIsInstance<PsiToken>().filter { it.type == PsiTokenType.IDENTIFIER }.drop(1) else emptyList() // Drop the name

        enum class MacroDefinitionT : AsmDirectiveT {
            MACRO, ENDM;

            override val keyWord: String by lazy { ".${name.lowercase()}" }
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.isNotEmpty()) {
                    MacroDefinition(this@MacroDefinitionT, range, *children)
                } else null
            }

            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword consumed by caller
                if (this@MacroDefinitionT == MACRO) {
                    skipWhitespaceAndComments()
                    // **MODIFIED**: Expect identifier token for name
                    if (!expect(PsiTokenType.IDENTIFIER, "Expected macro name (identifier token) after .macro")) {
                        // Error reported by expect, stop parsing args
                    } else {
                        // Parse optional comma-separated parameters (identifier tokens)
                        skipWhitespaceAndComments()
                        // Allow comma directly after name or after space
                        if (currentIs(",")) {
                            advance()
                            skipWhitespaceAndComments()
                        }
                        // Loop parsing parameters
                        var firstParam = true
                        while(!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) {
                            // **MODIFIED**: Check if next token is identifier before expecting comma
                            if (getTokenType() != PsiTokenType.IDENTIFIER) break // Stop if not an identifier

                            if (!firstParam) {
                                if (!expect(",")) break // Stop if no comma before next identifier
                                skipWhitespaceAndComments()
                            }
                            firstParam = false

                            // **MODIFIED**: Expect identifier token for parameter
                            if (!expect(PsiTokenType.IDENTIFIER, "Expected parameter name (identifier token) ${if (!firstParam) "after ','" else ""} for .macro")) {
                                break // Error reported by expect
                            }
                            skipWhitespaceAndComments()
                        }
                    }
                } else { // .endm
                    skipWhitespaceAndComments()
                    if (!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) {
                        error("Unexpected tokens after .endm: '${peek()?.value}'")
                    }
                }
                marker.done(this@MacroDefinitionT)
                return true
            }
        }
    }

    // --- Conditional Assembly Directives ---
    class ConditionalAssembly(override val type: ConditionalAssemblyT, range: IntRange, vararg children: PsiElement) : AsmDirective(type, range, *children) {
        // Condition is expression OR symbol (token)
        val conditionExpression: PsiStatement.Expr? = children.firstInstance()
        val conditionSymbol: PsiToken? = children.firstInstance<PsiToken> { it.type == PsiTokenType.IDENTIFIER }

        enum class ConditionalAssemblyT : AsmDirectiveT {
            IF, IFDEF, IFNDEF, ELSEIF, ELSE, ENDIF;

            override val keyWord: String by lazy { ".${name.lowercase()}" }
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.isNotEmpty()) {
                    ConditionalAssembly(this@ConditionalAssemblyT, range, *children)
                } else null
            }

            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword consumed by caller
                when (this@ConditionalAssemblyT) {
                    IF, ELSEIF -> {
                        skipWhitespaceAndComments()
                        if (with(asmParser) { parseExpression() } == null) {
                            error("Expected expression after $keyWord")
                        }
                    }
                    IFDEF, IFNDEF -> {
                        skipWhitespaceAndComments()
                        // **MODIFIED**: Expect identifier token
                        if (!expect(PsiTokenType.IDENTIFIER, "Expected symbol name (identifier token) after $keyWord")) {
                            // Error reported by expect
                        }
                    }
                    ELSE, ENDIF -> {
                        skipWhitespaceAndComments()
                        if (!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) {
                            error("Unexpected tokens after $keyWord: '${peek()?.value}'")
                        }
                    }
                }
                marker.done(this@ConditionalAssemblyT)
                return true
            }
        }
    }

    // --- Debugging Information Directives ---
    class Debugging(override val type: DebuggingT, range: IntRange, vararg children: PsiElement) : AsmDirective(type, range, *children) {
        // Arguments vary widely
        val arguments = children.drop(1) // Example: get all non-keyword children

        enum class DebuggingT : AsmDirectiveT {
            FILE,   // .file [index] "filename"
            LOC,    // .loc fileno lineno [col] [options...] (options: basic_block, prologue_end, epilogue_begin, is_stmt value)
            CFI_STARTPROC, // .cfi_startproc [simple]
            CFI_ENDPROC,   // .cfi_endproc
            CFI_DEF_CFA, // .cfi_def_cfa register, offset
            CFI_DEF_CFA_REGISTER, // .cfi_def_cfa_register register
            CFI_DEF_CFA_OFFSET,   // .cfi_def_cfa_offset offset
            CFI_ADJUST_CFA_OFFSET,// .cfi_adjust_cfa_offset offset
            CFI_OFFSET, // .cfi_offset register, offset
            CFI_REL_OFFSET, // .cfi_rel_offset register, offset
            CFI_REGISTER, // .cfi_register register1, register2
            CFI_RESTORE,  // .cfi_restore register
            CFI_UNDEFINED,// .cfi_undefined register
            CFI_SAME_VALUE, // .cfi_same_value register
            CFI_REMEMBER_STATE, // .cfi_remember_state
            CFI_RESTORE_STATE,  // .cfi_restore_state
            // Add others as needed (.cfi_sections, .cfi_window_save, .cfi_escape, ...)
            ;

            override val keyWord: String by lazy { ".${name.lowercase()}" }
            override val builder: NodeBuilderFn = { _, children, range ->
                if (children.isNotEmpty()) {
                    Debugging(this@DebuggingT, range, *children)
                } else null
            }

            // NOTE: CFI parsing can be quite complex. This is a simplified version.
            override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {
                // Keyword consumed by caller
                skipWhitespaceAndComments()

                // Simple argument parsing based on common patterns
                when (this@DebuggingT) {
                    FILE -> {
                        // Optional index (integer literal), required filename (string literal)
                        // Ambiguity: Is the first token an index or filename? Assume index if integer.
                        var fileIndexParsed: PsiBuilder.Marker? = null
                        val currentTokenType = getTokenType()
                        if (currentTokenType is PsiTokenType.LITERAL.INTEGER) {
                            fileIndexParsed = with(asmParser) { parseIntegerLiteral() } // Assuming helper
                            skipWhitespaceAndComments()
                        }
                        // Now expect filename
                        if (with(asmParser) { parseString() } == null) {
                            error("Expected filename (string literal) for .file")
                            // Handle case where index might have been mistaken for filename if no string found?
                            // If fileIndexParsed != null && string fails, maybe rollback index parse? Complex recovery.
                        }
                    }
                    LOC -> {
                        // fileno (int), lineno (int), optional col (int), optional keywords
                        if (with(asmParser) { parseIntegerLiteral() } == null) { error("Expected file number for .loc"); marker.done(this@DebuggingT); return true }
                        skipWhitespaceAndComments()
                        if (with(asmParser) { parseIntegerLiteral() } == null) { error("Expected line number for .loc"); marker.done(this@DebuggingT); return true }
                        skipWhitespaceAndComments()
                        // Optional column
                        if (getTokenType() is PsiTokenType.LITERAL.INTEGER) {
                            with(asmParser) { parseIntegerLiteral() }
                            skipWhitespaceAndComments()
                        }
                        // Optional keywords (parse as identifiers)
                        while (!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) {
                            // Check for known keywords or just parse identifiers
                            if (getTokenType() == PsiTokenType.IDENTIFIER || getTokenType() == PsiTokenType.KEYWORD) {
                                with(asmParser) { parseIdentifier() } // Treat options as identifiers
                                skipWhitespaceAndComments()
                                // Could check if identifier is one of the known options:
                                // basic_block, prologue_end, epilogue_begin, is_stmt
                                // If is_stmt, expect value (integer)
                                // if (lastParsedIdentifierText == "is_stmt") parseIntegerLiteral() ...
                            } else {
                                error("Unexpected token in .loc options: '${peek()?.value}'")
                                advance() // Consume unexpected token
                            }
                        }
                    }
                    CFI_STARTPROC -> {
                        // Optional 'simple' keyword/identifier
                        if (currentIs("simple")) {
                            with(asmParser) { parseIdentifier() }
                        }
                    }
                    CFI_ENDPROC, CFI_REMEMBER_STATE, CFI_RESTORE_STATE -> {
                        // No arguments usually expected
                        if (!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) { error("Unexpected tokens after $keyWord") }
                    }
                    CFI_DEF_CFA, CFI_OFFSET, CFI_REL_OFFSET, CFI_REGISTER -> {
                        // register, offset/register
                        if (!expect(PsiTokenType.IDENTIFIER, "Expected register name (identifier token) for $keyWord")) { marker.done(this@DebuggingT); return true }
                        skipWhitespaceAndComments()
                        if (!expect(",", "Expected ',' after register in $keyWord")) { marker.done(this@DebuggingT); return true }
                        skipWhitespaceAndComments()

                        if (this@DebuggingT == CFI_REGISTER) {
                           expect(PsiTokenType.IDENTIFIER, "Expected second register name (identifier token) for $keyWord")
                        } else {
                            // Expect expression for offset
                            if (with(asmParser) { parseExpression() } == null) { error("Expected offset expression for $keyWord") }
                        }
                    }
                    CFI_DEF_CFA_REGISTER, CFI_RESTORE, CFI_UNDEFINED, CFI_SAME_VALUE -> {
                        // Single register argument
                        expect(PsiTokenType.IDENTIFIER, "Expected register name (identifier token) for $keyWord")
                    }
                    CFI_DEF_CFA_OFFSET, CFI_ADJUST_CFA_OFFSET -> {
                        // Single offset (expression) argument
                        if (with(asmParser) { parseExpression() } == null) { error("Expected offset expression for $keyWord") }
                    }
                    // Handle other CFI directives similarly based on their expected arguments
                    /*else -> {
                        // Default: Maybe consume remaining tokens on line as generic arguments?
                        // Or report "Parsing not fully implemented for $keyWord"
                        while (!isAtEnd() && !currentIs(PsiTokenType.LINEBREAK)) {
                            advance() // Consume remaining tokens as unknown args
                        }
                    }*/
                }
                marker.done(this@DebuggingT)
                return true
            }
        }
    }

}