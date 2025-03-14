package cengine.lang.asm.ast.target.riscv.rv64

import cengine.lang.asm.ast.Component.*
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.ast.target.riscv.RVBaseRegs


enum class RV64ParamType(val pseudo: Boolean, val exampleString: String, val rule: Rule?) {
    // NORMAL INSTRUCTIONS
    RD_I20(
        false, "rd, imm20",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR)
            )
        }
    ), // rd, imm
    RD_Off12(
        false, "rd, imm12(rs)",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR),
                Specific("("),
                Reg(RVBaseRegs.entries),
                Specific(")")
            )
        }
    ), // rd, imm12(rs)
    RS2_Off12(
        false, "rs2, imm12(rs1)",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR),
                Specific("("),
                Reg(RVBaseRegs.entries),
                Specific(")")
            )
        }
    ), // rs2, imm5(rs1)
    RD_RS1_RS2(
        false, "rd, rs1, rs2",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                Reg(RVBaseRegs.entries),
                Specific(","),
                Reg(RVBaseRegs.entries)
            )
        }
    ), // rd, rs1, rs2
    RD_RS1_I12(
        false, "rd, rs1, imm12",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                Reg(RVBaseRegs.entries),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR)
            )
        }
    ), // rd, rs, imm
    RD_RS1_SHAMT6(
        false, "rd, rs1, shamt6",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                Reg(RVBaseRegs.entries),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR)
            )
        }
    ), // rd, rs, shamt
    RS1_RS2_LBL(false, "rs1, rs2, lbl", Rule {
        Seq(
            Reg(RVBaseRegs.entries),
            Specific(","),
            Reg(RVBaseRegs.entries),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    PRED_SUCC(false, "pred, succ", Rule {
        Seq(
            SpecNode(ASNodeType.INT_EXPR),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR),
        )
    }),
    CSR_RD_OFF12_RS1(
        false, "rd, csr12, rs1",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                XOR(
                    Reg(isNotContainedBy = RVBaseRegs.entries),
                    SpecNode(ASNodeType.INT_EXPR)
                ),
                Specific(","),
                Reg(RVBaseRegs.entries)
            )
        }
    ),
    CSR_RD_OFF12_UIMM5(
        false, "rd, offset, uimm5",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                XOR(
                    Reg(isNotContainedBy = RVBaseRegs.entries),
                    SpecNode(ASNodeType.INT_EXPR)
                ),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR)
            )
        }
    ),

    // PSEUDO INSTRUCTIONS
    PS_RD_LI_I64(
        true, "rd, imm64",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR)
            )
        }
    ), // rd, imm64
    PS_RS1_Jlbl(
        true, "rs, jlabel",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR)
            )
        }
    ), // rs, label
    PS_RD_Albl(
        true, "rd, alabel",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                SpecNode(ASNodeType.INT_EXPR)
            )
        }
    ), // rd, label
    PS_lbl(true, "jlabel", Rule {
        Seq(SpecNode(ASNodeType.INT_EXPR))
    }),  // label
    PS_RD_RS1(
        true, "rd, rs",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                Reg(RVBaseRegs.entries)
            )
        }
    ), // rd, rs
    PS_RS1(
        true, "rs1",
        Rule {
            Seq(Reg(RVBaseRegs.entries))
        }
    ),
    PS_CSR_RS1(
        true, "csr, rs1",
        Rule {
            Seq(
                XOR(
                    Reg(isNotContainedBy = RVBaseRegs.entries),
                    SpecNode(ASNodeType.INT_EXPR)
                ),
                Specific(","),
                Reg(RVBaseRegs.entries)
            )
        }
    ),
    PS_RD_CSR(
        true, "rd, csr",
        Rule {
            Seq(
                Reg(RVBaseRegs.entries),
                Specific(","),
                XOR(
                    Reg(isNotContainedBy = RVBaseRegs.entries),
                    SpecNode(ASNodeType.INT_EXPR)
                ),
            )
        }
    ),

    // NONE PARAM INSTR
    NONE(false, "none", null),
    PS_NONE(true, "none", null);
}