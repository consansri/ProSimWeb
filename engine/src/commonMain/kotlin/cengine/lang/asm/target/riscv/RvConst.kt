package cengine.lang.asm.target.riscv

import cengine.psi.semantic.expr.EvaluationException
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32

data object RvConst {
    // OpCodes
    val OPC_LUI = UInt32(0b00110111U)
    val OPC_AUIPC = UInt32(0b0010111U)
    val OPC_JAL = UInt32(0b1101111U)
    val OPC_JALR = UInt32(0b1100111U)
    val OPC_BRANCH = UInt32(0b1100011U)
    val OPC_LOAD = UInt32(0b0000011U)
    val OPC_STORE = UInt32(0b0100011U)
    val OPC_ARITH_IMM = UInt32(0b0010011U)
    val OPC_ARITH = UInt32(0b0110011U)
    val OPC_FENCE = UInt32(0b0001111U)
    val OPC_SYSTEM = UInt32(0b1110011U) // EBREAK, ECALL, CSR

    val OPC_ARITH_WORD = UInt32(0b0111011U)
    val OPC_ARITH_IMM_WORD = UInt32(0b0011011U)

    // Funct3 - BRANCH
    val FUNCT3_CBRA_BEQ = UInt32(0b000U)
    val FUNCT3_CBRA_BNE = UInt32(0b001U)
    val FUNCT3_CBRA_BLT = UInt32(0b100U)
    val FUNCT3_CBRA_BGE = UInt32(0b101U)
    val FUNCT3_CBRA_BLTU = UInt32(0b110U)
    val FUNCT3_CBRA_BGEU = UInt32(0b111U)

    // Funct3 - LOAD
    val FUNCT3_LOAD_B = UInt32(0b000U)
    val FUNCT3_LOAD_H = UInt32(0b001U)
    val FUNCT3_LOAD_W = UInt32(0b010U)
    val FUNCT3_LOAD_D = UInt32(0b011U)
    val FUNCT3_LOAD_BU = UInt32(0b100U)
    val FUNCT3_LOAD_HU = UInt32(0b101U)
    val FUNCT3_LOAD_WU = UInt32(0b110U)

    // Funct3 - STORE
    val FUNCT3_STORE_B = UInt32(0b000U)
    val FUNCT3_STORE_H = UInt32(0b001U)
    val FUNCT3_STORE_W = UInt32(0b010U)
    val FUNCT3_STORE_D = UInt32(0b011U)

    // Funct3 - ARITH_IMM / ARITH
    val FUNCT3_ADDI_ADD_SUB = UInt32(0b000U)
    val FUNCT3_SLLI_SLL = UInt32(0b001U)
    val FUNCT3_SLTI_SLT = UInt32(0b010U)
    val FUNCT3_SLTIU_SLTU = UInt32(0b011U)
    val FUNCT3_XORI_XOR = UInt32(0b100U)
    val FUNCT3_SRLI_SRL_SRAI_SRA = UInt32(0b101U)
    val FUNCT3_ORI_OR = UInt32(0b110U)
    val FUNCT3_ANDI_AND = UInt32(0b111U)

    // Funct3 - FENCE
    val FUNCT3_FENCE = UInt32(0b000U)
    val FUNCT3_FENCE_I = UInt32(0b001U)

    // Funct3 - SYSTEM (CSR)
    val FUNCT3_ECALL_EBREAK_MRET_etc = UInt32(0b000U) // Differentiates based on imm12/funct7
    val FUNCT3_CSR_RW = UInt32(0b001U)
    val FUNCT3_CSR_RS = UInt32(0b010U)
    val FUNCT3_CSR_RC = UInt32(0b011U)
    val FUNCT3_CSR_RWI = UInt32(0b101U)
    val FUNCT3_CSR_RSI = UInt32(0b110U)
    val FUNCT3_CSR_RCI = UInt32(0b111U)

    // Funct7
    val FUNCT7_ADD_SLL_SLT_STLU_XOR_SRL_OR_AND = UInt32(0b0000000U)
    val FUNCT7_SUB_SRA = UInt32(0b0100000U)

    // System Function Values (using funct7 field for non-CSR)
    // Ecall/Ebreak use imm[11:0] field instead of funct7
    val IMM12_ECALL = UInt32(0b0U)
    val IMM12_EBREAK = UInt32(0b1U)
    // Others use specific funct7 values when rs2=0
    val FUNCT7_SFENCE_VMA = UInt32(0b001001U) // requires rs2 != 0 check later
    val FUNCT7_WFI = UInt32(0b0001000U) // rs2=0b00101
    val FUNCT7_MRET = UInt32(0b0011000U) // rs2=0b00010
    val FUNCT7_SRET = UInt32(0b0001000U) // rs2=0b00010

    val FUNCT3_M_MUL = UInt32(0b000U)
    val FUNCT3_M_MULH = UInt32(0b001U)
    val FUNCT3_M_MULHSU = UInt32(0b010U)
    val FUNCT3_M_MULHU = UInt32(0b011U)
    val FUNCT3_M_DIV = UInt32(0b100U)
    val FUNCT3_M_DIVU = UInt32(0b101U)
    val FUNCT3_M_REM = UInt32(0b110U)
    val FUNCT3_M_REMU = UInt32(0b111U)


    /**
     * FUNCT7 CONSTANTS
     */

    val FUNCT7_SHIFT_ARITH_OR_SUB = UInt32(0b0100000U)
    val FUNCT7_M = UInt32(0b0000001U)

    /**
     * Relocation Types
     *
     * See: https://github.com/riscv-non-isa/riscv-elf-psabi-doc/blob/master/riscv-elf.adoc
     */

    val R_RISCV_NONE = UInt32(0U)
    val R_RISCV_32 = UInt32(1U)
    val R_RISCV_64 = UInt32(2U)
    val R_RISCV_BRANCH = UInt32(16U)
    val R_RISCV_JAL = UInt32(17U)
    val R_RISCV_CALL = UInt32(18U)
    val R_RISCV_PCREL_HI20 = UInt32(23U)
    val R_RISCV_PCREL_LO12_I = UInt32(24U)
    val R_RISCV_PCREL_LO12_S = UInt32(25U)
    val R_RISCV_HI20 = UInt32(26U)
    val R_RISCV_LO12_I = UInt32(27U)
    val R_RISCV_LO12_S = UInt32(28U)

    enum class RelocType {
        R_RISCV_NONE,
        R_RISCV_32,
        R_RISCV_64,
        R_RISCV_RELATIVE,
        R_RISCV_COPY,
        JUMP_SLOT,
        TLS_DTPMOD32,
        TLS_DTPMOD64,
        TLS_DTPREL32,
        TLS_DTPREL64,
        TLS_TPREL32,
        TLS_TPREL64,
        TLS_DESC,
        BRANCH,
        JAL,
        CALL,
        CALL_PLT,
        GOT_HI20,
        TLS_GOT_HI20,
        TLS_GD_HI20,
        PCREL_LO12_I,
        PCREL_LO12_S,
        HI20,
        LO12_I,
        LO12_S,
        TPREL_HI20,
        TPREL_LO12_I,
        TPREL_LO12_S,
        TPREL_ADD,
        ADD8,
        ADD16,
        ADD32,
        ADD64,
        SUB8,
        SUB16,
        SUB32,
        SUB64,
        GOT32_PCREL,
        _Reserved0,
        ALIGN,
        RVC_BRANCH,
        RVC_JUMP,
    }


    // Helper for Immediate Packing
    fun packImmI12(imm: UInt32): UInt32 = (imm and 0xFFFu.toUInt32()) shl 20
    fun packImmS(imm: UInt32): UInt32 = (((imm shr 5) and 0x7Fu.toUInt32()) shl 25) or ((imm and 0x1Fu.toUInt32()) shl 7)
    fun packImmB(imm: UInt32): UInt32 = (((imm shr 12) and 0x1u.toUInt32()) shl 31) or // imm[12]
            (((imm shr 5) and 0x3Fu.toUInt32()) shl 25) or // imm[10:5]
            (((imm shr 1) and 0xFu.toUInt32()) shl 8) or  // imm[4:1]
            (((imm shr 11) and 0x1u.toUInt32()) shl 7)    // imm[11]
    fun packImmU(imm: UInt32): UInt32 = (imm and 0xFFFFF000u.toUInt32()) // imm[31:12] << 12 is already done by structure
    fun packImmJ(imm: UInt32): UInt32 = (((imm shr 20) and 0x1u.toUInt32()) shl 31) or // imm[20]
            (((imm shr 1) and 0x3FFu.toUInt32()) shl 21) or // imm[10:1]
            (((imm shr 11) and 0x1u.toUInt32()) shl 20) or // imm[11]
            (((imm shr 12) and 0xFFu.toUInt32()) shl 12)   // imm[19:12]

    // Helper to split a 32-bit immediate for LUI + ADDI pair
    // Returns Pair(hi20 for LUI, lo12 for ADDI)
    fun splitImm32(imm32: UInt32): Pair<UInt32, UInt32> {
        // Standard GAS %hi/%lo calculation:
        // lo = imm[11:0] (sign extended)
        // hi = (imm - lo) >> 12 , but needs upper 20 bits for lui
        // Easier: lo = imm[11:0]
        // hi = (imm + 0x800) & 0xFFFFF000  -- this gives the bits for LUI directly
        val lo12 = imm32 and 0xFFF
        val adjImm = imm32 + 0x800 // Add offset to handle rounding for %hi
        val hi20 = adjImm and 0xFFFFF000 // Take upper 20 bits for LUI field
        return Pair(hi20, lo12) // Note: hi20 is already shifted correctly for packImmU
    }

    // Helper for PC-relative AUIPC + I-type (e.g., ADDI, JALR) offset calculation
    // Takes the full PC-relative offset and returns the pair (immU for AUIPC, immI for I-type)
    fun calculateImmUPCRelAndImmI(offset: IntNumber<*>): Pair<UInt32, UInt32> {
        if (!offset.fitsInSigned(32)) { // Check if offset fits in 32 bits for standard AUIPC+ADDI/JALR
            // Handle error or potentially use longer sequence if needed
            throw EvaluationException("PC-relative offset $offset exceeds 32-bit range")
        }
        val offset32 = offset.toInt32().toUInt32()
        // Same logic as %pcrel_hi/%pcrel_lo
        val immI = offset32 and 0xFFF // Lower 12 bits for ADDI/JALR
        // Adjust upper part based on sign bit of lower part
        val immU = if (immI.bit(11) == UInt32.ONE) {
            (offset32 + 0x1000) and 0xFFFFF000
        } else {
            offset32 and 0xFFFFF000
        }
        return Pair(immU, immI) // immU is ready for packImmU, immI is ready for packImmI
    }
    // Helper for PC-relative AUIPC only (e.g., if only %pcrel_hi is used)
    fun calculateImmU_PCREL(offset: IntNumber<*>): UInt32 {
        return calculateImmUPCRelAndImmI(offset).first
    }

    /**
     * MASKS
     */

    fun UInt32.mask12Hi7(): UInt32 = this shr 5

    fun UInt32.mask12Lo5(): UInt32 = this.lowest(5)

    fun UInt32.mask32Hi20(): UInt32 = this shr 12

    fun UInt32.mask32Lo12(): UInt32 = this.lowest(12)


    /**
     * Expects the relative target offset.
     *
     * @return the jType starting from index 0 (needs to be shifted for 12 bit to the left when used in opcode)
     */
    fun UInt32.mask20jType(): UInt32 {
        val bit20 = (this shr 19) and 1
        val bits10to1 = (this shr 1).lowest(10)
        val bit11 = (this shr 11) and 1
        val bits19to12 = (this shr 12).lowest(8)
    
        return (bit20 shl 19) or
                (bits19to12) or
                (bit11 shl 8) or
                (bits10to1 shl 9)
    }

    fun UInt32.mask12bType7(): UInt32 = (bit(12) shl 6) or (this shr 5).lowest(6)

    fun UInt32.mask12bType5(): UInt32 = (shr(1).lowest(4) shl 1) or bit(10)


}




