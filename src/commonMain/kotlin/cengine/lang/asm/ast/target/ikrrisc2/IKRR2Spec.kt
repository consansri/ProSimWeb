package cengine.lang.asm.ast.target.ikrrisc2

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASDirType
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.mif.MifGenerator
import cengine.lang.obj.elf.*
import cengine.psi.PsiManager
import cengine.util.Endianness
import cengine.util.buffer.Int32Buffer
import cengine.util.integer.BigInt
import cengine.util.integer.Int32
import cengine.util.integer.IntNumberStatic
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

data object IKRR2Spec: TargetSpec<MifGenerator<Int32Buffer>> {
    override val name: String = "IKR RISC-II"
    override val emuLink: EmuLink = EmuLink.IKRRISC2

    override val detectRegistersByName: Boolean = true
    override val prefices: AsmLexer.Prefices = object : AsmLexer.Prefices {
        override val hex: String = "$"
        override val bin: String = "%"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = ";"
        override val symbol: Regex = Regex("""[a-zA-Z._][a-zA-Z0-9$._]*""")
    }
    override val allRegs: List<RegTypeInterface> = IKRR2BaseRegs.entries
    override val allInstrs: List<InstrTypeInterface> = IKRR2InstrType.entries
    override val allDirs: List<DirTypeInterface> = ASDirType.entries

    override fun createGenerator(manager: PsiManager<*, *>): MifGenerator<Int32Buffer> = MifGenerator(object : LinkerScript {
        override val textStart: BigInt = BigInt.ZERO
        override val dataStart: BigInt? = null
        override val rodataStart: BigInt? = null
        override val segmentAlign: UInt64 = 0x10000U.toUInt64()
    }, manager, Int32) {
        Int32Buffer(Endianness.BIG)
    }

    override fun toString(): String = name
}