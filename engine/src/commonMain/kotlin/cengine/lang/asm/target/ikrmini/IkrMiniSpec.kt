package cengine.lang.asm.target.ikrmini

import cengine.lang.asm.AsmSpec
import cengine.lang.asm.psi.AsmDirectiveT
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.mif.MifGenerator
import cengine.lang.obj.elf.LinkerScript
import cengine.util.Endianness
import cengine.util.buffer.Int16Buffer
import cengine.util.integer.BigInt
import cengine.util.integer.Int16
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

data object IkrMiniSpec : AsmSpec<MifGenerator<Int16Buffer>> {
    override val name: String = "IKR Mini"
    override val emuLink: EmuLink = EmuLink.IKRMINI


    override val dirTypes: List<AsmDirectiveT>
        get() = TODO("Not yet implemented")
    override val instrTypes: List<AsmInstructionT>
        get() = TODO("Not yet implemented")

    override val commentSlAlt: String = ";"
    override val litIntBinPrefix: String = "%"
    override val litIntHexPrefix: String = "$"

    override fun createGenerator(): MifGenerator<Int16Buffer> = MifGenerator(object : LinkerScript {
        override val textStart: BigInt = BigInt.ZERO
        override val dataStart: BigInt? = null
        override val rodataStart: BigInt? = null
        override val segmentAlign: UInt64 = 0x4000U.toUInt64()
    }, Int16) {
        Int16Buffer(Endianness.LITTLE)
    }

    override fun toString(): String = name
}