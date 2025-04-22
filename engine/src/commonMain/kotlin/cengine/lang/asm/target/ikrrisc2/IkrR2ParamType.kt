package cengine.lang.asm.target.ikrrisc2

import cengine.lang.asm.AsmParser
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


    fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker): Boolean {

        skipWhitespaceAndComments()

        peek() ?: run {
            error("")
            return false
        }

        with(asmParser) {

            when (this@IkrR2ParamType) {
                I_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("#")) return false
                    parseExpression()
                }

                R2_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                }

                R1_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                }

                L_OFF_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                L_INDEX_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    parseExpression()
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                S_OFF_TYPE -> {
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
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
                    if (!expect(*IkrR2BaseRegs.allNames)) return false

                }

                S_INDEX_TYPE -> {
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(":")) return false
                    if (!expect("=")) return false
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                }

                B_DISP18_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    parseExpression()
                }

                B_DISP26_TYPE -> {
                    parseExpression()
                }

                B_REG_TYPE -> {
                    expect(*IkrR2BaseRegs.allNames)
                }
            }
        }

        return true
    }

}