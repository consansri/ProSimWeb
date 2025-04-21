package cengine.lang.asm.target.riscv

import cengine.lang.asm.psi.AsmDirective
import cengine.lang.asm.psi.AsmDirectiveT
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.obj.elf.*
import cengine.lang.obj.elf.Elf_Word
import cengine.util.integer.BigInt
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

data object Rv32Spec : RvSpec<ELFGenerator> {
    override val name: String = "RISC-V 32 Bit"

    override val xlen = RvSpec.XLEN.X32
    override val emuLink: EmuLink = EmuLink.RV32I
    override val instrTypes: List<AsmInstructionT> = RvInstrT.BaseT.entries + RvInstrT.MBaseT.entries + RvInstrT.PseudoT.BaseT.entries
    override val dirTypes: List<AsmDirectiveT> = AsmDirective.Companion.all

    override fun createGenerator(): ELFGenerator = ExecELFGenerator(
        ei_class = E_IDENT.Companion.ELFCLASS32,
        ei_data = E_IDENT.Companion.ELFDATA2LSB,
        ei_osabi = E_IDENT.Companion.ELFOSABI_SYSV,
        ei_abiversion = E_IDENT.Companion.ZERO,
        e_machine = Ehdr.Companion.EM_RISCV,
        e_flags = Elf_Word.ZERO,
        linkerScript = object : LinkerScript {
            override val textStart: BigInt = BigInt.Companion.ZERO
            override val dataStart: BigInt? = null
            override val rodataStart: BigInt? = null
            override val segmentAlign: UInt64 = 0x40000U.toUInt64()
        }
    )

    override fun toString(): String = name
}