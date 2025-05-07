package cengine.lang.asm.target.riscv

import cengine.console.SysOut
import cengine.lang.asm.AsmTreeParser
import cengine.lang.asm.gas.AsmBackend
import cengine.lang.asm.gas.AsmCodeGenerator
import cengine.lang.asm.psi.AsmInstruction
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.asm.target.riscv.RvParamT.*
import cengine.psi.parser.PsiBuilder
import cengine.psi.semantic.expr.EvaluationException
import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32

/**
 * Represents the different RISC-V instruction formats and pseudo-instructions.
 */
sealed interface RvInstrT : AsmInstructionT {

    // --- Base RISC-V Instruction Formats ---

    /** The expected operand syntax type for this instruction. */
    val paramT: RvParamT

    /** Default parsing logic using the specified paramT */
    override fun PsiBuilder.parse(asmTreeParser: AsmTreeParser, marker: PsiBuilder.Marker): Boolean {
        skipWhitespaceAndComments()

        // Use the specific operand parsing logic defined in RvParamT
        with(paramT) {
            return parse(asmTreeParser)
        }
    }

    /** Base RV32I/RV64I Instructions (+Zicsr, Zifencei) */
    enum class BaseT(override val keyWord: String, override val paramT: RvParamT) : RvInstrT {
        // U-Type
        LUI("lui", RD_IMM20),
        AUIPC("auipc", RD_IMM20),

        // J-Type
        JAL("jal", RD_LABEL), // rd, label (resolves to imm20 offset)

        // I-Type (Immediate Arithmetic/Logic)
        ADDI("addi", RD_RS1_IMM12),
        SLTI("slti", RD_RS1_IMM12),
        SLTIU("sltiu", RD_RS1_IMM12),
        XORI("xori", RD_RS1_IMM12),
        ORI("ori", RD_RS1_IMM12),
        ANDI("andi", RD_RS1_IMM12),

        // I-Type (Shifts) - shamt size depends on XLEN (handled by parser/assembler)
        SLLI("slli", RD_RS1_SHAMT),
        SRLI("srli", RD_RS1_SHAMT),
        SRAI("srai", RD_RS1_SHAMT),

        // I-Type (Loads)
        LB("lb", RD_IMM12_RS1),
        LH("lh", RD_IMM12_RS1),
        LW("lw", RD_IMM12_RS1),
        LBU("lbu", RD_IMM12_RS1),
        LHU("lhu", RD_IMM12_RS1),

        // I-Type (Jump and Link Register)
        JALR("jalr", RD_RS1_IMM12), // Often used as `rd, rs1` or `rd, offset(rs1)`

        // R-Type (Register-Register Arithmetic/Logic)
        ADD("add", RD_RS1_RS2),
        SUB("sub", RD_RS1_RS2),
        SLL("sll", RD_RS1_RS2),
        SLT("slt", RD_RS1_RS2),
        SLTU("sltu", RD_RS1_RS2),
        XOR("xor", RD_RS1_RS2),
        SRL("srl", RD_RS1_RS2),
        SRA("sra", RD_RS1_RS2),
        OR("or", RD_RS1_RS2),
        AND("and", RD_RS1_RS2),

        // B-Type (Branches)
        BEQ("beq", RS1_RS2_LABEL),
        BNE("bne", RS1_RS2_LABEL),
        BLT("blt", RS1_RS2_LABEL),
        BGE("bge", RS1_RS2_LABEL),
        BLTU("bltu", RS1_RS2_LABEL),
        BGEU("bgeu", RS1_RS2_LABEL),

        // S-Type (Stores)
        SB("sb", RS2_IMM12_RS1),
        SH("sh", RS2_IMM12_RS1),
        SW("sw", RS2_IMM12_RS1),

        // Zicsr - System Instructions (CSR access)
        CSRRW("csrrw", RD_CSR_RS1),
        CSRRS("csrrs", RD_CSR_RS1),
        CSRRC("csrrc", RD_CSR_RS1),
        CSRRWI("csrrwi", RD_CSR_UIMM5),
        CSRRSI("csrrsi", RD_CSR_UIMM5),
        CSRRCI("csrrci", RD_CSR_UIMM5),

        // Fence Instructions
        FENCE("fence", PRED_SUCC), // Operands like "iorw, iorw"
        FENCE_I("fence.i", NONE), // Zifencei

        // System Instructions (Environment)
        ECALL("ecall", NONE),
        EBREAK("ebreak", NONE),
        SRET("sret", NONE),
        MRET("mret", NONE),
        WFI("wfi", NONE),
        SFENCE_VMA("sfence.vma", OPT_RS1_RS2), //TODO("RS1_RS2 Is missing") // Operands rs1, rs2 often optional/zero in assembly

        ; // End of enum entries

        // Determine if an instruction *inherently* depends on a label value for pass 1.
        // Note: AUIPC/LUI might depend on labels via linker relaxations (%pcrel_hi/%lo),
        // handled by the expression evaluator later or specific relocation types.
        // JALR often uses immediates resolved in pass 1, unless the immediate itself involves a label.
        private val isLabelDependentPass1: Boolean
            get() = when (this) {
                JAL, BEQ, BNE, BLT, BGE, BLTU, BGEU -> true
                // AUIPC might depend on a label expression for %pcrel_hi, defer to pass 2 if expression contains label
                // JALR might depend on a label expression for %pcrel_lo, defer if expression contains label
                else -> false
            }

        override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
            if (isLabelDependentPass1) {
                context.section.queueLateInit(instr, 4)
                return
            }

            val spec = context.spec as? RvSpec ?: throw Exception("Internal error: RISC-V backend requires RvSpec")

            // Evaluate expressions - expect simple integers or constants here
            val exprs = instr.exprs.map { absIntEvaluator.evaluate(it, context) }
            // Get register numbers
            val regs = instr.regs.map { it.type.address.toUInt32() }
            // --- Generate binary for instructions resolvable in Pass 1 ---
            var binary: UInt32 = UInt32.ZERO


            // Use the pre-calculated 'regs' and 'exprValues' lists directly
            binary = when (this@BaseT) {
                // U-Type
                LUI -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                    val immVal = exprs.getOrNull(0) ?: throw Exception("Missing immediate for $keyWord")
                    val imm = immVal.toInt32().toUInt32()
                    RvConst.packImmU(imm) or (rd shl 7) or RvConst.OPC_LUI
                }

                AUIPC -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                    val immVal = exprs.getOrNull(0) ?: throw Exception("Missing immediate for $keyWord")
                    val imm = immVal.toInt32().toUInt32()
                    RvConst.packImmU(imm) or (rd shl 7) or RvConst.OPC_AUIPC
                }

                // I-Type (Immediate Arithmetic/Logic)
                ADDI, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                    val rs1 = regs.getOrNull(1) ?: throw Exception("Missing required register operand 1 for $keyWord")
                    val immVal = exprs.getOrNull(0) ?: throw Exception("Missing immediate for $keyWord")
                    if (!immVal.fitsInSigned(12)) {
                        throw Exception("Immediate $immVal does not fit in 12 signed bits for $keyWord")
                    }
                    val imm = immVal.toInt32().toUInt32()
                    val funct3 = when (this@BaseT) {
                        ADDI -> RvConst.FUNCT3_ADDI_ADD_SUB
                        SLTI -> RvConst.FUNCT3_SLTI_SLT
                        SLTIU -> RvConst.FUNCT3_SLTIU_SLTU
                        XORI -> RvConst.FUNCT3_XORI_XOR
                        ORI -> RvConst.FUNCT3_ORI_OR
                        ANDI -> RvConst.FUNCT3_ANDI_AND
                        else -> throw Exception("Internal error: Unexpected I-type instruction")
                    }
                    RvConst.packImmI12(imm) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM
                }

                // I-Type (Shifts) - Assuming RV32 shamt (5 bits)
                SLLI, SRLI, SRAI -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                    val rs1 = regs.getOrNull(1) ?: throw Exception("Missing required register operand 1 for $keyWord")
                    val shamtVal = exprs.getOrNull(0) ?: throw Exception("Missing shamt for $keyWord")
                    if (!shamtVal.fitsInUnsigned(5)) { // RV32 uses 5 bits
                        throw Exception("Shift amount $shamtVal does not fit in 5 unsigned bits for $keyWord")
                    }

                    val shamt = when (spec) {
                        Rv32Spec -> shamtVal.toUInt32() and 0x1fu.toUInt32() // Mask to 5 bits
                        Rv64Spec -> shamtVal.toUInt32() and 0x3fu.toUInt32() // Mask to 6 bits
                    }

                    val funct3 = when (this@BaseT) {
                        SLLI -> RvConst.FUNCT3_SLLI_SLL
                        SRLI, SRAI -> RvConst.FUNCT3_SRLI_SRL_SRAI_SRA
                        else -> throw Exception("Internal error: Unexpected Shift-I instruction")
                    }
                    val funct7 = if (this@BaseT == SRAI) RvConst.FUNCT7_SUB_SRA else RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND
                    (funct7 shl 25) or (shamt shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM
                }

                // I-Type (Loads)
                LB, LH, LW, LBU, LHU -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                    val immVal = exprs.getOrNull(0) ?: throw Exception("Missing immediate offset for $keyWord")
                    val rs1 = regs.getOrNull(1) ?: throw Exception("Missing required register operand 1 for $keyWord") // Base register is second reg operand
                    if (!immVal.fitsInSigned(12)) {
                        throw Exception("Immediate offset $immVal does not fit in 12 signed bits for $keyWord")
                    }
                    val imm = immVal.toInt32().toUInt32()
                    val funct3 = when (this@BaseT) {
                        LB -> RvConst.FUNCT3_LOAD_B
                        LH -> RvConst.FUNCT3_LOAD_H
                        LW -> RvConst.FUNCT3_LOAD_W
                        LBU -> RvConst.FUNCT3_LOAD_BU
                        LHU -> RvConst.FUNCT3_LOAD_HU
                        else -> throw Exception("Internal error: Unexpected Load instruction")
                    }
                    RvConst.packImmI12(imm) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_LOAD
                }

                // I-Type (Jump and Link Register)
                JALR -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                    val rs1 = regs.getOrNull(1) ?: throw Exception("Missing required register operand 1 for $keyWord")
                    val immVal = exprs.getOrNull(0) ?: throw Exception("Missing immediate offset for $keyWord")
                    if (!immVal.fitsInSigned(12)) {
                        throw Exception("Immediate offset $immVal does not fit in 12 signed bits for $keyWord")
                    }
                    val imm = immVal.toInt32().toUInt32()
                    RvConst.packImmI12(imm) or (rs1 shl 15) or (rd shl 7) or RvConst.OPC_JALR
                }

                // R-Type (Register-Register Arithmetic/Logic)
                ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                    val rs1 = regs.getOrNull(1) ?: throw Exception("Missing required register operand 1 for $keyWord")
                    val rs2 = regs.getOrNull(2) ?: throw Exception("Missing required register operand 2 for $keyWord")
                    val funct3 = when (this@BaseT) {
                        ADD, SUB -> RvConst.FUNCT3_ADDI_ADD_SUB
                        SLL -> RvConst.FUNCT3_SLLI_SLL
                        SLT -> RvConst.FUNCT3_SLTI_SLT
                        SLTU -> RvConst.FUNCT3_SLTIU_SLTU
                        XOR -> RvConst.FUNCT3_XORI_XOR
                        SRL, SRA -> RvConst.FUNCT3_SRLI_SRL_SRAI_SRA
                        OR -> RvConst.FUNCT3_ORI_OR
                        AND -> RvConst.FUNCT3_ANDI_AND
                        else -> throw Exception("Internal error: Unexpected R-type instruction")
                    }
                    val funct7 = when (this@BaseT) {
                        SUB, SRA -> RvConst.FUNCT7_SUB_SRA
                        else -> RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND
                    }
                    (funct7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_ARITH
                }

                // S-Type (Stores)
                SB, SH, SW -> {
                    val rs2 = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 (rs2) for $keyWord") // Source reg
                    val immVal = exprs.getOrNull(0) ?: throw Exception("Missing immediate offset for $keyWord")
                    val rs1 = regs.getOrNull(1) ?: throw Exception("Missing required register operand 1 (rs1/base) for $keyWord")  // Base reg
                    if (!immVal.fitsInSigned(12)) {
                        throw Exception("Immediate offset $immVal does not fit in 12 signed bits for $keyWord")
                    }
                    val imm = immVal.toInt32().toUInt32()
                    val funct3 = when (this@BaseT) {
                        SB -> RvConst.FUNCT3_STORE_B
                        SH -> RvConst.FUNCT3_STORE_H
                        SW -> RvConst.FUNCT3_STORE_W
                        else -> throw Exception("Internal error: Unexpected Store instruction")
                    }
                    RvConst.packImmS(imm) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or RvConst.OPC_STORE
                }

                // Zicsr - System Instructions (CSR access)
                CSRRW, CSRRS, CSRRC -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 (rd) for $keyWord")
                    val csrVal = regs.getOrNull(1) ?: throw Exception("Missing CSR address for $keyWord")
                    val rs1 = regs.getOrNull(2) ?: throw Exception("Missing required register operand 1 (rs1) for $keyWord")
                    if (!csrVal.fitsInUnsigned(12)) {
                        throw Exception("CSR address $csrVal does not fit in 12 unsigned bits")
                    }
                    val csr = (csrVal and 0xFFFu.toUInt32())
                    val funct3 = when (this@BaseT) {
                        CSRRW -> RvConst.FUNCT3_CSR_RW
                        CSRRS -> RvConst.FUNCT3_CSR_RS
                        CSRRC -> RvConst.FUNCT3_CSR_RC
                        else -> throw Exception("Internal error: Unexpected CSR instruction")
                    }
                    (csr shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_SYSTEM
                }

                CSRRWI, CSRRSI, CSRRCI -> {
                    val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 (rd) for $keyWord")
                    val csrVal = regs.getOrNull(1) ?: throw Exception("Missing CSR address for $keyWord")
                    val uimmVal = exprs.getOrNull(2) ?: throw Exception("Missing immediate value (uimm5) for $keyWord")
                    if (!csrVal.fitsInUnsigned(12)) {
                        throw Exception("CSR address $csrVal does not fit in 12 unsigned bits")
                    }
                    if (!uimmVal.fitsInUnsigned(5)) {
                        throw Exception("Immediate $uimmVal does not fit in 5 unsigned bits for $keyWord")
                    }
                    val csr = (csrVal and 0xFFFu.toUInt32())
                    val uimm = (uimmVal.toUInt32() and 0x1Fu.toUInt32())
                    val funct3 = when (this@BaseT) {
                        CSRRWI -> RvConst.FUNCT3_CSR_RWI
                        CSRRSI -> RvConst.FUNCT3_CSR_RSI
                        CSRRCI -> RvConst.FUNCT3_CSR_RCI
                        else -> throw Exception("Internal error: Unexpected CSRI instruction")
                    }
                    (csr shl 20) or (uimm shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_SYSTEM
                }

                // Fence Instructions
                FENCE -> {
                    val predVal = exprs.getOrNull(0) ?: UInt32.ZERO
                    val succVal = exprs.getOrNull(1) ?: UInt32.ZERO
                    if (!predVal.fitsInUnsigned(4)) throw Exception("Fence predecessor $predVal out of range (0-15)")
                    if (!succVal.fitsInUnsigned(4)) throw Exception("Fence successor $succVal out of range (0-15)")
                    val pred = predVal.toUInt32() and 0xFu.toUInt32()
                    val succ = succVal.toUInt32() and 0xFu.toUInt32()
                    (pred shl 24) or (succ shl 20) or (UInt32.ZERO shl 15) or (RvConst.FUNCT3_FENCE shl 12) or (UInt32.ZERO shl 7) or RvConst.OPC_FENCE
                }

                FENCE_I -> {
                    (UInt32.ZERO shl 20) or (UInt32.ZERO shl 15) or (RvConst.FUNCT3_FENCE_I shl 12) or (UInt32.ZERO shl 7) or RvConst.OPC_FENCE
                }

                // System Instructions (Environment)
                ECALL -> (RvConst.IMM12_ECALL shl 20) or (UInt32.ZERO shl 15) or (RvConst.FUNCT3_ECALL_EBREAK_MRET_etc shl 12) or (UInt32.ZERO shl 7) or RvConst.OPC_SYSTEM
                EBREAK -> (RvConst.IMM12_EBREAK shl 20) or (UInt32.ZERO shl 15) or (RvConst.FUNCT3_ECALL_EBREAK_MRET_etc shl 12) or (UInt32.ZERO shl 7) or RvConst.OPC_SYSTEM
                SRET -> (RvConst.FUNCT7_SRET shl 25) or (0b00010.toUInt32() shl 20) or (UInt32.ZERO shl 15) or (RvConst.FUNCT3_ECALL_EBREAK_MRET_etc shl 12) or (UInt32.ZERO shl 7) or RvConst.OPC_SYSTEM
                MRET -> (RvConst.FUNCT7_MRET shl 25) or (0b00010.toUInt32() shl 20) or (UInt32.ZERO shl 15) or (RvConst.FUNCT3_ECALL_EBREAK_MRET_etc shl 12) or (UInt32.ZERO shl 7) or RvConst.OPC_SYSTEM
                WFI -> (RvConst.FUNCT7_WFI shl 25) or (0b00101.toUInt32() shl 20) or (UInt32.ZERO shl 15) or (RvConst.FUNCT3_ECALL_EBREAK_MRET_etc shl 12) or (UInt32.ZERO shl 7) or RvConst.OPC_SYSTEM
                SFENCE_VMA -> {
                    val rs1 = regs.getOrNull(0) ?: UInt32.ZERO // Default to x0 if optional operand missing
                    val rs2 = regs.getOrNull(1) ?: UInt32.ZERO // Default to x0 if optional operand missing
                    (RvConst.FUNCT7_SFENCE_VMA shl 25) or (rs2 shl 20) or (rs1 shl 15) or (RvConst.FUNCT3_ECALL_EBREAK_MRET_etc shl 12) or (UInt32.ZERO shl 7) or RvConst.OPC_SYSTEM
                }

                // Should have been deferred if they reached here
                JAL, BEQ, BNE, BLT, BGE, BLTU, BGEU -> throw Exception("Internal error: Label-dependent instruction ${this@BaseT.keyWord} not deferred in pass 1")

            } // End when(this)

            context.section.content.put(binary)
        }

        override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
            // ... (Pass 2 implementation using RvConst and IntNumber checks as before) ...
            // It retrieves regs and evaluates exprValues locally within pass 2 context
            var binary: UInt32 = UInt32.ZERO
            val instructionAddress = context.currentAddress // This is IntNumber<*> usually
            context.spec as? RvSpec ?: throw Exception("Internal error: RISC-V backend requires RvSpec")

            try {
                val regs = instr.regs.map { it.type.address.toUInt32() }
                // Evaluate expressions - label references MUST be resolved here
                val absExprs = instr.exprs.map {
                    absIntEvaluator.evaluate(it, context) // Pass 2 context resolves labels
                }

                val relExprs = instr.exprs.map {
                    relIntEvaluator.evaluate(it, context)
                }

                binary = when (this@BaseT) {
                    // U-Type (only if deferred due to label in expression, e.g. %pcrel_hi)
                    LUI -> {
                        val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                        val immVal = absExprs.getOrNull(0) ?: throw Exception("Missing immediate for LUI")
                        // WARNING: Assumes evaluator handled %hi correctly or it's simple. Relocations are better.
                        val imm = immVal.toInt32().toUInt32()
                        RvConst.packImmU(imm) or (rd shl 7) or RvConst.OPC_LUI
                    }

                    AUIPC -> { // Handles deferred case (label in expression)
                        val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                        val offset = relExprs.getOrNull(0) ?: throw Exception("Missing immediate/label for AUIPC")
                        // Calculate PC-relative offset
                        // Need upper 20 bits of offset, potentially adjusted.
                        // WARNING: Correct handling involves R_RISCV_PCREL_HI20 relocation.
                        // Simple approximation (may fail edge cases handled by linker):
                        val offsetLo12 = offset.toInt32().toUInt32() and 0xFFFu.toUInt32()
                        val imm = if ((offsetLo12 and 0x800u.toUInt32()) != UInt32.ZERO) { // sign bit of lower 12
                            (offset.toInt32().toUInt32() + 0x1000u.toUInt32()) and 0xFFFFF000u.toUInt32()
                        } else {
                            offset.toInt32().toUInt32() and 0xFFFFF000u.toUInt32()
                        }
                        RvConst.packImmU(imm) or (rd shl 7) or RvConst.OPC_AUIPC
                    }

                    // J-Type
                    JAL -> {
                        val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 for $keyWord")
                        instr.exprs.getOrNull(0) ?: throw Exception("Missing label expression for JAL") // Get original expr for range
                        val relativeOffset = relExprs[0] // Already evaluated IntNumber
                        if (!relativeOffset.fitsInSigned(21)) {
                            throw Exception("Jump target out of range for JAL (21-bit signed offset: $relativeOffset)")
                        }

                        if (relativeOffset % 2 != BigInt.ZERO) {
                            throw Exception("JAL target offset must be 2-byte aligned ($relativeOffset)")
                        }
                        val imm = relativeOffset.toInt32().toUInt32()
                        RvConst.packImmJ(imm) or (rd shl 7) or RvConst.OPC_JAL
                    }

                    // B-Type (Branches)
                    BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                        val rs1 = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 (rs1) for $keyWord")
                        val rs2 = regs.getOrNull(1) ?: throw Exception("Missing required register operand 1 (rs2) for $keyWord")
                        instr.exprs.getOrNull(0) ?: throw Exception("Missing label expression for $keyWord") // Get original expr for range
                        val relativeOffset = relExprs[0] // Already evaluated IntNumber
                        if (!relativeOffset.fitsInSigned(13)) {
                            throw Exception("Branch target out of range for $keyWord (13-bit signed offset: $relativeOffset)")
                        }

                        if (relativeOffset % 2 != BigInt.ZERO) {
                            throw Exception("$keyWord target offset must be 2-byte aligned ($relativeOffset)")
                        }
                        val imm = relativeOffset.toInt32().toUInt32()
                        val funct3 = when (this@BaseT) {
                            BEQ -> RvConst.FUNCT3_CBRA_BEQ
                            BNE -> RvConst.FUNCT3_CBRA_BNE
                            BLT -> RvConst.FUNCT3_CBRA_BLT
                            BGE -> RvConst.FUNCT3_CBRA_BGE
                            BLTU -> RvConst.FUNCT3_CBRA_BLTU
                            BGEU -> RvConst.FUNCT3_CBRA_BGEU
                            else -> throw Exception("Internal error: Unexpected Branch instruction")
                        }
                        RvConst.packImmB(imm) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or RvConst.OPC_BRANCH
                    }

                    // I-Type (JALR - only if deferred due to label in immediate)
                    JALR -> { // Handles deferred case (label in immediate)
                        val rd = regs.getOrNull(0) ?: throw Exception("Missing required register operand 0 (rd) for $keyWord")
                        val rs1 = regs.getOrNull(1) ?: throw Exception("Missing required register operand 1 (rs1) for $keyWord")
                        val immVal = absExprs.getOrNull(0) ?: throw Exception("Missing immediate offset for JALR")
                        // WARNING: Assumes evaluator handled %pcrel_lo or similar. Relocations are better.
                        if (!immVal.fitsInSigned(12)) {
                            throw Exception("Immediate offset $immVal does not fit in 12 signed bits for JALR")
                        }
                        val imm = immVal.toInt32().toUInt32()
                        RvConst.packImmI12(imm) or (rs1 shl 15) or (rd shl 7) or RvConst.OPC_JALR
                    }

                    // Others should have been handled in Pass 1
                    else -> throw Exception("Internal error: Instruction ${this@BaseT.keyWord} should not require pass 2 binary generation")
                } // End when(this)

            } catch (ae: Exception) {
                instr.addError(ae.message ?: "Unknown error")
            }

            // Write the final binary to the reserved location (using Int address)
            context.section.content[context.offsetInSection] = binary
        }
    }

    /** RV64I Base Instructions */
    enum class I64T(override val paramT: RvParamT) : RvInstrT {
        ADDIW(RD_RS1_IMM12),
        SLLIW(RD_RS1_SHAMT), // shamt[4:0] used
        SRLIW(RD_RS1_SHAMT), // shamt[4:0] used
        SRAIW(RD_RS1_SHAMT), // shamt[4:0] used
        ADDW(RD_RS1_RS2),
        SUBW(RD_RS1_RS2),
        SLLW(RD_RS1_RS2),
        SRLW(RD_RS1_RS2),
        SRAW(RD_RS1_RS2),
        LWU(RD_IMM12_RS1),
        LD(RD_IMM12_RS1),
        SD(RS2_IMM12_RS1),
        ;

        override val keyWord: String = name.lowercase()

        override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {

            if (context.spec != Rv64Spec) throw Exception("Internal error: I64 extension does need $Rv64Spec as context.")

            val exprs = instr.exprs.map { absIntEvaluator.evaluate(it, context) } // Evaluate expressions
            val regs = instr.regs.map { it.type.address.toUInt32() } // Get register numbers

            var binary: UInt32 = UInt32.ZERO
            try {

                binary = when (this@I64T) {
                    // --- I-Type Word Immediate ---
                    ADDIW -> {
                        val rd = regs[0]
                        val rs1 = regs[1]
                        val immVal = exprs[0]
                        if (!immVal.fitsInSigned(12)) instr.addError("Immediate $immVal out of 12-bit signed range for $keyWord")
                        val imm = immVal.toInt32().toUInt32()
                        // ADDIW: funct3=000, opcode=OPC_ARITH_IMM_WORD
                        val binary = RvConst.packImmI12(imm) or (rs1 shl 15) or (RvConst.FUNCT3_ADDI_ADD_SUB shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM_WORD
                        SysOut.log( "ADDIW: " + binary.toString(2))
                        binary
                    }

                    SLLIW, SRLIW, SRAIW -> {
                        val rd = regs[0]
                        val rs1 = regs[1]
                        val shamtVal = exprs[0]
                        // Word shifts only use lower 5 bits of shamt
                        if (!shamtVal.fitsInUnsigned(5)) instr.addError("Shift amount $shamtVal out of 5-bit unsigned range for $keyWord")
                        val shamt = shamtVal.toUInt32() and 0x1Fu.toUInt32()
                        val funct3 = when (this@I64T) {
                            SLLIW -> RvConst.FUNCT3_SLLI_SLL
                            SRLIW, SRAIW -> RvConst.FUNCT3_SRLI_SRL_SRAI_SRA
                            else -> UInt32.ZERO
                        }
                        // funct7 distinguishes SRAIW(0100000) from SRLIW(0000000). SLLIW is 0000000.
                        // Combine funct7 and shamt[4:0] into imm[11:0] field (imm[11:5] is funct7, imm[4:0] is shamt)
                        val funct7 = if (this@I64T == SRAIW) RvConst.FUNCT7_SUB_SRA else RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND
                        val immField = (funct7 shl 5) or shamt
                        (immField shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM_WORD
                    }

                    // --- R-Type Word ---
                    ADDW, SUBW, SLLW, SRLW, SRAW -> {
                        val rd = regs[0]
                        val rs1 = regs[1]
                        val rs2 = regs[2]
                        val funct3 = when (this@I64T) {
                            ADDW, SUBW -> RvConst.FUNCT3_ADDI_ADD_SUB
                            SLLW -> RvConst.FUNCT3_SLLI_SLL
                            SRLW, SRAW -> RvConst.FUNCT3_SRLI_SRL_SRAI_SRA
                            else -> UInt32.ZERO
                        }
                        val funct7 = when (this@I64T) {
                            SUBW, SRAW -> RvConst.FUNCT7_SUB_SRA // 0100000
                            else -> RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND // 0000000
                        }
                        (funct7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_ARITH_WORD
                    }

                    // --- Load/Store 64-bit ---
                    LWU, LD -> { // Load Word Unsigned, Load Doubleword
                        val rd = regs[0]
                        val immVal = exprs[0]
                        val rs1 = regs[1]
                        if (!immVal.fitsInSigned(12)) instr.addError("Immediate offset $immVal out of 12-bit signed range for $keyWord")
                        val imm = immVal.toInt32().toUInt32()
                        val funct3 = when (this@I64T) {
                            LWU -> RvConst.FUNCT3_LOAD_WU
                            LD -> RvConst.FUNCT3_LOAD_D
                            else -> UInt32.ZERO
                        }
                        RvConst.packImmI12(imm) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_LOAD
                    }

                    SD -> { // Store Doubleword
                        val rs2 = regs[0]
                        val immVal = exprs[0]
                        val rs1 = regs[1]
                        if (!immVal.fitsInSigned(12)) instr.addError("Immediate offset $immVal out of 12-bit signed range for $keyWord")
                        val imm = immVal.toInt32().toUInt32()
                        // SD: funct3=011, opcode=OPC_STORE
                        RvConst.packImmS(imm) or (rs2 shl 20) or (rs1 shl 15) or (RvConst.FUNCT3_STORE_D shl 12) or RvConst.OPC_STORE
                    }
                } // end when

            } catch (ae: EvaluationException) {
                instr.addError("Pass 1 Error: Could not evaluate expression: ${ae.message}")
            }

            context.section.content.put(binary)
        }

        override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
            // These instructions don't depend on labels
            instr.addError("Internal error: Instruction ${this@I64T.keyWord} unexpectedly required pass 2 binary generation (check for label usage in immediate)")
        }
    }

    /** M Standard Extension Instructions (RV32M/RV64M) */
    enum class MBaseT(override val paramT: RvParamT) : RvInstrT {
        MUL(RD_RS1_RS2),
        MULH(RD_RS1_RS2),
        MULHSU(RD_RS1_RS2),
        MULHU(RD_RS1_RS2),
        DIV(RD_RS1_RS2),
        DIVU(RD_RS1_RS2),
        REM(RD_RS1_RS2),
        REMU(RD_RS1_RS2),
        ;

        override val keyWord: String = name.lowercase()

        override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
            val regs = instr.regs.map { it.type.address.toUInt32() }
            var binary: UInt32 = UInt32.ZERO
            try {
                val rd = regs[0]
                val rs1 = regs[1]
                val rs2 = regs[2]

                // All are R-Type, use OPC_ARITH, funct7 = 0000001 (M extension marker)
                val funct7 = RvConst.FUNCT7_M // Standard M Extension R-type
                val funct3 = when (this@MBaseT) {
                    MUL -> RvConst.FUNCT3_M_MUL; MULH -> RvConst.FUNCT3_M_MULH; MULHSU -> RvConst.FUNCT3_M_MULHSU; MULHU -> RvConst.FUNCT3_M_MULHU
                    DIV -> RvConst.FUNCT3_M_DIV; DIVU -> RvConst.FUNCT3_M_DIVU; REM -> RvConst.FUNCT3_M_REM; REMU -> RvConst.FUNCT3_M_REMU
                }
                binary = (funct7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_ARITH

            } catch (ae: Exception) {
                instr.addError("Pass 1 Error: Could not evaluate expression: ${ae.message}") // Should not happen for R-type
            }
            context.section.content[context.offsetInSection] = binary

        }

        override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
            instr.addError("Internal error: Instruction ${this@MBaseT.keyWord} unexpectedly required pass 2 binary generation (check for label usage in immediate)")
        }
    }

    /** M Standard Extension Instructions (RV64M Only) */
    enum class M64T(override val paramT: RvParamT) : RvInstrT {
        MULW(RD_RS1_RS2),
        DIVW(RD_RS1_RS2),
        DIVUW(RD_RS1_RS2),
        REMW(RD_RS1_RS2),
        REMUW(RD_RS1_RS2),
        ;

        override val keyWord: String = name.lowercase()

        override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
            // Get register numbers
            val regs = instr.regs.map { it.type.address.toUInt32() }
            var binary: UInt32 = UInt32.ZERO
            try {

                val rd = regs[0]
                val rs1 = regs[1]
                val rs2 = regs[2]

                // All are R-Type, use OPC_ARITH_WORD, funct7 = 0000001 (M extension marker)
                val funct7 = RvConst.FUNCT7_M // Standard M Extension R-type
                val funct3 = when (this@M64T) {
                    MULW -> RvConst.FUNCT3_M_MUL; DIVW -> RvConst.FUNCT3_M_DIV; DIVUW -> RvConst.FUNCT3_M_DIVU
                    REMW -> RvConst.FUNCT3_M_REM; REMUW -> RvConst.FUNCT3_M_REMU
                }
                binary = (funct7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or RvConst.OPC_ARITH_WORD

            } catch (ae: Exception) {
                instr.addError("Pass 1 Error: Could not evaluate expression: ${ae.message}")
            }
            context.section.content[context.offsetInSection] = binary
        }

        override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
            instr.addError("Internal error: Instruction ${this@M64T.keyWord} unexpectedly required pass 2 binary generation")
        }
    }

    // --- RISC-V Pseudo-Instructions ---
    // These are convenient instructions provided by the assembler that typically
    // expand into one or more base instructions. Representing them can be useful
    // for the parser/PSI layer, focusing on their common *syntax*.

    /** Generic Pseudo-Instruction Type */
    sealed interface PseudoT : RvInstrT {

        // Helper to generate ADDI
        fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateAddi(rd: UInt32, rs1: UInt32, imm: UInt32, instr: AsmInstruction? = null): UInt32 {
            // Basic range check (can be more sophisticated if needed)
            if (!imm.fitsInSignedOrUnsigned(12)) instr?.addError("Internal Error: ADDI immediate ($imm) out of range during pseudo-op expansion")
            return RvConst.packImmI12(imm) or (rs1 shl 15) or (RvConst.FUNCT3_ADDI_ADD_SUB shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM
        }

        // Helper to generate LUI
        fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateLui(rd: UInt32, imm: UInt32, instr: AsmInstruction? = null): UInt32 {
            // Imm is expected to be the upper 20 bits already
            return RvConst.packImmU(imm) or (rd shl 7) or RvConst.OPC_LUI
        }

        // Helper to generate ADDIW (RV64)
        fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateAddiw(rd: UInt32, rs1: UInt32, imm: UInt32, instr: AsmInstruction? = null): UInt32 {
            if (!imm.fitsInSignedOrUnsigned(12)) instr?.addError("Internal Error: ADDIW immediate ($imm) out of range during pseudo-op expansion")
            val binary = RvConst.packImmI12(imm) or (rs1 shl 15) or (RvConst.FUNCT3_ADDI_ADD_SUB shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM_WORD
            SysOut.log("Li addiw: ${binary.toString(2)}")
            return binary
        }

        // Helper to generate SLLI
        fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateSlli(rd: UInt32, rs1: UInt32, shamtVal: UInt32, xlen: RvSpec.XLEN, instr: AsmInstruction? = null): UInt32 {
            val shamtBits = if (xlen == RvSpec.XLEN.X64) 6 else 5
            val shamtMask = (1u shl shamtBits) - 1u
            if (!shamtVal.toBigInt().fitsInUnsigned(shamtBits)) instr?.addError("Internal Error: SLLI shift amount ($shamtVal) out of range during pseudo-op expansion")
            val shamt = shamtVal and shamtMask.toUInt32()
            val immField = if (xlen == RvSpec.XLEN.X64) {
                shamt // RV64: funct7=0, shamt is imm[5:0]
            } else {
                shamt // RV32: funct7=0, imm[11:5]=0, shamt is imm[4:0]
            }
            return (immField shl 20) or (rs1 shl 15) or (RvConst.FUNCT3_SLLI_SLL shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM
        }

        // Helper to generate JAL
        fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateJal(rd: UInt32, imm: UInt32, instr: AsmInstruction? = null): UInt32 {
            // Imm is expected to be the packed J-type immediate
            return imm or (rd shl 7) or RvConst.OPC_JAL
        }

        // Helper to generate JALR
        fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateJalr(rd: UInt32, rs1: UInt32, imm: UInt32, instr: AsmInstruction? = null): UInt32 {
            if (!imm.fitsInSignedOrUnsigned(12)) instr?.addError("Internal Error: JALR immediate ($imm) out of range during pseudo-op expansion")
            return RvConst.packImmI12(imm) or (rs1 shl 15) or (rd shl 7) or RvConst.OPC_JALR
        }

        // Helper to generate AUIPC
        fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateAuipc(rd: UInt32, imm: UInt32, instr: AsmInstruction? = null): UInt32 {
            // Imm is expected to be the packed U-type immediate (adjusted hi20)
            return RvConst.packImmU(imm) or (rd shl 7) or RvConst.OPC_AUIPC
        }

        // Helper to generate Branch
        fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateBranch(rs1: UInt32, rs2: UInt32, imm: UInt32, funct3: UInt32, instr: AsmInstruction? = null): UInt32 {
            // Imm is expected to be the packed B-type immediate
            return imm or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or RvConst.OPC_BRANCH
        }

        enum class BaseT(override val paramT: RvParamT) : PseudoT {
            // Common Pseudo Ops
            NOP(PS_NONE),          // -> addi x0, x0, 0
            LI(PS_RD_IMM),         // -> lui+addi or just addi
            LA(PS_RD_SYMBOL),      // -> auipc+addi (or variant for position independence)
            MV(PS_RD_RS1),         // -> addi rd, rs, 0
            NOT(PS_RD_RS1),        // -> xori rd, rs, -1
            NEG(PS_RD_RS1),        // -> sub rd, x0, rs
            NEGW(PS_RD_RS1),       // -> subw rd, x0, rs (RV64)
            SEQZ(PS_RD_RS1),       // -> sltiu rd, rs, 1
            SNEZ(PS_RD_RS1),       // -> sltu rd, x0, rs
            SLTZ(PS_RD_RS1),       // -> slt rd, rs, x0
            SGTZ(PS_RD_RS1),       // -> slt rd, x0, rs

            // Pseudo Branches (Zero Comparison)
            BEQZ(PS_RS1_LABEL),    // -> beq rs, x0, label
            BNEZ(PS_RS1_LABEL),    // -> bne rs, x0, label
            BLEZ(PS_RS1_LABEL),    // -> bge x0, rs, label
            BGEZ(PS_RS1_LABEL),    // -> blt x0, rs, label
            BLTZ(PS_RS1_LABEL),    // -> blt rs, x0, label
            BGTZ(PS_RS1_LABEL),    // -> bge rs, x0, label (or slt+bne)

            // Pseudo Branches (Two Reg Comparison, Reversed Operands)
            BGT(RS1_RS2_LABEL),    // -> blt rs2, rs1, label
            BLE(RS1_RS2_LABEL),    // -> bge rs2, rs1, label
            BGTU(RS1_RS2_LABEL),   // -> bltu rs2, rs1, label
            BLEU(RS1_RS2_LABEL),   // -> bgeu rs2, rs1, label

            // Pseudo Jumps/Calls/Returns
            J(LABEL),              // -> jal x0, label
            JAL(LABEL),            // -> jal x1, label (Syntactic sugar for common case)
            JR(PS_RS1),            // -> jalr x0, 0(rs1)
            JALR(PS_RS1),          // -> jalr x1, 0(rs1) (Syntactic sugar for common case)
            RET(PS_NONE),          // -> jalr x0, 0(x1)
            CALL(LABEL),           // -> auipc+jalr (common expansion for far calls) - offset(reg) form also exists
            TAIL(LABEL),           // -> auipc+jalr or just jal (common expansions) - offset(reg) form also exists

            // Pseudo CSR Access
            CSRR(RD_CSR),          // -> csrrs rd, csr, x0
            CSRW(CSR_RS1),         // -> csrrw x0, csr, rs1
            CSRS(CSR_RS1),         // -> csrrs x0, csr, rs1
            CSRC(CSR_RS1),         // -> csrrc x0, csr, rs1

            // CSRWI, CSRSI, CSRCI also exist using UIMM
            CSRWI(RD_CSR_UIMM5),   // -> csrrwi x0, csr, uimm
            CSRSI(RD_CSR_UIMM5),   // -> csrrsi x0, csr, uimm
            CSRCI(RD_CSR_UIMM5),   // -> csrrci x0, csr, uimm
            ;

            override val keyWord: String = name.lowercase()

            // Determine required size for deferral in Pass 1
            // Returns null if not deferred or size cannot be determined in pass 1
            fun getDeferredSize(): Int? {
                return when (this@BaseT) {
                    // Label dependent branches/jumps
                    BEQZ, BNEZ, BLEZ, BGEZ, BLTZ, BGTZ -> 4
                    BGT, BLE, BGTU, BLEU -> 4
                    J, JAL -> 4
                    // PC-relative addressing
                    LA -> 8 // auipc + addi
                    CALL, TAIL -> 8 // auipc + jalr (common case)
                    // Others are expanded in Pass 1 or fixed size
                    else -> null
                }
            }

            override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
                val spec = context.spec as? RvSpec ?: throw Exception("Internal Error: RvSpec required for getDeferredSize")

                val deferredSize = getDeferredSize()
                if (deferredSize != null) {
                    context.section.queueLateInit(instr, deferredSize)
                    return
                }

                val exprs = instr.exprs.map { absIntEvaluator.evaluate(it, context) } // Evaluate expressions
                val regs = instr.regs.map { it.type.address.toUInt32() } // Get register numbers

                // Handle non-deferred pseudo-instructions by expanding them now
                try {

                    when (this@BaseT) {
                        NOP -> context.section.content.put(generateAddi(RvRegT.IntT.ZERO.uint32, RvRegT.IntT.ZERO.uint32, UInt32.ZERO)) // addi x0, x0, 0
                        MV -> { // addi rd, rs, 0
                            val rd = regs[0]
                            val rs1 = regs[1]
                            context.section.content.put(generateAddi(rd, rs1, UInt32.ZERO))
                        }

                        NOT -> { // xori rd, rs, -1
                            val rd = regs[0]
                            val rs1 = regs[1]
                            val imm = (-1).toUInt32() // Gets sign extended by packImmI
                            val binary = RvConst.packImmI12(imm) or (rs1 shl 15) or (RvConst.FUNCT3_XORI_XOR shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM
                            context.section.content.put(binary)
                        }

                        NEG -> { // sub rd, x0, rs
                            val rd = regs[0]
                            val rs1 = regs[1] // Note: rs1 is operand, becomes rs2 in SUB
                            val binary = (RvConst.FUNCT7_SUB_SRA shl 25) or (rs1 shl 20) or (RvRegT.IntT.ZERO.uint32 shl 15) or (RvConst.FUNCT3_ADDI_ADD_SUB shl 12) or (rd shl 7) or RvConst.OPC_ARITH
                            context.section.content.put(binary)
                        }

                        NEGW -> { // subw rd, x0, rs (RV64 only)
                            if (spec.xlen != RvSpec.XLEN.X64) instr.addError("$keyWord requires RV64")
                            val rd = regs[0]
                            val rs1 = regs[1]
                            val binary = (RvConst.FUNCT7_SUB_SRA shl 25) or (rs1 shl 20) or (RvRegT.IntT.ZERO.uint32 shl 15) or (RvConst.FUNCT3_ADDI_ADD_SUB shl 12) or (rd shl 7) or RvConst.OPC_ARITH_WORD
                            context.section.content.put(binary)
                        }

                        SEQZ -> { // sltiu rd, rs, 1
                            val rd = regs[0]
                            val rs1 = regs[1]
                            val binary = RvConst.packImmI12(1.toUInt32()) or (rs1 shl 15) or (RvConst.FUNCT3_SLTIU_SLTU shl 12) or (rd shl 7) or RvConst.OPC_ARITH_IMM
                            context.section.content.put(binary)
                        }

                        SNEZ -> { // sltu rd, x0, rs
                            val rd = regs[0]
                            val rs1 = regs[1] // rs1 is operand, becomes rs2 in SLTU
                            val binary = (RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND shl 25) or (rs1 shl 20) or (RvRegT.IntT.ZERO.uint32 shl 15) or (RvConst.FUNCT3_SLTIU_SLTU shl 12) or (rd shl 7) or RvConst.OPC_ARITH
                            context.section.content.put(binary)
                        }

                        SLTZ -> { // slt rd, rs, x0
                            val rd = regs[0]
                            val rs1 = regs[1]
                            val binary = (RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND shl 25) or (RvRegT.IntT.ZERO.uint32 shl 20) or (rs1 shl 15) or (RvConst.FUNCT3_SLTI_SLT shl 12) or (rd shl 7) or RvConst.OPC_ARITH
                            context.section.content.put(binary)
                        }

                        SGTZ -> { // slt rd, x0, rs
                            val rd = regs[0]
                            val rs1 = regs[1] // rs1 is operand, becomes rs2 in SLT
                            val binary = (RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND shl 25) or (rs1 shl 20) or (RvRegT.IntT.ZERO.uint32 shl 15) or (RvConst.FUNCT3_SLTI_SLT shl 12) or (rd shl 7) or RvConst.OPC_ARITH
                            context.section.content.put(binary)
                        }

                        JR -> { // jalr x0, 0(rs1)
                            val rs1 = regs[0]
                            context.section.content.put(generateJalr(RvRegT.IntT.ZERO.uint32, rs1, UInt32.ZERO))
                        }

                        JALR -> { // jalr x1, 0(rs1)
                            val rs1 = regs[0]
                            context.section.content.put(generateJalr(RvRegT.IntT.RA.uint32, rs1, UInt32.ZERO))
                        }

                        RET -> { // jalr x0, 0(x1)
                            context.section.content.put(generateJalr(RvRegT.IntT.ZERO.uint32, RvRegT.IntT.RA.uint32, UInt32.ZERO))
                        }

                        // Pseudo CSR Access (expand to base CSR using x0)
                        CSRR -> { // csrrs rd, csr, x0
                            val rd = regs[0]
                            val csrVal = regs[1]
                            if (!csrVal.fitsInUnsigned(12)) instr.addError("CSR address $csrVal out of 12-bit range")
                            val csr = csrVal and 0xFFFu.toUInt32()
                            val binary = (csr shl 20) or (RvRegT.IntT.ZERO.uint32 shl 15) or (RvConst.FUNCT3_CSR_RS shl 12) or (rd shl 7) or RvConst.OPC_SYSTEM
                            context.section.content.put(binary)
                        }

                        CSRW -> { // csrrw x0, csr, rs1
                            val csrVal = regs[0]
                            val rs1 = regs[1]
                            if (!csrVal.fitsInUnsigned(12)) instr.addError("CSR address $csrVal out of 12-bit range")
                            val csr = csrVal and 0xFFFu.toUInt32()
                            val binary = (csr shl 20) or (rs1 shl 15) or (RvConst.FUNCT3_CSR_RW shl 12) or (RvRegT.IntT.ZERO.uint32 shl 7) or RvConst.OPC_SYSTEM
                            context.section.content.put(binary)
                        }

                        CSRS -> { // csrrs x0, csr, rs1
                            val csrVal = regs[0]
                            val rs1 = regs[1]
                            if (!csrVal.fitsInUnsigned(12)) instr.addError("CSR address $csrVal out of 12-bit range")
                            val csr = csrVal and 0xFFFu.toUInt32()
                            val binary = (csr shl 20) or (rs1 shl 15) or (RvConst.FUNCT3_CSR_RS shl 12) or (RvRegT.IntT.ZERO.uint32 shl 7) or RvConst.OPC_SYSTEM
                            context.section.content.put(binary)
                        }

                        CSRC -> { // csrrc x0, csr, rs1
                            val csrVal = regs[0]
                            val rs1 = regs[1]
                            if (!csrVal.fitsInUnsigned(12)) instr.addError("CSR address $csrVal out of 12-bit range")
                            val csr = csrVal and 0xFFFu.toUInt32()
                            val binary = (csr shl 20) or (rs1 shl 15) or (RvConst.FUNCT3_CSR_RC shl 12) or (RvRegT.IntT.ZERO.uint32 shl 7) or RvConst.OPC_SYSTEM
                            context.section.content.put(binary)
                        }
                        // Note: Parameter types changed to CSR_UIMM5 for these
                        CSRWI -> { // csrrwi x0, csr, uimm5
                            val csrVal = regs[0]
                            val uimmVal = exprs[0]
                            if (!csrVal.fitsInUnsigned(12)) instr.addError("CSR address $csrVal out of 12-bit range")
                            if (!uimmVal.fitsInUnsigned(5)) instr.addError("Immediate $uimmVal out of 5-bit range")
                            val csr = csrVal and 0xFFFu.toUInt32()
                            val uimm = uimmVal.toUInt32() and 0x1Fu.toUInt32()
                            val binary = (csr shl 20) or (uimm shl 15) or (RvConst.FUNCT3_CSR_RWI shl 12) or (RvRegT.IntT.ZERO.uint32 shl 7) or RvConst.OPC_SYSTEM
                            context.section.content.put(binary)
                        }

                        CSRSI -> { // csrrsi x0, csr, uimm5
                            val csrVal = regs[0]
                            val uimmVal = exprs[0]
                            if (!csrVal.fitsInUnsigned(12)) instr.addError("CSR address $csrVal out of 12-bit range")
                            if (!uimmVal.fitsInUnsigned(5)) instr.addError("Immediate $uimmVal out of 5-bit range")
                            val csr = csrVal and 0xFFFu.toUInt32()
                            val uimm = uimmVal.toUInt32() and 0x1Fu.toUInt32()
                            val binary = (csr shl 20) or (uimm shl 15) or (RvConst.FUNCT3_CSR_RSI shl 12) or (RvRegT.IntT.ZERO.uint32 shl 7) or RvConst.OPC_SYSTEM
                            context.section.content.put(binary)
                        }

                        CSRCI -> { // csrrci x0, csr, uimm5
                            val csrVal = regs[0]
                            val uimmVal = exprs[0]
                            if (!csrVal.fitsInUnsigned(12)) instr.addError("CSR address $csrVal out of 12-bit range")
                            if (!uimmVal.fitsInUnsigned(5)) instr.addError("Immediate $uimmVal out of 5-bit range")
                            val csr = csrVal and 0xFFFu.toUInt32()
                            val uimm = uimmVal.toUInt32() and 0x1Fu.toUInt32()
                            val binary = (csr shl 20) or (uimm shl 15) or (RvConst.FUNCT3_CSR_RCI shl 12) or (RvRegT.IntT.ZERO.uint32 shl 7) or RvConst.OPC_SYSTEM
                            context.section.content.put(binary)
                        }

                        // --- LI - Load Immediate ---
                        LI -> {
                            val rd = regs[0]
                            val imm = exprs[0]
                            val writer: (UInt32) -> Unit = { context.section.content.put(it) }

                            if ((context.spec as? RvSpec)?.xlen == RvSpec.XLEN.X32) {
                                generateRv32LiSequence(imm, rd, writer, instr)
                            } else { // XLEN = 64
                                generateRv64LiSequence(imm, rd, writer, instr)
                            }
                        }

                        // Should have been deferred
                        LA, CALL, TAIL, J, JAL,
                        BEQZ, BNEZ, BLEZ, BGEZ, BLTZ, BGTZ,
                        BGT, BLE, BGTU, BLEU,
                            -> throw IllegalStateException("Internal error: Pseudo-instruction ${this@BaseT.keyWord} should have been deferred in pass 1")
                    } // end when

                } catch (e: Exception) {
                    instr.addError("Pass 1 Error during $keyWord expansion: ${e.message}")
                    val size = getDeferredSize() ?: 4 // Estimate size if possible
                    context.section.queueLateInit(instr, size)
                    return
                }
            } // End Pass 1

            // --- LI Sequence Generation Helpers ---

            private fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateRv32LiSequence(
                imm: IntNumber<*>, rd: UInt32, writer: (UInt32) -> Unit, instr: AsmInstruction? = null,
            ) {
                if (!imm.fitsInSignedOrUnsigned(32)) {
                    instr?.addError("Immediate $imm exceeds 32 bits for RV32 LI")
                    return
                }

                if (imm.fitsInSigned(12)) {
                    // Single ADDI rd, x0, imm
                    writer(generateAddi(rd, RvRegT.IntT.ZERO.uint32, imm.toInt32().toUInt32(), instr))
                } else {
                    // LUI + ADDI
                    val imm32 = imm.toInt32().toUInt32()
                    val (hi20, lo12) = RvConst.splitImm32(imm32)
                    writer(generateLui(rd, hi20, instr))
                    if (lo12 != UInt32.ZERO) {
                        writer(generateAddi(rd, rd, lo12, instr))
                    }
                }
            }

            // Adapted from the provided RV64InstrType example
            private fun <T : AsmCodeGenerator.Section> AsmBackend<T>.generateRv64LiSequence(
                imm: IntNumber<*>, rd: UInt32, writer: (UInt32) -> Unit, instr: AsmInstruction? = null,
            ) {
                if (!imm.fitsInSignedOrUnsigned(64)) {
                    instr?.addError("Immediate $imm exceeds 64 bits for RV64 LI")
                    return
                }

                when {
                    // Optimal case: ADDI rd, x0, imm12
                    imm.fitsInSigned(12) -> {
                        writer(generateAddi(rd, RvRegT.IntT.ZERO.uint32, imm.toInt32().toUInt32(), instr))
                    }

                    // Next best: LUI + ADDI rd, rd, imm12
                    imm.fitsInSigned(32) -> {
                        val imm32 = imm.toInt32().toUInt32()
                        val (hi20, lo12) = RvConst.splitImm32(imm32)
                        writer(generateLui(rd, hi20, instr))
                        if (lo12 != UInt32.ZERO || hi20 == UInt32.ZERO) { // Addi needed if low part non-zero OR high part was zero (edge case for small numbers not fitting in 12 bits)
                            writer(generateAddi(rd, rd, lo12, instr))
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

                        // Build LUI Bundle
                        writer(generateLui(rd, l3, instr))

                        // Build ADDIW Bundle
                        if (l2 != UInt32.ZERO) {
                            writer(generateAddiw(rd, rd, l2, instr))
                        }

                        // Build SLLI Bundle
                        writer(generateSlli(rd, rd, 12U.toUInt32(), RvSpec.XLEN.X64, instr))

                        if (l1 != UInt32.ZERO) {
                            writer(generateAddi(rd, rd, l1, instr))
                        }
                    }

                    imm.fitsInSigned(56) -> {
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
                        val l3 = resized.shr(12 + 12).lowest(12).toUInt32() + l2.bit(11)
                        val l4 = resized.shr(12 + 12 + 12).lowest(20).toUInt32() + l3.bit(11)

                        var shiftNeeded = UInt32.ZERO

                        writer(generateLui(rd, l4, instr))

                        if (l3 != UInt32.ZERO) {
                            writer(generateAddiw(rd, rd, l3, instr))
                        }

                        shiftNeeded += 12

                        if (l2 != UInt32.ZERO) {

                            writer(generateSlli(rd, rd, shiftNeeded, RvSpec.XLEN.X64, instr))

                            shiftNeeded = UInt32.ZERO

                            writer(generateAddi(rd, rd, l2, instr))
                        }

                        shiftNeeded += 12

                        writer(generateSlli(rd, rd, shiftNeeded, RvSpec.XLEN.X64, instr))

                        if (l1 != UInt32.ZERO) {
                            writer(generateAddi(rd, rd, l1, instr))
                        }
                    }

                    else -> {
                        val resized = try {
                            imm.toInt64().toUInt64()
                        } catch (e: Exception) {
                            imm.toUInt64()
                        }

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

                        writer(generateLui(rd, l5, instr))

                        var shiftNeeded = UInt32.ZERO

                        if (l4 != UInt32.ZERO) {
                            writer(generateAddiw(rd, rd, l4, instr))
                        }

                        shiftNeeded += 12

                        if (l3 != UInt32.ZERO) {
                            writer(generateSlli(rd, rd, shiftNeeded, RvSpec.XLEN.X64, instr))

                            shiftNeeded = UInt32.ZERO

                            writer(generateAddi(rd, rd, l3, instr))
                        }

                        shiftNeeded += 12
                        if (l2 != UInt32.ZERO) {
                            writer(generateSlli(rd, rd, shiftNeeded, RvSpec.XLEN.X64, instr))

                            shiftNeeded = UInt32.ZERO

                            writer(generateAddi(rd, rd, l2, instr))
                        }

                        shiftNeeded += 12

                        writer(generateSlli(rd, rd, shiftNeeded, RvSpec.XLEN.X64, instr))

                        if (l1 != UInt32.ZERO) {
                            writer(generateAddi(rd, rd, l1, instr))
                        }
                    }
                } // end when
            }


            override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
                // Handle deferred pseudo-instructions (Branches, Jumps, LA, CALL, TAIL)
                val instructionAddress = context.currentAddress
                val exprs = instr.exprs.map { absIntEvaluator.evaluate(it, context) } // Evaluate expressions
                val regs = instr.regs.map { it.type.address.toUInt32() } // Get register numbers

                try {
                    when (this@BaseT) {
                        // --- Pseudo Branches (Zero Comparison) ---
                        BEQZ -> { // beq rs, x0, label
                            val rs1 = regs[0]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(rs1, RvRegT.IntT.ZERO.uint32, imm, RvConst.FUNCT3_CBRA_BEQ, instr)
                        }

                        BNEZ -> { // bne rs, x0, label
                            val rs1 = regs[0]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(rs1, RvRegT.IntT.ZERO.uint32, imm, RvConst.FUNCT3_CBRA_BNE, instr)
                        }

                        BLEZ -> { // bge x0, rs, label
                            val rs1 = regs[0]
                            val targetAddr = exprs[0] // rs1 is operand, becomes rs2 in BGE
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(RvRegT.IntT.ZERO.uint32, rs1, imm, RvConst.FUNCT3_CBRA_BGE, instr)
                        }

                        BGEZ -> { // bge rs, x0, label
                            val rs1 = regs[0]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(rs1, RvRegT.IntT.ZERO.uint32, imm, RvConst.FUNCT3_CBRA_BGE, instr)
                        }

                        BLTZ -> { // blt rs, x0, label
                            val rs1 = regs[0]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(rs1, RvRegT.IntT.ZERO.uint32, imm, RvConst.FUNCT3_CBRA_BLT, instr)
                        }

                        BGTZ -> { // blt x0, rs, label
                            val rs1 = regs[0]
                            val targetAddr = exprs[0] // rs1 is operand, becomes rs2 in BLT
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(RvRegT.IntT.ZERO.uint32, rs1, imm, RvConst.FUNCT3_CBRA_BLT, instr)
                        }

                        // --- Pseudo Branches (Reversed Compare) ---
                        BGT -> { // blt rs2, rs1, label
                            val rs1 = regs[0]
                            val rs2 = regs[1]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(rs2, rs1, imm, RvConst.FUNCT3_CBRA_BLT, instr)
                        }

                        BLE -> { // bge rs2, rs1, label
                            val rs1 = regs[0]
                            val rs2 = regs[1]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(rs2, rs1, imm, RvConst.FUNCT3_CBRA_BGE, instr)
                        }

                        BGTU -> { // bltu rs2, rs1, label
                            val rs1 = regs[0]
                            val rs2 = regs[1]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(rs2, rs1, imm, RvConst.FUNCT3_CBRA_BLTU, instr)
                        }

                        BLEU -> { // bgeu rs2, rs1, label
                            val rs1 = regs[0]
                            val rs2 = regs[1]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(13) || offset % 2 != BigInt.ZERO) instr.addError("Branch target out of range/misaligned")
                            val imm = RvConst.packImmB(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateBranch(rs2, rs1, imm, RvConst.FUNCT3_CBRA_BGEU, instr)
                        }

                        // --- Pseudo Jumps ---
                        J -> { // jal x0, label
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(21) || offset % 2 != BigInt.ZERO) instr.addError("Jump target out of range/misaligned")
                            val imm = RvConst.packImmJ(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateJal(UInt32.ZERO, imm, instr)
                        }

                        JAL -> { // jal x1, label
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            if (!offset.fitsInSigned(21) || offset % 2 != BigInt.ZERO) instr.addError("Jump target out of range/misaligned")
                            val imm = RvConst.packImmJ(offset.toInt32().toUInt32())
                            context.section.content[context.offsetInSection] = generateJal(RvRegT.IntT.RA.uint32, imm, instr)
                        }

                        // --- PC-Relative Addressing / Calls ---
                        LA -> { // auipc rd, %pcrel_hi(lbl) + addi rd, rd, %pcrel_lo(lbl)
                            val rd = regs[0]
                            val targetAddr = exprs[0]
                            val offset = targetAddr - instructionAddress
                            val (imm_u, imm_i) = RvConst.calculateImmUPCRelAndImmI(offset) // Helper to get hi20(adj) and lo12
                            context.section.content[context.offsetInSection] = generateAuipc(rd, imm_u, instr)
                            context.section.content[context.offsetInSection + 4] = generateAddi(rd, rd, imm_i, instr)
                        }

                        CALL -> { // auipc t1, %pcrel_hi(lbl) + jalr ra, t1, %pcrel_lo(lbl)
                            val targetAddr = exprs[0]
                            val tempReg = RvRegT.IntT.T1.address.toUInt32() // Use t1 (x6) as temporary
                            val linkReg = RvRegT.IntT.RA.address.toUInt32() // Use ra (x1) for return address
                            val offset = targetAddr - instructionAddress
                            val (imm_u, imm_i) = RvConst.calculateImmUPCRelAndImmI(offset)
                            context.section.content[context.offsetInSection] = generateAuipc(tempReg, imm_u, instr)
                            context.section.content[context.offsetInSection + 4] = generateJalr(linkReg, tempReg, imm_i, instr)
                        }

                        TAIL -> { // auipc t1, %pcrel_hi(lbl) + jalr x0, t1, %pcrel_lo(lbl)
                            val targetAddr = exprs[0]
                            val tempReg = RvRegT.IntT.T1.uint32 // Use t1 (x6)
                            val linkReg = RvRegT.IntT.ZERO.uint32 // No return address saved
                            val offset = targetAddr - instructionAddress
                            val (imm_u, imm_i) = RvConst.calculateImmUPCRelAndImmI(offset)
                            context.section.content[context.offsetInSection] = generateAuipc(tempReg, imm_u, instr)
                            context.section.content[context.offsetInSection + 4] = generateJalr(linkReg, tempReg, imm_i, instr)
                        }

                        // Should have been handled in Pass 1
                        else -> {
                            instr.addError("Internal error: Pseudo-instruction ${this@BaseT.keyWord} unexpectedly required pass 2 binary generation")
                        }
                    }

                } catch (e: Exception) {
                    instr.addError("Pass 2 Error during $keyWord expansion: ${e.message}")
                }
            }


        }
    }
}