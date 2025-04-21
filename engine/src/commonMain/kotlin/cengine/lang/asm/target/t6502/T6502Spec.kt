package cengine.lang.asm.target.t6502

import cengine.lang.asm.AsmSpec
import cengine.lang.asm.psi.AsmDirectiveT
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.obj.elf.*
import cengine.util.integer.BigInt
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

object T6502Spec : AsmSpec<ELFGenerator> {
    override val name: String = "6502 MOS"
    override val emuLink: EmuLink = TODO() // EmuLink.T6502

    override val instrTypes: List<AsmInstructionT>
        get() = TODO("Not yet implemented")

    override val dirTypes: List<AsmDirectiveT>
        get() = TODO("Not yet implemented")

    override val litIntBinPrefix: String = "%"
    override val litIntHexPrefix: String = "$"
    override val commentSlAlt: String = ";"


    override fun createGenerator(): ELFGenerator = ExecELFGenerator(
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
        }
    )

    override fun toString(): String = name
}