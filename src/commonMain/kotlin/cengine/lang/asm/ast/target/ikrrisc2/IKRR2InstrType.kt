package cengine.lang.asm.ast.target.ikrrisc2

import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32
import nativeLog


enum class IKRR2InstrType(override val detectionName: String, val paramType: IKRR2ParamType, val descr: String = "", val labelDependent: Boolean = false, override val addressInstancesNeeded: Int? = 1) : InstrTypeInterface {
    ADD("add", IKRR2ParamType.R2_TYPE, "addiere"),
    ADDI("addi", IKRR2ParamType.I_TYPE, "addiere Konstante (erweitere Konstante vorzeichenrichtig)"),
    ADDLI("addli", IKRR2ParamType.I_TYPE, "addiere Konstante (erweitere Konstante vorzeichenlos)"),
    ADDHI("addhi", IKRR2ParamType.I_TYPE, "addiere Konstante, höherwertiges Halbwort"),
    ADDX("addx", IKRR2ParamType.R2_TYPE, "berechne ausschließlich Übertrag der Addition"),

    //
    SUB("sub", IKRR2ParamType.R2_TYPE, "subtrahiere"),
    SUBX("subx", IKRR2ParamType.R2_TYPE, "berechne ausschließlich Übertrag der Subtraktion"),

    //
    CMPU("cmpu", IKRR2ParamType.R2_TYPE, "vergleiche vorzeichenlos"),
    CMPS("cmps", IKRR2ParamType.R2_TYPE, "vergleiche vorzeichenbehaftet"),
    CMPUI("cmpui", IKRR2ParamType.I_TYPE, "vergleiche vorzeichenlos mit vorzeichenlos erweiterter Konstanten"),
    CMPSI("cmpsi", IKRR2ParamType.I_TYPE, "vergleiche vorzeichenbehaftet mit vorzeichenrichtig erweiterter Konstanten"),

    //
    AND("and", IKRR2ParamType.R2_TYPE, "verknüpfe logisch Und"),
    AND0I("and0i", IKRR2ParamType.I_TYPE, "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 00...0)"),
    AND1I("and1i", IKRR2ParamType.I_TYPE, "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 11...1)"),

    //
    OR("or", IKRR2ParamType.R2_TYPE, "verknüpfe logisch Oder"),
    ORI("ori", IKRR2ParamType.I_TYPE, "verknüpfe logisch Oder mit Konstante"),

    //
    XOR("xor", IKRR2ParamType.R2_TYPE, "verknüpfe logisch Exklusiv-Oder"),
    XORI("xori", IKRR2ParamType.I_TYPE, "verknüpfe logisch Exklusiv-Oder mit Konstante"),

    //
    LSL("lsl", IKRR2ParamType.R1_TYPE, "schiebe um eine Stelle logisch nach links"),
    LSR("lsr", IKRR2ParamType.R1_TYPE, "schiebe um eine Stelle logisch nach rechts"),
    ASL("asl", IKRR2ParamType.R1_TYPE, "schiebe um eine Stelle arithmetisch nach links"),
    ASR("asr", IKRR2ParamType.R1_TYPE, "schiebe um eine Stelle arithmetisch nach rechts"),
    ROL("rol", IKRR2ParamType.R1_TYPE, "rotiere um eine Stelle nach links"),
    ROR("ror", IKRR2ParamType.R1_TYPE, "rotiere um eine Stelle nach rechts"),

    //
    EXTB("extb", IKRR2ParamType.R1_TYPE, "erweitere niederwertigstes Byte (Byte 0) vorzeichenrichtig"),
    EXTH("exth", IKRR2ParamType.R1_TYPE, "erweitere niederwertiges Halbwort (Byte 1, Byte 0) vorzeichenrichtig"),

    //
    SWAPB("swapb", IKRR2ParamType.R1_TYPE, "vertausche Byte 3 mit Byte 2 und Byte 1 mit Byte 0"),
    SWAPH("swaph", IKRR2ParamType.R1_TYPE, "vertausche höherwertiges Halbwort und niederwertiges Halbwort"),

    //
    NOT("not", IKRR2ParamType.R1_TYPE, "invertiere bitweise"),

    //
    LDD("ldd", IKRR2ParamType.L_OFF_TYPE, "load (Register indirekt mit Offset)"),
    LDR("ldr", IKRR2ParamType.L_INDEX_TYPE, "load (Register indirekt mit Index)"),

    //
    STD("std", IKRR2ParamType.S_OFF_TYPE, "store (Register indirekt mit Offset)"),
    STR("str", IKRR2ParamType.S_INDEX_TYPE, "store (Register indirekt mit Index)"),

    //
    BEQ("beq", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc gleich 0 (EQual to 0)", true),
    BNE("bne", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc ungleich 0 (Not Equal to 0)", true),
    BLT("blt", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc kleiner als 0 (Less Than 0)", true),
    BGT("bgt", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc größer als 0 (Greater Than 0)", true),
    BLE("ble", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc kleiner oder gleich 0 (Less than or Equal to 0)", true),
    BGE("bge", IKRR2ParamType.B_DISP18_TYPE, "verzweige, falls rc größer oder gleich 0 (Greater than or Equal to 0)", true),

    //
    BRA("bra", IKRR2ParamType.B_DISP26_TYPE, "verzweige unbedingt (branch always)", true),
    BSR("bsr", IKRR2ParamType.B_DISP26_TYPE, "verzweige in Unterprogramm (sichere Rücksprungadresse in r31)", true),

    //
    JMP("jmp", IKRR2ParamType.B_REG_TYPE, "springe an Adresse in rb"),
    JSR("jsr", IKRR2ParamType.B_REG_TYPE, "springe in Unterprg. an Adresse in rb (sichere Rücksprungadr. in r31)");

    override val inCodeInfo: String?
        get() = descr

    override val paramRule: Rule?
        get() = paramType.rule

    override val typeName: String
        get() = name

    override fun resolve(builder: AsmCodeGenerator<*>, instr: ASNode.Instruction) {
        val regs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> IKRR2BaseRegs.entries.firstOrNull { it.recognizable.contains(token.value) }?.ordinal?. toUInt32() }
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (paramType) {
            IKRR2ParamType.I_TYPE -> {
                val expr = exprs[0]
                val rc = regs[0]
                val rb = regs[1]

                val opc = when (this) {
                    ADDI -> IKRR2Const.I_OP6_ADDI
                    ADDLI -> IKRR2Const.I_OP6_ADDLI
                    ADDHI -> IKRR2Const.I_OP6_ADDHI
                    AND0I -> IKRR2Const.I_OP6_AND0I
                    AND1I -> IKRR2Const.I_OP6_AND1I
                    ORI -> IKRR2Const.I_OP6_ORI
                    XORI -> IKRR2Const.I_OP6_XORI
                    CMPUI -> IKRR2Const.I_OP6_CMPUI
                    CMPSI -> IKRR2Const.I_OP6_CMPSI
                    else -> UInt32.ZERO
                }

                val imm = expr.evaluate(builder)

                if (!imm.fitsInSignedOrUnsigned(16)) {
                    expr.addError("${expr.eval} exceeds 16 bits")
                }

                val imm16 = try {
                    imm.toInt16().toUInt16().toUInt32()
                } catch (e: Exception) {
                    expr.addWarn("Interpreted as unsigned!")
                    imm.toUInt16().toUInt32()
                }

                val bundle = (opc shl 26) or (rc shl 21) or (rb shl 16) or imm16
                nativeLog("IKRR2 I-Type: ${bundle.toString(16)}, imm: ${imm16.toString(16)}")
                builder.currentSection.content.put(bundle)
            }

            IKRR2ParamType.R2_TYPE -> {
                val funct6 = IKRR2Const.FUNCT6_R2
                val rc = regs[0]
                val rb = regs[1]
                val ra = regs[2]
                val opc = when (this) {
                    ADD -> IKRR2Const.R2_OP6_ADD
                    ADDX -> IKRR2Const.R2_OP6_ADDX
                    SUB -> IKRR2Const.R2_OP6_SUB
                    SUBX -> IKRR2Const.R2_OP6_SUBX
                    AND -> IKRR2Const.R2_OP6_AND
                    OR -> IKRR2Const.R2_OP6_OR
                    XOR -> IKRR2Const.R2_OP6_XOR
                    CMPU -> IKRR2Const.R2_OP6_CMPU
                    CMPS -> IKRR2Const.R2_OP6_CMPS
                    LDR -> IKRR2Const.R2_OP6_LDR
                    STR -> IKRR2Const.R2_OP6_STR
                    else -> UInt32.ZERO
                }
                val bundle = (funct6 shl 26) or (rc shl 21) or (rb shl 16) or (opc shl 10) or ra

                builder.currentSection.content.put(bundle)
            }

            IKRR2ParamType.R1_TYPE -> {
                val funct6 = IKRR2Const.FUNCT6_R1

                val rc = regs[0]
                val rb = regs[1]

                val const6 = when (this) {
                    LSL, LSR, ASL, ASR, ROL, ROR -> IKRR2Const.CONST6_SHIFT_ROTATE
                    SWAPH -> IKRR2Const.CONST6_SWAPH
                    else -> UInt32.ZERO
                }

                val opc = when (this) {
                    LSL -> IKRR2Const.R1_OP6_LSL
                    LSR -> IKRR2Const.R1_OP6_LSR
                    ASL -> IKRR2Const.R1_OP6_ASL
                    ASR -> IKRR2Const.R1_OP6_ASR
                    ROL -> IKRR2Const.R1_OP6_ROL
                    ROR -> IKRR2Const.R1_OP6_ROR
                    EXTB -> IKRR2Const.R1_OP6_EXTB
                    EXTH -> IKRR2Const.R1_OP6_EXTH
                    SWAPB -> IKRR2Const.R1_OP6_SWAPB
                    SWAPH -> IKRR2Const.R1_OP6_SWAPH
                    NOT -> IKRR2Const.R1_OP6_NOT
                    else -> UInt32.ZERO
                }

                val bundle = (funct6 shl 26) or (rc shl 21) or (rb shl 16) or (opc shl 10) or const6

                builder.currentSection.content.put(bundle)
            }

            IKRR2ParamType.L_OFF_TYPE -> {
                val expr = exprs[0]
                val rc = regs[0]
                val rb = regs[1]

                val opc = when (this) {
                    LDD -> IKRR2Const.I_OP6_LDD
                    else -> UInt32.ZERO
                }

                val imm = expr.evaluate(builder)

                if (!imm.fitsInSignedOrUnsigned(16)) {
                    expr.addError("$imm exceeds 16 bits")
                }

                val imm16 = try {
                    imm.toInt16().toUInt16().toUInt32()
                } catch (e: Exception) {
                    expr.addWarn("Interpreted as unsigned!")
                    imm.toUInt16().toUInt32()
                }

                val bundle = (opc shl 26) or (rc shl 21) or (rb shl 16) or imm16
                builder.currentSection.content.put(bundle)
            }

            IKRR2ParamType.L_INDEX_TYPE -> {
                val funct6 = IKRR2Const.FUNCT6_R2
                val rc = regs[0]
                val rb = regs[1]
                val ra = regs[2]
                val opc = when (this) {
                    ADD -> IKRR2Const.R2_OP6_ADD
                    ADDX -> IKRR2Const.R2_OP6_ADDX
                    SUB -> IKRR2Const.R2_OP6_SUB
                    SUBX -> IKRR2Const.R2_OP6_SUBX
                    AND -> IKRR2Const.R2_OP6_AND
                    OR -> IKRR2Const.R2_OP6_OR
                    XOR -> IKRR2Const.R2_OP6_XOR
                    CMPU -> IKRR2Const.R2_OP6_CMPU
                    CMPS -> IKRR2Const.R2_OP6_CMPS
                    LDR -> IKRR2Const.R2_OP6_LDR
                    STR -> IKRR2Const.R2_OP6_STR
                    else -> UInt32.ZERO
                }
                val bundle = (funct6 shl 26) or (rc shl 21) or (rb shl 16) or (opc shl 10) or ra

                builder.currentSection.content.put(bundle)
            }

            IKRR2ParamType.S_OFF_TYPE -> {
                val expr = exprs[0]

                val rb = regs[0]
                val rc = regs[1]

                val opc = when (this) {
                    STD -> IKRR2Const.I_OP6_STD
                    else -> UInt32.ZERO
                }

                val imm = expr.evaluate(builder)

                if (!imm.fitsInSignedOrUnsigned(16)) {
                    expr.addError("${expr.eval} exceeds 16 bits")
                }

                val imm16 = try {
                    imm.toInt16().toUInt16().toUInt32()
                } catch (e: Exception) {
                    expr.addWarn("Interpreted as unsigned!")
                    imm.toUInt16().toUInt32()
                }

                val bundle = (opc shl 26) or (rc shl 21) or (rb shl 16) or imm16
                builder.currentSection.content.put(bundle)
            }

            IKRR2ParamType.S_INDEX_TYPE -> {
                val funct6 = IKRR2Const.FUNCT6_R2
                val rb = regs[0]
                val ra = regs[1]
                val rc = regs[2]
                val opc = when (this) {
                    STR -> IKRR2Const.R2_OP6_STR
                    else -> UInt32.ZERO
                }
                val bundle = (funct6 shl 26) or (rc shl 21) or (rb shl 16) or (opc shl 10) or ra

                builder.currentSection.content.put(bundle)
            }

            IKRR2ParamType.B_DISP18_TYPE -> {} // evaluate later
            IKRR2ParamType.B_DISP26_TYPE -> {} // evaluate later
            IKRR2ParamType.B_REG_TYPE -> {
                val funct6 = IKRR2Const.FUNCT6_R1

                val rb = regs[0]

                val opc = when (this) {
                    JMP -> IKRR2Const.R1_OP6_JMP
                    JSR -> IKRR2Const.R1_OP6_JSR
                    else -> UInt32.ZERO
                }

                val bundle = (funct6 shl 26) or (rb shl 16) or (opc shl 10)

                builder.currentSection.content.put(bundle)
            }
        }

        if (labelDependent) {
            return builder.currentSection.queueLateInit(instr, addressInstancesNeeded ?: 1)
        }
    }

    override fun lateEvaluation(builder: AsmCodeGenerator<*>, section: AsmCodeGenerator.Section, instr: ASNode.Instruction, index: Int) {
        val regs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> IKRR2BaseRegs.entries.firstOrNull { it.recognizable.contains(token.value) }?.ordinal?. toUInt32() }
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (paramType) {
            IKRR2ParamType.B_DISP26_TYPE -> {
                val expr = exprs[0]

                val opc = when (this) {
                    BRA -> IKRR2Const.B_OP6_BRA
                    BSR -> IKRR2Const.B_OP6_BSR
                    else -> UInt32.ZERO
                }

                val imm26 = expr.displacement(builder, section, index)

                if (!imm26.fitsInSignedOrUnsigned(26)) {
                    expr.addError("$imm26 exceeds 26 bits")
                }

                val bundle = (opc shl 26) or imm26.lowest(26)

                section.content[index] = bundle
            }

            IKRR2ParamType.B_DISP18_TYPE -> {
                val expr = exprs[0]

                val opc = IKRR2Const.B_OP6_COND_BRA
                val rc = regs[0]

                val funct3 = when (this) {
                    BEQ -> IKRR2Const.B_FUNCT3_BEQ
                    BNE -> IKRR2Const.B_FUNCT3_BNE
                    BLT -> IKRR2Const.B_FUNCT3_BLT
                    BGT -> IKRR2Const.B_FUNCT3_BGT
                    BLE -> IKRR2Const.B_FUNCT3_BLE
                    BGE -> IKRR2Const.B_FUNCT3_BGE
                    else -> UInt32.ZERO
                }

                val imm18 = expr.displacement(builder, section, index)

                if (!imm18.fitsInSignedOrUnsigned(18)) {
                    expr.addError("$imm18 exceeds 18 bits")
                }

                val bundle = (opc shl 26) or (rc shl 21) or (funct3 shl 18) or imm18.lowest(18)

                section.content[index] = bundle
            }

            else -> {}
        }
    }

    private fun ASNode.NumericExpr.targetAddr(builder: AsmCodeGenerator<*>): UInt32 {
        return evaluate(builder).toUInt32()
    }

    private fun AsmCodeGenerator.Section.thisAddr(index: Int): UInt32 {
        return this.address.toUInt32() + index
    }

    private fun ASNode.NumericExpr.displacement(builder: AsmCodeGenerator<*>, section: AsmCodeGenerator.Section, index: Int): UInt32 {
        return targetAddr(builder) - section.thisAddr(index)
    }


}