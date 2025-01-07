package cengine.lang.asm.ast.target.riscv.rv32

import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.lang.asm.ast.target.riscv.RVBaseRegs
import cengine.lang.asm.ast.target.riscv.RVConst
import cengine.lang.asm.ast.target.riscv.RVConst.mask12Hi7
import cengine.lang.asm.ast.target.riscv.RVConst.mask12Lo5
import cengine.lang.asm.ast.target.riscv.RVConst.mask12bType5
import cengine.lang.asm.ast.target.riscv.RVConst.mask12bType7
import cengine.lang.asm.ast.target.riscv.RVConst.mask20jType
import cengine.lang.asm.ast.target.riscv.RVConst.mask32Hi20
import cengine.lang.asm.ast.target.riscv.RVConst.mask32Lo12
import cengine.lang.asm.ast.target.riscv.RVCsr
import cengine.lang.obj.elf.ELFGenerator
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32

enum class RV32InstrType(override val detectionName: String, val isPseudo: Boolean, val paramType: RV32ParamType, val labelDependent: Boolean = false, override val addressInstancesNeeded: Int? = 4) : InstrTypeInterface {
    LUI("LUI", false, RV32ParamType.RD_I20),
    AUIPC("AUIPC", false, RV32ParamType.RD_I20),
    JAL("JAL", false, RV32ParamType.RD_I20, true),
    JALR("JALR", false, RV32ParamType.RD_RS1_I12),
    ECALL("ECALL", false, RV32ParamType.NONE),
    EBREAK("EBREAK", false, RV32ParamType.NONE),
    BEQ("BEQ", false, RV32ParamType.RS1_RS2_LBL, true),
    BNE("BNE", false, RV32ParamType.RS1_RS2_LBL, true),
    BLT("BLT", false, RV32ParamType.RS1_RS2_LBL, true),
    BGE("BGE", false, RV32ParamType.RS1_RS2_LBL, true),
    BLTU("BLTU", false, RV32ParamType.RS1_RS2_LBL, true),
    BGEU("BGEU", false, RV32ParamType.RS1_RS2_LBL, true),
    LB("LB", false, RV32ParamType.RD_OFF12),
    LH("LH", false, RV32ParamType.RD_OFF12),
    LW("LW", false, RV32ParamType.RD_OFF12),
    LBU("LBU", false, RV32ParamType.RD_OFF12),
    LHU("LHU", false, RV32ParamType.RD_OFF12),
    SB("SB", false, RV32ParamType.RS2_OFF12),
    SH("SH", false, RV32ParamType.RS2_OFF12),
    SW("SW", false, RV32ParamType.RS2_OFF12),
    ADDI("ADDI", false, RV32ParamType.RD_RS1_I12),
    SLTI("SLTI", false, RV32ParamType.RD_RS1_I12),
    SLTIU("SLTIU", false, RV32ParamType.RD_RS1_I12),
    XORI("XORI", false, RV32ParamType.RD_RS1_I12),
    ORI("ORI", false, RV32ParamType.RD_RS1_I12),
    ANDI("ANDI", false, RV32ParamType.RD_RS1_I12),
    SLLI("SLLI", false, RV32ParamType.RD_RS1_SHAMT5),
    SRLI("SRLI", false, RV32ParamType.RD_RS1_SHAMT5),
    SRAI("SRAI", false, RV32ParamType.RD_RS1_SHAMT5),
    ADD("ADD", false, RV32ParamType.RD_RS1_RS2),
    SUB("SUB", false, RV32ParamType.RD_RS1_RS2),
    SLL("SLL", false, RV32ParamType.RD_RS1_RS2),
    SLT("SLT", false, RV32ParamType.RD_RS1_RS2),
    SLTU("SLTU", false, RV32ParamType.RD_RS1_RS2),
    XOR("XOR", false, RV32ParamType.RD_RS1_RS2),
    SRL("SRL", false, RV32ParamType.RD_RS1_RS2),
    SRA("SRA", false, RV32ParamType.RD_RS1_RS2),
    OR("OR", false, RV32ParamType.RD_RS1_RS2),
    AND("AND", false, RV32ParamType.RD_RS1_RS2),

    FENCE("FENCE", false, RV32ParamType.PRED_SUCC),
    FENCEI("FENCE.I", false, RV32ParamType.NONE),

    // CSR Extension
    CSRRW("CSRRW", false, RV32ParamType.CSR_RD_OFF12_RS1),
    CSRRS("CSRRS", false, RV32ParamType.CSR_RD_OFF12_RS1),
    CSRRC("CSRRC", false, RV32ParamType.CSR_RD_OFF12_RS1),
    CSRRWI("CSRRWI", false, RV32ParamType.CSR_RD_OFF12_UIMM5),
    CSRRSI("CSRRSI", false, RV32ParamType.CSR_RD_OFF12_UIMM5),
    CSRRCI("CSRRCI", false, RV32ParamType.CSR_RD_OFF12_UIMM5),

    CSRW("CSRW", true, RV32ParamType.PS_CSR_RS1),
    CSRR("CSRR", true, RV32ParamType.PS_RD_CSR),

    // M Extension
    MUL("MUL", false, RV32ParamType.RD_RS1_RS2),
    MULH("MULH", false, RV32ParamType.RD_RS1_RS2),
    MULHSU("MULHSU", false, RV32ParamType.RD_RS1_RS2),
    MULHU("MULHU", false, RV32ParamType.RD_RS1_RS2),
    DIV("DIV", false, RV32ParamType.RD_RS1_RS2),
    DIVU("DIVU", false, RV32ParamType.RD_RS1_RS2),
    REM("REM", false, RV32ParamType.RD_RS1_RS2),
    REMU("REMU", false, RV32ParamType.RD_RS1_RS2),

    Nop("NOP", true, RV32ParamType.PS_NONE),
    Mv("MV", true, RV32ParamType.PS_RD_RS1),
    Li("LI", true, RV32ParamType.PS_RD_I32, addressInstancesNeeded = 8),
    La("LA", true, RV32ParamType.PS_RD_ALBL, true, addressInstancesNeeded = 8),
    Not("NOT", true, RV32ParamType.PS_RD_RS1),
    Neg("NEG", true, RV32ParamType.PS_RD_RS1),
    Seqz("SEQZ", true, RV32ParamType.PS_RD_RS1),
    Snez("SNEZ", true, RV32ParamType.PS_RD_RS1),
    Sltz("SLTZ", true, RV32ParamType.PS_RD_RS1),
    Sgtz("SGTZ", true, RV32ParamType.PS_RD_RS1),
    Beqz("BEQZ", true, RV32ParamType.PS_RS1_JLBL, true),
    Bnez("BNEZ", true, RV32ParamType.PS_RS1_JLBL, true),
    Blez("BLEZ", true, RV32ParamType.PS_RS1_JLBL, true),
    Bgez("BGEZ", true, RV32ParamType.PS_RS1_JLBL, true),
    Bltz("BLTZ", true, RV32ParamType.PS_RS1_JLBL, true),
    Bgtz("BGTZ", true, RV32ParamType.PS_RS1_JLBL, true),
    Bgt("BGT", true, RV32ParamType.RS1_RS2_LBL, true),
    Ble("BLE", true, RV32ParamType.RS1_RS2_LBL, true),
    Bgtu("BGTU", true, RV32ParamType.RS1_RS2_LBL, true),
    Bleu("BLEU", true, RV32ParamType.RS1_RS2_LBL, true),
    J("J", true, RV32ParamType.PS_JLBL, true),
    JAL1("JAL", true, RV32ParamType.PS_JLBL, true),
    Jr("JR", true, RV32ParamType.PS_RS1),
    JALR1("JALR", true, RV32ParamType.PS_RS1),
    Ret("RET", true, RV32ParamType.PS_NONE),
    Call("CALL", true, RV32ParamType.PS_JLBL, true, addressInstancesNeeded = 8),
    Tail("TAIL", true, RV32ParamType.PS_JLBL, true, addressInstancesNeeded = 8);

    override val inCodeInfo: String? = if (isPseudo) "${addressInstancesNeeded ?: "?"} bytes" else null

    override val paramRule: Rule?
        get() = paramType.rule

    override val typeName: String = name.lowercase()

    override fun resolve(builder: AsmCodeGenerator<*>, instr: ASNode.Instruction) {
        val regs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVBaseRegs.entries.firstOrNull { it.recognizable.contains(token.value) } }.map { it.numericalValue.toUInt32() }
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (this.paramType) {
            RV32ParamType.RD_I20 -> {
                val expr = exprs[0]
                if (this != JAL) {
                    val opcode = when (this) {
                        LUI -> RVConst.OPC_LUI
                        AUIPC -> RVConst.OPC_AUIPC
                        JAL -> RVConst.OPC_JAL
                        else -> UInt32.ZERO
                    }
                    val rd = regs[0]
                    val imm = expr.evaluate(builder)
                    val imm20 = imm.toInt32().toUInt32().lowest(20)
                    if (!imm.fitsInSignedOrUnsigned(20)) {
                        expr.addError("${expr.eval} exceeds 20 bits")
                    }

                    val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                    builder.currentSection.content.put(bundle)
                }
            }

            RV32ParamType.RD_OFF12 -> {
                // Load
                val rs1 = regs[1]

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(12)) {
                    expr.addError("${expr.eval} exceeds 12 bits")
                }

                val imm12 = imm.toInt32().toUInt32().lowest(12)

                val funct3 = when (this) {
                    LB -> RVConst.FUNCT3_LOAD_B
                    LH -> RVConst.FUNCT3_LOAD_H
                    LW -> RVConst.FUNCT3_LOAD_W
                    LBU -> RVConst.FUNCT3_LOAD_BU
                    LHU -> RVConst.FUNCT3_LOAD_HU
                    else -> UInt32.ZERO
                }

                val opcode = RVConst.OPC_LOAD
                val rd = regs[0]
                val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode

                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.RS2_OFF12 -> {
                // Store
                val rs1 = regs[1]

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(12)) {
                    expr.addError("${expr.eval} exceeds 12 bits")
                }

                val imm12 = imm.toInt32().toUInt32().lowest(12)

                val funct3 = when (this) {
                    SB -> RVConst.FUNCT3_STORE_B
                    SH -> RVConst.FUNCT3_STORE_H
                    SW -> RVConst.FUNCT3_STORE_W
                    else -> UInt32.ZERO
                }

                val opcode = RVConst.OPC_STORE
                val rs2 = regs[0]
                val bundle = (imm12.mask12Hi7() shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm12.mask12Lo5() shl 7) or opcode

                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.RD_RS1_RS2 -> {
                val opcode = RVConst.OPC_ARITH
                val funct7 = when (this) {
                    SRA -> RVConst.FUNCT7_SHIFT_ARITH_OR_SUB
                    SUB -> RVConst.FUNCT7_SHIFT_ARITH_OR_SUB
                    MUL, MULH, MULHSU, MULHU, DIV, DIVU, REM, REMU -> RVConst.FUNCT7_M
                    else -> UInt32.ZERO
                }

                val rd = regs[0]
                val rs1 = regs[1]
                val rs2 = regs[2]

                val funct3 = when (this) {
                    MUL -> RVConst.FUNCT3_M_MUL
                    MULH -> RVConst.FUNCT3_M_MULH
                    MULHSU -> RVConst.FUNCT3_M_MULHSU
                    MULHU -> RVConst.FUNCT3_M_MULHU
                    DIV -> RVConst.FUNCT3_M_DIV
                    DIVU -> RVConst.FUNCT3_M_DIVU
                    REM -> RVConst.FUNCT3_M_REM
                    REMU -> RVConst.FUNCT3_M_REMU
                    ADD, SUB -> RVConst.FUNCT3_OPERATION
                    SLL -> RVConst.FUNCT3_SHIFT_LEFT
                    SLT -> RVConst.FUNCT3_SLT
                    SLTU -> RVConst.FUNCT3_SLTU
                    XOR -> RVConst.FUNCT3_XOR
                    SRL, SRA -> RVConst.FUNCT3_SHIFT_RIGHT
                    OR -> RVConst.FUNCT3_OR
                    AND -> RVConst.FUNCT3_AND
                    else -> UInt32.ZERO
                }

                val bundle = (funct7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.RD_RS1_I12 -> {
                val opcode = when (this) {
                    JALR -> RVConst.OPC_JALR
                    else -> RVConst.OPC_ARITH_IMM
                }

                val funct3 = when (this) {
                    ADDI -> RVConst.FUNCT3_OPERATION
                    SLTI -> RVConst.FUNCT3_SLT
                    SLTIU -> RVConst.FUNCT3_SLTU
                    XORI -> RVConst.FUNCT3_XOR
                    ORI -> RVConst.FUNCT3_OR
                    ANDI -> RVConst.FUNCT3_AND
                    else -> UInt32.ZERO
                }

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(12)) {
                    expr.addError("$imm exceeds 12 bits")
                }
                val imm12 = imm.toInt32().toUInt32().lowest(12)

                val rd = regs[0]
                val rs1 = regs[1]

                val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.RD_RS1_SHAMT5 -> {
                val opcode = RVConst.OPC_ARITH_IMM
                val funct7 = when (this) {
                    SRAI -> RVConst.FUNCT7_SHIFT_ARITH_OR_SUB
                    else -> UInt32.ZERO
                }
                val funct3 = when (this) {
                    SLLI -> RVConst.FUNCT3_SHIFT_LEFT
                    else -> RVConst.FUNCT3_SHIFT_RIGHT
                }
                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(5)) {
                    expr.addError("$imm exceeds 5 bits")
                }
                val shamt = imm.toInt32().toUInt32().lowest(5)

                val rd = regs[0]
                val rs1 = regs[1]
                val bundle = (funct7 shl 25) or (shamt shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.CSR_RD_OFF12_RS1 -> {
                val csrs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVCsr.regs.firstOrNull { it.recognizable.contains(token.value) } }

                val opcode = RVConst.OPC_OS
                val funct3 = when (this) {
                    CSRRW -> RVConst.FUNCT3_CSR_RW
                    CSRRS -> RVConst.FUNCT3_CSR_RS
                    CSRRC -> RVConst.FUNCT3_CSR_RC
                    else -> UInt32.ZERO
                }

                val csr = if (csrs.isEmpty()) {
                    instr.nodes.filterIsInstance<ASNode.NumericExpr>().first().evaluate(builder).toUInt32()
                } else {
                    csrs[0].numericalValue.toUInt32()
                }

                if (csr shr 12 != UInt32.ZERO) {
                    instr.addError("Invalid CSR Offset 0x${csr.toString(16)}")
                }

                val rd = regs[0]
                val rs1 = regs[1]

                val bundle = (csr shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.CSR_RD_OFF12_UIMM5 -> {
                val csrs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVCsr.regs.firstOrNull { it.recognizable.contains(token.value) } }

                val opcode = RVConst.OPC_OS
                val funct3 = when (this) {
                    CSRRWI -> RVConst.FUNCT3_CSR_RWI
                    CSRRSI -> RVConst.FUNCT3_CSR_RSI
                    CSRRCI -> RVConst.FUNCT3_CSR_RCI
                    else -> UInt32.ZERO
                }

                val csr = if (csrs.isEmpty()) {
                    instr.nodes.filterIsInstance<ASNode.NumericExpr>().first().evaluate(builder).toUInt32()
                } else {
                    csrs[0].numericalValue.toUInt32()
                }

                if (csr shr 12 != UInt32.ZERO) {
                    instr.addError("Invalid CSR Offset 0x${csr.toString(16)}")
                }

                val rd = regs[0]
                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(5)) {
                    expr.addError("$imm exceeds 5 bits")
                }

                val zimm = imm.toInt32().toUInt32().lowest(5)

                val bundle = (csr shl 20) or (zimm shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.NONE -> {
                when (this) {
                    EBREAK, ECALL -> {
                        val opcode = RVConst.OPC_OS
                        val imm12 = when (this) {
                            EBREAK -> UInt32.ONE
                            else -> UInt32.ZERO
                        }
                        val bundle = (imm12 shl 20) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    FENCEI -> {
                        val bundle = (RVConst.FUNCT3_FENCE_I shl 12) or RVConst.OPC_FENCE
                        builder.currentSection.content.put(bundle)
                    }

                    else -> {}
                }

            }

            RV32ParamType.PS_RD_I32 -> {
                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$imm exceeds 32 bits")
                }

                val imm32 = imm.toInt32().toUInt32()

                val lower = imm32.lowest(12)
                val upper = imm32 - lower.signExtend(12)

                val imm20 = upper.shr(12)
                val imm12 = lower.lowest(12)

                // Build LUI Bundle
                val luiOPC = RVConst.OPC_LUI
                val luiBundle = (imm20 shl 12) or (regs[0] shl 7) or luiOPC
                builder.currentSection.content.put(luiBundle)

                // Build ADDI Bundle
                val addiBundle = (imm12 shl 20) or (regs[0] shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (regs[0] shl 7) or RVConst.OPC_ARITH_IMM
                if (imm12 != UInt32.ZERO) {
                    builder.currentSection.content.put(addiBundle)
                }
            }

            RV32ParamType.PS_RD_RS1 -> {
                when (this) {
                    Mv -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_OPERATION
                        val rd = regs[0]
                        val rs1 = regs[1]
                        val bundle = (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Not -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_XOR
                        val imm12 = (-1).toUInt32().mask32Lo12()
                        val rd = regs[0]
                        val rs1 = regs[1]
                        val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Neg -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_OPERATION
                        val funct7 = RVConst.FUNCT7_SHIFT_ARITH_OR_SUB
                        val rd = regs[0]
                        val rs2 = regs[1]

                        val bundle = (funct7 shl 25) or (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Seqz -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_SLTU
                        val imm12 = UInt32.ONE
                        val rd = regs[0]
                        val rs1 = regs[1]
                        val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Snez -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLTU
                        val rd = regs[0]
                        val rs2 = regs[1]
                        val bundle = (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Sltz -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLT
                        val rd = regs[0]
                        val rs1 = regs[1]
                        val bundle = (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Sgtz -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLT
                        val rd = regs[0]
                        val rs2 = regs[1]
                        val bundle = (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    else -> {
                        // Should never happen
                    }
                }
            }

            RV32ParamType.PS_RS1 -> {
                val opcode = RVConst.OPC_JALR
                val rs1 = regs[0]
                val rd = when (this) {
                    JALR1 -> RVBaseRegs.RA.ordinal.toUInt32()
                    else -> RVBaseRegs.ZERO.ordinal.toUInt32()
                }
                val bundle = (rs1 shl 15) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.PS_CSR_RS1 -> {
                val csrs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVCsr.regs.firstOrNull { it.recognizable.contains(token.value) } }
                val opcode = RVConst.OPC_OS
                val funct3 = RVConst.FUNCT3_CSR_RW
                val csr = if (csrs.isEmpty()) {
                    instr.nodes.filterIsInstance<ASNode.NumericExpr>().first().evaluate(builder).toUInt32()
                } else {
                    csrs[0].numericalValue.toUInt32()
                }
                if (csr shr 12 != UInt32.ZERO) {
                    instr.addError("Invalid CSR Offset 0x${csr.toString(16)}")
                }
                val rs1 = regs[0]
                val bundle = (csr shl 20) or (rs1 shl 15) or (funct3 shl 12) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.PS_RD_CSR -> {
                val csrs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVCsr.regs.firstOrNull { it.recognizable.contains(token.value) } }
                val opcode = RVConst.OPC_OS
                val funct3 = RVConst.FUNCT3_CSR_RS
                val csr = if (csrs.isEmpty()) {
                    instr.nodes.filterIsInstance<ASNode.NumericExpr>().first().evaluate(builder).toUInt32()
                } else {
                    csrs[0].numericalValue.toUInt32()
                }
                if (csr shr 12 != UInt32.ZERO) {
                    instr.addError("Invalid CSR Offset 0x${csr.toString(16)}")
                }
                val rd = regs[0]
                val bundle = (csr shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.PS_NONE -> {
                when (this) {
                    Nop -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val bundle = opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Ret -> {
                        val opcode = RVConst.OPC_JALR
                        val rs1 = RVBaseRegs.RA.ordinal.toUInt32()
                        val bundle = (rs1 shl 15) or opcode
                        builder.currentSection.content.put(bundle)
                    }


                    else -> {
                        // should not happen
                    }

                }
            }

            RV32ParamType.PRED_SUCC -> {
                val predUnchecked = exprs[0].evaluate(builder)
                val succUnchecked = exprs[1].evaluate(builder)
                if (!predUnchecked.fitsInSignedOrUnsigned(4)) {
                    exprs[0].addError("$predUnchecked exceeds 4 Bit!")
                }

                if (!succUnchecked.fitsInSignedOrUnsigned(4)) {
                    exprs[1].addError("$succUnchecked exceeds 4 Bit!")
                }

                val pred = predUnchecked.toUInt32().lowest(4)
                val succ = succUnchecked.toUInt32().lowest(4)
                val bundle = (pred shl 24) or (succ shl 20) or RVConst.OPC_FENCE
                builder.currentSection.content.put(bundle)
            }

            RV32ParamType.RS1_RS2_LBL -> {} // Will be evaluated later
            RV32ParamType.PS_RS1_JLBL -> {} // Will be evaluated later
            RV32ParamType.PS_RD_ALBL -> {} // Will be evaluated later
            RV32ParamType.PS_JLBL -> {} // Will be evaluated later

        }

        if (this.labelDependent) {
            return builder.currentSection.queueLateInit(instr, addressInstancesNeeded ?: 4)
        }
    }

    override fun lateEvaluation(builder: AsmCodeGenerator<*>, section: AsmCodeGenerator.Section, instr: ASNode.Instruction, index: Int) {
        if (builder !is ELFGenerator) return
        if (section !is ELFGenerator.ELFSection) return
        val regs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVBaseRegs.entries.firstOrNull { it.recognizable.contains(token.value) } }.map { it.numericalValue.toUInt32() }
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (this) {
            JAL -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt())
                }

                val target = targetAddr.toInt32().toUInt32()
                val relative = target - section.address(index)

                if (!relative.fitsInSignedOrUnsigned(21)) {
                    expr.addError("$relative exceeds 21 bits")
                }

                val imm20 = relative.mask20jType()

                val rd = regs[0]
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt())
                }
                if (!targetAddr.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$targetAddr exceeds 32 bits")
                }

                val target = targetAddr.toInt32().toUInt32()
                val relative = target - section.address(index)

                if (!relative.fitsInSignedOrUnsigned(12)) {
                    expr.addError("$relative exceeds 12 bits")
                }

                val imm7 = relative.mask12bType7()
                val imm5 = relative.mask12bType5()

                val opcode = RVConst.OPC_CBRA
                val funct3 = when (this) {
                    BEQ -> RVConst.FUNCT3_CBRA_BEQ
                    BNE -> RVConst.FUNCT3_CBRA_BNE
                    BLT -> RVConst.FUNCT3_CBRA_BLT
                    BGE -> RVConst.FUNCT3_CBRA_BGE
                    BLTU -> RVConst.FUNCT3_CBRA_BLTU
                    BGEU -> RVConst.FUNCT3_CBRA_BGEU
                    else -> throw Exception("Implementation Error!")
                }
                val rs1 = regs[0]
                val rs2 = regs[1]

                val bundle = (imm7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm5 shl 7) or opcode
                section.content[index] = bundle
            }

            La -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt())
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt() + 4U)
                }

                val thisAddr = section.address(index)
                val target = targetAddr.toInt32().toUInt32()
                val relative = target - thisAddr

                if (!relative.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$relative exceeds 32 bits")
                }

                val lo12 = relative.mask32Lo12()
                var hi20 = relative.mask32Hi20()

                if (lo12.bit(12) == UInt32.ONE) {
                    hi20 += UInt32.ONE
                }

                val rd = regs[0]

                val auipcOpCode = RVConst.OPC_AUIPC
                val auipcBundle = (hi20 shl 12) or (rd shl 7) or auipcOpCode
                section.content[index] = auipcBundle

                val addiOpCode = RVConst.OPC_ARITH_IMM
                val funct3 = RVConst.FUNCT3_OPERATION
                val addiBundle = (lo12 shl 20) or (rd shl 15) or (funct3 shl 12) or (rd shl 7) or addiOpCode
                section.content[index + 4] = addiBundle
            }

            Beqz, Bnez, Blez, Bgez, Bltz, Bgtz -> {
                // Comparisons with Zero
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt())
                }

                if (!targetAddr.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$targetAddr exceeds 32 bits")
                }

                val target = targetAddr.toInt32().toUInt32()
                val relative = target - section.address(index)

                if (!relative.fitsInSignedOrUnsigned(12)) {
                    expr.addError("$relative exceeds 12 bits")
                }

                val imm7 = relative.mask12bType7()
                val imm5 = relative.mask12bType5()

                val opcode = RVConst.OPC_CBRA
                val funct3 = when (this) {
                    Beqz -> RVConst.FUNCT3_CBRA_BEQ
                    Bnez -> RVConst.FUNCT3_CBRA_BNE
                    Blez -> RVConst.FUNCT3_CBRA_BGE
                    Bgez -> RVConst.FUNCT3_CBRA_BGE
                    Bltz -> RVConst.FUNCT3_CBRA_BLT
                    Bgtz -> RVConst.FUNCT3_CBRA_BLT
                    else -> throw Exception("Implementation Error!")
                }

                val rs2 = when (this) {
                    Beqz -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    Bnez -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    Blez -> regs[0]
                    Bgez -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    Bltz -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    Bgtz -> regs[0]
                    else -> throw Exception("Implementation Error!")
                }

                val rs1 = when (this) {
                    Beqz -> regs[0]
                    Bnez -> regs[0]
                    Blez -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    Bgez -> regs[0]
                    Bltz -> regs[0]
                    Bgtz -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    else -> throw Exception("Implementation Error!")
                }

                val bundle = (imm7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm5 shl 7) or opcode
                section.content[index] = bundle
            }

            Bgt, Ble, Bgtu, Bleu -> {
                // Swapping rs1 and rs2
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt())
                }
                if (!targetAddr.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$targetAddr exceeds 32 bits")
                }


                val target = targetAddr.toInt32().toUInt32()
                val relative = target - section.address(index)

                if (!relative.fitsInSignedOrUnsigned(12)) {
                    expr.addError("$relative exceeds 12 bits")
                }

                val imm7 = relative.mask12bType7()
                val imm5 = relative.mask12bType5()

                val opcode = RVConst.OPC_CBRA
                val funct3 = when (this) {
                    Bgt -> RVConst.FUNCT3_CBRA_BLT
                    Ble -> RVConst.FUNCT3_CBRA_BGE
                    Bgtu -> RVConst.FUNCT3_CBRA_BLTU
                    Bleu -> RVConst.FUNCT3_CBRA_BGEU
                    else -> throw Exception("Implementation Error!")
                }

                val rs2 = regs[0]
                val rs1 = regs[1]

                val bundle = (imm7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm5 shl 7) or opcode
                section.content[index] = bundle
            }

            J -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt())
                }
                if (!targetAddr.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$targetAddr exceeds 32 bits")
                }

                val target = targetAddr.toInt32().toUInt32()
                val relative = target - section.address(index)

                if (!relative.fitsInSignedOrUnsigned(20)) {
                    expr.addError("$relative exceeds 20 bits")
                }

                val imm20 = relative.mask20jType()

                val rd = RVBaseRegs.ZERO.ordinal.toUInt32()
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            JAL1 -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt())
                }
                if (!targetAddr.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$targetAddr exceeds 32 bits")
                }

                val target = targetAddr.toInt32().toUInt32()
                val relative = target - section.address(index)

                if (!relative.fitsInSignedOrUnsigned(20)) {
                    expr.addError("$relative exceeds 20 bits")
                }

                val imm20 = relative.mask20jType()
                val rd = RVBaseRegs.RA.ordinal.toUInt32()
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            Call -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt())
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt() + 4U)
                }
                if (!targetAddr.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$targetAddr exceeds 32 bits")
                }

                val target = targetAddr.toInt32().toUInt32()
                val result = target - section.address(index)
                val lo12 = result.mask32Lo12()
                var hi20 = result.mask32Hi20()

                if (lo12.bit(12) == UInt32.ONE) {
                    hi20 += UInt32.ONE
                }

                val x6 = RVBaseRegs.T1.ordinal.toUInt32()
                val x1 = RVBaseRegs.RA.ordinal.toUInt32()

                val auipcOpcode = RVConst.OPC_AUIPC
                val jalrOpcode = RVConst.OPC_JALR

                val auipcBundle = (hi20 shl 12) or (x6 shl 7) or auipcOpcode
                val jalrBundle = (lo12 shl 20) or (x6 shl 15) or (x1 shl 7) or jalrOpcode

                section.content[index] = auipcBundle
                section.content[index + 4] = jalrBundle
            }

            Tail -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt())
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt() + 4U)
                }
                if (!targetAddr.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$targetAddr exceeds 32 bits")
                }

                val target = targetAddr.toInt32().toUInt32()
                val result = target - section.address(index)
                val lo12 = result.mask32Lo12()
                var hi20 = result.mask32Hi20()

                if (lo12.bit(12) == UInt32.ONE) {
                    hi20 += UInt32.ONE
                }

                val x6 = RVBaseRegs.T1.ordinal.toUInt32()
                val x0 = RVBaseRegs.ZERO.ordinal.toUInt32()

                val auipcOpcode = RVConst.OPC_AUIPC
                val jalrOpcode = RVConst.OPC_JALR

                val auipcBundle = (hi20 shl 12) or (x6 shl 7) or auipcOpcode
                val jalrBundle = (lo12 shl 20) or (x6 shl 15) or (x0 shl 7) or jalrOpcode

                section.content[index] = auipcBundle
                section.content[index + 4] = jalrBundle
            }

            else -> {
                // Nothing needs to be done
            }
        }
    }

    fun AsmCodeGenerator.Section.address(offset: Int): UInt32 = address.toUInt32() + offset

}