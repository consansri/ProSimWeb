package cengine.lang.asm.target.ikrmini

import cengine.lang.asm.AsmTreeParser
import cengine.lang.asm.gas.AsmBackend
import cengine.lang.asm.gas.AsmCodeGenerator
import cengine.lang.asm.psi.AsmInstruction
import cengine.lang.asm.psi.AsmInstructionT
import cengine.util.integer.UInt16
import cengine.lang.asm.target.ikrmini.IkrMiniParamT.*
import cengine.psi.parser.PsiBuilder

enum class IkrMiniInstrT(override val keyWord: String, val opcode: UInt16, val paramType: IkrMiniParamT, val description: String) : AsmInstructionT {
    // Data Transport
    LOAD_IMM("load", UInt16(0x010CU), IMM, "load AC"),
    LOAD_DIR("load", UInt16(0x020CU), DIR, "load AC"),
    LOAD_IND("load", UInt16(0x030CU), IND, "load AC"),
    LOAD_IND_OFF("load", UInt16(0x040CU), IND_OFF, "load AC"),

    LOADI("loadi", UInt16(0x200CU), IMPL, "load indirect"),
    STORE_DIR("store", UInt16(0x3200U), DIR, "store AC at address"),
    STORE_IND("store", UInt16(0x3300U), IND, "store AC at address"),
    STORE_IND_OFF("store", UInt16(0x3400U), IND_OFF, "store AC at address"),

    // Data Manipulation
    AND_IMM("and", UInt16(0x018AU), IMM, "and (logic)"),
    AND_DIR("and", UInt16(0x028AU), DIR, "and (logic)"),
    AND_IND("and", UInt16(0x038AU), IND, "and (logic)"),
    AND_IND_OFF("and", UInt16(0x048AU), IND_OFF, "and (logic)"),
    OR_IMM("or", UInt16(0x0188U), IMM, "or (logic)"),
    OR_DIR("or", UInt16(0x0288U), DIR, "or (logic)"),
    OR_IND("or", UInt16(0x0388U), IND, "or (logic)"),
    OR_IND_OFF("or", UInt16(0x0488U), IND_OFF, "or (logic)"),
    XOR_IMM("xor", UInt16(0x0189U), IMM, "xor (logic)"),
    XOR_DIR("xor", UInt16(0x0289U), DIR, "xor (logic)"),
    XOR_IND("xor", UInt16(0x0389U), IND, "xor (logic)"),
    XOR_IND_OFF("xor", UInt16(0x0489U), IND_OFF, "xor (logic)"),
    ADD_IMM("add", UInt16(0x018DU), IMM, "add"),
    ADD_DIR("add", UInt16(0x028DU), DIR, "add"),
    ADD_IND("add", UInt16(0x038DU), IND, "add"),
    ADD_IND_OFF("add", UInt16(0x048DU), IND_OFF, "add"),
    ADDC_IMM("addc", UInt16(0x01ADU), IMM, "add with carry"),
    ADDC_DIR("addc", UInt16(0x02ADU), DIR, "add with carry"),
    ADDC_IND("addc", UInt16(0x03ADU), IND, "add with carry"),
    ADDC_IND_OFF("addc", UInt16(0x04ADU), IND_OFF, "add with carry"),
    SUB_IMM("sub", UInt16(0x018EU), IMM, "sub"),
    SUB_DIR("sub", UInt16(0x028EU), DIR, "sub"),
    SUB_IND("sub", UInt16(0x038EU), IND, "sub"),
    SUB_IND_OFF("sub", UInt16(0x048EU), IND_OFF, "sub"),
    SUBC_IMM("subc", UInt16(0x01AEU), IMM, "sub with carry"),
    SUBC_DIR("subc", UInt16(0x02AEU), DIR, "sub with carry"),
    SUBC_IND("subc", UInt16(0x03AEU), IND, "sub with carry"),
    SUBC_IND_OFF("subc", UInt16(0x04AEU), IND_OFF, "sub with carry"),

    LSL("lsl", UInt16(0x00A0U), IMPL, "logic shift left"),
    LSL_DIR("lsl", UInt16(0x0220U), DIR, "logic shift left"),
    LSL_IND("lsl", UInt16(0x0320U), IND, "logic shift left"),
    LSL_IND_OFF("lsl", UInt16(0x0420U), IND_OFF, "logic shift left"),
    LSR("lsr", UInt16(0x00A1U), IMPL, "logic shift right"),
    LSR_DIR("lsr", UInt16(0x0221U), DIR, "logic shift right"),
    LSR_IND("lsr", UInt16(0x0321U), IND, "logic shift right"),
    LSR_IND_OFF("lsr", UInt16(0x0421U), IND_OFF, "logic shift right"),
    ROL("rol", UInt16(0x00A2U), IMPL, "rotate left"),
    ROL_DIR("rol", UInt16(0x0222U), DIR, "rotate left"),
    ROL_IND("rol", UInt16(0x0322U), IND, "rotate left"),
    ROL_IND_OFF("rol", UInt16(0x0422U), IND_OFF, "rotate left"),
    ROR("ror", UInt16(0x00A3U), IMPL, "rotate right"),
    ROR_DIR("ror", UInt16(0x0223U), DIR, "rotate right"),
    ROR_IND("ror", UInt16(0x0323U), IND, "rotate right"),
    ROR_IND_OFF("ror", UInt16(0x0423U), IND_OFF, "rotate right"),
    ASL("asl", UInt16(0x00A4U), IMPL, "arithmetic shift left"),
    ASL_DIR("asl", UInt16(0x0224U), DIR, "arithmetic shift left"),
    ASL_IND("asl", UInt16(0x0324U), IND, "arithmetic shift left"),
    ASL_IND_OFF("asl", UInt16(0x0424U), IND_OFF, "arithmetic shift left"),
    ASR("asr", UInt16(0x00A5U), IMPL, "arithmetic shift right"),
    ASR_DIR("asr", UInt16(0x0225U), DIR, "arithmetic shift right"),
    ASR_IND("asr", UInt16(0x0325U), IND, "arithmetic shift right"),
    ASR_IND_OFF("asr", UInt16(0x0425U), IND_OFF, "arithmetic shift right"),

    RCL("rcl", UInt16(0x00A6U), IMPL, "rotate left with carry"),
    RCL_IMM("rcl", UInt16(0x0126U), IMM, "rotate left with carry"),
    RCL_DIR("rcl", UInt16(0x0226U), DIR, "rotate left with carry"),
    RCL_IND("rcl", UInt16(0x0326U), IND, "rotate left with carry"),
    RCL_IND_OFF("rcl", UInt16(0x0426U), IND_OFF, "rotate left with carry"),
    RCR("rcr", UInt16(0x00A7U), IMPL, "rotate right with carry"),
    RCR_IMM("rcr", UInt16(0x0127U), IMM, "rotate right with carry"),
    RCR_DIR("rcr", UInt16(0x0227U), DIR, "rotate right with carry"),
    RCR_IND("rcr", UInt16(0x0327U), IND, "rotate right with carry"),
    RCR_IND_OFF("rcr", UInt16(0x0427U), IND_OFF, "rotate right with carry"),
    NOT("not", UInt16(0x008BU), IMPL, "invert (logic not)"),
    NOT_DIR("not", UInt16(0x020BU), DIR, "invert (logic not)"),
    NOT_IND("not", UInt16(0x030BU), IND, "invert (logic not)"),
    NOT_IND_OFF("not", UInt16(0x040BU), IND_OFF, "invert (logic not)"),

    NEG_DIR("neg", UInt16(0x024EU), DIR, "negotiate"),
    NEG_IND("neg", UInt16(0x034EU), IND, "negotiate"),
    NEG_IND_OFF("neg", UInt16(0x044EU), IND_OFF, "negotiate"),

    CLR("clr", UInt16(0x004CU), IMPL, "clear"),

    INC("inc", UInt16(0x009CU), IMPL, "increment (+1)"),
    INC_DIR("inc", UInt16(0x021CU), DIR, "increment (+1)"),
    INC_IND("inc", UInt16(0x031CU), IND, "increment (+1)"),
    INC_IND_OFF("inc", UInt16(0x041CU), IND_OFF, "increment (+1)"),
    DEC("dec", UInt16(0x009FU), IMPL, "decrement (-1)"),
    DEC_DIR("dec", UInt16(0x021FU), DIR, "decrement (-1)"),
    DEC_IND("dec", UInt16(0x031FU), IND, "decrement (-1)"),
    DEC_IND_OFF("dec", UInt16(0x041FU), IND_OFF, "decrement (-1)"),

    // Unconditional Branches
    BSR("bsr", UInt16(0x510CU), DEST, "branch and save return address in AC"),
    JMP("jmp", UInt16(0x4000U), IMPL, "jump to address in AC"),
    BRA("bra", UInt16(0x6101U), DEST, "branch"),

    // Conditional Branches
    BHI("bhi", UInt16(0x6102U), DEST, "branch if higher"),
    BLS("bls", UInt16(0x6103U), DEST, "branch if lower or same"),
    BCC("bcc", UInt16(0x6104U), DEST, "branch if carry clear"),
    BCS("bcs", UInt16(0x6105U), DEST, "branch if carry set"),
    BNE("bne", UInt16(0x6106U), DEST, "branch if not equal"),
    BEQ("beq", UInt16(0x6107U), DEST, "branch if equal"),
    BVC("bvc", UInt16(0x6108U), DEST, "branch if overflow clear"),
    BVS("bvs", UInt16(0x6109U), DEST, "branch if overflow set"),
    BPL("bpl", UInt16(0x610AU), DEST, "branch if positive"),
    BMI("bmi", UInt16(0x610BU), DEST, "branch if negative"),
    BGE("bge", UInt16(0x610CU), DEST, "branch if greater or equal"),
    BLT("blt", UInt16(0x610DU), DEST, "branch if less than"),
    BGT("bgt", UInt16(0x610EU), DEST, "branch if greater than"),
    BLE("ble", UInt16(0x610FU), DEST, "branch if less or equal")

    ;

    override val typeName: String
        get() = name

    override fun PsiBuilder.parse(asmTreeParser: AsmTreeParser, marker: PsiBuilder.Marker): Boolean {
        skipWhitespaceAndComments()

        with(asmTreeParser) {
            when (paramType) {
                IND -> {
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                IND_OFF -> {
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                DIR -> {
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (currentIs("(")) return false // If this is the case it could be a IND expression!
                    if (parseExpression() == null) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                IMM -> {
                    if (!expect("#")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false
                }

                DEST -> {
                    if (parseExpression() == null) return false
                }

                IMPL -> {}
            }
        }

        return true
    }

    override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass1BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
        if (paramType == DEST) {
            context.section.queueLateInit(instr, paramType.size)
            return
        }

        val exprs = instr.exprs.map { it to absIntEvaluator.evaluate(it, context) }

        when (paramType) {
            IND -> {
                context.section.content.put(opcode)
                val (expr, eval) = exprs.getOrNull(0) ?: run { instr.addError("No expression given"); return }
                if (!eval.fitsInSignedOrUnsigned(16)) expr.addError("$eval does not fit in 16 bits")

                val imm = if (eval.fitsInSigned(16)) eval.toInt16().toUInt16() else eval.toUInt16()
                context.section.content.put(imm)
            }

            IND_OFF -> {
                context.section.content.put(opcode)
                val (addrExpr, addrEval) = exprs.getOrNull(0) ?: run { instr.addError("No address given"); return }
                val (offExpr, offEval) = exprs.getOrNull(1) ?: run { instr.addError("No offset given"); return }
                if (!addrEval.fitsInSignedOrUnsigned(16)) addrExpr.addError("$addrEval does not fit in 16 bits")
                if (!offEval.fitsInSignedOrUnsigned(16)) offExpr.addError("$offEval does not fit in 16 bits")
                val address = if (addrEval.fitsInSigned(16)) addrEval.toInt16().toUInt16() else addrEval.toUInt16()
                val offset = if (offEval.fitsInSigned(16)) offEval.toInt16().toUInt16() else offEval.toUInt16()
                context.section.content.put(address)
                context.section.content.put(offset)
            }

            DIR -> {
                context.section.content.put(opcode)
                val (expr, eval) = exprs.getOrNull(0) ?: run { instr.addError("No expression given"); return }
                if (!eval.fitsInSignedOrUnsigned(16)) expr.addError("$eval does not fit in 16 bits")

                val imm = if (eval.fitsInSigned(16)) eval.toInt16().toUInt16() else eval.toUInt16()
                context.section.content.put(imm)
            }

            IMM -> {
                context.section.content.put(opcode)
                val (expr, eval) = exprs.getOrNull(0) ?: run { instr.addError("No expression given"); return }
                if (!eval.fitsInSignedOrUnsigned(16)) expr.addError("$eval does not fit in 16 bits")

                val imm = if (eval.fitsInSigned(16)) eval.toInt16().toUInt16() else eval.toUInt16()
                context.section.content.put(imm)
            }

            IMPL -> {
                context.section.content.put(opcode)
            }

            DEST -> {} // Already handled
        }
    }

    override fun <T : AsmCodeGenerator.Section> AsmBackend<T>.pass2BinaryGeneration(instr: AsmInstruction, context: AsmBackend<T>.AsmEvaluationContext) {
        val relExprs = instr.exprs.map { it to relIntEvaluator.evaluate(it, context) }

        when (paramType) {
            DEST -> {
                context.section.content.set(context.offsetInSection, opcode)
                val (targetExpr, relative) = relExprs.getOrNull(0) ?: run { instr.addError("No expression given"); return }
                if (!relative.fitsInUnsigned(16)) targetExpr.addError("$relative does not fit in 16 bits")
                context.section.content.set(context.offsetInSection + 1, relative.toUInt16())
            }

            else -> {} // Nothing to do
        }
    }


}