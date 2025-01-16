package cengine.lang.asm.ast.target.riscv.rv64

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASDirType
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.target.riscv.RVBaseRegs
import cengine.lang.asm.ast.target.riscv.RVCsr
import cengine.lang.asm.ast.target.riscv.RVDirType
import cengine.lang.obj.elf.*
import cengine.psi.PsiManager
import cengine.util.integer.BigInt
import cengine.util.integer.Int32
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

data object RV64Spec : TargetSpec<ELFGenerator> {
    override val name: String = "RISC-V 64 Bit"

    override val emuLink: EmuLink = EmuLink.RV64I

    override val detectRegistersByName: Boolean = true
    override val prefices: AsmLexer.Prefices = object : AsmLexer.Prefices {
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = "#"
        override val symbol: Regex = Regex("""[a-zA-Z$._][a-zA-Z0-9$._]*""")
    }

    override val allRegs: List<RegTypeInterface> = RVBaseRegs.entries + RVCsr.regs
    override val allInstrs: List<InstrTypeInterface> = RV64InstrType.entries
    override val allDirs: List<DirTypeInterface> = RVDirType.entries + ASDirType.entries

    override fun createGenerator(manager: PsiManager<*, *>): ELFGenerator = ExecELFGenerator(
        ei_class = E_IDENT.ELFCLASS64,
        ei_data = E_IDENT.ELFDATA2LSB,
        ei_osabi = E_IDENT.ELFOSABI_SYSV,
        ei_abiversion = E_IDENT.ZERO,
        e_machine = Ehdr.EM_RISCV,
        e_flags = Elf_Word.ZERO,
        linkerScript = object : LinkerScript {
            override val textStart: BigInt = BigInt.ZERO
            override val dataStart: BigInt? = null
            override val rodataStart: BigInt? = null
            override val segmentAlign: UInt64 = 0x40000U.toUInt64()
        },
        psiManager = manager
    )

    override fun toString(): String = name
}