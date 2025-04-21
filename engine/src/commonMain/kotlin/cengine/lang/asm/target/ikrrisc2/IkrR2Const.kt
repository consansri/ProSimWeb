package cengine.lang.asm.target.ikrrisc2

import cengine.util.integer.UInt32

data object IkrR2Const {

    val I_OP6_ADDI = UInt32(0x00U)
    val I_OP6_ADDLI = UInt32(0x01U)
    val I_OP6_ADDHI = UInt32(0x02U)
    val I_OP6_AND0I = UInt32(0x04U)
    val I_OP6_AND1I = UInt32(0x05U)
    val I_OP6_ORI = UInt32(0x06U)
    val I_OP6_XORI = UInt32(0x07U)
    val I_OP6_CMPUI = UInt32(0x08U)
    val I_OP6_CMPSI = UInt32(0x09U)
    val I_OP6_LDD = UInt32(0x10U)
    val I_OP6_STD = UInt32(0x14U)

    val FUNCT6_R2 = UInt32(0x3FU)

    val R2_OP6_ADD = UInt32(0x00U)
    val R2_OP6_ADDX = UInt32(0x20U)
    val R2_OP6_SUB = UInt32(0x02U)
    val R2_OP6_SUBX = UInt32(0x22U)
    val R2_OP6_AND = UInt32(0x04U)
    val R2_OP6_OR = UInt32(0x06U)
    val R2_OP6_XOR = UInt32(0x07U)
    val R2_OP6_CMPU = UInt32(0x08U)
    val R2_OP6_CMPS = UInt32(0x09U)
    val R2_OP6_LDR = UInt32(0x10U)
    val R2_OP6_STR = UInt32(0x14U)

    val FUNCT6_R1 = UInt32(0x3FU)
    val CONST6_SHIFT_ROTATE = UInt32(0x01U)
    val CONST6_SWAPH = UInt32(0x10U)

    val R1_OP6_LSL = UInt32(0x28U)
    val R1_OP6_LSR = UInt32(0x29U)
    val R1_OP6_ASL = UInt32(0x2AU)
    val R1_OP6_ASR = UInt32(0x2BU)
    val R1_OP6_ROL = UInt32(0x2CU)
    val R1_OP6_ROR = UInt32(0x2DU)
    val R1_OP6_EXTB = UInt32(0x30U)
    val R1_OP6_EXTH = UInt32(0x31U)
    val R1_OP6_SWAPB = UInt32(0x32U)
    val R1_OP6_SWAPH = UInt32(0x2CU)
    val R1_OP6_NOT = UInt32(0x33U)
    val R1_OP6_JMP = UInt32(0x3CU)
    val R1_OP6_JSR = UInt32(0x3DU)

    val B_OP6_BRA = UInt32(0x3CU)
    val B_OP6_BSR = UInt32(0x3DU)
    val B_OP6_COND_BRA = UInt32(0x3EU)
    val B_FUNCT3_BEQ = UInt32(0x0U)
    val B_FUNCT3_BNE = UInt32(0x1U)
    val B_FUNCT3_BLT = UInt32(0x2U)
    val B_FUNCT3_BGT = UInt32(0x3U)
    val B_FUNCT3_BLE = UInt32(0x4U)
    val B_FUNCT3_BGE = UInt32(0x5U)


}