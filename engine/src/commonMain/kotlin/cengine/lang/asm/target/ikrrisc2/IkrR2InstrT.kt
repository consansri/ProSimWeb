package cengine.lang.asm.target.ikrrisc2

import cengine.lang.asm.AsmTreeParser
import cengine.lang.asm.gas.AsmBackend
import cengine.lang.asm.gas.AsmCodeGenerator
import cengine.lang.asm.psi.AsmInstruction
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.asm.target.ikrrisc2.IkrR2ParamType.*
import cengine.psi.parser.PsiBuilder
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32


enum class IkrR2InstrT(override val keyWord: String, val paramType: IkrR2ParamType, val descr: String = "", val labelDependent: Boolean = false, val addressInstancesNeeded: Int? = 1) : AsmInstructionT {
    ADD("add", R2_TYPE, "addiere"),
    ADDI("addi", I_TYPE, "addiere Konstante (erweitere Konstante vorzeichenrichtig)"),
    ADDLI("addli", I_TYPE, "addiere Konstante (erweitere Konstante vorzeichenlos)"),
    ADDHI("addhi", I_TYPE, "addiere Konstante, höherwertiges Halbwort"),
    ADDX("addx", R2_TYPE, "berechne ausschließlich Übertrag der Addition"),

    //
    SUB("sub", R2_TYPE, "subtrahiere"),
    SUBX("subx", R2_TYPE, "berechne ausschließlich Übertrag der Subtraktion"),

    //
    CMPU("cmpu", R2_TYPE, "vergleiche vorzeichenlos"),
    CMPS("cmps", R2_TYPE, "vergleiche vorzeichenbehaftet"),
    CMPUI("cmpui", I_TYPE, "vergleiche vorzeichenlos mit vorzeichenlos erweiterter Konstanten"),
    CMPSI("cmpsi", I_TYPE, "vergleiche vorzeichenbehaftet mit vorzeichenrichtig erweiterter Konstanten"),

    //
    AND("and", R2_TYPE, "verknüpfe logisch Und"),
    AND0I("and0i", I_TYPE, "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 00...0)"),
    AND1I("and1i", I_TYPE, "verknüpfe logisch Und mit Konstante (höherwertiges Halbwort 11...1)"),

    //
    OR("or", R2_TYPE, "verknüpfe logisch Oder"),
    ORI("ori", I_TYPE, "verknüpfe logisch Oder mit Konstante"),

    //
    XOR("xor", R2_TYPE, "verknüpfe logisch Exklusiv-Oder"),
    XORI("xori", I_TYPE, "verknüpfe logisch Exklusiv-Oder mit Konstante"),

    //
    LSL("lsl", R1_TYPE, "schiebe um eine Stelle logisch nach links"),
    LSR("lsr", R1_TYPE, "schiebe um eine Stelle logisch nach rechts"),
    ASL("asl", R1_TYPE, "schiebe um eine Stelle arithmetisch nach links"),
    ASR("asr", R1_TYPE, "schiebe um eine Stelle arithmetisch nach rechts"),
    ROL("rol", R1_TYPE, "rotiere um eine Stelle nach links"),
    ROR("ror", R1_TYPE, "rotiere um eine Stelle nach rechts"),

    //
    EXTB("extb", R1_TYPE, "erweitere niederwertigstes Byte (Byte 0) vorzeichenrichtig"),
    EXTH("exth", R1_TYPE, "erweitere niederwertiges Halbwort (Byte 1, Byte 0) vorzeichenrichtig"),

    //
    SWAPB("swapb", R1_TYPE, "vertausche Byte 3 mit Byte 2 und Byte 1 mit Byte 0"),
    SWAPH("swaph", R1_TYPE, "vertausche höherwertiges Halbwort und niederwertiges Halbwort"),

    //
    NOT("not", R1_TYPE, "invertiere bitweise"),

    //
    LDD("ldd", L_OFF_TYPE, "load (Register indirekt mit Offset)"),
    LDR("ldr", L_INDEX_TYPE, "load (Register indirekt mit Index)"),

    //
    STD("std", S_OFF_TYPE, "store (Register indirekt mit Offset)"),
    STR("str", S_INDEX_TYPE, "store (Register indirekt mit Index)"),

    //
    BEQ("beq", B_DISP18_TYPE, "verzweige, falls rc gleich 0 (EQual to 0)", true),
    BNE("bne", B_DISP18_TYPE, "verzweige, falls rc ungleich 0 (Not Equal to 0)", true),
    BLT("blt", B_DISP18_TYPE, "verzweige, falls rc kleiner als 0 (Less Than 0)", true),
    BGT("bgt", B_DISP18_TYPE, "verzweige, falls rc größer als 0 (Greater Than 0)", true),
    BLE("ble", B_DISP18_TYPE, "verzweige, falls rc kleiner oder gleich 0 (Less than or Equal to 0)", true),
    BGE("bge", B_DISP18_TYPE, "verzweige, falls rc größer oder gleich 0 (Greater than or Equal to 0)", true),

    //
    BRA("bra", B_DISP26_TYPE, "verzweige unbedingt (branch always)", true),
    BSR("bsr", B_DISP26_TYPE, "verzweige in Unterprogramm (sichere Rücksprungadresse in r31)", true),

    //
    JMP("jmp", B_REG_TYPE, "springe an Adresse in rb"),
    JSR("jsr", B_REG_TYPE, "springe in Unterprg. an Adresse in rb (sichere Rücksprungadr. in r31)");

    override fun PsiBuilder.parse(asmTreeParser: AsmTreeParser, marker: PsiBuilder.Marker): Boolean {
        skipWhitespaceAndComments()

        with(paramType) {
            return parse(asmTreeParser)
        }
    }

    override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
        if (paramType == B_DISP18_TYPE || paramType == B_DISP26_TYPE) {
            context.section.queueLateInit(instr, 1)
            return
        }

        val exprs = instr.exprs.map { absIntEvaluator.evaluate(it, context) }
        val regs = instr.regs.map { it.type.address.toUInt32() }

        when (paramType) {
            I_TYPE -> {
                val imm = exprs[0]
                val rc = regs[0]
                val rb = regs[1]

                val opc = when (this@IkrR2InstrT) {
                    ADDI -> IkrR2Const.I_OP6_ADDI
                    ADDLI -> IkrR2Const.I_OP6_ADDLI
                    ADDHI -> IkrR2Const.I_OP6_ADDHI
                    AND0I -> IkrR2Const.I_OP6_AND0I
                    AND1I -> IkrR2Const.I_OP6_AND1I
                    ORI -> IkrR2Const.I_OP6_ORI
                    XORI -> IkrR2Const.I_OP6_XORI
                    CMPUI -> IkrR2Const.I_OP6_CMPUI
                    CMPSI -> IkrR2Const.I_OP6_CMPSI
                    else -> UInt32.ZERO
                }

                if (!imm.fitsInSignedOrUnsigned(16)) {
                    instr.addError("$imm exceeds 16 bits")
                }

                val imm16 = try {
                    imm.toInt16().toUInt16().toUInt32()
                } catch (e: Exception) {
                    instr.addWarn("Interpreted as unsigned!")
                    imm.toUInt16().toUInt32()
                }

                val bundle = (opc shl 26) or (rc shl 21) or (rb shl 16) or imm16
                context.section.content.put(bundle)
            }

            R2_TYPE -> {
                val funct6 = IkrR2Const.FUNCT6_R2
                val rc = regs[0]
                val rb = regs[1]
                val ra = regs[2]
                val opc = when (this@IkrR2InstrT) {
                    ADD -> IkrR2Const.R2_OP6_ADD
                    ADDX -> IkrR2Const.R2_OP6_ADDX
                    SUB -> IkrR2Const.R2_OP6_SUB
                    SUBX -> IkrR2Const.R2_OP6_SUBX
                    AND -> IkrR2Const.R2_OP6_AND
                    OR -> IkrR2Const.R2_OP6_OR
                    XOR -> IkrR2Const.R2_OP6_XOR
                    CMPU -> IkrR2Const.R2_OP6_CMPU
                    CMPS -> IkrR2Const.R2_OP6_CMPS
                    LDR -> IkrR2Const.R2_OP6_LDR
                    STR -> IkrR2Const.R2_OP6_STR
                    else -> UInt32.ZERO
                }
                val bundle = (funct6 shl 26) or (rc shl 21) or (rb shl 16) or (opc shl 10) or ra
                context.section.content.put(bundle)
            }

            R1_TYPE -> {
                val funct6 = IkrR2Const.FUNCT6_R1

                val rc = regs[0]
                val rb = regs[1]

                val const6 = when (this@IkrR2InstrT) {
                    LSL, LSR, ASL, ASR, ROL, ROR -> IkrR2Const.CONST6_SHIFT_ROTATE
                    SWAPH -> IkrR2Const.CONST6_SWAPH
                    else -> UInt32.ZERO
                }

                val opc = when (this@IkrR2InstrT) {
                    LSL -> IkrR2Const.R1_OP6_LSL
                    LSR -> IkrR2Const.R1_OP6_LSR
                    ASL -> IkrR2Const.R1_OP6_ASL
                    ASR -> IkrR2Const.R1_OP6_ASR
                    ROL -> IkrR2Const.R1_OP6_ROL
                    ROR -> IkrR2Const.R1_OP6_ROR
                    EXTB -> IkrR2Const.R1_OP6_EXTB
                    EXTH -> IkrR2Const.R1_OP6_EXTH
                    SWAPB -> IkrR2Const.R1_OP6_SWAPB
                    SWAPH -> IkrR2Const.R1_OP6_SWAPH
                    NOT -> IkrR2Const.R1_OP6_NOT
                    else -> UInt32.ZERO
                }

                val bundle = (funct6 shl 26) or (rc shl 21) or (rb shl 16) or (opc shl 10) or const6
                context.section.content.put(bundle)
            }

            L_OFF_TYPE -> {
                val imm = exprs[0]
                val rc = regs[0]
                val rb = regs[1]

                val opc = when (this@IkrR2InstrT) {
                    LDD -> IkrR2Const.I_OP6_LDD
                    else -> UInt32.ZERO
                }

                if (!imm.fitsInSignedOrUnsigned(16)) {
                    instr.addError("$imm exceeds 16 bits")
                }

                val imm16 = try {
                    imm.toInt16().toUInt16().toUInt32()
                } catch (e: Exception) {
                    instr.addWarn("Interpreted as unsigned!")
                    imm.toUInt16().toUInt32()
                }

                val bundle = (opc shl 26) or (rc shl 21) or (rb shl 16) or imm16
                context.section.content.put(bundle)
            }

            L_INDEX_TYPE -> {
                val funct6 = IkrR2Const.FUNCT6_R2
                val rc = regs[0]
                val rb = regs[1]
                val ra = regs[2]
                val opc = when (this@IkrR2InstrT) {
                    ADD -> IkrR2Const.R2_OP6_ADD
                    ADDX -> IkrR2Const.R2_OP6_ADDX
                    SUB -> IkrR2Const.R2_OP6_SUB
                    SUBX -> IkrR2Const.R2_OP6_SUBX
                    AND -> IkrR2Const.R2_OP6_AND
                    OR -> IkrR2Const.R2_OP6_OR
                    XOR -> IkrR2Const.R2_OP6_XOR
                    CMPU -> IkrR2Const.R2_OP6_CMPU
                    CMPS -> IkrR2Const.R2_OP6_CMPS
                    LDR -> IkrR2Const.R2_OP6_LDR
                    STR -> IkrR2Const.R2_OP6_STR
                    else -> UInt32.ZERO
                }
                val bundle = (funct6 shl 26) or (rc shl 21) or (rb shl 16) or (opc shl 10) or ra
                context.section.content.put(bundle)
            }

            S_OFF_TYPE -> {
                val imm = exprs[0]

                val rb = regs[0]
                val rc = regs[1]

                val opc = when (this@IkrR2InstrT) {
                    STD -> IkrR2Const.I_OP6_STD
                    else -> UInt32.ZERO
                }

                if (!imm.fitsInSignedOrUnsigned(16)) {
                    instr.addError("$imm exceeds 16 bits")
                }

                val imm16 = try {
                    imm.toInt16().toUInt16().toUInt32()
                } catch (e: Exception) {
                    instr.addWarn("Interpreted as unsigned!")
                    imm.toUInt16().toUInt32()
                }

                val bundle = (opc shl 26) or (rc shl 21) or (rb shl 16) or imm16
                context.section.content.put(bundle)
            }

            S_INDEX_TYPE -> {
                val funct6 = IkrR2Const.FUNCT6_R2
                val rb = regs[0]
                val ra = regs[1]
                val rc = regs[2]
                val opc = when (this@IkrR2InstrT) {
                    STR -> IkrR2Const.R2_OP6_STR
                    else -> UInt32.ZERO
                }
                val bundle = (funct6 shl 26) or (rc shl 21) or (rb shl 16) or (opc shl 10) or ra
                context.section.content.put(bundle)
            }

            B_DISP18_TYPE -> {} // Evaluate in pass 2
            B_DISP26_TYPE -> {} // Evaluate in pass 2
            B_REG_TYPE -> {
                val funct6 = IkrR2Const.FUNCT6_R1

                val rb = regs[0]

                val opc = when (this@IkrR2InstrT) {
                    JMP -> IkrR2Const.R1_OP6_JMP
                    JSR -> IkrR2Const.R1_OP6_JSR
                    else -> UInt32.ZERO
                }

                val bundle = (funct6 shl 26) or (rb shl 16) or (opc shl 10)
                context.section.content.put(bundle)
            }
        }
    }

    override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
        val relExprs = instr.exprs.map { relIntEvaluator.evaluate(it, context) }
        val regs = instr.regs.map { it.type.address.toUInt32() }

        when (paramType) {
            B_DISP26_TYPE -> {
                val displacement = relExprs[0]

                val opc = when (this@IkrR2InstrT) {
                    BRA -> IkrR2Const.B_OP6_BRA
                    BSR -> IkrR2Const.B_OP6_BSR
                    else -> UInt32.ZERO
                }

                if (!displacement.fitsInSignedOrUnsigned(26)) {
                    instr.addError("$displacement exceeds 26 bits")
                }

                val bundle = (opc shl 26) or displacement.toInt32().toUInt32().lowest(26)
                context.section.content[context.offsetInSection] = bundle
            }

            B_DISP18_TYPE -> {
                val displacement = relExprs[0]

                val opc = IkrR2Const.B_OP6_COND_BRA
                val rc = regs[0]

                val funct3 = when (this@IkrR2InstrT) {
                    BEQ -> IkrR2Const.B_FUNCT3_BEQ
                    BNE -> IkrR2Const.B_FUNCT3_BNE
                    BLT -> IkrR2Const.B_FUNCT3_BLT
                    BGT -> IkrR2Const.B_FUNCT3_BGT
                    BLE -> IkrR2Const.B_FUNCT3_BLE
                    BGE -> IkrR2Const.B_FUNCT3_BGE
                    else -> UInt32.ZERO
                }

                if (!displacement.fitsInSignedOrUnsigned(18)) {
                    instr.addError("$displacement exceeds 18 bits")
                }

                val bundle = (opc shl 26) or (rc shl 21) or (funct3 shl 18) or displacement.toInt32().toUInt32().lowest(18)
                context.section.content[context.offsetInSection] = bundle
            }

            else -> instr.addError("Unexpected pass 2 binary generation")
        }
    }

    override val typeName: String
        get() = name


}