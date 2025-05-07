package cengine.lang.asm.gas

import cengine.console.IOContext
import cengine.console.SysOut
import cengine.lang.asm.AsmSpec
import cengine.lang.asm.gas.AsmCodeGenerator
import cengine.lang.asm.gas.AsmEvaluator.evaluate
import cengine.lang.asm.psi.AsmDirective
import cengine.lang.asm.psi.AsmInstruction
import cengine.lang.asm.psi.AsmLine
import cengine.lang.obj.elf.Shdr
import cengine.project.Project
import cengine.psi.elements.PsiFile
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.semantic.expr.EvaluationException
import cengine.psi.semantic.expr.IntegerExpressionEvaluator
import cengine.util.integer.BigInt
import cengine.util.integer.BigInt.Companion.toBigInt
import cengine.util.integer.UInt8
import cengine.util.integer.UInt8.Companion.toUInt8
import cengine.vfs.FPath

/**
 * Orchestrates the assembly process from a PSI tree to final binary output.
 * It utilizes an AsmCodeGenerator to manage sections, symbols, and binary generation.
 * The process is divided into multiple phases.
 */
class AsmBackend<T : AsmCodeGenerator.Section>(
    val project: Project,
    val entryPsiFile: PsiFile,
    val codeGenerator: AsmCodeGenerator<T>,
    val io: IOContext = SysOut,
) {

    companion object {
        val PASS_0_PREPROCESSING = 0
        val PASS_1_LAYOUT = 1
        val PASS_2_CODEGEN = 2
    }

    // Instantiate the integer evaluator with the specific resolver lambda for assembly
    val absIntEvaluator = IntegerExpressionEvaluator<AsmEvaluationContext>(
        processAssignment = { expr, value, context ->
            if (expr !is PsiStatement.Expr.Identifier) return@IntegerExpressionEvaluator
            val name = expr.name ?: return@IntegerExpressionEvaluator
            val symbol = codeGenerator.symbols.firstOrNull { it.name == expr.name && it is AsmCodeGenerator.Symbol.Abs } as? AsmCodeGenerator.Symbol.Abs
            if (symbol != null) {
                symbol.value = value
            } else {
                codeGenerator.createAbsSymbolInCurrentSection(name, value)
            }
        }
    ) { name, element, context ->
        // Lambda to resolve identifiers (symbols, labels, '.')

        // Handle '.' for current address
        if (name == ".") {
            return@IntegerExpressionEvaluator context.currentAddress
        }

        val symbol = context.symbols.firstOrNull { it.name == name } ?: return@IntegerExpressionEvaluator null // Symbol not found in this context/pass

        when (symbol) {
            is AsmCodeGenerator.Symbol.Abs -> symbol.value
            is AsmCodeGenerator.Symbol.Label -> {
                // Labels depend on section addresses, which are resolved *before* Pass 2
                if (context.pass == PASS_1_LAYOUT) {
                    // In Pass 1, we generally cannot resolve label addresses reliably,
                    // especially forward references or labels in sections whose base address is unknown.
                    // Return null to indicate it's unresolved in this pass.
                    // Specific directives like .size might try anyway and handle the exception.
                    null
                } else {
                    // In Pass 2, section addresses MUST be resolved.
                    symbol.address()
                }
            }
        }

        // Handle other symbol types if they exist (e.g., Common Symbols)
    }

    val relIntEvaluator = IntegerExpressionEvaluator<AsmEvaluationContext>(
        processAssignment = { expr, value, context ->
            if (expr !is PsiStatement.Expr.Identifier) return@IntegerExpressionEvaluator
            val name = expr.name ?: return@IntegerExpressionEvaluator
            val symbol = codeGenerator.symbols.firstOrNull { it.name == expr.name && it is AsmCodeGenerator.Symbol.Abs } as? AsmCodeGenerator.Symbol.Abs
            if (symbol != null) {
                symbol.value = value
            } else {
                codeGenerator.createAbsSymbolInCurrentSection(name, value)
            }
        }
    ) { name, element, context ->
        // Handle '.' for current address
        if (name == ".") {
            return@IntegerExpressionEvaluator BigInt.ZERO
        }

        val symbol = context.symbols.firstOrNull { it.name == name } ?: return@IntegerExpressionEvaluator null // Symbol not found in this context/pass

        when (symbol) {
            is AsmCodeGenerator.Symbol.Abs -> symbol.value
            is AsmCodeGenerator.Symbol.Label -> {
                // Labels depend on section addresses, which are resolved *before* Pass 2
                if (context.pass == PASS_1_LAYOUT) {
                    // In Pass 1, we generally cannot resolve label addresses reliably,
                    // especially forward references or labels in sections whose base address is unknown.
                    // Return null to indicate it's unresolved in this pass.
                    // Specific directives like .size might try anyway and handle the exception.
                    null
                } else {
                    // In Pass 2, section addresses MUST be resolved.
                    symbol.address() - context.currentAddress
                }
            }
        }
    }

    // Stores the sequence after preprocessing
    private lateinit var processedStatementSequence: Sequence<AsmLine>
    private var preprocessingErrors = false

    // Instantiate other evaluators if needed (e.g., for .float, .double directives)
    // private val floatEvaluator = FloatExpressionEvaluator<AsmEvaluationContext> { ... }
    // private val doubleEvaluator = DoubleExpressionEvaluator<AsmEvaluationContext> { ... }

    // --- Main Assembly Function ---

    /**
     * Executes all assembly phases in order.
     * Returns the generated byte array or null on failure.
     */
    suspend fun assemble(): ByteArray? {
        io.log("Starting assembly for ${entryPsiFile.file.path}")
        try {
            // Phase 1: Linking (.include resolution)
            if (!runLinking()) return null

            // Phase 2: Preprocessing (Macros, Conditionals, .set/.equ) - Simplified
            if (!runPreprocessing()) return null

            // Phase 3: Symbol Definition and Section Allocation (First Pass)
            if (!runSymbolAndSectionPass()) return null

            // Phase 4: Address & Layout Calculation
            if (!runAddressResolution()) return null

            // Phase 5: Code Generation & Relocation (Second Pass)
            if (!runCodeGenerationPass()) return null

            // Phase 6: Final Output Generation
            io.log("Assembly finished, generating output file.")
            return codeGenerator.writeFile()
        } catch (e: Exception) {
            io.error("Assembly failed with unexpected exception: ${e.message ?: e::class.simpleName}")
            io.debug { e.stackTraceToString() }
            return null
        }
    }

    // --- Assembly Phases ---

    /**
     * Phase 1: Resolves .include directives, linking multiple source files.
     * Uses AsmFileLinker.
     */
    private suspend fun runLinking(): Boolean {
        io.log("Phase 1: Linking files...")
        val linker = AsmFileLinker(project, io)
        try {
            // AsmFileLinker manages visited state and cycle detection.
            linker.link(entryPsiFile)
            // Check if critical errors were reported during linking (e.g., cycles)
            // Note: AsmFileLinker adds errors to PSI, we might need a global error flag if needed.
            // For now, assume success if it completes without throwing exceptions.
            io.log("Phase 1: Linking complete.")
            return true // Indicate success
        } catch (e: Exception) {
            io.error("Linking failed with unexpected exception: ${e.message ?: e::class.simpleName}")
            return false // Indicate failure
        }
    }

    /**
     * Phase 2: Handles preprocessing directives like .set, .equ.
     * TODO: Implement conditional assembly (.if, .ifdef, etc.)
     * TODO: Implement macro definition (.macro, .endm) and expansion.
     */
    private fun runPreprocessing(): Boolean {
        io.log("Phase 2: Preprocessing...")
        var success = true
        // We need to visit all linked files. A simple approach is to collect them

        val statementSequence = generateStatementSequence(entryPsiFile)

        try {
            for (line in statementSequence) {
                // Process directives relevant to preprocessing
                line.directive?.let { directive ->
                    when (directive) {
                        is AsmDirective.AssemblyControl -> {
                            // Only handle .set/.equ etc. here

                            if (!handleAssemblyControlDirective(directive)) success = false

                            // Ignore section/data directives etc. in this pass
                        }

                        is AsmDirective.Include -> {
                            // Include directives are handled by generateStatementSequence, ignore here.
                        }
                        // TODO: Handle Macro Definitions/Conditionals here
                        else -> {} // Ignore other directives in this pass
                    }
                }
                // Instructions and labels are ignored in this pass
            }
        } catch (e: Exception) {
            io.error("Preprocessing failed with unexpected exception: ${e.message ?: e::class.simpleName}")
            io.debug { e.stackTraceToString() }
            success = false
        }

        io.log("Phase 2: Preprocessing complete (${if (success) "OK" else "with errors"}).")
        return success
    }

    /**
     * Phase 3: Defines symbols (labels, .globl, .comm) and allocates space/data (.byte, .word, .space, instructions).
     * Calculates preliminary sizes for sections. Does *not* resolve forward references yet.
     */
    private fun runSymbolAndSectionPass(): Boolean {
        io.log("Phase 3: Symbol Definitions & Section Allocation Pass...")
        var success = true
        // Get the unified sequence of statements, including included content
        val statementSequence = generateStatementSequence(entryPsiFile)

        // Initialize with a default section if none is specified at the start
        if (codeGenerator.sections.isEmpty()) {
            // Typically defaults to .text, but check specified assembler/target behavior
            io.warn("No initial section directive found, defaulting to '.text'")
            codeGenerator.getOrCreateSectionAndSetCurrent(".text", Shdr.SHT_PROGBITS, Shdr.SHF_text.toUInt64())
        }

        try {
            for (line in statementSequence) {
                // 1. Handle Label Declaration
                line.label?.let { labelDecl ->
                    val labelName = labelDecl.name
                    if (labelName != null) {
                        // Offset calculation is now simpler: it's always the current size of the *current* section
                        val offset = codeGenerator.currentSection.content.size.toBigInt()
                        val existingLabel = codeGenerator.symbols.firstOrNull { it.name == labelName && it is AsmCodeGenerator.Symbol.Label }

                        if (existingLabel != null) {
                            labelDecl.addError("Label '$labelName' already defined.")
                            success = false
                        } else {
                            val existingBinding = codeGenerator.symbols.firstOrNull { it.name == labelName }?.binding

                            val symbol = AsmCodeGenerator.Symbol.Label(
                                labelName,
                                codeGenerator.currentSection,
                                existingBinding ?: AsmCodeGenerator.Symbol.Binding.LOCAL,
                                offset
                            )
                            if (!codeGenerator.symbols.add(symbol)) {
                                labelDecl.addError("Failed to add symbol '$labelName' (potential hash collision or concurrent issue).")
                                success = false
                            } else {
                                io.debug { "Defined label: $symbol at offset $offset in ${codeGenerator.currentSection.name}" }
                            }
                        }
                    } else {
                        labelDecl.addError("Label declaration is missing a name.")
                        success = false
                    }
                } // End label handling

                // 2. Handle Instruction or Directive
                line.instruction?.let { instr ->
                    // Pass 1: Estimate size and reserve space
                    if (!handleInstructionSizeEstimation(instr)) success = false
                }
                line.directive?.let { directive ->
                    // Pass 1: Handle directives affecting layout/symbols
                    // Skip includes, they are handled by the sequence generator
                    if (!handleDirectiveInSymbolPass(directive)) success = false
                } // End directive handling

                line.expr?.let { expr ->
                    absIntEvaluator.evaluate(expr, createPass1Context())
                }

            } // End loop through statement sequence
        } catch (e: Exception) {
            io.error("Symbol/Section Pass failed with unexpected exception: ${e.message ?: e::class.simpleName}")
            io.debug { e.stackTraceToString() }
            success = false
        }

        io.log("Phase 3: Symbol/Section Pass complete (${if (success) "OK" else "with errors"}).")
        return success
    }

    /**
     * Phase 4: Order sections and resolve absolute addresses for sections and symbols.
     * Delegates the core logic to the AsmCodeGenerator.
     */
    private fun runAddressResolution(): Boolean {
        io.log("Phase 4: Resolving Addresses...")

        try {
            codeGenerator.orderSectionsAndResolveAddresses() // Abstract method call

            // Debug: Print section addresses
            io.log("Section Addresses:")
            codeGenerator.sections.forEach { sec ->
                io.log("  ${sec.name}: Addr=0x${sec.address.toString(16)}, Size=${sec.content.size}")
            }
            // Debug: Print symbol addresses/values
            io.log("Symbol Table:")
            codeGenerator.symbols.forEach { sym ->
                when (sym) {
                    is AsmCodeGenerator.Symbol.Abs -> io.log("  Abs: ${sym.name} = ${sym.value} (${sym.binding}) in ${sym.section.name}")
                    is AsmCodeGenerator.Symbol.Label -> io.log("  Label: ${sym.name} @ 0x${sym.address().toString(16)} (${sym.binding}) in ${sym.section.name} (offset ${sym.offset})")
                }
            }

            io.log("Phase 4: Address Resolution complete.")
            return true
        } catch (e: Exception) {
            io.error("Phase 4: Address Resolution failed: ${e.message}")
            e.printStackTrace() // For debugging
            return false
        }
    }

    /**
     * Phase 5: Generate final machine code for instructions, filling in placeholders.
     * Uses the resolved addresses from Phase 4.
     */
    private fun runCodeGenerationPass(): Boolean {
        io.log("Phase 5: Code Generation Pass...")
        var success = true

        // Process reservations (instructions that needed late initialization)
        codeGenerator.sections.forEach { section ->
            // Skip code generation for NOBITS sections like .bss
            if (section.type == Shdr.SHT_NOBITS) {
                io.log("Skipping code generation for NOBITS section ${section.name}.")
                section.reservations.clear() // Clear reservations as they are irrelevant
                return@forEach
            }

            io.log("Generating code for section ${section.name}...")
            val iterator = section.reservations.iterator() // Use iterator for safe removal if needed

            while (iterator.hasNext()) {
                val reservation = iterator.next()
                val instr = reservation.instr
                // Ensure offset is Int for buffer access, handle potential overflow if section > 2GB
                val offsetInSection = try {
                    reservation.offset.toInt() // Keep offset as UInt in reservation? Convert here.
                } catch (e: NumberFormatException) {
                    instr.addError("Instruction offset ${reservation.offset} is too large for buffer indexing.")
                    success = false; iterator.remove(); continue
                }

                // Calculate the final absolute address of the instruction
                val currentAddress = section.address + offsetInSection.toBigInt() // Use original UInt/BigInt offset for address math

                val instrType = instr.type

                try {
                    io.debug { "Generating binary for ${instr.mnemonic} at offset $offsetInSection (Address: 0x${currentAddress.toString(16)})" }

                    val context = section.createPass2Context(reservation)
                    // Call the specific generateBinary method for the instruction type
                    with(instrType) {
                        pass2BinaryGeneration(
                            instr,
                            context
                        )
                    }

                    // Optionally verify the size written matches estimated size if needed?

                    iterator.remove() // Successfully processed, remove reservation

                } catch (e: EvaluationException) {
                    instr.addError("Failed to resolve symbol/expression for instruction: ${e.message}")
                    success = false
                } catch (e: Exception) {
                    instr.addError("Failed to generate code for instruction: ${e.message ?: e::class.simpleName}")
                    io.debug { e.stackTraceToString() }
                    success = false
                    // Don't remove reservation on error
                }
            }

            // Check if any reservations remained after processing (indicates errors)
            if (section.reservations.isNotEmpty()) {
                io.error("Section ${section.name} finished with ${section.reservations.size} unprocessed instruction reservations due to errors.")
                // Optionally list the failed instructions
                section.reservations.forEach { res ->
                    io.error(" -> Failed instruction: ${res.instr.mnemonic} at offset ${res.offset}")
                }
                success = false // Ensure overall failure is reported
                section.reservations.clear() // Clear remaining to avoid issues in subsequent phases? Or keep for debugging? Let's clear for now.
            }
        } // End loop through sections

        io.log("Phase 5: Code Generation complete (${if (success) "OK" else "with errors"}).")
        return success
    }

    // --- Directive and Instruction Handlers ---

    private fun handleAssemblyControlDirective(
        directive: AsmDirective.AssemblyControl,
    ): Boolean {
        val symName = directive.symbol?.value
        val expr = directive.expression
        val type = directive.type

        when (type) {
            AsmDirective.AssemblyControl.AssemblyControlT.EQU,
            AsmDirective.AssemblyControl.AssemblyControlT.EQUIV,
            AsmDirective.AssemblyControl.AssemblyControlT.SET,
                -> {
                if (symName == null) {
                    directive.addError("Directive '$type' requires a symbol name."); return false
                }
                if (expr == null) {
                    directive.addError("Directive '$type' requires an expression."); return false
                }

                return try {
                    // Evaluate expression in Pass 1 context
                    val value = absIntEvaluator.evaluate(expr, createPass1Context())

                    // Evaluate expression *now* during preprocessing
                    val existing = codeGenerator.symbols.firstOrNull { it.name == symName }
                    if (existing != null && type != AsmDirective.AssemblyControl.AssemblyControlT.SET) {
                        directive.addError("Symbol '$symName' already defined. Use '.set' to redefine.")
                        false
                    } else {
                        // Remove existing if it's a .set redefinition
                        if (existing != null) codeGenerator.symbols.remove(existing)
                        // Add/replace absolute symbol. Section association might be nominal here.
                        codeGenerator.createAbsSymbolInCurrentSection(symName, value)
                        io.debug { "Preprocessed '$type': $symName = $value" }
                        true
                    }
                } catch (e: Exception) {
                    expr.addError("Failed to evaluate expression for '$type': ${e.message}")
                    false
                }
            }

            AsmDirective.AssemblyControl.AssemblyControlT.INTEL_SYNTAX,
            AsmDirective.AssemblyControl.AssemblyControlT.ATT_SYNTAX,
                -> {
                // TODO: Implement syntax switching if the target code generator supports it.
                io.warn("Syntax switching directives (.intel_syntax, .att_syntax) not yet implemented.")
                // Add check for 'prefix'/'noprefix' argument if needed.
                return true // Allow directive for now
            }
        }
    }

    private fun handleInstructionSizeEstimation(instr: AsmInstruction): Boolean {
        val instrType = instr.type
        try {

            val context = createPass1Context()

            with(instrType) {
                pass1BinaryGeneration(instr, context)
            }

            return true
        } catch (e: Exception) {
            instr.addError("Failed to estimate size for instruction '${instr.mnemonic?.value ?: "unknown"}': ${e.message ?: e::class.simpleName}")
            io.debug { e.stackTraceToString() } // Log stack trace for debugging
            return false
        }
    }

    private fun handleDirectiveInSymbolPass(directive: AsmDirective): Boolean {
        return when (directive) {
            // Section Control
            is AsmDirective.SectionControl -> handleSectionControlDirective(directive)
            // Emissive Directives
            is AsmDirective.Emissive -> handleEmissiveDirective(directive)
            // Symbol Management
            is AsmDirective.SymbolManagement -> handleSymbolManagementDirective(directive)
            // Alignment & Padding
            is AsmDirective.Alignment -> handleAlignmentDirective(directive)
            // Assembly Control (.set/.equ handled in preprocessing)
            is AsmDirective.AssemblyControl -> true // Ignored in this pass
            // Macro Definition (Ignore in this pass)
            is AsmDirective.MacroDefinition -> true // Ignore .macro/.endm body processing here
            // Conditional Assembly (Structure ignored, content processed if condition met in preprocessing)
            is AsmDirective.ConditionalAssembly -> true // Ignore .if/.else/.endif markers themselves
            // Debugging Info
            is AsmDirective.Debugging -> handleDebuggingDirective(directive)
            // Include (Already processed)
            is AsmDirective.Include -> true
        }
    }

    private fun handleSectionControlDirective(directive: AsmDirective.SectionControl): Boolean {
        val type = directive.type
        try {
            when (type) {
                AsmDirective.SectionControl.SectionControlT.TEXT ->
                    codeGenerator.getOrCreateSectionAndSetCurrent(".text", Shdr.SHT_PROGBITS, Shdr.SHF_text.toUInt64())

                AsmDirective.SectionControl.SectionControlT.DATA ->
                    codeGenerator.getOrCreateSectionAndSetCurrent(".data", Shdr.SHT_PROGBITS, Shdr.SHF_data.toUInt64())

                AsmDirective.SectionControl.SectionControlT.RODATA ->
                    codeGenerator.getOrCreateSectionAndSetCurrent(".rodata", Shdr.SHT_PROGBITS, Shdr.SHF_rodata.toUInt64())

                AsmDirective.SectionControl.SectionControlT.BSS ->
                    // BSS sections are NOBITS, meaning they occupy space in memory but not in the file.
                    // The AsmCodeGenerator needs to handle this distinction.
                    codeGenerator.getOrCreateSectionAndSetCurrent(".bss", Shdr.SHT_NOBITS, Shdr.SHF_bss.toUInt64())

                AsmDirective.SectionControl.SectionControlT.SECTION -> {
                    val nameNode = directive.sectionName
                    val name = when (nameNode) {
                        is PsiStatement.Expr.Identifier -> nameNode.name
                        is PsiToken -> nameNode.value // Allow keywords? Needs careful check.
                        else -> null
                    }
                    if (name == null) {
                        directive.addError("'.section' directive requires a name.")
                        return false
                    }
                    // TODO: Parse flags (string literal) and type (@progbits etc.) properly
                    // val flagsStr = directive.sectionFlags?.evaluate() // Needs AsmEvaluator.evaluate
                    // val typeStr = directive.sectionType?.text // Needs parsing
                    // For now, use default progbits, alloc, write
                    io.warn("'.section' flags and type parsing not fully implemented for '$name'. Using defaults.")
                    codeGenerator.getOrCreateSectionAndSetCurrent(name, Shdr.SHT_PROGBITS, (Shdr.SHF_ALLOC + Shdr.SHF_WRITE).toUInt64())
                }
            }
            io.log("Switched to section: ${codeGenerator.currentSection.name}")
            return true
        } catch (e: Exception) {
            directive.addError("Failed to process section directive: ${e.message}")
            return false
        }
    }

    // Ensure these use the buffer's `put` methods
    private fun handleEmissiveDirective(directive: AsmDirective.Emissive): Boolean {
        val type = directive.type
        val section = codeGenerator.currentSection

        if (section.type == Shdr.SHT_NOBITS) {
            directive.addError("Cannot emit data (.$type) into a NOBITS section like '.bss'. Use '.space' or '.zero'.")
            return false
        }

        var success = true
        directive.arguments.forEach { argExpr ->
            try {
                // Evaluate expression relative to current position *within the section*

                val context = createPass1Context()

                when (type) {
                    AsmDirective.Emissive.EmissiveT.BYTE -> {
                        val value = absIntEvaluator.evaluate(argExpr, context)
                        context.section.content.put(value.toUInt8())
                    }

                    AsmDirective.Emissive.EmissiveT.SHORT, AsmDirective.Emissive.EmissiveT.HALF -> {
                        val value = absIntEvaluator.evaluate(argExpr, context)
                        context.section.content.put(value.toUInt16())
                    } // Check names
                    AsmDirective.Emissive.EmissiveT.WORD, AsmDirective.Emissive.EmissiveT.LONG -> {
                        val value = absIntEvaluator.evaluate(argExpr, context)
                        context.section.content.put(value.toUInt32())
                    }

                    AsmDirective.Emissive.EmissiveT.QUAD -> {
                        val value = absIntEvaluator.evaluate(argExpr, context)
                        context.section.content.put(value.toUInt64())
                    }

                    AsmDirective.Emissive.EmissiveT.FLOAT -> TODO("Handle .float directive - requires float parsing and buffer support")
                    AsmDirective.Emissive.EmissiveT.DOUBLE -> TODO("Handle .double directive - requires double parsing and buffer support")

                    AsmDirective.Emissive.EmissiveT.STRING, // GAS Manual: Not null-terminated
                    AsmDirective.Emissive.EmissiveT.ASCII,
                        -> { // Not null-terminated
                        val strLiteral = argExpr as? PsiStatement.Expr.Literal.String
                            ?: throw IllegalArgumentException("Expected string literal for .$type, found ${argExpr::class.simpleName}")
                        val stringValue = strLiteral.evaluate() // Gets the raw string content
                        context.section.content.putBytes(stringValue.encodeToByteArray()) // Use default UTF-8 or target-specific encoding? ASCII implies 7-bit? Check spec.
                    }

                    AsmDirective.Emissive.EmissiveT.ASCIZ -> { // Null-terminated
                        val strLiteral = argExpr as? PsiStatement.Expr.Literal.String
                            ?: throw IllegalArgumentException("Expected string literal for .$type, found ${argExpr::class.simpleName}")
                        val stringValue = strLiteral.evaluate()
                        context.section.content.putBytes(stringValue.encodeToByteArray())
                        context.section.content.put(UInt8.ZERO) // Add the null terminator
                    }
                    // Add cases for other emissive types if necessary
                    // e.g., .octa, .single, etc.
                }
            } catch (e: Exception) {
                argExpr.addError("Failed to process argument for '.$type': ${e.message ?: e::class.simpleName}")
                io.debug { e.stackTraceToString() }
                success = false
            }
        }
        return success
    }

    private fun handleSymbolManagementDirective(directive: AsmDirective.SymbolManagement): Boolean {
        val type = directive.type
        var success = true

        when (type) {
            AsmDirective.SymbolManagement.SymbolManagementT.GLOBL,
            AsmDirective.SymbolManagement.SymbolManagementT.LOCAL,
            AsmDirective.SymbolManagement.SymbolManagementT.WEAK,
                -> {
                val binding = when (type) {
                    AsmDirective.SymbolManagement.SymbolManagementT.GLOBL -> AsmCodeGenerator.Symbol.Binding.GLOBAL
                    AsmDirective.SymbolManagement.SymbolManagementT.LOCAL -> AsmCodeGenerator.Symbol.Binding.LOCAL
                    AsmDirective.SymbolManagement.SymbolManagementT.WEAK -> AsmCodeGenerator.Symbol.Binding.WEAK
                    else -> error("Unreachable") // Should not happen
                }
                if (directive.symbols.isEmpty()) {
                    directive.addError("Directive '.$type' requires at least one symbol name.")
                    return false
                }
                directive.symbols.forEach { symbolToken ->
                    val name = symbolToken.value
                    val symbol = codeGenerator.symbols.firstOrNull { it.name == name }
                    if (symbol == null) {
                        // Forward declaration - allowed in some assemblers. Need to handle later.
                        // For now, maybe warn or error. Or create a placeholder symbol?
                        // Let's add a placeholder absolute symbol at 0, maybe update later.
                        // TODO: Revisit forward declaration handling for .globl/.weak
                        io.warn("Symbol '$name' for directive '.$type' not defined yet (forward declaration?). Assuming value 0 for now.")
                        codeGenerator.symbols.add(AsmCodeGenerator.Symbol.Abs(name, codeGenerator.currentSection, binding, BigInt.ZERO))
                        // symbolToken.addWarn("Symbol '$name' not defined yet.")
                        // success = false // Treat as warning for now
                    } else {
                        symbol.binding = binding
                        io.log("Set binding for symbol '$name' to $binding")
                    }
                }
            }

            AsmDirective.SymbolManagement.SymbolManagementT.COMM,
            AsmDirective.SymbolManagement.SymbolManagementT.LCOMM,
                -> {
                // Define a common symbol (usually in BSS).
                // .comm name, size [, align]
                // .lcomm name, size [, align] (Local common)
                val symToken = directive.symbols.firstOrNull()
                val sizeExpr = directive.expressionArg
                // TODO: Handle optional alignment argument
                if (symToken == null || sizeExpr == null) {
                    directive.addError("Directive '.$type' requires a name and a size expression.")
                    return false
                }
                val name = symToken.value
                val binding = if (type == AsmDirective.SymbolManagement.SymbolManagementT.LCOMM) AsmCodeGenerator.Symbol.Binding.LOCAL else AsmCodeGenerator.Symbol.Binding.GLOBAL // Or WEAK for .comm? Check spec.

                try {
                    val size = absIntEvaluator.evaluate(sizeExpr, createPass1Context())
                    // TODO: How to represent COMMON symbols? Often put in .bss section.
                    // Need a way in AsmCodeGenerator to handle this. Maybe a dedicated section type or flag.
                    io.warn("Directive '.$type' handling for symbol '$name' size $size is not fully implemented (allocation in BSS).")
                    // Placeholder: add as absolute symbol in current section for now
                    if (codeGenerator.symbols.any { it.name == name }) {
                        symToken.addError("Symbol '$name' already defined.")
                        success = false
                    } else {
                        codeGenerator.symbols.add(AsmCodeGenerator.Symbol.Abs(name, codeGenerator.currentSection, binding, BigInt.ZERO)) // Value is usually 0 or offset in BSS
                    }
                } catch (e: Exception) {
                    sizeExpr.addError("Failed to evaluate size for '.$type': ${e.message}")
                    success = false
                }
            }

            AsmDirective.SymbolManagement.SymbolManagementT.TYPE -> {
                // .type name, @type_descriptor (e.g., @function, @object)
                val symToken = directive.symbols.firstOrNull()
                val typeDesc = directive.typeArg
                if (symToken == null || typeDesc == null) {
                    directive.addError("Directive '.type' requires a symbol name and a type descriptor (e.g., '@function').")
                    return false
                }
                val name = symToken.value
                val typeName = typeDesc.typeDescriptorName.value
                // TODO: Store this type information. Extend Symbol class or use a separate map?
                io.warn("Directive '.type' handling for symbol '$name' type '$typeName' not fully implemented (info not stored).")
                val symbol = codeGenerator.symbols.firstOrNull { it.name == name }
                if (symbol == null) {
                    symToken.addWarn("Symbol '$name' for '.type' directive not defined yet.")
                    // success = false
                }
            }

            AsmDirective.SymbolManagement.SymbolManagementT.SIZE -> {
                // .size name, expression (often .-name)
                val symToken = directive.symbols.firstOrNull()
                val expr = directive.expressionArg
                if (symToken == null || expr == null) {
                    directive.addError("Directive '.size' requires a symbol name and a size expression.")
                    return false
                }
                val name = symToken.value
                try {
                    // Evaluate the size expression. This often happens *after* the symbol is defined.
                    // Evaluation might need to be deferred until the second pass if it involves forward references.
                    // For now, try to evaluate in the symbol pass context.
                    val size = absIntEvaluator.evaluate(expr, createPass1Context())
                    // TODO: Store this size information. Extend Symbol class or use a separate map?
                    io.warn("Directive '.size' handling for symbol '$name' size $size not fully implemented (info not stored).")
                    val symbol = codeGenerator.symbols.firstOrNull { it.name == name }
                    if (symbol == null) {
                        symToken.addWarn("Symbol '$name' for '.size' directive not defined yet.")
                        // success = false
                    }
                } catch (e: Exception) {
                    // Defer evaluation? Or report error? For now, report.
                    expr.addError("Failed to evaluate size expression for '.size': ${e.message} (may require deferred evaluation)")
                    success = false
                }
            }
        }
        return success
    }

    private fun handleAlignmentDirective(directive: AsmDirective.Alignment): Boolean {
        val type = directive.type
        val section = codeGenerator.currentSection
        val buffer = section.content

        // Check for writing into NOBITS sections
        if (section.type == Shdr.SHT_NOBITS) {
            // Only directives that purely increase size without writing bytes are allowed
            return when (type) {
                AsmDirective.Alignment.AlignmentT.SPACE,
                AsmDirective.Alignment.AlignmentT.ZERO,
                AsmDirective.Alignment.AlignmentT.SKIP,
                    -> {
                    // These just reserve space (increase size), which is fine for BSS
                    handleSpaceOrZeroInNobits(directive, type, section)
                }

                AsmDirective.Alignment.AlignmentT.ALIGN,
                AsmDirective.Alignment.AlignmentT.P2ALIGN,
                AsmDirective.Alignment.AlignmentT.FILL,
                    -> {
                    directive.addError("Cannot use alignment directive '.$type' which may write fill bytes in a NOBITS section like '.bss'. Use '.space' or '.zero' for size reservation.")
                    false
                }
            }
        }

        // Handle directives for sections with content (PROGBITS etc.)
        val args = directive.arguments
        try {
            // Use BigInt for offsets/addresses until final buffer write
            val currentOffsetBigInt = buffer.size
            // Address calculation depends on Phase 4 having run, BUT this handler runs in Phase 3.
            // Alignment should ideally happen relative to section start (offset).
            // Absolute address alignment needs deferral or careful handling.
            // Let's calculate alignment based on current *offset* within the section.
            // val currentAddr = section.address + currentOffsetBigInt // Address known only in Phase 5!

            when (type) {
                AsmDirective.Alignment.AlignmentT.ALIGN, AsmDirective.Alignment.AlignmentT.P2ALIGN -> {
                    // ... (Argument parsing as before) ...
                    if (args.isEmpty()) {
                        directive.addError("Directive '.$type' requires an alignment argument."); return false
                    }
                    val alignExpr = args[0]
                    val fillExpr = args.getOrNull(1)
                    val maxSkipExpr = args.getOrNull(2)

                    // Evaluate using a temporary resolver/evaluator context (offset only)
                    // Evaluate alignment relative to offset 0 within the section for Phase 3 prediction
                    val alignment = absIntEvaluator.evaluate(alignExpr, createPass1Context()).toInt()
                    val boundary = if (type == AsmDirective.Alignment.AlignmentT.ALIGN) alignment else (1 shl alignment.toInt())

                    if (boundary <= 0) { /* Error */ return false
                    }

                    val context = createPass1Context()
                    val type = context.section.content.type

                    val fillValue = type.to(fillExpr?.let { absIntEvaluator.evaluate(it, context) } ?: type.ZERO) // Default fill 0
                    val maxSkip = maxSkipExpr?.let { absIntEvaluator.evaluate(it, context).toInt() }

                    // Calculate padding based on current buffer size (offset)
                    val misalignment = currentOffsetBigInt % boundary
                    if (misalignment != 0) {
                        val paddingNeeded = boundary - misalignment
                        if (maxSkip != null && paddingNeeded > maxSkip) {
                            io.log("Skipping alignment for '.$type': padding needed ($paddingNeeded) exceeds max skip ($maxSkip)")
                        } else {
                            io.log("Aligning section '${section.name}' from offset $currentOffsetBigInt by $paddingNeeded bytes (boundary $boundary) using fill $fillValue")
                            // Use the buffer's methods directly
                            try {
                                val paddingInt = paddingNeeded // Convert to Int for repeat/pad
                                if (fillValue == type.ZERO) {
                                    buffer.pad(paddingInt) // Efficiently add zeros
                                } else {
                                    repeat(paddingInt) {
                                        buffer.put(fillValue) // Add fill bytes one by one (or optimize with putBytes if possible)
                                    }
                                }
                            } catch (e: NumberFormatException) {
                                directive.addError("Padding needed ($paddingNeeded) is too large for buffer operations.")
                                return false
                            }
                        }
                    }
                }

                AsmDirective.Alignment.AlignmentT.FILL -> {
                    // ... (Argument parsing) ...
                    if (args.size < 3) { /* Error */ return false
                    }
                    val context = createPass1Context()
                    val repeatCount = absIntEvaluator.evaluate(args[0], context).toInt()
                    val itemSize = absIntEvaluator.evaluate(args[1], context).toInt()
                    val value = absIntEvaluator.evaluate(args[2], context) // Keep as BigInt initially

                    if (repeatCount < 0 || itemSize <= 0 || itemSize > 8) { /* Error */ return false
                    } // Size must be > 0

                    io.log("Filling $repeatCount times with $itemSize-byte pattern ${value.toString(16)}")

                    // Extract the relevant bytes from 'value' based on 'itemSize'
                    // Note: BigInt.toByteArray gives big-endian bytes. Buffer might need little-endian.
                    // Let the buffer handle the write correctly based on its endianness.
                    try {
                        repeat(repeatCount) {
                            when (itemSize) {
                                1 -> buffer.put(value.toUInt8())
                                2 -> buffer.put(value.toUInt16())
                                4 -> buffer.put(value.toUInt32())
                                8 -> buffer.put(value.toUInt64())
                                else -> { // Handle sizes 3, 5, 6, 7? Pad BigInt bytes? More complex.
                                    // Simplest for now: write lower bytes for non-standard sizes
                                    val bytes = value.value.toByteArray().takeLast(itemSize) // BE bytes
                                    // Need to reverse if buffer is LE? Buffer.putBytes should handle this ideally.
                                    // Assuming putBytes takes standard ByteArray
                                    val byteArray = ByteArray(itemSize) { i -> bytes.getOrElse(i) { 0 } } // Pad if needed
                                    buffer.putBytes(byteArray) // Assumes putBytes handles endianness if needed, or expects specific order
                                    // Safer: Use put(UIntX) for standard sizes.
                                    directive.addWarn(".fill with non-standard size $itemSize might have endianness issues.")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        directive.addError("Failed during .fill operation: ${e.message}")
                        return false
                    }
                }

                AsmDirective.Alignment.AlignmentT.SKIP,
                AsmDirective.Alignment.AlignmentT.SPACE,
                    -> {
                    // ... (Argument parsing) ...
                    if (args.isEmpty()) { /* Error */ return false
                    }
                    val context = createPass1Context()
                    val sizeToSkip = absIntEvaluator.evaluate(args[0], context).toInt() // Evaluate size
                    val fillValue = args.getOrNull(1)?.let { absIntEvaluator.evaluate(it, context).toUInt8() } ?: 0u.toUInt8() // Default fill 0

                    if (sizeToSkip < 0) { /* Error */ return false
                    }

                    if (sizeToSkip > 0) {
                        io.log("Skipping $sizeToSkip bytes using fill $fillValue in section '${section.name}' using '.$type'")
                        try {
                            if (fillValue == 0u.toUInt8()) {
                                buffer.pad(sizeToSkip) // Use efficient padding
                            } else {
                                repeat(sizeToSkip) { buffer += fillValue }
                            }
                        } catch (e: Exception) {
                            directive.addError("Failed during .$type operation: ${e.message}")
                            return false
                        }
                    }
                }

                AsmDirective.Alignment.AlignmentT.ZERO -> {
                    if (args.isEmpty()) { /* Error */ return false
                    }
                    val context = createPass1Context()
                    val sizeToZero = absIntEvaluator.evaluate(args[0], context).toInt()

                    if (sizeToZero < 0) { /* Error */ return false
                    }

                    if (sizeToZero > 0) {
                        io.log("Writing $sizeToZero zero bytes in section '${section.name}' using '.zero'")
                        try {
                            buffer.pad(sizeToZero) // pad adds zeros
                        } catch (e: Exception) {
                            directive.addError("Failed during .zero operation: ${e.message}")
                            return false
                        }
                    }
                }
            }
            return true
        } catch (e: Exception) {
            directive.addError("Failed to process alignment directive '.$type': ${e.message ?: e::class.simpleName}")
            io.debug { e.stackTraceToString() }
            return false
        }
    }

    // Helper for handling .space/.zero/.skip in NOBITS sections (Phase 3)
    private fun handleSpaceOrZeroInNobits(directive: AsmDirective.Alignment, type: AsmDirective.Alignment.AlignmentT, section: T): Boolean {
        val args = directive.arguments
        val buffer = section.content
        try {
            if (args.isEmpty()) {
                directive.addError("Directive '.$type' requires a size argument."); return false
            }
            val context = createPass1Context()
            val size = absIntEvaluator.evaluate(args[0], context).toInt() // Evaluate size

            if (size < 0) {
                args[0].addError("Size for '.$type' must be non-negative."); return false
            }

            if (size > 0) {
                io.log("Reserving $size bytes in NOBITS section '${section.name}' using '.$type'")
                // For NOBITS, just increase the tracked size by padding (which adds zeros conceptually)
                buffer.pad(size)
            }
            return true
        } catch (e: Exception) {
            directive.addError("Failed to evaluate size for '.$type' in NOBITS section: ${e.message}")
            return false
        }
    }

    private fun handleDebuggingDirective(directive: AsmDirective.Debugging): Boolean {
        // TODO: Implement parsing and storage/processing of DWARF or other debugging info.
        // This is complex and often target/format specific.
        val type = directive.type
        io.warn("Debugging directive '${type.keyWord}' found but not processed.")
        // Return true to indicate it was recognized but ignored.
        return true
    }

    // --- Utility Functions ---

    /**
     * Generates a lazy sequence of AsmLine elements by recursively traversing
     * the entry PsiFile and any files included via the '.include' directive.
     * Requires Phase 1 (Linking) to have successfully resolved the include references.
     *
     * @param startFile The PsiFile to start traversal from.
     * @param visited Used internally to detect and prevent infinite recursion from circular includes.
     * @return A Sequence of AsmLine elements in the order they should be processed.
     */
    private fun generateStatementSequence(
        startFile: PsiFile,
        visited: MutableSet<FPath> = mutableSetOf(), // Use canonical path or unique ID for visited check
    ): Sequence<AsmLine> = sequence {
        val filePath = startFile.file.path // Use canonical path to handle relative includes robustly
        if (!visited.add(filePath)) {
            // Cycle detected or file already processed *in this specific include chain*.
            // Note: This prevents infinite loops like A includes B, B includes A.
            // It *allows* A includes B, C includes B, processing B twice if needed by standard include semantics.
            // If includes should strictly be "include once" globally, the visited set needs to be managed differently (e.g., passed by reference without removing).
            io.warn("Skipping recursive include of already visited file in this path: ${startFile.file.name} ($filePath)")
            return@sequence // Stop recursion for this path
        }

        io.debug { ">>> Processing statements from: ${startFile.file.name} ($filePath)" }

        for (element in startFile.children) {
            // We are interested only in AsmLine elements which contain the actual code/directives
            if (element is AsmLine) {
                val includeDirective = element.directive as? AsmDirective.Include
                if (includeDirective != null) {
                    val includedFile = includeDirective.reference // Get the resolved PsiFile from the linking phase
                    if (includedFile != null) {
                        // Recursively yield statements from the included file
                        io.debug { "    ... Entering include: ${includedFile.file.name} (referenced in ${startFile.file.name})" }
                        yieldAll(generateStatementSequence(includedFile, visited)) // Pass the *same* visited set down
                        io.debug { "    ... Exiting include: ${includedFile.file.name}" }
                    } else {
                        // Error should have been added during linking if reference is null
                        io.error("Include directive references an unresolved file: ${includeDirective.pathExpr.evaluate()} in ${startFile.file.name}. Skipping include.")
                        // Optionally yield the line itself so the error attached to it is visible/processed
                        yield(element)
                    }
                } else {
                    // Yield regular lines (instructions, labels, other directives)
                    yield(element)
                }
            }
            // Ignore other top-level elements if any (e.g., pure comment nodes if they aren't part of AsmLine)
        }
        io.debug { "<<< Finished processing statements from: ${startFile.file.name} ($filePath)" }

        // --- IMPORTANT ---
        // Remove the file path from 'visited' *after* processing its content and its includes *within its own call stack*.
        // This allows the file to be included again via a *different* path if necessary,
        // while still preventing direct A->B->A recursion within a single path.
        visited.remove(filePath)
        // If you need strict "include once" behavior globally (like #pragma once in C),
        // then you should *not* remove from visited here and manage the set at a higher level or pass it without removing.
        // The current implementation mimics standard assembler include behavior where a file *can* be included multiple times.
    }


    // --- Helper function to simplify evaluation calls ---

    private fun createPass1Context(): AsmEvaluationContext {
        val section = codeGenerator.currentSection
        return AsmEvaluationContext(
            section,
            BigInt.ZERO + section.content.size,
            PASS_1_LAYOUT,
            codeGenerator.symbols.filter { it !is AsmCodeGenerator.Symbol.Label<*> }.toSet(),
            section.content.size
        )
    }

    private fun T.createPass2Context(reservation: AsmCodeGenerator.InstrReservation): AsmEvaluationContext = AsmEvaluationContext(
        this,
        this.address + reservation.offset.toBigInt(),
        PASS_2_CODEGEN,
        codeGenerator.symbols,
        reservation.offset.toInt()
    )

    // --- Classes ---

    // Define the context data class for evaluation
    inner class AsmEvaluationContext(
        val section: T,
        val currentAddress: BigInt, // The '.' value
        val pass: Int, // 1 for layout/symbol pass, 2 for code generation pass
        val symbols: Set<AsmCodeGenerator.Symbol<*>>, // Current view of symbols
        val offsetInSection: Int,
    ) {
        val io: IOContext get() = this@AsmBackend.io
        val codeGenerator get() = this@AsmBackend.codeGenerator
        val spec: AsmSpec<*>? get() = this@AsmBackend.project.getAsmLang()?.spec
    }

}