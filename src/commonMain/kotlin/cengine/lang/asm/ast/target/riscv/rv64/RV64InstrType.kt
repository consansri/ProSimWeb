package cengine.lang.asm.ast.target.riscv.rv64


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
import cengine.util.integer.UInt64
import debug.DebugTools
import emulator.kit.nativeLog

enum class RV64InstrType(override val detectionName: String, val isPseudo: Boolean, val paramType: RV64ParamType, val labelDependent: Boolean = false, override val addressInstancesNeeded: Int? = 4) : InstrTypeInterface {
    LUI("LUI", false, RV64ParamType.RD_I20),
    AUIPC("AUIPC", false, RV64ParamType.RD_I20),
    JAL("JAL", false, RV64ParamType.RD_I20, true),
    JALR("JALR", false, RV64ParamType.RD_RS1_I12),
    ECALL("ECALL", false, RV64ParamType.NONE),
    EBREAK("EBREAK", false, RV64ParamType.NONE),
    BEQ("BEQ", false, RV64ParamType.RS1_RS2_LBL, true),
    BNE("BNE", false, RV64ParamType.RS1_RS2_LBL, true),
    BLT("BLT", false, RV64ParamType.RS1_RS2_LBL, true),
    BGE("BGE", false, RV64ParamType.RS1_RS2_LBL, true),
    BLTU("BLTU", false, RV64ParamType.RS1_RS2_LBL, true),
    BGEU("BGEU", false, RV64ParamType.RS1_RS2_LBL, true),
    LB("LB", false, RV64ParamType.RD_Off12),
    LH("LH", false, RV64ParamType.RD_Off12),
    LW("LW", false, RV64ParamType.RD_Off12),
    LD("LD", false, RV64ParamType.RD_Off12),
    LBU("LBU", false, RV64ParamType.RD_Off12),
    LHU("LHU", false, RV64ParamType.RD_Off12),
    LWU("LWU", false, RV64ParamType.RD_Off12),
    SB("SB", false, RV64ParamType.RS2_Off12),
    SH("SH", false, RV64ParamType.RS2_Off12),
    SW("SW", false, RV64ParamType.RS2_Off12),
    SD("SD", false, RV64ParamType.RS2_Off12),
    ADDI("ADDI", false, RV64ParamType.RD_RS1_I12),
    ADDIW("ADDIW", false, RV64ParamType.RD_RS1_I12),
    SLTI("SLTI", false, RV64ParamType.RD_RS1_I12),
    SLTIU("SLTIU", false, RV64ParamType.RD_RS1_I12),
    XORI("XORI", false, RV64ParamType.RD_RS1_I12),
    ORI("ORI", false, RV64ParamType.RD_RS1_I12),
    ANDI("ANDI", false, RV64ParamType.RD_RS1_I12),
    SLLI("SLLI", false, RV64ParamType.RD_RS1_SHAMT6),
    SLLIW("SLLIW", false, RV64ParamType.RD_RS1_SHAMT6),
    SRLI("SRLI", false, RV64ParamType.RD_RS1_SHAMT6),
    SRLIW("SRLIW", false, RV64ParamType.RD_RS1_SHAMT6),
    SRAI("SRAI", false, RV64ParamType.RD_RS1_SHAMT6),
    SRAIW("SRAIW", false, RV64ParamType.RD_RS1_SHAMT6),
    ADD("ADD", false, RV64ParamType.RD_RS1_RS2),
    ADDW("ADDW", false, RV64ParamType.RD_RS1_RS2),
    SUB("SUB", false, RV64ParamType.RD_RS1_RS2),
    SUBW("SUBW", false, RV64ParamType.RD_RS1_RS2),
    SLL("SLL", false, RV64ParamType.RD_RS1_RS2),
    SLLW("SLLW", false, RV64ParamType.RD_RS1_RS2),
    SLT("SLT", false, RV64ParamType.RD_RS1_RS2),
    SLTU("SLTU", false, RV64ParamType.RD_RS1_RS2),
    XOR("XOR", false, RV64ParamType.RD_RS1_RS2),
    SRL("SRL", false, RV64ParamType.RD_RS1_RS2),
    SRLW("SRLW", false, RV64ParamType.RD_RS1_RS2),
    SRA("SRA", false, RV64ParamType.RD_RS1_RS2),
    SRAW("SRAW", false, RV64ParamType.RD_RS1_RS2),
    OR("OR", false, RV64ParamType.RD_RS1_RS2),
    AND("AND", false, RV64ParamType.RD_RS1_RS2),

    FENCE("FENCE", false, RV64ParamType.PRED_SUCC),
    FENCEI("FENCE.I", false, RV64ParamType.NONE),

    // CSR Extension
    CSRRW("CSRRW", false, RV64ParamType.CSR_RD_OFF12_RS1),
    CSRRS("CSRRS", false, RV64ParamType.CSR_RD_OFF12_RS1),
    CSRRC("CSRRC", false, RV64ParamType.CSR_RD_OFF12_RS1),
    CSRRWI("CSRRWI", false, RV64ParamType.CSR_RD_OFF12_UIMM5),
    CSRRSI("CSRRSI", false, RV64ParamType.CSR_RD_OFF12_UIMM5),
    CSRRCI("CSRRCI", false, RV64ParamType.CSR_RD_OFF12_UIMM5),

    // M Extension
    MUL("MUL", false, RV64ParamType.RD_RS1_RS2),
    MULH("MULH", false, RV64ParamType.RD_RS1_RS2),
    MULHSU("MULHSU", false, RV64ParamType.RD_RS1_RS2),
    MULHU("MULHU", false, RV64ParamType.RD_RS1_RS2),
    DIV("DIV", false, RV64ParamType.RD_RS1_RS2),
    DIVU("DIVU", false, RV64ParamType.RD_RS1_RS2),
    REM("REM", false, RV64ParamType.RD_RS1_RS2),
    REMU("REMU", false, RV64ParamType.RD_RS1_RS2),

    // RV64 M Extension
    MULW("MULW", false, RV64ParamType.RD_RS1_RS2),
    DIVW("DIVW", false, RV64ParamType.RD_RS1_RS2),
    DIVUW("DIVUW", false, RV64ParamType.RD_RS1_RS2),
    REMW("REMW", false, RV64ParamType.RD_RS1_RS2),
    REMUW("REMUW", false, RV64ParamType.RD_RS1_RS2),


    // Pseudo

    CSRW("CSRW", true, RV64ParamType.PS_CSR_RS1),
    CSRR("CSRR", true, RV64ParamType.PS_RD_CSR),

    Nop("NOP", true, RV64ParamType.PS_NONE),
    Mv("MV", true, RV64ParamType.PS_RD_RS1),
    Li64("LI", true, RV64ParamType.PS_RD_LI_I64, addressInstancesNeeded = null),
    La("LA", true, RV64ParamType.PS_RD_Albl, true, addressInstancesNeeded = 8),
    Not("NOT", true, RV64ParamType.PS_RD_RS1),
    Neg("NEG", true, RV64ParamType.PS_RD_RS1),
    Seqz("SEQZ", true, RV64ParamType.PS_RD_RS1),
    Snez("SNEZ", true, RV64ParamType.PS_RD_RS1),
    Sltz("SLTZ", true, RV64ParamType.PS_RD_RS1),
    Sgtz("SGTZ", true, RV64ParamType.PS_RD_RS1),
    Beqz("BEQZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bnez("BNEZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Blez("BLEZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bgez("BGEZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bltz("BLTZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bgtz("BGTZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bgt("BGT", true, RV64ParamType.RS1_RS2_LBL, true),
    Ble("BLE", true, RV64ParamType.RS1_RS2_LBL, true),
    Bgtu("BGTU", true, RV64ParamType.RS1_RS2_LBL, true),
    Bleu("BLEU", true, RV64ParamType.RS1_RS2_LBL, true),
    J("J", true, RV64ParamType.PS_lbl, true),
    JAL1("JAL", true, RV64ParamType.PS_lbl, true),
    Jr("JR", true, RV64ParamType.PS_RS1),
    JALR1("JALR", true, RV64ParamType.PS_RS1),
    Ret("RET", true, RV64ParamType.PS_NONE),
    Call("CALL", true, RV64ParamType.PS_lbl, true, addressInstancesNeeded = 8),
    Tail("TAIL", true, RV64ParamType.PS_lbl, true, addressInstancesNeeded = 8);

    override val inCodeInfo: String? = if (isPseudo) "${addressInstancesNeeded ?: "?"} bytes" else null

    override val paramRule: Rule? = paramType.rule

    override val typeName: String = name

    override fun resolve(builder: AsmCodeGenerator<*>, instr: ASNode.Instruction) {
        val regs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVBaseRegs.entries.firstOrNull { it.recognizable.contains(token.value) } }
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (paramType) {
            RV64ParamType.RD_I20 -> {
                val expr = exprs[0]
                if (this != JAL) {
                    val opcode = when (this) {
                        LUI -> RVConst.OPC_LUI
                        AUIPC -> RVConst.OPC_AUIPC
                        else -> UInt32.ZERO
                    }
                    val rd = regs[0].ordinal.toUInt32()
                    val imm = expr.evaluate(builder)
                    if (!imm.fitsInSignedOrUnsigned(20)) {
                        expr.addError("${expr.eval} exceeds ${20} bits")
                    }
                    val imm20 = imm.toUInt32().lowest(20)

                    val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                    builder.currentSection.content.put(bundle)
                }
            }

            RV64ParamType.RD_Off12 -> {
                // Load
                val rs1 = regs[1].ordinal.toUInt32()

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
                    LD -> RVConst.FUNCT3_LOAD_D
                    LBU -> RVConst.FUNCT3_LOAD_BU
                    LHU -> RVConst.FUNCT3_LOAD_HU
                    LWU -> RVConst.FUNCT3_LOAD_WU
                    else -> UInt32.ZERO
                }

                val opcode = RVConst.OPC_LOAD
                val rd = regs[0].ordinal.toUInt32()
                val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode

                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RS2_Off12 -> {
                // Store
                val rs1 = regs[1].ordinal.toUInt32()

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(12)) {
                    expr.addError("${expr.eval} exceeds 12 bits")
                }

                val imm12 = imm.toInt().toUInt32().lowest(12)

                val funct3 = when (this) {
                    SB -> RVConst.FUNCT3_STORE_B
                    SH -> RVConst.FUNCT3_STORE_H
                    SW -> RVConst.FUNCT3_STORE_W
                    SD -> RVConst.FUNCT3_STORE_D
                    else -> UInt32.ZERO
                }

                val opcode = RVConst.OPC_STORE
                val rs2 = regs[0].ordinal.toUInt32()
                val bundle = (imm12.mask12Hi7() shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm12.mask12Lo5() shl 7) or opcode

                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RD_RS1_RS2 -> {
                val opcode = when (this) {
                    ADDW, SUBW, SLLW, SRLW, SRAW, MULW, DIVW, DIVUW, REMW, REMUW -> RVConst.OPC_ARITH_WORD
                    else -> RVConst.OPC_ARITH
                }


                val funct7 = when (this) {
                    SRA, SRAW -> RVConst.FUNCT7_SHIFT_ARITH_OR_SUB
                    SUB, SUBW -> RVConst.FUNCT7_SHIFT_ARITH_OR_SUB
                    MUL, MULH, MULHSU, MULHU, DIV, DIVU, REM, REMU -> RVConst.FUNCT7_M
                    else -> UInt32.ZERO
                }

                val rd = regs[0].ordinal.toUInt32()
                val rs1 = regs[1].ordinal.toUInt32()
                val rs2 = regs[2].ordinal.toUInt32()

                val funct3 = when (this) {
                    MUL, MULW -> RVConst.FUNCT3_M_MUL
                    MULH -> RVConst.FUNCT3_M_MULH
                    MULHSU -> RVConst.FUNCT3_M_MULHSU
                    MULHU -> RVConst.FUNCT3_M_MULHU
                    DIV, DIVW -> RVConst.FUNCT3_M_DIV
                    DIVU, DIVUW -> RVConst.FUNCT3_M_DIVU
                    REM, REMW -> RVConst.FUNCT3_M_REM
                    REMU, REMUW -> RVConst.FUNCT3_M_REMU
                    ADD, ADDW, SUB, SUBW -> RVConst.FUNCT3_OPERATION
                    SLL, SLLW -> RVConst.FUNCT3_SHIFT_LEFT
                    SRL, SRLW, SRA, SRAW -> RVConst.FUNCT3_SHIFT_RIGHT
                    SLT -> RVConst.FUNCT3_SLT
                    SLTU -> RVConst.FUNCT3_SLTU
                    XOR -> RVConst.FUNCT3_XOR
                    OR -> RVConst.FUNCT3_OR
                    AND -> RVConst.FUNCT3_AND
                    else -> UInt32.ZERO
                }

                val bundle = (funct7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RD_RS1_I12 -> {
                val opcode = when (this) {
                    JALR -> RVConst.OPC_JALR
                    ADDIW -> RVConst.OPC_ARITH_IMM_WORD
                    else -> RVConst.OPC_ARITH_IMM
                }

                val funct3 = when (this) {
                    ADDI, ADDIW -> RVConst.FUNCT3_OPERATION
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
                val imm12 = imm.toInt().toUInt32().lowest(12)

                val rd = regs[0].ordinal.toUInt32()
                val rs1 = regs[1].ordinal.toUInt32()

                val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RD_RS1_SHAMT6 -> {
                val opcode = when (this) {
                    SRAIW, SLLIW, SRLIW -> RVConst.OPC_ARITH_IMM_WORD
                    else -> RVConst.OPC_ARITH_IMM
                }

                val funct7 = when (this) {
                    SRAI, SRAIW -> RVConst.FUNCT7_SHIFT_ARITH_OR_SUB
                    else -> UInt32.ZERO
                }
                val funct3 = when (this) {
                    SLLI, SLLIW -> RVConst.FUNCT3_SHIFT_LEFT
                    else -> RVConst.FUNCT3_SHIFT_RIGHT
                }
                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(6)) {
                    expr.addError("$imm exceeds 6 bits")
                }
                val shamt = imm.toInt32().toUInt32().lowest(6)

                val rd = regs[0].ordinal.toUInt32()
                val rs1 = regs[1].ordinal.toUInt32()
                val bundle = (funct7 shl 25) or (shamt shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.CSR_RD_OFF12_RS1 -> {
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

                val rd = regs[0].ordinal.toUInt32()
                val rs1 = regs[1].ordinal.toUInt32()

                val bundle = (csr shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.CSR_RD_OFF12_UIMM5 -> {
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

                val rd = regs[0].ordinal.toUInt32()
                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.fitsInSignedOrUnsigned(5)) {
                    expr.addError("$imm exceeds 5 bits")
                }

                val zimm = imm.toInt().toUInt32().lowest(5)

                val bundle = (csr shl 20) or (zimm shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.PS_RD_LI_I64 -> {

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                val rd = regs[0].ordinal.toUInt32()
                val x0 = RVBaseRegs.ZERO.ordinal.toUInt32()

                if (!imm.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$imm exceeds 64 bits")
                }

                when {
                    imm.fitsInSigned(12) -> {
                        val imm12 = imm.toInt32().lowest(12).toUInt32()
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 12 Bit Signed for 0x${imm12.toString(16)}")
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_OPERATION

                        val addiBundle = (imm12 shl 20) or (x0 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(addiBundle)
                    }

                    imm.fitsInSigned(32) -> {
                        val imm32 = imm.toInt32().toUInt32()
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 32 Bit Signed for 0x${imm32.toString(16)}")
                        // resized = upper + lower
                        // upper = resized - lower
                        val lower = imm32.lowest(12)
                        val upper = imm32 - lower.signExtend(12)

                        val imm20 = upper.shr(12)
                        val imm12 = lower.lowest(12)

                        // Build LUI Bundle
                        val luiOPC = RVConst.OPC_LUI
                        val luiBundle = (imm20 shl 12) or (rd shl 7) or luiOPC
                        builder.currentSection.content.put(luiBundle)

                        // Build ADDI Bundle
                        val addiBundle = (imm12 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        if (imm12 != UInt32.ZERO) {
                            builder.currentSection.content.put(addiBundle)
                        }
                    }

                    imm.fitsInSigned(44) -> {
                        val resized = imm.toInt64()

                        /**
                         *  val64 = lui + addiw + addi3 + addi2 + addi1
                         *
                         *  LUI
                         *  ADDIW
                         *  SLLI 12
                         *  ADDI
                         */
                        val l1 = resized.lowest(12).toUInt32()
                        val l2 = resized.shr(12).lowest(12).toUInt32() + l1.bit(11)
                        val l3 = resized.shr(12 + 12).lowest(20).toUInt32() + l2.bit(11)

                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 44 Bit Signed for 0x${resized.toString(16)}:\n" +
                                "\tl1: ${l1.toString(16)}\n" +
                                "\tl2: ${l2.toString(16)}\n" +
                                "\tl3: ${l3.toString(16)}\n")


                        // Build LUI Bundle
                        val luiBundle = (l3 shl 12) or (rd shl 7) or RVConst.OPC_LUI
                        builder.currentSection.content.put(luiBundle)

                        // Build ADDIW Bundle
                        if (l2 != UInt32.ZERO) {
                            val addiwBundle = (l2 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM_WORD
                            builder.currentSection.content.put(addiwBundle)
                        }

                        // Build SLLI Bundle
                        val slliBundle = (UInt32.ZERO shl 25) or (0xCU.toUInt32() shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(slliBundle)

                        if (l1 != UInt32.ZERO) {
                            // Build ADDI Bundle
                            val addiBundle = (l1 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(addiBundle)
                        }
                    }

                    imm.fitsInSigned(56) -> {
                        val resized = imm.toInt64().toUInt64().lowest(56)
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 56 Bit Signed for ${resized.toString(16)}")
                        /**
                         *  val64 = lui + addiw + addi3 + addi2 + addi1
                         *
                         *  LUI
                         *  ADDIW
                         *  SLLI 12
                         *  ADDI
                         *  SLLI 12
                         *  ADDI
                         */
                        val l1 = resized.lowest(12).toUInt32()
                        val l2 = resized.shr(12).lowest(12).toUInt32() + l1.bit(11)
                        val l3 = resized.shr(12 + 12).lowest(12).toUInt32() + l2.bit(11)
                        val l4 = resized.shr(12 + 12 + 12).lowest(20).toUInt32() + l3.bit(11)

                        var shiftNeeded = UInt32.ZERO

                        // Build LUI Bundle
                        val luiBundle = (l4 shl 12) or (rd shl 7) or RVConst.OPC_LUI
                        builder.currentSection.content.put(luiBundle)

                        if (l3 != UInt32.ZERO) {
                            // Build ADDIW Bundle
                            val addiwBundle = (l3 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM_WORD
                            builder.currentSection.content.put(addiwBundle)
                        }

                        shiftNeeded += 0xC

                        if (l2 != UInt32.ZERO) {
                            // Build SLLI Bundle
                            val slliBundle = (UInt32.ZERO shl 25) or (shiftNeeded shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(slliBundle)

                            shiftNeeded = UInt32.ZERO

                            // Build ADDI Bundle
                            val addiBundle1 = (l2 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(addiBundle1)
                        }

                        shiftNeeded += 0xC

                        // Build SLLI Bundle
                        val slliBundle = (UInt32.ZERO shl 25) or (shiftNeeded shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(slliBundle)

                        if (l1 != UInt32.ZERO) {
                            // Build ADDI Bundle
                            val addiBundle2 = (l1 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(addiBundle2)
                        }
                    }

                    else -> {
                        val resized = try {
                            imm.toInt64().toUInt64()
                        } catch (e: Exception) {
                            imm.toUInt64()
                        }
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 64 Bit Signed for ${resized.toString(16)}")
                        /**
                         *  val64 = lui + addiw + addi3 + addi2 + addi1
                         *
                         *  LUI
                         *  ADDIW
                         *  SLLI 12
                         *  ADDI
                         *  SLLI 12
                         *  ADDI
                         *  SLLI 12
                         *  ADDI
                         */

                        val l1 = resized.lowest(12).toUInt32()
                        val l2 = resized.shr(12).lowest(12).toUInt32() + l1.bit(11)
                        val l3 = resized.shr(12 + 12).lowest(12).toUInt32() + l2.bit(11)
                        val l4 = resized.shr(12 + 12 + 12).lowest(12).toUInt32() + l3.bit(11)
                        val l5 = resized.shr(12 + 12 + 12 + 12).toUInt32() + l4.bit(11)

                        // Build LUI Bundle
                        val luiBundle = (l5 shl 12) or (rd shl 7) or RVConst.OPC_LUI
                        builder.currentSection.content.put(luiBundle)

                        var shiftNeeded: UInt32 = UInt32.ZERO

                        if (l4 != UInt32.ZERO) {
                            // Build ADDIW Bundle
                            val addiwBundle = (l4 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM_WORD
                            builder.currentSection.content.put(addiwBundle)
                        }

                        shiftNeeded += 0xC

                        if (l3 != UInt32.ZERO) {
                            // Build SLLI Bundle
                            val slliBundleC = (UInt32.ZERO shl 25) or (shiftNeeded shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(slliBundleC)

                            shiftNeeded = UInt32.ZERO

                            // Build ADDI Bundle
                            val addiBundle1 = (l3 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(addiBundle1)
                        }

                        shiftNeeded += 0xC

                        if (l2 != UInt32.ZERO) {
                            // Build SLLI Bundle
                            val slliBundleC = (UInt32.ZERO shl 25) or (shiftNeeded shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(slliBundleC)

                            shiftNeeded = UInt32.ZERO

                            // Build ADDI Bundle
                            val addiBundle2 = (l2 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(addiBundle2)
                        }

                        shiftNeeded += 0xC

                        // Build SLLI Bundle
                        val slliBundleC = (UInt32.ZERO shl 25) or (shiftNeeded shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(slliBundleC)

                        if (l1 != UInt32.ZERO) {
                            // Build ADDI Bundle
                            val addiBundle3 = (l1 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                            builder.currentSection.content.put(addiBundle3)
                        }
                    }
                }
            }

            RV64ParamType.PS_RD_RS1 -> {
                when (this) {
                    Mv -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_OPERATION
                        val rd = regs[0].ordinal.toUInt32()
                        val rs1 = regs[1].ordinal.toUInt32()
                        val bundle = (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Not -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_XOR
                        val imm12 = (-1).toUInt32().mask32Lo12()
                        val rd = regs[0].ordinal.toUInt32()
                        val rs1 = regs[1].ordinal.toUInt32()
                        val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Neg -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_OPERATION
                        val funct7 = RVConst.FUNCT7_SHIFT_ARITH_OR_SUB
                        val rd = regs[0].ordinal.toUInt32()
                        val rs2 = regs[1].ordinal.toUInt32()

                        val bundle = (funct7 shl 25) or (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Seqz -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_SLTU
                        val imm12 = UInt32.ONE
                        val rd = regs[0].ordinal.toUInt32()
                        val rs1 = regs[1].ordinal.toUInt32()
                        val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Snez -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLTU
                        val rd = regs[0].ordinal.toUInt32()
                        val rs2 = regs[1].ordinal.toUInt32()
                        val bundle = (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Sltz -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLT
                        val rd = regs[0].ordinal.toUInt32()
                        val rs1 = regs[1].ordinal.toUInt32()
                        val bundle = (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Sgtz -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLT
                        val rd = regs[0].ordinal.toUInt32()
                        val rs2 = regs[1].ordinal.toUInt32()
                        val bundle = (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    else -> {
                        // Should never happen
                    }
                }
            }

            RV64ParamType.PS_RS1 -> {
                val opcode = RVConst.OPC_JALR
                val rs1 = regs[0].ordinal.toUInt32()
                val rd = when (this) {
                    JALR1 -> RVBaseRegs.RA.ordinal.toUInt32()
                    else -> RVBaseRegs.ZERO.ordinal.toUInt32()
                }
                val bundle = (rs1 shl 15) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.PS_CSR_RS1 -> {
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

                val rs1 = regs[0].ordinal.toUInt32()
                val bundle = (csr shl 20) or (rs1 shl 15) or (funct3 shl 12) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.PS_RD_CSR -> {
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

                val rd = regs[0].ordinal.toUInt32()
                val bundle = (csr shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.NONE -> {
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

            RV64ParamType.PS_NONE -> {
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

            RV64ParamType.PRED_SUCC -> {
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

            RV64ParamType.RS1_RS2_LBL -> {} // Will be evaluated later
            RV64ParamType.PS_RS1_Jlbl -> {} // Will be evaluated later
            RV64ParamType.PS_RD_Albl -> {} // Will be evaluated later
            RV64ParamType.PS_lbl -> {} // Will be evaluated later

        }

        if (labelDependent) {
            return builder.currentSection.queueLateInit(instr, addressInstancesNeeded ?: 4)
        }
    }

    override fun lateEvaluation(builder: AsmCodeGenerator<*>, section: AsmCodeGenerator.Section, instr: ASNode.Instruction, index: Int) {
        if (builder !is ELFGenerator) return
        if (section !is ELFGenerator.ELFSection) return
        val regs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVBaseRegs.entries.firstOrNull { it.recognizable.contains(token.value) } }
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (this) {
            JAL -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt32())
                }

                if (!targetAddr.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$targetAddr exceeds 64 bits")
                }

                val target = targetAddr.toUInt64()
                val relative = target - section.thisAddr(index)
                val imm = relative shr 1
                if (!imm.fitsInSignedOrUnsigned(20)) {
                    expr.addError("$imm exceeds 20 bits")
                }

                val imm20 = relative.toUInt32().mask20jType()

                val rd = regs[0].ordinal.toUInt32()
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt32())
                }
                if (!targetAddr.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$targetAddr exceeds 64 bits")
                }

                val relative = targetAddr.toUInt64() - section.thisAddr(index)
                if (!relative.fitsInSignedOrUnsigned(12)) {
                    expr.addError("$relative exceeds 12 bits")
                }

                val imm7 = relative.toUInt32().mask12bType7()
                val imm5 = relative.toUInt32().mask12bType5()

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
                val rs1 = regs[0].ordinal.toUInt32()
                val rs2 = regs[1].ordinal.toUInt32()

                val bundle = (imm7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm5 shl 7) or opcode
                section.content[index] = bundle
            }

            La -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt32())
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt32() + 4U)
                }

                if (!targetAddr.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$targetAddr exceeds 32 bits")
                }

                val result = targetAddr.toInt32().toUInt32()

                val lo12 = result.mask32Lo12()
                var hi20 = result.mask32Hi20()

                if (lo12.bit(11) == UInt32.ONE) {
                    hi20 += UInt32.ONE
                }

                val rd = regs[0].ordinal.toUInt32()

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
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt32())
                }

                if (!targetAddr.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$targetAddr exceeds 64 bits")
                }

                val target = targetAddr.toInt64().toUInt64()
                val relative = target - section.thisAddr(index)

                if (!relative.fitsInSignedOrUnsigned(12)) {
                    expr.addError("$relative exceeds 12 bits")
                }

                val imm7 = relative.toUInt32().mask12bType7()
                val imm5 = relative.toUInt32().mask12bType5()

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
                    Blez -> regs[0].ordinal.toUInt32()
                    Bgez -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    Bltz -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    Bgtz -> regs[0].ordinal.toUInt32()
                    else -> throw Exception("Implementation Error!")
                }

                val rs1 = when (this) {
                    Beqz -> regs[0].ordinal.toUInt32()
                    Bnez -> regs[0].ordinal.toUInt32()
                    Blez -> RVBaseRegs.ZERO.ordinal.toUInt32()
                    Bgez -> regs[0].ordinal.toUInt32()
                    Bltz -> regs[0].ordinal.toUInt32()
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
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt32())
                }
                if (!targetAddr.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$targetAddr exceeds 64 bits")
                }

                val target = targetAddr.toInt64().toUInt64()
                val relative = target - section.thisAddr(index)

                if (!relative.fitsInSignedOrUnsigned(12)) {
                    expr.addError("$relative exceeds 12 bits")
                }

                val imm7 = relative.toUInt32().mask12bType7()
                val imm5 = relative.toUInt32().mask12bType5()

                val opcode = RVConst.OPC_CBRA
                val funct3 = when (this) {
                    Bgt -> RVConst.FUNCT3_CBRA_BLT
                    Ble -> RVConst.FUNCT3_CBRA_BGE
                    Bgtu -> RVConst.FUNCT3_CBRA_BLTU
                    Bleu -> RVConst.FUNCT3_CBRA_BGEU
                    else -> throw Exception("Implementation Error!")
                }

                val rs2 = regs[0].ordinal.toUInt32()
                val rs1 = regs[1].ordinal.toUInt32()

                val bundle = (imm7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm5 shl 7) or opcode
                section.content[index] = bundle
            }

            J -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt32())
                }
                if (!targetAddr.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$targetAddr exceeds 64 bits")
                }

                val target = targetAddr.toInt64().toUInt64()
                val relative = target - section.thisAddr(index)

                if (!relative.fitsInSignedOrUnsigned(20)) {
                    expr.addError("$relative exceeds 20 bits")
                }

                val imm20 = relative.toUInt32().mask20jType()

                val rd = RVBaseRegs.ZERO.ordinal.toUInt32()
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            JAL1 -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt32())
                }
                if (!targetAddr.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$targetAddr exceeds 64 bits")
                }

                val target = targetAddr.toInt64().toUInt64()
                val relative = target - section.thisAddr(index)

                if (!relative.fitsInSignedOrUnsigned(20)) {
                    expr.addError("$relative exceeds 20 bits")
                }

                val imm20 = relative.toUInt32().mask20jType()

                val rd = RVBaseRegs.RA.ordinal.toUInt32()
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            Call -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt32())
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt32() + 4U)
                }
                if (!targetAddr.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$targetAddr exceeds 64 bits")
                }

                val target = targetAddr.toInt64().toUInt64()
                val relative = target - section.thisAddr(index)

                if (!relative.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$relative exceeds 32 bits")
                }

                val lo12 = relative.toUInt32().mask32Lo12()
                var hi20 = relative.toUInt32().mask32Hi20()

                if (lo12.bit(11) == UInt32.ONE) {
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
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt32())
                    // builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt32() + 4U)
                }
                if (!targetAddr.fitsInSignedOrUnsigned(64)) {
                    expr.addError("$targetAddr exceeds 64 bits")
                }

                val target = targetAddr.toInt64().toUInt64()
                val relative = target - section.thisAddr(index)

                if (!relative.fitsInSignedOrUnsigned(32)) {
                    expr.addError("$relative exceeds 32 bits")
                }

                val lo12 = relative.toUInt32().mask32Lo12()
                var hi20 = relative.toUInt32().mask32Hi20()

                if (lo12.bit(11) == UInt32.ONE) {
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

    fun AsmCodeGenerator.Section.thisAddr(offset: Int): UInt64 = address.toUInt64() + offset


}