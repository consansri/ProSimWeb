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


    fun PsiBuilder.parse(asmParser: AsmParser, marker: PsiBuilder.Marker) {

        skipWhitespaceAndComments()

        peek() ?: run {
            error("")
            return
        }

        with(asmParser) {

            when (this@IkrR2ParamType) {
                I_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(":")) return
                    if (!expect("=")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(",")) return
                    skipWhitespaceAndComments()
                    if (!expect("#")) return
                    parseExpression()
                }

                R2_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(":")) return
                    if (!expect("=")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(",")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                }

                R1_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(":")) return
                    if (!expect("=")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                }

                L_OFF_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(":")) return
                    if (!expect("=")) return
                    skipWhitespaceAndComments()
                    if (!expect("(")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(",")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(")")) return
                }

                L_INDEX_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(":")) return
                    if (!expect("=")) return
                    skipWhitespaceAndComments()
                    if (!expect("(")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(",")) return
                    skipWhitespaceAndComments()
                    parseExpression()
                    skipWhitespaceAndComments()
                    if (!expect(")")) return
                }

                S_OFF_TYPE -> {
                    if (!expect("(")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(",")) return
                    skipWhitespaceAndComments()
                    parseExpression()
                    skipWhitespaceAndComments()
                    if (!expect(")")) return
                    skipWhitespaceAndComments()
                    if (!expect(":")) return
                    if (!expect("=")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return

                }

                S_INDEX_TYPE -> {
                    if (!expect("(")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(",")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(")")) return
                    skipWhitespaceAndComments()
                    if (!expect(":")) return
                    if (!expect("=")) return
                    skipWhitespaceAndComments()
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                }

                B_DISP18_TYPE -> {
                    if (!expect(*IkrR2BaseRegs.allNames)) return
                    skipWhitespaceAndComments()
                    if (!expect(",")) return
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
            return
        }
    }

}