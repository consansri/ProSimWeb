package cengine.lang.asm.target.riscv

import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.riscv.RvDisassembler.InstrType.*
import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32

class RvDisassembler(private val addrConstrained: BigInt.() -> BigInt) : AsmDisassembler() {
    override fun disassemble(startAddr: BigInt, buffer: List<IntNumber<*>>): List<Decoded> {
        var currIndex = 0
        var currInstr: RVInstrInfoProvider
        val decoded = mutableListOf<Decoded>()
        val words = buffer.chunked(4) { bytes -> bytes.reversed().joinToString("") { it.zeroPaddedHex() }.toUInt(16).toUInt32() }

        while ((currIndex / 4) < words.size) {
            currInstr = try {
                RVInstrInfoProvider(words[currIndex / 4], addrConstrained)
            } catch (e: IndexOutOfBoundsException) {
                break
            }

            val instr = currInstr.decode(startAddr, currIndex)
            decoded.add(instr)

            // cengine.console.SysOut.log("Instr: ${currInstr.binary.toString(16).padStart(32, '0')} -> ${currInstr.binary}: ${instr.disassembled}")

            currIndex += 4
        }

        return decoded
    }

    class RVInstrInfoProvider(val binary: UInt32, private val addrConstrained: BigInt.() -> BigInt) : InstrProvider {

        private val opcode = binary lowest 7
        private val funct3 = (binary shr 12) lowest 3
        private val funct7 = binary.shr(25) lowest 7
        val rd = (binary shr 7) lowest 5
        val rs1 = (binary shr 15) lowest 5
        val rs2 = (binary shr 20) lowest 5
        val imm12iType = (binary shr 20) lowest 12
        val iTypeImm get() = imm12iType.toUInt64().signExtend(12)
        private val imm12sType = ((binary shr 25) shl 5) or rd
        val sTypeImm get() = imm12sType.toUInt64().signExtend(12)
        private val imm12bType = ((binary shr 31) shl 12) or (((binary shr 7) lowest 1) shl 11) or (((binary shr 25) lowest 6) shl 5) or (((binary shr 8) lowest 4) shl 1)
        val bTypeOffset get() = imm12bType.toUInt64().signExtend(13)
        val imm20uType = binary shr 12
        private val imm20jType = ((binary shr 31) shl 20) or (((binary shr 12) lowest 8) shl 12) or (((binary shr 20) lowest 1) shl 11) or (((binary shr 21) lowest 10) shl 1)
        val jTypeOffset get() = imm20jType.toUInt64().signExtend(21)
        val shamt = imm12iType lowest 6
        val pred = binary shr 24 lowest 4
        val succ = binary shr 20 lowest 4
        val type: InstrType? = when (opcode) {
            RvConst.OPC_LUI -> LUI
            RvConst.OPC_AUIPC -> AUIPC
            RvConst.OPC_JAL -> JAL
            RvConst.OPC_JALR -> JALR
            RvConst.OPC_SYSTEM -> {
                when (funct3) {
                    RvConst.FUNCT3_CSR_RW -> CSRRW
                    RvConst.FUNCT3_CSR_RS -> CSRRS
                    RvConst.FUNCT3_CSR_RC -> CSRRC
                    RvConst.FUNCT3_CSR_RWI -> CSRRWI
                    RvConst.FUNCT3_CSR_RSI -> CSRRSI
                    RvConst.FUNCT3_CSR_RCI -> CSRRCI
                    RvConst.FUNCT3_ECALL_EBREAK_MRET_etc -> when (imm12iType) {
                        UInt32.ONE -> EBREAK
                        else -> ECALL
                    }

                    else -> null
                }
            }

            RvConst.OPC_BRANCH -> {
                when (funct3) {
                    RvConst.FUNCT3_CBRA_BEQ -> BEQ
                    RvConst.FUNCT3_CBRA_BNE -> BNE
                    RvConst.FUNCT3_CBRA_BLT -> BLT
                    RvConst.FUNCT3_CBRA_BGE -> BGE
                    RvConst.FUNCT3_CBRA_BLTU -> BLTU
                    RvConst.FUNCT3_CBRA_BGEU -> BGEU
                    else -> null
                }
            }

            RvConst.OPC_LOAD -> {
                when (funct3) {
                    RvConst.FUNCT3_LOAD_B -> LB
                    RvConst.FUNCT3_LOAD_H -> LH
                    RvConst.FUNCT3_LOAD_W -> LW
                    RvConst.FUNCT3_LOAD_D -> LD
                    RvConst.FUNCT3_LOAD_BU -> LBU
                    RvConst.FUNCT3_LOAD_HU -> LHU
                    RvConst.FUNCT3_LOAD_WU -> LWU
                    else -> null
                }
            }

            RvConst.OPC_STORE -> {
                when (funct3) {
                    RvConst.FUNCT3_STORE_B -> SB
                    RvConst.FUNCT3_STORE_H -> SH
                    RvConst.FUNCT3_STORE_W -> SW
                    RvConst.FUNCT3_STORE_D -> SD
                    else -> null
                }
            }

            RvConst.OPC_ARITH -> {
                when (funct7) {
                    RvConst.FUNCT7_SHIFT_ARITH_OR_SUB -> {
                        when (funct3) {
                            RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> SRA
                            RvConst.FUNCT3_ADDI_ADD_SUB -> SUB
                            else -> null
                        }
                    }

                    RvConst.FUNCT7_M -> {
                        when (funct3) {
                            RvConst.FUNCT3_M_MUL -> MUL
                            RvConst.FUNCT3_M_MULH -> MULH
                            RvConst.FUNCT3_M_MULHSU -> MULHSU
                            RvConst.FUNCT3_M_MULHU -> MULHU
                            RvConst.FUNCT3_M_DIV -> DIV
                            RvConst.FUNCT3_M_DIVU -> DIVU
                            RvConst.FUNCT3_M_REM -> REM
                            RvConst.FUNCT3_M_REMU -> REMU
                            else -> null
                        }
                    }

                    else -> {
                        when (funct3) {
                            RvConst.FUNCT3_SLLI_SLL -> SLL
                            RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> SRL
                            RvConst.FUNCT3_SLTI_SLT -> SLT
                            RvConst.FUNCT3_SLTIU_SLTU -> SLTU
                            RvConst.FUNCT3_ORI_OR -> OR
                            RvConst.FUNCT3_ANDI_AND -> AND
                            RvConst.FUNCT3_XORI_XOR -> XOR
                            RvConst.FUNCT3_ADDI_ADD_SUB -> ADD
                            else -> null
                        }
                    }
                }
            }

            RvConst.OPC_ARITH_WORD -> {
                when (funct7) {
                    RvConst.FUNCT7_SHIFT_ARITH_OR_SUB -> {
                        when (funct3) {
                            RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> SRAW
                            RvConst.FUNCT3_ADDI_ADD_SUB -> SUBW
                            else -> null
                        }
                    }

                    RvConst.FUNCT7_M -> {
                        when (funct3) {
                            RvConst.FUNCT3_M_MUL -> MULW
                            RvConst.FUNCT3_M_DIV -> DIVW
                            RvConst.FUNCT3_M_DIVU -> DIVUW
                            RvConst.FUNCT3_M_REM -> REMW
                            RvConst.FUNCT3_M_REMU -> REMUW
                            else -> null
                        }
                    }

                    else -> {
                        when (funct3) {
                            RvConst.FUNCT3_SLLI_SLL -> SLLW
                            RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> SRLW
                            RvConst.FUNCT3_ADDI_ADD_SUB -> ADDW
                            else -> null
                        }
                    }
                }
            }

            RvConst.OPC_ARITH_IMM -> {
                when (funct3) {
                    RvConst.FUNCT3_ADDI_ADD_SUB -> ADDI
                    RvConst.FUNCT3_SLTI_SLT -> SLTI
                    RvConst.FUNCT3_SLTIU_SLTU -> SLTIU
                    RvConst.FUNCT3_XORI_XOR -> XORI
                    RvConst.FUNCT3_ORI_OR -> ORI
                    RvConst.FUNCT3_ANDI_AND -> ANDI
                    RvConst.FUNCT3_SLLI_SLL -> SLLI
                    RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> when (funct7) {
                        RvConst.FUNCT7_SHIFT_ARITH_OR_SUB -> SRAI
                        else -> SRLI
                    }

                    else -> null
                }
            }

            RvConst.OPC_ARITH_IMM_WORD -> {
                when (funct3) {
                    RvConst.FUNCT3_ADDI_ADD_SUB -> ADDIW
                    RvConst.FUNCT3_SLLI_SLL -> SLLIW
                    RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> when (funct7) {
                        RvConst.FUNCT7_SHIFT_ARITH_OR_SUB -> SRAIW
                        else -> SRLIW
                    }

                    else -> null
                }
            }

            RvConst.OPC_FENCE -> {
                when (funct3) {
                    UInt32.ONE -> FENCE
                    RvConst.FUNCT3_FENCE_I -> FENCEI
                    else -> null
                }
            }

            else -> null
        }

        override fun decode(segmentAddr: BigInt, offset: Int): Decoded {
            return when (type) {
                LUI -> Decoded(offset, binary, "lui    ${rdName()}, 0x${imm20uType.toString(16)}")
                AUIPC -> Decoded(offset, binary, "auipc  ${rdName()}, 0x${imm20uType.toString(16)}")
                JAL -> {
                    val target = (segmentAddr + offset + jTypeOffset.toLong()).addrConstrained()
                    Decoded(offset, binary, "jal    ${rdName()}, ${jTypeOffset.toInt64()}", target)
                }

                JALR -> Decoded(offset, binary, "jalr   ${rdName()}, ${rs1Name()}, ${iTypeImm.toInt64()}")
                ECALL -> Decoded(offset, binary, "ecall")
                EBREAK -> Decoded(offset, binary, "ebreak")
                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    val target = (segmentAddr + offset.toLong() + bTypeOffset.toLong()).addrConstrained()
                    Decoded(offset, binary, "${type.lc6char} ${rs1Name()}, ${rs2Name()}, ${bTypeOffset.toInt64()}", target)
                }

                LB, LH, LW, LD, LBU, LHU, LWU -> {
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${iTypeImm.toInt64()}(${rs1Name()})")
                }

                SB, SH, SW, SD -> {
                    Decoded(offset, binary, "${type.lc6char} ${rs2Name()}, ${sTypeImm.toInt64()}(${rs1Name()})")
                }

                ADDI, ADDIW, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${iTypeImm.toInt64()}")
                }

                SLLI, SLLIW, SRLI, SRLIW, SRAI, SRAIW -> Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, $shamt")
                ADD, ADDW, SUB, SUBW, SLL, SLLW,
                SLT, SLTU, XOR, SRL, SRLW, SRA,
                SRAW, OR, AND, MUL, MULH, MULHSU,
                MULHU, DIV, DIVU, REM, REMU, MULW,
                DIVW, DIVUW, REMW, REMUW,
                    -> Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${rs2Name()}")

                FENCE -> Decoded(offset, binary, "${type.lc6char} $pred,$succ")
                FENCEI -> Decoded(offset, binary, type.lc6char)
                CSRRW, CSRRS, CSRRC -> Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${csrName()}, ${rs1Name()}")
                CSRRWI, CSRRSI, CSRRCI -> Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${csrName()}, 0b${rs1.toString(2)}")
                null -> Decoded(offset, binary, "[invalid]")
            }
        }

        private fun csrName(): String = RvRegT.RvCsrT.rv32CsrRegs.firstOrNull { it.numericalValue.toUInt32() == imm12iType }?.recognizable?.first() ?: "0x${imm12iType.toString(16)}"
        private fun rdName(): String = RvRegT.IntT.entries.getOrNull(rd.toInt())?.recognizable?.first() ?: "[invalid reg]"
        private fun rs1Name(): String = RvRegT.IntT.entries.getOrNull(rs1.toInt())?.recognizable?.first() ?: "[invalid reg]"
        private fun rs2Name(): String = RvRegT.IntT.entries.getOrNull(rs2.toInt())?.recognizable?.first() ?: "[invalid reg]"
    }

    enum class InstrType(special: String? = null) {
        LUI,
        AUIPC,
        JAL,
        JALR,
        ECALL,
        EBREAK,
        BEQ,
        BNE,
        BLT,
        BGE,
        BLTU,
        BGEU,
        LB,
        LH,
        LW,
        LD,
        LBU,
        LHU,
        LWU,
        SB,
        SH,
        SW,
        SD,
        ADDI,
        ADDIW,
        SLTI,
        SLTIU,
        XORI,
        ORI,
        ANDI,
        SLLI,
        SLLIW,
        SRLI,
        SRLIW,
        SRAI,
        SRAIW,
        ADD,
        ADDW,
        SUB,
        SUBW,
        SLL,
        SLLW,
        SLT,
        SLTU,
        XOR,
        SRL,
        SRLW,
        SRA,
        SRAW,
        OR,
        AND,
        FENCE,
        FENCEI("fence.i"),
        CSRRW,
        CSRRS,
        CSRRC,
        CSRRWI,
        CSRRSI,
        CSRRCI,
        MUL,
        MULH,
        MULHSU,
        MULHU,
        DIV,
        DIVU,
        REM,
        REMU,
        MULW,
        DIVW,
        DIVUW,
        REMW,
        REMUW;

        val lc6char: String = (special ?: name).lowercase().padEnd(6, ' ')
    }

}