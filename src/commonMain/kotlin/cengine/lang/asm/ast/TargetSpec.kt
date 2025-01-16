package cengine.lang.asm.ast

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.target.ikrmini.IKRMiniSpec
import cengine.lang.asm.ast.target.ikrrisc2.IKRR2Spec
import cengine.lang.asm.ast.target.riscv.rv32.RV32Spec
import cengine.lang.asm.ast.target.riscv.rv64.RV64Spec
import cengine.lang.asm.ast.target.t6502.T6502Spec
import cengine.lang.obj.elf.LinkerScript
import cengine.psi.PsiManager
import cengine.util.integer.IntNumberStatic
import emulator.EmuLink

/**
 * Interface representing a defined assembly configuration.
 */
interface TargetSpec<T : AsmCodeGenerator<*>> {
    companion object {
        val specs = setOf(RV32Spec, RV64Spec, IKRR2Spec, IKRMiniSpec)
    }

    val name: String
    val emuLink: EmuLink?

    /** Determines if registers are detected by name. */
    val detectRegistersByName: Boolean

    /** The lexer prefices used for parsing instructions and directives. */
    val prefices: AsmLexer.Prefices

    val allRegs: List<RegTypeInterface>

    val allInstrs: List<InstrTypeInterface>

    val allDirs: List<DirTypeInterface>

    fun createLexer(input: String): AsmLexer = AsmLexer(input, this)
    fun createGenerator(manager: PsiManager<*, *>): T

    override fun toString(): String

}