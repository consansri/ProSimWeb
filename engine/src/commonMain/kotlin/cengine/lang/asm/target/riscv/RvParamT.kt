package cengine.lang.asm.target.riscv

import cengine.lang.asm.AsmTreeParser
import cengine.lang.asm.target.riscv.RvRegT.FloatT.Companion.parseRvFloatReg
import cengine.lang.asm.target.riscv.RvRegT.IntT.Companion.parseRvIntReg
import cengine.lang.asm.target.riscv.RvRegT.RvCsrT.Companion.parseRvCsrReg
import cengine.lang.asm.target.riscv.RvRegT.VectorMaskT.parseVectorMask
import cengine.lang.asm.target.riscv.RvRegT.VectorT.Companion.parseRvVectorReg
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.PsiBuilder

enum class RvParamT(val exampleString: String) {
    // === No Operands ===
    NONE(""), // e.g., fence, fence.i, ecall, ebreak, mret, sret, uret, wfi, c.ebreak, c.nop, ret (pseudo)

    // === Base Integer Instructions (RV32I/RV64I) ===
    RD_RS1_RS2("rd, rs1, rs2"),        // R-type (add, sub, xor, etc.)
    RD_RS1_IMM12("rd, rs1, imm12"),     // I-type (addi, slti, xori, etc.)
    RD_IMM12_RS1("rd, offset12(rs1)"), // I-type (lb, lh, lw, lwu, ld)
    RS2_IMM12_RS1("rs2, offset12(rs1)"),// S-type (sb, sh, sw, sd)
    RS1_RS2_LABEL("rs1, rs2, label"),  // B-type (beq, bne, blt, etc.) - Label resolves to imm12 offset
    RD_IMM20("rd, imm20"),             // U-type (lui, auipc)
    RD_LABEL("rd, label"),             // J-type (jal) - Label resolves to imm20 offset
    LABEL("label"),                    // J-type (jal with rd=x0, pseudo `j`) - Label resolves to imm20 offset
    RD_RS1_SHAMT("rd, rs1, shamt"),     // I-type shifts (slli, srli, srai) - shamt depends on XLEN

    // === M Standard Extension (Multiply/Divide) ===
    // Uses R-type: RD_RS1_RS2

    // === A Standard Extension (Atomic Instructions) ===
    RD_RS2_RS1ADDR("rd, rs2, (rs1)"),           // Basic atomic memory ops (amoswap, amoadd, etc.)
    RD_RS2_RS1ADDR_AQRL("rd, rs2, (rs1), aqrl"), // Atomics with acquire (aq) and/or release (rl) flags (syntax varies, sometimes .aq/.rl suffix)
    RD_RS1ADDR("rd, (rs1)"),                   // Load-reserved (lr.w, lr.d)
    RD_RS2_RS1ADDR_STORE("rd, rs2, (rs1)"),      // Store-conditional (sc.w, sc.d) - rd indicates success/failure

    // === F, D Standard Extensions (Floating-Point) ===
    FRD_FRS1_FRS2("frd, frs1, frs2"),              // FP R-type (fadd, fsub, fmul, fdiv, etc.)
    FRD_FRS1("frd, frs1"),                         // FP unary ops (fsqrt, fabs, fneg, fmv.s, fmv.d)
    FRD_RS1("frd, rs1"),                           // FP conversion/move (fcvt.s.w, fcvt.d.l, fmv.x.w, fmv.d.x etc.)
    RD_FRS1("rd, frs1"),                           // FP conversion/move (fcvt.w.s, fcvt.l.d, fmv.w.x, etc.)
    FRD_FRS1_FRS2_FRS3_RM("frd, frs1, frs2, frs3, rm"),// FP R4-type (fmadd, fmsub, fnmsub, fnmadd) - rm is optional rounding mode
    FRD_FRS1_FRS2_RM("frd, frs1, frs2, rm"),       // FP R-type with optional rounding mode (some conversions, compares)
    RD_FRS1_FRS2("rd, frs1, frs2"),                // FP compares (feq, flt, fle), fclass
    FRD_IMM12_RS1("frd, offset12(rs1)"),           // FP Load (flw, fld)
    FRS2_IMM12_RS1("frs2, offset12(rs1)"),         // FP Store (fsw, fsd)

    // === Zicsr Standard Extension (Control and Status Registers) ===
    RD_CSR_RS1("rd, csr, rs1"),     // CSRRW, CSRRS, CSRRC
    RD_CSR_UIMM5("rd, csr, uimm5"), // CSRRWI, CSRRSI, CSRRCI
    CSR_RS1("csr, rs1"),            // Pseudo-op CSRW, CSRS, CSRC
    RD_CSR("rd, csr"),              // Pseudo-op CSRR

    // === C Standard Extension (Compressed Instructions) - Syntax focused ===
    // Note: rd', rs1', rs2' often denote restricted register sets (x8-x15)
    // Note: Offsets/immediates have specific scaling/ranges in compressed format
    CRD_IMM6("c.rd, imm"),                  // c.li (non-zero imm), c.lui (non-zero imm)
    CRD_NZIIMM6("c.rd, nzimm"),             // c.addi16sp (rd=sp), c.lui (rd!=x0,x2)
    CRDPRIME_CRS1PRIME_NZUIMM6("c.rd', rs1', nzuimm"), // c.addi, c.addiw, c.slli
    CRDPRIME_IMM6_SP("c.rd', offset(sp)"), // c.lwsp (rd!=x0), c.ldsp, c.lqsp, c.flwsp, c.fldsp
    CRS2_IMM6_SP("c.rs2, offset(sp)"),     // c.swsp, c.sdsp, c.sqsp, c.fswsp, c.fsdsp
    CRD_CRS1("c.rd, rs1"),                 // c.mv (rd!=x0, rs1!=x0), c.jalr (rd=x1), c.jr (rd=x0)
    CRD_CRS1_ADD("c.rd, rs1"),             // c.add (rd!=x0, rs1!=x0) - Same syntax as c.mv, different opcode field
    CRS1PRIME_LABEL("c.rs1', label"),      // c.beqz, c.bnez
    CLABEL("label"),                       // c.j, c.jal (rd=x1)
    CRDPRIME_IMM6_CRS1PRIME("c.rd', offset(rs1')"), // c.lw, c.ld, c.lq, c.flw, c.fld
    CRS2PRIME_IMM6_CRS1PRIME("c.rs2', offset(rs1')"),// c.sw, c.sd, c.sq, c.fsw, c.fsd

    // CRDPRIME_CRS1PRIME_CRS2PRIME("c.rd', rs1', rs2'"), // c.sub, c.xor, c.or, c.and, c.subw, c.addw
    CRDPRIME_CRS1PRIME_NZUIMM5("c.rd', rs1', nzuimm[5]"), // c.srli, c.srai (RV64C/RV128C use [6])
    CRDPRIME_CRS1PRIME_IMM6_ANDI("c.rd', rs1', imm"), // c.andi

    // === V Standard Extension (Vector Instructions) ===
    // Basic Arithmetic (Masked versions often append ", vm")
    VD_VS1_VS2("vd, vs1, vs2"),        // V V V (vadd.vv)
    VD_RS1_VS2("vd, rs1, vs2"),        // V S V (vadd.vx) // Note: order differs vs spec for consistency vd,src1,src2
    VD_IMM5_VS2("vd, imm5, vs2"),      // V I V (vadd.vi)
    VD_VS1_VS2_VM("vd, vs1, vs2, vm"), // V V V Masked (vadd.vv)
    VD_RS1_VS2_VM("vd, rs1, vs2, vm"), // V S V Masked (vadd.vx)
    VD_IMM5_VS2_VM("vd, imm5, vs2, vm"), // V I V Masked (vadd.vi)

    // Widening/Narrowing often involve 2*SEW vs SEW registers but syntax is similar

    // Permutation Instructions
    VD_VS1_RS2("vd, vs1, rs2"),        // V V S (vmv.v.x, vslideup.vx)
    VD_VS1_IMM5("vd, vs1, imm5"),      // V V I (vslideup.vi)
    RD_VS2("rd, vs2"),                 // S V (vmv.x.s)

    // Loads/Stores (Unit Stride, Masked versions often append ", vm")
    VD_RS1ADDR("vd, (rs1)"),           // Vector Load unit stride (vleN.v)
    VS3_RS1ADDR("vs3, (rs1)"),         // Vector Store unit stride (vseN.v) - vs3 holds data
    VD_RS1ADDR_VM("vd, (rs1), vm"),    // Vector Load unit stride masked
    VS3_RS1ADDR_VM("vs3, (rs1), vm"),  // Vector Store unit stride masked

    // Loads/Stores (Strided, Masked versions often append ", vm")
    VD_RS1ADDR_RS2STRIDE("vd, (rs1), rs2"), // Vector Load strided (vlseN.v)
    VS3_RS1ADDR_RS2STRIDE("vs3, (rs1), rs2"),// Vector Store strided (vsseN.v)
    VD_RS1ADDR_RS2STRIDE_VM("vd, (rs1), rs2, vm"),
    VS3_RS1ADDR_RS2STRIDE_VM("vs3, (rs1), rs2, vm"),

    // Loads/Stores (Indexed, Masked versions often append ", vm")
    VD_RS1ADDR_VS2INDEXED("vd, (rs1), vs2"), // Vector Load indexed unordered/ordered (vluxeiN.v, vloxeiN.v)
    VS3_RS1ADDR_VS2INDEXED("vs3, (rs1), vs2"),// Vector Store indexed unordered/ordered (vsuxeiN.v, vsoxeiN.v)
    VD_RS1ADDR_VS2INDEXED_VM("vd, (rs1), vs2, vm"),
    VS3_RS1ADDR_VS2INDEXED_VM("vs3, (rs1), vs2, vm"),

    // Segment Loads/Stores (Similar patterns but multiple vd/vs3 registers, or implicit sequence)
    // Syntax varies, often like unit stride but instruction implies multiple registers (e.g., vlseg2e8.v vd, (rs1))
    // Representing with the base unit-stride pattern for now, parser needs context.

    // Whole Register Load/Store -> Covered by VD_RS1ADDR / VS3_RS1ADDR syntax
    // VLREG_RS1ADDR("vl[1-8]r, (rs1)"), // Syntax: vd, (rs1) -> Use VD_RS1ADDR
    // VSREG_RS1ADDR("vs[1-8]r, (rs1)"), // Syntax: vs3, (rs1) -> Use VS3_RS1ADDR

    // Configuration Setting Instructions
    VSETVL_RD_RS1_RS2("rd, rs1, rs2"),     // vsetvl
    VSETVLI_RD_RS1_IMM11("rd, rs1, imm11"),// vsetvli (imm encodes vtype) - simplified imm representation

    // === Fence Instructions ===
    PRED_SUCC("pred, succ"), // fence instruction operands (numeric or symbolic like 'iorw')
    OPT_RS1_RS2("rs1, rs2"), // e.g. sfence.vma rs1, rs2

    // === Pseudo Instructions (Common Syntactic Forms) ===
    PS_RD_IMM("rd, immediate"),    // li (immediate can be large), specific immediate pseudo-ops
    PS_RD_SYMBOL("rd, symbol"),    // la, lla (load address)
    PS_RD_RS1("rd, rs1"),          // mv
    PS_RS1_LABEL("rs1, label"),    // beqz, bnez, bgez, etc. (branch pseudo-ops)
    PS_RS1("rs1"),                 // jr, jalr (with implicit rd/imm), sometimes call/tail rs1
    PS_OFFSET_RS1("offset(rs1)"),  // call offset(reg), tail offset(reg)
    PS_NONE("");                   // ret, nop

    fun PsiBuilder.parse(asmTreeParser: AsmTreeParser): Boolean {

        with(asmTreeParser) {


            when (this@RvParamT) {
                // === No Operands ===
                NONE, PS_NONE -> { /* No operands */
                }

                // === Base Integer Instructions (RV32I/RV64I) ===
                RD_RS1_RS2 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2
                }

                RD_RS1_IMM12, RD_RS1_SHAMT -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // imm12 or shamt
                }

                RD_IMM12_RS1 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // offset12 expr
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 base address
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                RS2_IMM12_RS1 -> {
                    if (!parseRvIntReg()) return false // rs2 source data
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // offset12 expr
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 base address
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                RS1_RS2_LABEL -> {
                    if (!parseRvIntReg()) return false // rs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // label
                }

                RD_IMM20 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // imm20
                }

                RD_LABEL -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // label
                }

                LABEL, CLABEL -> {
                    if (parseExpression() == null) return false // label
                    skipWhitespaceAndComments()
                    if (currentIs(",")) return false // no other operands expected!
                }

                // === A Standard Extension (Atomic Instructions) ===
                RD_RS2_RS1ADDR, RD_RS2_RS1ADDR_STORE -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                RD_RS2_RS1ADDR_AQRL -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    // Optional AQ/RL flags (syntax varies, often part of mnemonic)
                    // If present as separate operands:
                    skipWhitespaceAndComments()
                    if (currentIs(",")) {
                        advance() // Consume comma
                        skipWhitespaceAndComments()
                        // Parse aq/rl flags - using expression for flexibility
                        if (parseExpression() == null) return false
                    }
                }

                RD_RS1ADDR -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                // === F, D Standard Extensions (Floating-Point) ===
                FRD_FRS1_FRS2 -> {
                    if (!parseRvFloatReg()) return false // frd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs2
                }

                FRD_FRS1 -> {
                    if (!parseRvFloatReg()) return false // frd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs1
                }

                FRD_RS1 -> {
                    if (!parseRvFloatReg()) return false // frd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                }

                RD_FRS1 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs1
                }

                FRD_FRS1_FRS2_FRS3_RM -> {
                    if (!parseRvFloatReg()) return false // frd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs2
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs3
                    // Optional rounding mode
                    skipWhitespaceAndComments()
                    if (currentIs(",")) {
                        advance() // Consume comma
                        skipWhitespaceAndComments()
                        // Parse rounding mode - using expression for flexibility
                        if (parseExpression() == null) return false
                    }
                }

                FRD_FRS1_FRS2_RM -> {
                    if (!parseRvFloatReg()) return false // frd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs2
                    // Optional rounding mode
                    skipWhitespaceAndComments()
                    if (currentIs(",")) {
                        advance() // Consume comma
                        skipWhitespaceAndComments()
                        // Parse rounding mode - using expression for flexibility
                        if (parseExpression() == null) return false
                    }
                }

                RD_FRS1_FRS2 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvFloatReg()) return false // frs2
                }

                FRD_IMM12_RS1 -> {
                    if (!parseRvFloatReg()) return false // frd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // offset12 expr
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 base address
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                FRS2_IMM12_RS1 -> {
                    if (!parseRvFloatReg()) return false // frs2 source data
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // offset12 expr
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 base address
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                // === Zicsr Standard Extension (Control and Status Registers) ===
                RD_CSR_RS1 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvCsrReg()) return false // csr
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                }

                RD_CSR_UIMM5 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvCsrReg()) return false // csr
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // uimm5
                }

                CSR_RS1 -> {
                    if (!parseRvCsrReg()) return false // csr
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                }

                RD_CSR -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvCsrReg()) return false // csr
                }

                // === C Standard Extension (Compressed Instructions) ===
                // IMPORTANT: Using generic parsers. Constraints (reg range, imm format) not checked here.
                CRD_IMM6, CRD_NZIIMM6 -> { // c.li, c.lui, c.addi16sp
                    if (!parseRvIntReg()) return false // rd (may be restricted or sp)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // imm (may have constraints)
                }

                CRDPRIME_CRS1PRIME_NZUIMM6 -> { // c.addi, c.addiw, c.slli
                    if (!parseRvIntReg()) return false // rd' (restricted range)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1' (restricted range, or same as rd' for slli)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // nzuimm (non-zero)
                }

                CRDPRIME_IMM6_SP -> { // c.lwsp, c.ldsp, ...
                    if (!parseRvIntReg()) return false // rd' (restricted, non-zero)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // offset (scaled)
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    // Expect 'sp' specifically for C-ext
                    val spMarker = mark()
                    if (expect("sp")) {
                        spMarker.done(RvRegT.IntT.SP) // Or specific SP element
                    } else {
                        return false
                    }
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                CRS2_IMM6_SP -> { // c.swsp, c.sdsp, ...
                    if (!parseRvIntReg()) return false // rs2' (restricted)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // offset (scaled)
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    // Expect 'sp' specifically for C-ext
                    val spMarker = mark()
                    if (expect("sp")) {
                        spMarker.done(RvRegT.IntT.SP) // Or specific SP element
                    } else {
                        return false
                    }
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                CRD_CRS1, CRD_CRS1_ADD -> { // c.mv, c.add, c.jr, c.jalr
                    if (!parseRvIntReg()) return false // rd (may be restricted, non-zero or x0/x1)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 (may be restricted, non-zero)
                }

                CRS1PRIME_LABEL -> { // c.beqz, c.bnez
                    if (!parseRvIntReg()) return false // rs1' (restricted)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // label (offset)
                }
                // CLABEL handled by LABEL
                CRDPRIME_IMM6_CRS1PRIME -> { // c.lw, c.ld, c.flw, ...
                    if (!parseRvIntReg()) return false // rd' (restricted)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // offset (scaled)
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1' base address (restricted)
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                CRS2PRIME_IMM6_CRS1PRIME -> { // c.sw, c.sd, c.fsw, ...
                    if (!parseRvIntReg()) return false // rs2' source (restricted)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // offset (scaled)
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1' base address (restricted)
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }
                // CRDPRIME_CRS1PRIME_CRS2PRIME handled by RD_RS1_RS2
                CRDPRIME_CRS1PRIME_NZUIMM5 -> { // c.srli, c.srai
                    if (!parseRvIntReg()) return false // rd' (restricted)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1' (restricted, same as rd')
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // nzuimm5 (shamt, non-zero)
                }

                CRDPRIME_CRS1PRIME_IMM6_ANDI -> { // c.andi
                    if (!parseRvIntReg()) return false // rd' (restricted)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1' (restricted, same as rd')
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // imm6
                }


                // === V Standard Extension (Vector Instructions) ===
                VD_VS1_VS2 -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2
                }

                VD_RS1_VS2 -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2
                }

                VD_IMM5_VS2 -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // imm5
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2
                }

                VD_VS1_VS2_VM -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VD_RS1_VS2_VM -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VD_IMM5_VS2_VM -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // imm5
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VD_VS1_RS2 -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2
                }

                VD_VS1_IMM5 -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // imm5
                }

                RD_VS2 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2
                }

                VD_RS1ADDR /* Also VLREG */ -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                VS3_RS1ADDR /* Also VSREG */ -> {
                    if (!parseRvVectorReg()) return false // vs3 source data
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }

                VD_RS1ADDR_VM -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VS3_RS1ADDR_VM -> {
                    if (!parseRvVectorReg()) return false // vs3 source data
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VD_RS1ADDR_RS2STRIDE -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2 stride
                }

                VS3_RS1ADDR_RS2STRIDE -> {
                    if (!parseRvVectorReg()) return false // vs3 source data
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2 stride
                }

                VD_RS1ADDR_RS2STRIDE_VM -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2 stride
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VS3_RS1ADDR_RS2STRIDE_VM -> {
                    if (!parseRvVectorReg()) return false // vs3 source data
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2 stride
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VD_RS1ADDR_VS2INDEXED -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2 index
                }

                VS3_RS1ADDR_VS2INDEXED -> {
                    if (!parseRvVectorReg()) return false // vs3 source data
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2 index
                }

                VD_RS1ADDR_VS2INDEXED_VM -> {
                    if (!parseRvVectorReg()) return false // vd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2 index
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VS3_RS1ADDR_VS2INDEXED_VM -> {
                    if (!parseRvVectorReg()) return false // vs3 source data
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 address base
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvVectorReg()) return false // vs2 index
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false // Comma before mask
                    skipWhitespaceAndComments()
                    if (!parseVectorMask()) return false // vm
                }

                VSETVL_RD_RS1_RS2 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs2
                }

                VSETVLI_RD_RS1_IMM11 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // imm11 (vtype encoding)
                }

                // === Fence Instructions ===
                PRED_SUCC -> {
                    if (parseExpression() == null) return false // pred (e.g., 'iorw' identifier or number)
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // succ
                }

                OPT_RS1_RS2 -> {
                    // Handle optionally type NONE
                    if (currentIs(PsiTokenType.LINEBREAK)) return false

                    if (!parseRvIntReg()) return false
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false
                }

                // === Pseudo Instructions (Common Syntactic Forms) ===
                PS_RD_IMM -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // immediate
                }

                PS_RD_SYMBOL -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // symbol (label)
                }

                PS_RD_RS1 -> {
                    if (!parseRvIntReg()) return false // rd
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1
                }

                PS_RS1_LABEL -> {
                    if (!parseRvIntReg()) return false // rs1
                    skipWhitespaceAndComments()
                    if (!expect(",")) return false
                    skipWhitespaceAndComments()
                    if (parseExpression() == null) return false // label
                }

                PS_RS1 -> {
                    if (!parseRvIntReg()) return false // rs1
                }

                PS_OFFSET_RS1 -> {
                    if (parseExpression() == null) return false // offset expr
                    skipWhitespaceAndComments()
                    if (!expect("(")) return false
                    skipWhitespaceAndComments()
                    if (!parseRvIntReg()) return false // rs1 base address
                    skipWhitespaceAndComments()
                    if (!expect(")")) return false
                }
            }
        }

        return true
    }
}