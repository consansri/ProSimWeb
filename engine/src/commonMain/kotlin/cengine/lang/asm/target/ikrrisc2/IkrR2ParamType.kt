package cengine.lang.asm.target.ikrrisc2

import cengine.lang.asm.AsmParser
import cengine.lang.asm.target.ikrrisc2.IkrR2BaseRegs.Companion.parseIkrR2BaseReg
import cengine.psi.parser.PsiBuilder

enum class IkrR2ParamType(val exampleString: String) {
    I_TYPE("rc := rb,#imm16"),
    R2_TYPE("rc := rb,ra"),
    R1_TYPE("rc := rb"),
    L_OFF_TYPE("rc := (rb,disp16)"),
    L_INDEX_TYPE("rc := (rb,ra)"),
    S_OFF_TYPE("(rb,disp16) := rc"),
    S_INDEX_TYPE("(rb,ra) := rc"),
    B_DISP18_TYPE("rc,disp18"),
    B_DISP26_TYPE("disp26"),
    B_REG_TYPE("rb");


    fun PsiBuilder.parse(asmParser: AsmParser): Boolean {

        skipWhitespaceAndComments()

        peek() ?: run {
            error("")
            return false
        }

        with(asmParser) {

            when (this@IkrR2ParamType) {
                I_TYPE -> {
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("#")) return false
                    parseExpression()
                }

                R2_TYPE -> {
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                }

                R1_TYPE -> {
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                }

                L_OFF_TYPE -> {
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    parseExpression()
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                L_INDEX_TYPE -> {
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                S_OFF_TYPE -> {
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    parseExpression()
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false

                }

                S_INDEX_TYPE -> {
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!parseIkrR2BaseReg()) return false
                }

                B_DISP18_TYPE -> {
                    if (!parseIkrR2BaseReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    parseExpression()
                }

                B_DISP26_TYPE -> {
                    parseExpression()
                }

                B_REG_TYPE -> {
                    parseIkrR2BaseReg()
                }
            }
        }

        return true
    }

}