package cengine.lang.asm.ast.target.t6502

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASDirType
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.obj.elf.*
import cengine.psi.PsiManager
import cengine.util.integer.BigInt
import cengine.util.integer.Int16
import cengine.util.integer.UInt16
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

object T6502Spec : TargetSpec<ELFGenerator> {
    override val name: String = "6502 MOS"
    override val emuLink: EmuLink = TODO() // EmuLink.T6502

    override val detectRegistersByName: Boolean = false
    override val prefices: AsmLexer.Prefices = object : AsmLexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val comment: String = ";"
        override val oct: String = "0"
        override val symbol: Regex = Regex("""[a-zA-Z._][a-zA-Z0-9$._]*""")
    }

    override val allRegs: List<RegTypeInterface> = emptyList()
    override val allInstrs: List<InstrTypeInterface> = T6502InstrType.entries
    override val allDirs: List<DirTypeInterface> = ASDirType.entries
    override fun createGenerator(manager: PsiManager<*, *>): ELFGenerator = ExecELFGenerator(
        ei_class = E_IDENT.ELFCLASS32,
        ei_data = E_IDENT.ELFDATA2LSB,
        ei_osabi = E_IDENT.ELFOSABI_SYSV,
        ei_abiversion = Ehdr.EV_CURRENT.toUInt8(),
        e_machine = Ehdr.EM_CUSTOM_T6502,
        e_flags = Elf_Word.ZERO,
        linkerScript = object : LinkerScript {
            override val textStart: BigInt = BigInt.ZERO
            override val dataStart: BigInt? = null
            override val rodataStart: BigInt? = null
            override val segmentAlign: UInt64 = 0x4000U.toUInt64()
        },
        psiManager = manager
    )

    override fun toString(): String = name
}