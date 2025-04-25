package cengine.lang.asm.target.riscv

import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.riscv.RvDisassembler.InstrType.*
import cengine.util.integer.FixedSizeIntNumber
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32
import cengine.util.integer.UnsignedFixedSizeIntNumber

/**
 * Disassembles RISC-V 32-bit instruction words.
 * Handles Base (RV32I/RV64I + Zifencei, Zicsr), M, and RV64-specific instructions.
 * Does *not* currently expand pseudo-instructions during disassembly.
 *
 * @param addrConstrained A function to constrain calculated addresses (e.g., based on XLEN).
 */
class RvDisassembler(private val addrConstrained: UnsignedFixedSizeIntNumber<*>.() -> UnsignedFixedSizeIntNumber<*>) : AsmDisassembler() {
    override fun disassemble(startAddr: UnsignedFixedSizeIntNumber<*>, buffer: List<FixedSizeIntNumber<*>>): List<Decoded> {
        var currIndex = 0
        var currInstr: RVInstrInfoProvider
        val decoded = mutableListOf<Decoded>()
        // Convert byte list to UInt32 words assuming little-endian
        val words = buffer.chunked(4) { bytes ->
            if (bytes.size < 4) return@chunked null // Handle incomplete last word
            // Assuming Little Endian: bytes[0] is LSB, bytes[3] is MSB
            (bytes[3].toUInt32() shl 24) or
                    (bytes[2].toUInt32() shl 16) or
                    (bytes[1].toUInt32() shl 8) or
                    bytes[0].toUInt32()
        }.filterNotNull() // Remove null entry if the buffer size wasn't multiple of 4

        while ((currIndex / 4) < words.size) {
            currInstr = try {
                RVInstrInfoProvider(words[currIndex / 4], addrConstrained)
            } catch (e: IndexOutOfBoundsException) {
                break // Should not happen with the pre-check, but defensive coding
            }

            val instr = currInstr.decode(startAddr, currIndex)
            decoded.add(instr)

            currIndex += 4 // Advance by 4 bytes for each 32-bit instruction
        }

        return decoded
    }

    /**
     * Helper class to decode a single 32-bit RISC-V instruction word.
     */
    class RVInstrInfoProvider(val binary: UInt32, private val addrConstrained: UnsignedFixedSizeIntNumber<*>.() -> UnsignedFixedSizeIntNumber<*>) : InstrProvider {

        // --- Instruction Field Extraction ---
        val opcode = binary lowest 7
        val rd = (binary shr 7) lowest 5
        val funct3 = (binary shr 12) lowest 3
        val rs1 = (binary shr 15) lowest 5
        val rs2 = (binary shr 20) lowest 5
        val funct7 = binary shr 25 lowest 7

        // --- Immediate Value Calculation ---
        // I-Type Immediate (Sign Extended)
        private val imm12iType = binary shr 20
        val iTypeImm get() = imm12iType.toUInt64().signExtend(12)

        // S-Type Immediate (Sign Extended)
        private val imm12sType = ((binary shr 25) shl 5) or rd // Reconstruct from imm[11:5] (funct7) and imm[4:0] (rd)
        val sTypeImm get() = imm12sType.toUInt64().signExtend(12)

        // B-Type Immediate (Sign Extended Offset)
        private val imm12bType = (((binary shr 31) lowest 1) shl 12) or // imm[12]
                (((binary shr 7) lowest 1) shl 11) or  // imm[11]
                (((binary shr 25) lowest 6) shl 5) or  // imm[10:5]
                (((binary shr 8) lowest 4) shl 1)      // imm[4:1]

        // Bit 0 is always 0, offset scaled by 2
        val bTypeOffset get() = imm12bType.toUInt64().signExtend(13)

        // U-Type Immediate (Raw Upper 20 bits)
        val imm20uType = binary shr 12

        // J-Type Immediate (Sign Extended Offset)
        private val imm20jType = (((binary shr 31) lowest 1) shl 20) or // imm[20]
                (((binary shr 12) lowest 8) shl 12) or // imm[19:12]
                (((binary shr 20) lowest 1) shl 11) or // imm[11]
                (((binary shr 21) lowest 10) shl 1)    // imm[10:1]

        // Bit 0 is always 0, offset scaled by 2
        val jTypeOffset get() = imm20jType.toUInt64().signExtend(21)

        // Shift Amount (Lower bits of I-immediate, depends on XLEN for max value but field is 6 bits for RV64)
        val shamtRV32 = imm12iType lowest 5 // For RV32 shifts
        val shamtRV64 = (imm12iType lowest 6).toUInt64() // For RV64 shifts

        // Fence Operands
        val pred = binary shr 24 lowest 4
        val succ = binary shr 20 lowest 4

        // CSR Address (from I-immediate field)
        val csrAddr = imm12iType

        // UIMM5 (from rs1 field in CSRI)
        val uimm5 = rs1 // In CSR immediate instructions, rs1 field holds the uimm5 value


        /**
         * Decodes the instruction based on opcode, funct3, and funct7.
         */
        val type: InstrType? = decodeInstructionType()

        private fun decodeInstructionType(): InstrType? {
            return when (opcode) {
                // --- Load / Store ---
                RvConst.OPC_LOAD -> when (funct3) {
                    RvConst.FUNCT3_LOAD_B -> LB
                    RvConst.FUNCT3_LOAD_H -> LH
                    RvConst.FUNCT3_LOAD_W -> LW
                    RvConst.FUNCT3_LOAD_BU -> LBU
                    RvConst.FUNCT3_LOAD_HU -> LHU
                    RvConst.FUNCT3_LOAD_WU -> LWU // RV64I
                    RvConst.FUNCT3_LOAD_D -> LD   // RV64I
                    else -> null // Invalid funct3 for LOAD
                }

                RvConst.OPC_STORE -> when (funct3) {
                    RvConst.FUNCT3_STORE_B -> SB
                    RvConst.FUNCT3_STORE_H -> SH
                    RvConst.FUNCT3_STORE_W -> SW
                    RvConst.FUNCT3_STORE_D -> SD   // RV64I
                    else -> null // Invalid funct3 for STORE
                }

                // --- Branch ---
                RvConst.OPC_BRANCH -> when (funct3) {
                    RvConst.FUNCT3_CBRA_BEQ -> BEQ
                    RvConst.FUNCT3_CBRA_BNE -> BNE
                    RvConst.FUNCT3_CBRA_BLT -> BLT
                    RvConst.FUNCT3_CBRA_BGE -> BGE
                    RvConst.FUNCT3_CBRA_BLTU -> BLTU
                    RvConst.FUNCT3_CBRA_BGEU -> BGEU
                    else -> null // Invalid funct3 for BRANCH
                }

                // --- Jump / Link ---
                RvConst.OPC_JAL -> JAL
                RvConst.OPC_JALR -> when (funct3) { // JALR only has one funct3
                    UInt32.ZERO -> JALR
                    else -> null
                }

                // --- Immediate Arithmetic ---
                RvConst.OPC_ARITH_IMM -> when (funct3) {
                    RvConst.FUNCT3_ADDI_ADD_SUB -> ADDI
                    RvConst.FUNCT3_SLTI_SLT -> SLTI
                    RvConst.FUNCT3_SLTIU_SLTU -> SLTIU
                    RvConst.FUNCT3_XORI_XOR -> XORI
                    RvConst.FUNCT3_ORI_OR -> ORI
                    RvConst.FUNCT3_ANDI_AND -> ANDI
                    RvConst.FUNCT3_SLLI_SLL -> when (funct7 and 0b1111110) {
                        RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND -> SLLI
                        else -> null // Invalid funct7 for SLLI RV32 and RV64
                    }

                    RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> when (funct7) { // Distinguishes SRLI/SRAI
                        RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND -> SRLI
                        RvConst.FUNCT7_SUB_SRA -> SRAI
                        else -> null // Invalid funct7 for SRLI/SRAI
                    }

                    else -> null // Invalid funct3 for ARITH_IMM
                }

                // --- Register Arithmetic ---
                RvConst.OPC_ARITH -> when (funct7) {
                    RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND -> when (funct3) { // Standard non-M, non-SUB/SRA
                        RvConst.FUNCT3_ADDI_ADD_SUB -> ADD
                        RvConst.FUNCT3_SLLI_SLL -> SLL
                        RvConst.FUNCT3_SLTI_SLT -> SLT
                        RvConst.FUNCT3_SLTIU_SLTU -> SLTU
                        RvConst.FUNCT3_XORI_XOR -> XOR
                        RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> SRL
                        RvConst.FUNCT3_ORI_OR -> OR
                        RvConst.FUNCT3_ANDI_AND -> AND
                        else -> null
                    }

                    RvConst.FUNCT7_SUB_SRA -> when (funct3) { // SUB or SRA
                        RvConst.FUNCT3_ADDI_ADD_SUB -> SUB
                        RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> SRA
                        else -> null
                    }

                    RvConst.FUNCT7_M -> when (funct3) { // M Extension (RV32M/RV64M)
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

                    else -> null // Invalid funct7 for ARITH
                }

                // --- Immediate Arithmetic Word (RV64I) ---
                RvConst.OPC_ARITH_IMM_WORD -> when (funct3) {
                    RvConst.FUNCT3_ADDI_ADD_SUB -> ADDIW
                    RvConst.FUNCT3_SLLI_SLL -> SLLIW
                    RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> when (funct7) { // Distinguishes SRLIW/SRAIW
                        RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND -> SRLIW // 0000000
                        RvConst.FUNCT7_SUB_SRA -> SRAIW // 0100000
                        else -> null // Invalid funct7 for SRLIW/SRAIW
                    }

                    else -> null // Invalid funct3 for ARITH_IMM_WORD
                }

                // --- Register Arithmetic Word (RV64I) ---
                RvConst.OPC_ARITH_WORD -> when (funct7) {
                    RvConst.FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND -> when (funct3) { // Standard non-M, non-SUBW/SRAW
                        RvConst.FUNCT3_ADDI_ADD_SUB -> ADDW
                        RvConst.FUNCT3_SLLI_SLL -> SLLW
                        RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> SRLW
                        else -> null
                    }

                    RvConst.FUNCT7_SUB_SRA -> when (funct3) { // SUBW or SRAW
                        RvConst.FUNCT3_ADDI_ADD_SUB -> SUBW
                        RvConst.FUNCT3_SRLI_SRL_SRAI_SRA -> SRAW
                        else -> null
                    }

                    RvConst.FUNCT7_M -> when (funct3) { // M Extension (RV64M)
                        RvConst.FUNCT3_M_MUL -> MULW
                        RvConst.FUNCT3_M_DIV -> DIVW
                        RvConst.FUNCT3_M_DIVU -> DIVUW
                        RvConst.FUNCT3_M_REM -> REMW
                        RvConst.FUNCT3_M_REMU -> REMUW
                        else -> null
                    }

                    else -> null // Invalid funct7 for ARITH_WORD
                }

                // --- Upper Immediate ---
                RvConst.OPC_LUI -> LUI
                RvConst.OPC_AUIPC -> AUIPC

                // --- System / CSR / Fence ---
                RvConst.OPC_SYSTEM -> when (funct3) {
                    RvConst.FUNCT3_ECALL_EBREAK_MRET_etc -> { // Check funct7/imm12 for specific instructions
                        val imm12 = binary shr 20 // Immediate field used for encoding details
                        val funct7_sub = imm12 shr 5 // Upper 7 bits of imm12 can act like funct7

                        when (imm12) { // Check full imm12 first for ECALL/EBREAK
                            RvConst.IMM12_ECALL -> ECALL
                            RvConst.IMM12_EBREAK -> EBREAK
                            else -> when (funct7_sub) { // Check specific funct7-like patterns
                                // Need to also check rs2/funct5 field for URET/SRET/MRET/WFI/SFENCE.VMA
                                // These use R-type format within SYSTEM opcode space
                                RvConst.FUNCT7_SFENCE_VMA -> if (rd == 0u.toUInt32()) SFENCE_VMA else null // rd must be 0
                                RvConst.FUNCT7_WFI -> if (rd == 0u.toUInt32() && rs1 == 0u.toUInt32() && rs2 == 0u.toUInt32()) WFI else null // rd,rs1,rs2 must be 0 for WFI encoding 0x10500073
                                else -> {
                                    // Check specific encodings for RET instructions based on funct7 AND rs2 field
                                    val funct7_full = binary shr 25 // Use actual funct7
                                    when {
                                        funct7_full == RvConst.FUNCT7_SRET && rs2 == 0b00010u.toUInt32() && rd == 0u.toUInt32() && rs1 == 0u.toUInt32() -> SRET
                                        funct7_full == RvConst.FUNCT7_MRET && rs2 == 0b00010u.toUInt32() && rd == 0u.toUInt32() && rs1 == 0u.toUInt32() -> MRET
                                        else -> null // Unknown SYSTEM instruction
                                    }
                                }
                            }
                        }
                    }
                    // CSR Instructions
                    RvConst.FUNCT3_CSR_RW -> CSRRW
                    RvConst.FUNCT3_CSR_RS -> CSRRS
                    RvConst.FUNCT3_CSR_RC -> CSRRC
                    RvConst.FUNCT3_CSR_RWI -> CSRRWI
                    RvConst.FUNCT3_CSR_RSI -> CSRRSI
                    RvConst.FUNCT3_CSR_RCI -> CSRRCI
                    else -> null // Invalid funct3 for SYSTEM
                }

                RvConst.OPC_FENCE -> when (funct3) {
                    RvConst.FUNCT3_FENCE -> FENCE
                    RvConst.FUNCT3_FENCE_I -> FENCEI // Zifencei
                    else -> null // Invalid funct3 for FENCE
                }

                // --- Unknown Opcode ---
                else -> null
            }
        }

        /**
         * Formats the decoded instruction into a string.
         */
        override fun decode(segmentAddr: UnsignedFixedSizeIntNumber<*>, offset: Int): Decoded {
            val currentAddr = (segmentAddr + offset.toLong()).addrConstrained()

            return when (type) {
                // --- U-Type ---
                LUI -> Decoded(offset, binary, "lui    ${rdName()}, 0x${imm20uType.toString(16)}")
                AUIPC -> {
                    // Calculate target address for annotation, even though syntax is immediate
                    // AUIPC adds PC + sign_extend(imm20 << 12)
                    val pcOffset = imm20uType.toUInt64().signExtend(32) shl 12 // Sign-extend full 32 bits then shift
                    val target = (currentAddr + pcOffset.toLong()).addrConstrained()
                    Decoded(offset, binary, "auipc  ${rdName()}, 0x${imm20uType.toString(16)}", target) // Show target addr as comment perhaps? Or just imm. Standard shows imm.
                }

                // --- J-Type ---
                JAL -> {
                    val target = (currentAddr + jTypeOffset.toLong()).addrConstrained()
                    // Format target address relative to current instruction pointer
                    val displacement = jTypeOffset.toInt64()
                    Decoded(offset, binary, "jal    ${rdName()}, $displacement", target)
                    // Alternative format: Decoded(offset, binary, "jal    ${rdName()}, ${target.toAddressString()}", target)
                }

                // --- I-Type (JALR) ---
                JALR -> {
                    // Target calculation is runtime dependent (rs1 + imm), cannot reliably show absolute target
                    Decoded(offset, binary, "jalr   ${rdName()}, ${rs1Name()}, ${iTypeImm.toInt64()}")
                    // Alternative format with offset(base): Decoded(offset, binary, "jalr   ${rdName()}, ${iTypeImm.toInt64()}(${rs1Name()})")
                }

                // --- B-Type ---
                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                    val target = (currentAddr + bTypeOffset.toLong()).addrConstrained()
                    val displacement = bTypeOffset.toInt64()
                    Decoded(offset, binary, "${type.lc6char} ${rs1Name()}, ${rs2Name()}, $displacement", target)
                    // Alternative format: Decoded(offset, binary, "${type.lc6char} ${rs1Name()}, ${rs2Name()}, ${target.toAddressString()}", target)
                }

                // --- I-Type (Loads) ---
                LB, LH, LW, LBU, LHU, LWU, LD -> {
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${iTypeImm.toInt64()}(${rs1Name()})")
                }

                // --- S-Type (Stores) ---
                SB, SH, SW, SD -> {
                    Decoded(offset, binary, "${type.lc6char} ${rs2Name()}, ${sTypeImm.toInt64()}(${rs1Name()})")
                }

                // --- I-Type (Immediate Arithmetic & Shifts) ---
                ADDI, SLTI, SLTIU, XORI, ORI, ANDI -> {
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${iTypeImm.toInt64()}")
                }

                ADDIW -> { // RV64I
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${iTypeImm.toInt64()}")
                }

                SLLI, SRLI, SRAI -> { // Assuming RV32 shamt display
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, $shamtRV64")
                }

                SLLIW, SRLIW, SRAIW -> { // RV64I - Word shifts use 5 bit shamt
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, $shamtRV32") // Display 5-bit shamt
                }

                // --- R-Type (Register Arithmetic & Shifts) ---
                ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND -> {
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                }

                ADDW, SUBW, SLLW, SRLW, SRAW -> { // RV64I
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                }
                // M Extension
                MUL, MULH, MULHSU, MULHU, DIV, DIVU, REM, REMU -> {
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                }

                MULW, DIVW, DIVUW, REMW, REMUW -> { // RV64M
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${rs1Name()}, ${rs2Name()}")
                }

                // --- System ---
                ECALL -> Decoded(offset, binary, "ecall")
                EBREAK -> Decoded(offset, binary, "ebreak")
                SRET -> Decoded(offset, binary, "sret")
                MRET -> Decoded(offset, binary, "mret")
                WFI -> Decoded(offset, binary, "wfi")
                SFENCE_VMA -> Decoded(offset, binary, "sfence.vma ${rs1Name()}, ${rs2Name()}") // Show optional regs

                // CSR Instructions
                CSRRW, CSRRS, CSRRC -> {
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${csrName()}, ${rs1Name()}")
                }

                CSRRWI, CSRRSI, CSRRCI -> { // uimm is in rs1 field position
                    Decoded(offset, binary, "${type.lc6char} ${rdName()}, ${csrName()}, $uimm5")
                }

                // --- Fence ---
                FENCE -> {
                    // Optional: Decode pred/succ bits into characters (I,O,R,W)
                    val predStr = decodeFenceArg(pred)
                    val succStr = decodeFenceArg(succ)
                    Decoded(offset, binary, "fence  $predStr, $succStr")
                }

                FENCEI -> Decoded(offset, binary, "fence.i")

                // --- Unknown / Invalid ---
                null -> Decoded(offset, binary, "[invalid opcode: 0x${opcode.toString(16)} or encoding]")
            }
        }

        // --- Helper Functions ---
        private fun rdName(): String = RvRegT.IntT.entries.getOrNull(rd.toInt())?.recognizable?.first() ?: "inv_rd${rd}"
        private fun rs1Name(): String = RvRegT.IntT.entries.getOrNull(rs1.toInt())?.recognizable?.first() ?: "inv_rs1${rs1}"
        private fun rs2Name(): String = RvRegT.IntT.entries.getOrNull(rs2.toInt())?.recognizable?.first() ?: "inv_rs2${rs2}"

        private fun csrName(): String {
            // Combine known CSRs from different specs/extensions if needed
            val allCsrs = RvRegT.RvCsrT.rv32CsrRegs // Add more lists if needed (e.g., RV64 CSRs)
            return allCsrs.firstOrNull { it.address.toUInt32() == csrAddr }?.recognizable?.first()
                ?: "0x${csrAddr.toString(16)}" // Fallback to hex address
        }

        private fun decodeFenceArg(arg: UInt32): String {
            val sb = StringBuilder()
            if ((arg and 0x8) != UInt32.ZERO) sb.append('i') // Bit 3: Device Input
            if ((arg and 0x4) != UInt32.ZERO) sb.append('o') // Bit 2: Device Output
            if ((arg and 0x2) != UInt32.ZERO) sb.append('r') // Bit 1: Memory Read
            if ((arg and 0x1) != UInt32.ZERO) sb.append('w') // Bit 0: Memory Write
            return if (sb.isEmpty()) "0" else sb.toString() // Should not happen based on spec, but safe
        }
    }

    /**
     * Enum representing the decoded RISC-V instruction types supported by this disassembler.
     * Includes Base I, M, Zicsr, Zifencei, and RV64I/M instructions.
     */
    @Suppress("unused") // Entries used via 'type' property in RVInstrInfoProvider
    enum class InstrType(special: String? = null) {
        // Base RV32I/RV64I (+Zicsr, Zifencei)
        LUI, AUIPC, JAL, JALR,
        BEQ, BNE, BLT, BGE, BLTU, BGEU,
        LB, LH, LW, LBU, LHU,
        SB, SH, SW,
        ADDI, SLTI, SLTIU, XORI, ORI, ANDI, SLLI, SRLI, SRAI,
        ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND,
        FENCE, FENCEI("fence.i"),
        ECALL, EBREAK, SRET, MRET, WFI, SFENCE_VMA("sfence.vma"),
        CSRRW, CSRRS, CSRRC, CSRRWI, CSRRSI, CSRRCI,

        // RV64I Specific
        LWU, LD, SD,
        ADDIW, SLLIW, SRLIW, SRAIW,
        ADDW, SUBW, SLLW, SRLW, SRAW,

        // M Standard Extension (RV32M/RV64M)
        MUL, MULH, MULHSU, MULHU, DIV, DIVU, REM, REMU,

        // M Standard Extension (RV64M Only)
        MULW, DIVW, DIVUW, REMW, REMUW;

        /** Lowercase mnemonic, padded to 6 characters for alignment */
        val lc6char: String = (special ?: name).lowercase().padEnd(6, ' ')
    }
}