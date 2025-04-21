package cengine.lang.asm.target.ikrrisc2

import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.ikrrisc2.IkrR2Disassembler.InstrType.*
import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32

object IkrR2Disassembler : AsmDisassembler() {
    override fun disassemble(startAddr: BigInt, buffer: List<IntNumber<*>>): List<Decoded> {
        var currIndex = 0
        var currInstr: IKRR2InstrProvider
        val decoded = mutableListOf<Decoded>()

        while (currIndex < buffer.size) {
            currInstr = IKRR2InstrProvider(buffer[currIndex].toUInt32())

            decoded.add(currInstr.decode(startAddr, currIndex))

            currIndex += 1
            if(currIndex >= buffer.size) break
        }

        return decoded
    }

    data class IKRR2InstrProvider(val binary: UInt32) : InstrProvider {

        val opcodeRType = binary.shr(10) lowest 6
        val opcodeIType = binary.shr(26)
        val ra = binary lowest 5
        val rb = binary.shr(16) lowest 5
        val rc = binary.shr(21) lowest 5
        val raReg get() = IkrR2BaseRegs.entries[ra.toInt()].displayName
        val rbReg get() = IkrR2BaseRegs.entries[rb.toInt()].displayName
        val rcReg get() = IkrR2BaseRegs.entries[rc.toInt()].displayName
        val imm16 = binary lowest 16
        val disp16 get() = imm16
        val disp18 = binary lowest 18
        val disp26 = binary lowest 26
        val bType = binary.shr(18) lowest 3

        val type: InstrType? = when (opcodeIType.value) {
            0x3FU -> {
                // R Type
                when (opcodeRType.value) {
                    0x00U -> ADD
                    0x20U -> ADDX
                    0x02U -> SUB
                    0x22U -> SUBX
                    0x04U -> AND
                    0x06U -> OR
                    0x07U -> XOR
                    0x08U -> CMPU
                    0x09U -> CMPS
                    0x10U -> LDR
                    0x14U -> STR
                    0x28U -> LSL
                    0x29U -> LSR
                    0x2AU -> ASL
                    0x2BU -> ASR
                    0x2CU -> when (ra.value) {
                        0x01U -> ROL
                        0x10U -> SWAPH
                        else -> null
                    }

                    0x2DU -> ROR
                    0x30U -> EXTB
                    0x31U -> EXTH
                    0x32U -> SWAPB
                    0x33U -> NOT
                    0x3CU -> JMP
                    0x3DU -> JSR
                    else -> null
                }
            }
            // I Type
            0x00U -> ADDI
            0x01U -> ADDLI
            0x02U -> ADDHI
            0x04U -> AND0I
            0x05U -> AND1I
            0x06U -> ORI
            0x07U -> XORI
            0x08U -> CMPUI
            0x09U -> CMPSI
            0x10U -> LDD
            0x14U -> STD
            0x3CU -> BRA
            0x3DU -> BSR
            0x3EU -> {
                when (bType.value) {
                    0x0U -> BEQ
                    0x1U -> BNE
                    0x2U -> BLT
                    0x3U -> BGT
                    0x4U -> BLE
                    0x5U -> BGE
                    else -> null
                }
            }

            else -> null
        }

        override fun decode(segmentAddr: BigInt, offset: Int): Decoded {
            val hexPrefix = IkrR2Spec.litIntHexPrefix
            return when (type) {
                ADD, ADDX, SUB, SUBX, AND, OR, XOR, CMPU, CMPS -> Decoded(offset, binary, "${type.lc5Name} $rcReg := $rbReg, $raReg")
                ADDI, ADDLI, ADDHI, CMPUI, CMPSI, AND0I, AND1I, ORI, XORI -> Decoded(offset, binary, "${type.lc5Name} $rcReg := $rbReg, #$hexPrefix${imm16.toString(16)}")
                LSL, LSR, ASL, ASR, ROL, ROR, SWAPH, SWAPB, EXTB, EXTH, NOT -> Decoded(offset, binary, "${type.lc5Name} $rcReg := $rbReg")
                LDD -> Decoded(offset, binary, "${type.lc5Name} $rcReg := ($rbReg, $hexPrefix${disp16.toString(16)})")
                LDR -> Decoded(offset, binary, "${type.lc5Name} $rcReg := ($rbReg, $raReg)")
                STD -> Decoded(offset, binary, "${type.lc5Name} ($rbReg, $hexPrefix${disp16.toString(16)}) := $rcReg")
                STR -> Decoded(offset, binary, "${type.lc5Name} ($rbReg, $raReg) := $rcReg")
                BEQ, BNE, BLT, BGT, BLE, BGE -> {
                    val offset18 = disp18.signExtend(18)
                    val target = segmentAddr.toUInt32() + offset.toUInt32() + offset18
                    Decoded(offset, binary, "${type.lc5Name} $rcReg, $hexPrefix${disp18.toString(16)}", target.toBigInt())
                }

                BRA, BSR -> {
                    val offset26 = disp26.signExtend(26)
                    val target = segmentAddr.toUInt32() + offset.toUInt32() + offset26
                    Decoded(offset, binary, "${type.lc5Name} $hexPrefix${disp26.toString(16)}", target.toBigInt())
                }

                JMP, JSR -> Decoded(offset, binary, "${type.lc5Name} $rbReg")
                null -> Decoded(offset, binary, "[invalid]")
            }
        }
    }

    enum class InstrType {
        ADD,
        ADDI,
        ADDLI,
        ADDHI,
        ADDX,
        SUB,
        SUBX,
        CMPU,
        CMPS,
        CMPUI,
        CMPSI,
        AND,
        AND0I,
        AND1I,
        OR,
        ORI,
        XOR,
        XORI,
        LSL,
        LSR,
        ASL,
        ASR,
        ROL,
        ROR,
        EXTB,
        EXTH,
        SWAPB,
        SWAPH,
        NOT,
        LDD,
        LDR,
        STD,
        STR,
        BEQ,
        BNE,
        BLT,
        BGT,
        BLE,
        BGE,
        BRA,
        BSR,
        JMP,
        JSR;

        val lc5Name: String = name.lowercase().padEnd(5, ' ')
    }

}