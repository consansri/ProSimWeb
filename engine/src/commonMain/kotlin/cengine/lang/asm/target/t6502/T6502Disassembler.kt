package cengine.lang.asm.target.t6502

import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.t6502.T6502Disassembler.InstrType.*
import cengine.util.integer.FixedSizeIntNumber
import cengine.util.integer.UInt8
import cengine.util.integer.UnsignedFixedSizeIntNumber

class T6502Disassembler : AsmDisassembler() {
    override fun disassemble(startAddr: UnsignedFixedSizeIntNumber<*>, buffer: List<FixedSizeIntNumber<*>>): List<Decoded> {
        TODO("Not yet implemented")
    }


    data class T6502Decoded(
        val opcode: UInt8,
        val secondByte: UInt8,
        val thirdByte: UInt8,
    ) : InstrProvider {


        val type: InstrType? = when (opcode.value.toUInt().toInt()) {
            0xAD -> LDA_ABS
            0xBD -> LDA_ABS_X
            0xB9 -> LDA_ABS_Y
            0xA9 -> LDA_IMM
            0xA5 -> LDA_ZP
            0xA1 -> LDA_ZP_X_IND
            0xB5 -> LDA_ZP_X
            0xB1 -> LDA_ZPIND_Y

            0xAE -> LDX_ABS
            0xBE -> LDX_ABS_Y
            0xA2 -> LDX_IMM
            0xA6 -> LDX_ZP
            0xB6 -> LDX_ZP_Y

            0xAC -> LDY_ABS
            0xBC -> LDY_ABS_X
            0xA0 -> LDY_IMM
            0xA4 -> LDY_ZP
            0xB4 -> LDY_ZP_X


            0x8D -> STA_ABS
            0x9D -> STA_ABS_X
            0x99 -> STA_ABS_Y
            0x85 -> STA_ZP
            0x81 -> STA_ZP_X_IND
            0x95 -> STA_ZP_X
            0x91 -> STA_ZPIND_Y

            0x8E -> STX_ABS
            0x86 -> STX_ZP
            0x96 -> STX_ZP_Y

            0x8C -> STY_ABS
            0x84 -> STY_ZP
            0x94 -> STY_ZP_X


            0xAA -> TAX_IMPLIED
            0xa8 -> TAY_IMPLIED
            0xba -> TSX_IMPLIED
            0x8a -> TXA_IMPLIED
            0x9a -> TXS_IMPLIED
            0x98 -> TYA_IMPLIED


            0x48 -> PHA
            0x08 -> PHP
            0x68 -> PLA
            0x28 -> PLP


            0xCE -> DEC_ABS
            0xDE -> DEC_ABS_X
            0xC6 -> DEC_ZP
            0xD6 -> DEC_ZP_X

            0xCA -> DEX_IMPLIED
            0x88 -> DEY_IMPLIED

            0xEE -> INC_ABS
            0xFE -> INC_ABS_X
            0xE6 -> INC_ZP
            0xF6 -> INC_ZP_X

            0xE8 -> INX_IMPLIED
            0xC8 -> INY_IMPLIED


            0x6D -> ADC_ABS
            0x7D -> ADC_ABS_X
            0x79 -> ADC_ABS_Y
            0x69 -> ADC_IMM
            0x65 -> ADC_ZP
            0x61 -> ADC_ZP_X_IND
            0x75 -> ADC_ZP_X
            0x71 -> ADC_ZPIND_Y

            0xED -> SBC_ABS
            0xFD -> SBC_ABS_X
            0xF9 -> SBC_ABS_Y
            0xE9 -> SBC_IMM
            0xE5 -> SBC_ZP
            0xE1 -> SBC_ZP_X_IND
            0xF5 -> SBC_ZP_X
            0xF1 -> SBC_ZPIND_Y

            0xCD -> CMP_ABS
            0xDD -> CMP_ABS_X
            0xD9 -> CMP_ABS_Y
            0xC9 -> CMP_IMM
            0xC5 -> CMP_ZP
            0xC1 -> CMP_ZP_X_IND
            0xD5 -> CMP_ZP_X
            0xD1 -> CMP_ZPIND_Y

            0xEC -> CPX_ABS
            0xE0 -> CPX_IMM
            0xE4 -> CPX_ZP

            0xCC -> CPY_ABS
            0xC0 -> CPY_IMM
            0xC4 -> CPY_ZP


            0x2D -> AND_ABS
            0x3D -> AND_ABS_X
            0x39 -> AND_ABS_Y
            0x29 -> AND_IMM
            0x25 -> AND_ZP
            0x21 -> AND_ZP_X_IND
            0x35 -> AND_ZP_X
            0x31 -> AND_ZPIND_Y


            0x4D -> EOR_ABS
            0x5D -> EOR_ABS_X
            0x59 -> EOR_ABS_Y
            0x49 -> EOR_IMM
            0x45 -> EOR_ZP
            0x41 -> EOR_ZP_X_IND
            0x55 -> EOR_ZP_X
            0x51 -> EOR_ZPIND_Y

            0x0D -> ORA_ABS
            0x1D -> ORA_ABS_X
            0x19 -> ORA_ABS_Y
            0x09 -> ORA_IMM
            0x05 -> ORA_ZP
            0x01 -> ORA_ZP_X_IND
            0x15 -> ORA_ZP_X
            0x11 -> ORA_ZPIND_Y

            0x2C -> BIT_ABS
            0x89 -> BIT_IMM
            0x24 -> BIT_ZP

            0x0E -> ASL_ABS
            0x1E -> ASL_ABS_X
            0x0A -> ASL_ACC
            0x06 -> ASL_ZP
            0x16 -> ASL_ZP_X

            0x4E -> LSR_ABS
            0x5E -> LSR_ABS_X
            0x4A -> LSR_ACC
            0x46 -> LSR_ZP
            0x56 -> LSR_ZP_X

            0x2E -> ROL_ABS
            0x3E -> ROL_ABS_X
            0x2A -> ROL_ACC
            0x26 -> ROL_ZP
            0x36 -> ROL_ZP_X

            0x6E -> ROR_ABS
            0x7E -> ROR_ABS_X
            0x6A -> ROR_ACC
            0x66 -> ROR_ZP
            0x76 -> ROR_ZP_X


            0x18 -> CLC
            0xDA -> CLD
            0x58 -> CLI
            0xB8 -> CLV
            0x38 -> SEC
            0xF8 -> SED
            0x78 -> SEI


            0x4C -> JMP_ABS
            0x6C -> JMP_IND
            0x20 -> JSR_ABS
            0x60 -> RTS_IMPLIED
            0x40 -> RTI_IMPLIED


            0x90 -> BCC_REL
            0xB0 -> BCS_REL
            0xF0 -> BEQ_REL
            0x30 -> BMI_REL
            0xD0 -> BNE_REL
            0x10 -> BPL_REL
            0x50 -> BVC_REL
            0x70 -> BVS_REL

            0x00 -> BRK_IMPLIED

            0xEA -> NOP
            else -> null
        }

        override fun decode(segmentAddr: UnsignedFixedSizeIntNumber<*>, offset: Int): Decoded = when (type) {
            LDA_ABS -> TODO()
            LDA_ABS_X -> TODO()
            LDA_ABS_Y -> TODO()
            LDA_IMM -> TODO()
            LDA_ZP -> TODO()
            LDA_ZP_X_IND -> TODO()
            LDA_ZP_X -> TODO()
            LDA_ZPIND_Y -> TODO()
            LDX_ABS -> TODO()
            LDX_ABS_Y -> TODO()
            LDX_IMM -> TODO()
            LDX_ZP -> TODO()
            LDX_ZP_Y -> TODO()
            LDY_ABS -> TODO()
            LDY_ABS_X -> TODO()
            LDY_IMM -> TODO()
            LDY_ZP -> TODO()
            LDY_ZP_X -> TODO()
            STA_ABS -> TODO()
            STA_ABS_X -> TODO()
            STA_ABS_Y -> TODO()
            STA_ZP -> TODO()
            STA_ZP_X_IND -> TODO()
            STA_ZP_X -> TODO()
            STA_ZPIND_Y -> TODO()
            STX_ABS -> TODO()
            STX_ZP -> TODO()
            STX_ZP_Y -> TODO()
            STY_ABS -> TODO()
            STY_ZP -> TODO()
            STY_ZP_X -> TODO()
            TAX_IMPLIED -> TODO()
            TAY_IMPLIED -> TODO()
            TSX_IMPLIED -> TODO()
            TXA_IMPLIED -> TODO()
            TXS_IMPLIED -> TODO()
            TYA_IMPLIED -> TODO()
            PHA -> TODO()
            PHP -> TODO()
            PLA -> TODO()
            PLP -> TODO()
            DEC_ABS -> TODO()
            DEC_ABS_X -> TODO()
            DEC_ZP -> TODO()
            DEC_ZP_X -> TODO()
            DEX_IMPLIED -> TODO()
            DEY_IMPLIED -> TODO()
            INC_ABS -> TODO()
            INC_ABS_X -> TODO()
            INC_ZP -> TODO()
            INC_ZP_X -> TODO()
            INX_IMPLIED -> TODO()
            INY_IMPLIED -> TODO()
            ADC_ABS -> TODO()
            ADC_ABS_X -> TODO()
            ADC_ABS_Y -> TODO()
            ADC_IMM -> TODO()
            ADC_ZP -> TODO()
            ADC_ZP_X_IND -> TODO()
            ADC_ZP_X -> TODO()
            ADC_ZPIND_Y -> TODO()
            SBC_ABS -> TODO()
            SBC_ABS_X -> TODO()
            SBC_ABS_Y -> TODO()
            SBC_IMM -> TODO()
            SBC_ZP -> TODO()
            SBC_ZP_X_IND -> TODO()
            SBC_ZP_X -> TODO()
            SBC_ZPIND_Y -> TODO()
            CMP_ABS -> TODO()
            CMP_ABS_X -> TODO()
            CMP_ABS_Y -> TODO()
            CMP_IMM -> TODO()
            CMP_ZP -> TODO()
            CMP_ZP_X_IND -> TODO()
            CMP_ZP_X -> TODO()
            CMP_ZPIND_Y -> TODO()
            CPX_ABS -> TODO()
            CPX_IMM -> TODO()
            CPX_ZP -> TODO()
            CPY_ABS -> TODO()
            CPY_IMM -> TODO()
            CPY_ZP -> TODO()
            AND_ABS -> TODO()
            AND_ABS_X -> TODO()
            AND_ABS_Y -> TODO()
            AND_IMM -> TODO()
            AND_ZP -> TODO()
            AND_ZP_X_IND -> TODO()
            AND_ZP_X -> TODO()
            AND_ZPIND_Y -> TODO()
            EOR_ABS -> TODO()
            EOR_ABS_X -> TODO()
            EOR_ABS_Y -> TODO()
            EOR_IMM -> TODO()
            EOR_ZP -> TODO()
            EOR_ZP_X_IND -> TODO()
            EOR_ZP_X -> TODO()
            EOR_ZPIND_Y -> TODO()
            ORA_ABS -> TODO()
            ORA_ABS_X -> TODO()
            ORA_ABS_Y -> TODO()
            ORA_IMM -> TODO()
            ORA_ZP -> TODO()
            ORA_ZP_X_IND -> TODO()
            ORA_ZP_X -> TODO()
            ORA_ZPIND_Y -> TODO()
            BIT_ABS -> TODO()
            BIT_IMM -> TODO()
            BIT_ZP -> TODO()
            ASL_ABS -> TODO()
            ASL_ABS_X -> TODO()
            ASL_ACC -> TODO()
            ASL_ZP -> TODO()
            ASL_ZP_X -> TODO()
            LSR_ABS -> TODO()
            LSR_ABS_X -> TODO()
            LSR_ACC -> TODO()
            LSR_ZP -> TODO()
            LSR_ZP_X -> TODO()
            ROL_ABS -> TODO()
            ROL_ABS_X -> TODO()
            ROL_ACC -> TODO()
            ROL_ZP -> TODO()
            ROL_ZP_X -> TODO()
            ROR_ABS -> TODO()
            ROR_ABS_X -> TODO()
            ROR_ACC -> TODO()
            ROR_ZP -> TODO()
            ROR_ZP_X -> TODO()
            CLC -> TODO()
            CLD -> TODO()
            CLI -> TODO()
            CLV -> TODO()
            SEC -> TODO()
            SED -> TODO()
            SEI -> TODO()
            JMP_ABS -> TODO()
            JMP_IND -> TODO()
            JSR_ABS -> TODO()
            RTS_IMPLIED -> TODO()
            RTI_IMPLIED -> TODO()
            BCC_REL -> TODO()
            BCS_REL -> TODO()
            BEQ_REL -> TODO()
            BMI_REL -> TODO()
            BNE_REL -> TODO()
            BPL_REL -> TODO()
            BVC_REL -> TODO()
            BVS_REL -> TODO()
            BRK_IMPLIED -> TODO()
            NOP -> TODO()
            null -> TODO()
        }
    }


    enum class InstrType {
        // Load, store, interregister transfer
        LDA_ABS,
        LDA_ABS_X,
        LDA_ABS_Y,
        LDA_IMM,
        LDA_ZP,
        LDA_ZP_X_IND,
        LDA_ZP_X,
        LDA_ZPIND_Y,

        LDX_ABS,
        LDX_ABS_Y,
        LDX_IMM,
        LDX_ZP,
        LDX_ZP_Y,

        LDY_ABS,
        LDY_ABS_X,
        LDY_IMM,
        LDY_ZP,
        LDY_ZP_X,

        // STA instructions
        STA_ABS,
        STA_ABS_X,
        STA_ABS_Y,
        STA_ZP,
        STA_ZP_X_IND,
        STA_ZP_X,
        STA_ZPIND_Y,

        STX_ABS,
        STX_ZP,
        STX_ZP_Y,

        STY_ABS,
        STY_ZP,
        STY_ZP_X,

        TAX_IMPLIED,
        TAY_IMPLIED,
        TSX_IMPLIED,
        TXA_IMPLIED,
        TXS_IMPLIED,
        TYA_IMPLIED,

        // Stack instructions
        PHA,
        PHP,
        PLA,
        PLP,

        // Decrements, Increments
        DEC_ABS,
        DEC_ABS_X,
        DEC_ZP,
        DEC_ZP_X,

        DEX_IMPLIED,
        DEY_IMPLIED,

        INC_ABS,
        INC_ABS_X,
        INC_ZP,
        INC_ZP_X,

        INX_IMPLIED,
        INY_IMPLIED,

        // Arithmetic Operations
        ADC_ABS,
        ADC_ABS_X,
        ADC_ABS_Y,
        ADC_IMM,
        ADC_ZP,
        ADC_ZP_X_IND,
        ADC_ZP_X,
        ADC_ZPIND_Y,

        SBC_ABS,
        SBC_ABS_X,
        SBC_ABS_Y,
        SBC_IMM,
        SBC_ZP,
        SBC_ZP_X_IND,
        SBC_ZP_X,
        SBC_ZPIND_Y,

        CMP_ABS,
        CMP_ABS_X,
        CMP_ABS_Y,
        CMP_IMM,
        CMP_ZP,
        CMP_ZP_X_IND,
        CMP_ZP_X,
        CMP_ZPIND_Y,

        CPX_ABS,
        CPX_IMM,
        CPX_ZP,

        CPY_ABS,
        CPY_IMM,
        CPY_ZP,

        // Logic Operations
        AND_ABS,
        AND_ABS_X,
        AND_ABS_Y,
        AND_IMM,
        AND_ZP,
        AND_ZP_X_IND,
        AND_ZP_X,
        AND_ZPIND_Y,

        EOR_ABS,
        EOR_ABS_X,
        EOR_ABS_Y,
        EOR_IMM,
        EOR_ZP,
        EOR_ZP_X_IND,
        EOR_ZP_X,
        EOR_ZPIND_Y,

        ORA_ABS,
        ORA_ABS_X,
        ORA_ABS_Y,
        ORA_IMM,
        ORA_ZP,
        ORA_ZP_X_IND,
        ORA_ZP_X,
        ORA_ZPIND_Y,

        BIT_ABS,
        BIT_IMM,
        BIT_ZP,

        // Shifts and Rotates
        ASL_ABS,
        ASL_ABS_X,
        ASL_ACC,
        ASL_ZP,
        ASL_ZP_X,

        LSR_ABS,
        LSR_ABS_X,
        LSR_ACC,
        LSR_ZP,
        LSR_ZP_X,

        ROL_ABS,
        ROL_ABS_X,
        ROL_ACC,
        ROL_ZP,
        ROL_ZP_X,

        ROR_ABS,
        ROR_ABS_X,
        ROR_ACC,
        ROR_ZP,
        ROR_ZP_X,

        // Flags
        CLC,
        CLD,
        CLI,
        CLV,
        SEC,
        SED,
        SEI,

        // Jumps, Calls, Returns
        JMP_ABS,
        JMP_IND,
        JSR_ABS,
        RTS_IMPLIED,
        RTI_IMPLIED,

        // Branching
        BCC_REL,
        BCS_REL,
        BEQ_REL,
        BMI_REL,
        BNE_REL,
        BPL_REL,
        BVC_REL,
        BVS_REL,

        BRK_IMPLIED,

        NOP;
    }
}