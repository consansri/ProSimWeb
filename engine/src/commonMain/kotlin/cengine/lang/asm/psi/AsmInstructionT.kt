package cengine.lang.asm.psi

import cengine.lang.asm.AsmTreeParser
import cengine.lang.asm.gas.AsmBackend
import cengine.lang.asm.gas.AsmCodeGenerator
import cengine.psi.core.NodeBuilderFn
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.PsiBuilder

interface AsmInstructionT : PsiStatement.PsiStatementTypeDef {

    val keyWord: String

    override val typeName: String get() = "instr('$keyWord')"

    override val builder: NodeBuilderFn
        get() = { markerInfo, children, range ->
            if (
                children.size >= 1
                && children[0].type == PsiTokenType.KEYWORD
            ) {
                AsmInstruction(this@AsmInstructionT, range, *children)
            } else null
        }

    /**
     * Parses the arguments/parameters following a specific instruction mnemonic.
     * @param marker Already consumed the instruction mnemonic token. Don't finish or drop [marker]! The caller will do this.
     */
    fun PsiBuilder.parse(asmTreeParser: AsmTreeParser, marker: PsiBuilder.Marker): Boolean

    /**
     * Phase 3: Generates the pass 1 binary machine code for the instruction.
     * This is called *before* all addresses are resolved. Therefor it won't allow label references in expressions.
     * It should write the bytes directly at the end of the target buffer.
     * The buffer's methods handle endianness.
     *
     * @param instr The specific instruction PSI node.
     * @param context The pass 1 evaluation context for the instruction.
     * @throws Exception for other generation errors (e.g., encoding impossible, value out of range).
     */
    fun <T: AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(
        instr: AsmInstruction,
        context: AsmBackend<T>.AsmEvaluationContext
    )

    /**
     * Phase 5: Generates the final binary machine code for the instruction.
     * This is called *after* all symbols and section addresses are resolved.
     * It should write the bytes directly into the target buffer at the specified offset,
     * overwriting any padding bytes added during Phase 3.
     * The buffer's methods handle endianness.
     *
     * @param instr The specific instruction PSI node.
     * @param context The pass 2 evaluation context for the instruction.
     * @throws Exception for other generation errors (e.g., encoding impossible, value out of range).
     */
    fun <T: AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(
        instr: AsmInstruction,
        context: AsmBackend<T>.AsmEvaluationContext
    )

}