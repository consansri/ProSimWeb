package cengine.lang.asm.target.ikrmini

import cengine.lang.asm.AsmSpec
import cengine.lang.asm.psi.AsmDirective
import cengine.lang.asm.psi.AsmDirectiveT
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.mif.MifGenerator
import cengine.lang.obj.elf.LinkerScript
import cengine.util.Endianness
import cengine.util.buffer.Buffer16
import cengine.util.integer.BigInt
import cengine.util.integer.UInt16
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

data object IkrMiniSpec : AsmSpec<MifGenerator<Buffer16>> {
    override val name: String = "IKR Mini"
    override val emuLink: EmuLink = EmuLink.IKRMINI

    override val dirTypes: List<AsmDirectiveT> = AsmDirective.all
    override val instrTypes: List<AsmInstructionT> = IkrMiniInstrT.entries

    override val commentSlAlt: String = ";"
    override val litIntBinPrefix: String = "%"
    override val litIntHexPrefix: String = "$"
    override val addPunctuations: Set<String> = setOf("#")

    override fun createGenerator(): MifGenerator<Buffer16> = MifGenerator(object : LinkerScript {
        override val textStart: BigInt = BigInt.ZERO
        override val dataStart: BigInt? = null
        override val rodataStart: BigInt? = null
        override val segmentAlign: UInt64 = 0x4000U.toUInt64()
    }, UInt16) {
        Buffer16(Endianness.LITTLE)
    }

    override fun toString(): String = name
}