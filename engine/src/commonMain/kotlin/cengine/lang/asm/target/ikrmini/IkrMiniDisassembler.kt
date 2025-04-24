package cengine.lang.asm.target.ikrmini

import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.ikrmini.IkrMiniDisassembler.InstrType.*
import cengine.util.integer.BigInt
import cengine.util.integer.FixedSizeIntNumber
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt16
import cengine.util.integer.UInt16.Companion.toUInt16
import cengine.util.integer.UInt32
import cengine.util.integer.UnsignedFixedSizeIntNumber

object IkrMiniDisassembler : AsmDisassembler() {

    override fun disassemble(startAddr: UnsignedFixedSizeIntNumber<*>, buffer: List<FixedSizeIntNumber<*>>): List<Decoded> {
        var currIndex = 0
        var currInstr: IKRMiniInstrProvider
        val decoded = mutableListOf<Decoded>()
        val words = buffer.map { it.toUInt16() }

        while (currIndex < words.size) {
            currInstr = try {
                IKRMiniInstrProvider(words[currIndex], words.getOrNull(currIndex + 1) ?: UInt16.ZERO, words.getOrNull(currIndex + 2) ?: UInt16.ZERO)
            } catch (e: IndexOutOfBoundsException) {
                break
            }

            val instr = currInstr.decode(startAddr, currIndex)
            decoded.add(instr)

            currIndex += currInstr.type?.length ?: 1
        }

        return decoded
    }

    class IKRMiniInstrProvider(val first: UInt16, val second: UInt16, val third: UInt16) : InstrProvider {

        val type: InstrType? = InstrType.entries.firstOrNull { it.opcode == first }

        val data: UInt32 = if (type?.length == 2) {
            (first.toUInt32() shl 16) or second.toUInt32()
        } else {
            first.toUInt32()
        }

        override fun decode(segmentAddr: UnsignedFixedSizeIntNumber<*>, offset: Int): Decoded {
            return when (type) {
                LOAD_IMM, AND_IMM, OR_IMM, XOR_IMM, ADD_IMM, ADDC_IMM, SUB_IMM, SUBC_IMM, RCL_IMM, RCR_IMM,
                    -> Decoded(offset, data, "${type.displayName} #${second.toShort()}")

                LOAD_DIR, STORE_DIR, AND_DIR, OR_DIR, XOR_DIR, ADD_DIR, ADDC_DIR, SUB_DIR, SUBC_DIR,
                LSL_DIR, LSR_DIR, ROL_DIR, ROR_DIR, ASL_DIR, ASR_DIR, RCL_DIR, RCR_DIR, NOT_DIR, NEG_DIR, INC_DIR, DEC_DIR,
                    -> Decoded(offset, data, "${type.displayName} (${IkrMiniSpec.litIntHexPrefix}${second.toString(16)})")

                LOAD_IND, STORE_IND, AND_IND, OR_IND, XOR_IND, ADD_IND, ADDC_IND, SUB_IND, SUBC_IND, LSL_IND, LSR_IND, ROL_IND,
                ROR_IND, ASL_IND, ASR_IND, RCL_IND, RCR_IND, NOT_IND, NEG_IND, INC_IND, DEC_IND,
                    -> Decoded(offset, data, "${type.displayName} ((${IkrMiniSpec.litIntHexPrefix}${second.toString(16)}))")

                LOAD_IND_OFF, STORE_IND_OFF, AND_IND_OFF, OR_IND_OFF, XOR_IND_OFF, ADD_IND_OFF, ADDC_IND_OFF, SUB_IND_OFF, SUBC_IND_OFF,
                LSL_IND_OFF, LSR_IND_OFF, ROL_IND_OFF, ROR_IND_OFF, ASL_IND_OFF, ASR_IND_OFF, RCL_IND_OFF, RCR_IND_OFF, NOT_IND_OFF, NEG_IND_OFF,
                INC_IND_OFF, DEC_IND_OFF,
                    -> Decoded(offset, data, "${type.displayName} (${third.toShort()},(${IkrMiniSpec.litIntHexPrefix}${second.toString(16)}))")

                LOADI, LSL, LSR, ROL, ROR, ASL, ASR, RCL, RCR, NOT, CLR, INC, DEC, JMP,
                    -> Decoded(offset, data, type.displayName)

                BSR, BRA, BHI, BLS, BCC, BCS, BNE, BEQ, BVC, BVS, BPL, BMI, BGE, BLT, BGT, BLE,
                    -> Decoded(offset, data, "${type.displayName} ${second.toShort()}")

                null -> Decoded(offset, data, "[invalid]")
            }
        }

    }


    enum class InstrType(opcode: UShort, val length: Int = 2) {
        LOAD_IMM(0x010CU),
        LOAD_DIR(0x020CU),
        LOAD_IND(0x030CU),
        LOAD_IND_OFF(0x040CU),

        LOADI(0x200CU, 1),

        STORE_DIR(0x3200U),
        STORE_IND(0x3300U),
        STORE_IND_OFF(0x3400U),

        AND_IMM(0x018aU),
        AND_DIR(0x028aU),
        AND_IND(0x038aU),
        AND_IND_OFF(0x048aU),

        OR_IMM(0x0188U),
        OR_DIR(0x0288U),
        OR_IND(0x0388U),
        OR_IND_OFF(0x0488U),

        XOR_IMM(0x0189U),
        XOR_DIR(0x0289U),
        XOR_IND(0x0389U),
        XOR_IND_OFF(0x0489U),

        ADD_IMM(0x018DU),
        ADD_DIR(0x028DU),
        ADD_IND(0x038DU),
        ADD_IND_OFF(0x048DU),

        ADDC_IMM(0x01ADU),
        ADDC_DIR(0x02ADU),
        ADDC_IND(0x03ADU),
        ADDC_IND_OFF(0x04ADU),

        SUB_IMM(0x018EU),
        SUB_DIR(0x028EU),
        SUB_IND(0x038EU),
        SUB_IND_OFF(0x048EU),

        SUBC_IMM(0x01AEU),
        SUBC_DIR(0x02AEU),
        SUBC_IND(0x03AEU),
        SUBC_IND_OFF(0x04AEU),

        LSL(0x00A0U, 1),
        LSL_DIR(0x0220U),
        LSL_IND(0x0320U),
        LSL_IND_OFF(0x0420U),

        LSR(0x00A1U, 1),
        LSR_DIR(0x0221U),
        LSR_IND(0x0321U),
        LSR_IND_OFF(0x0421U),

        ROL(0x00A2U, 1),
        ROL_DIR(0x0222U),
        ROL_IND(0x0322U),
        ROL_IND_OFF(0x0422U),

        ROR(0x00A3U, 1),
        ROR_DIR(0x0223U),
        ROR_IND(0x0323U),
        ROR_IND_OFF(0x0423U),

        ASL(0x00A4U, 1),
        ASL_DIR(0x0224U),
        ASL_IND(0x0324U),
        ASL_IND_OFF(0x0424U),

        ASR(0x00A5U, 1),
        ASR_DIR(0x0225U),
        ASR_IND(0x0325U),
        ASR_IND_OFF(0x0425U),

        RCL(0x00A6U, 1),
        RCL_IMM(0x0126U),
        RCL_DIR(0x0226U),
        RCL_IND(0x0326U),
        RCL_IND_OFF(0x0426U),

        RCR(0x00A7U, 1),
        RCR_IMM(0x0127U),
        RCR_DIR(0x0227U),
        RCR_IND(0x0327U),
        RCR_IND_OFF(0x0427U),

        NOT(0x008BU, 1),
        NOT_DIR(0x020BU),
        NOT_IND(0x030BU),
        NOT_IND_OFF(0x040BU),

        NEG_DIR(0x024EU),
        NEG_IND(0x034EU),
        NEG_IND_OFF(0x044EU),

        CLR(0x004CU, 1),

        INC(0x009CU, 1),
        INC_DIR(0x021CU),
        INC_IND(0x031CU),
        INC_IND_OFF(0x041CU),

        DEC(0x009FU, 1),
        DEC_DIR(0x021FU),
        DEC_IND(0x031FU),
        DEC_IND_OFF(0x041FU),

        BSR(0x510CU),
        JMP(0x4000U, 1),
        BRA(0x6101U),

        BHI(0x6102U),
        BLS(0x6103U),
        BCC(0x6104U),
        BCS(0x6105U),
        BNE(0x6106U),
        BEQ(0x6107U),
        BVC(0x6108U),
        BVS(0x6109U),
        BPL(0x610AU),
        BMI(0x610BU),
        BGE(0x610CU),
        BLT(0x610DU),
        BGT(0x610EU),
        BLE(0x610FU);

        val opcode = opcode.toUInt16()

        val displayName: String = name.takeWhile { it != '_' }.lowercase().padEnd(5, ' ')
    }

}