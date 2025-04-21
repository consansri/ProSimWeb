package cengine.lang.asm.target.ikrrisc2

import cengine.lang.asm.AsmParser
import cengine.lang.asm.gas.AsmBackend
import cengine.lang.asm.gas.AsmCodeGenerator
import cengine.lang.asm.psi.AsmInstruction
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.asm.target.ikrrisc2.IkrR2ParamType.*
import cengine.psi.parser.PsiBuilder


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

    override fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker) {
        skipWhitespaceAndComments()

        parse(asmParser, marker)

        marker.done(this@IkrR2InstrT)
    }

    override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
        TODO("Not yet implemented")
    }

    override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
        TODO("Not yet implemented")
    }

    override val typeName: String
        get() = name


}