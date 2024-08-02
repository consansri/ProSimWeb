package cengine.lang.asm.ast

import cengine.lang.asm.ast.gas.GASNode
import cengine.lang.asm.lexer.AsmLexer
import emulator.core.Size
import emulator.kit.assembler.gas.GASParser
import emulator.kit.common.RegContainer
import emulator.kit.optional.Feature

/**
 * Interface representing a defined assembly configuration.
 */
interface AsmSpec {
    /** The size of memory addresses. */
    val memAddrSize: Size

    /** The size of words. */
    val wordSize: Size

    /**
     * USHR Amount for calculating the true memory address. (0 is byte aligned memory addresses)
     */
    val addrShift: Int

    /** Determines if registers are detected by name. */
    val detectRegistersByName: Boolean

    /** The lexer prefices used for parsing instructions and directives. */
    val prefices: AsmLexer.Prefices

    val allRegs: List<RegContainer.Register>

    /**
     * Retrieves the instruction types based on the provided features.
     *
     * @param features The optional features influencing instruction retrieval.
     * @return A list of instruction type interfaces.
     */
    fun instrTypes(features: List<Feature>): List<InstrTypeInterface>


    fun allInstrTypes(): List<InstrTypeInterface>

    /**
     * Retrieves additional directive types.
     *
     * @return A list of directive type interfaces.
     */
    fun additionalDirectives(): List<DirTypeInterface>

    /**
     * Parses instruction parameters from the provided raw instruction node.
     *
     * @param rawInstr The raw instruction node to parse.
     * @param tempContainer The temporary container for parsing.
     * @return A list of section contents representing parsed instruction parameters.
     */
    fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent>

    fun createLexer(input: String): AsmLexer = AsmLexer(input, this)
}