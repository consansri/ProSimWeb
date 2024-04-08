package emulator.archs.riscv64

import emulator.kit.assembly.Compiler
import emulator.kit.assembly.standards.StandardSyntax
import emulator.kit.types.Variable
import emulator.archs.riscv64.RV64BinMapper.MaskLabel
import emulator.kit.assembly.Syntax.TokenSeq.Component.InSpecific.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.*

class RV64Syntax : StandardSyntax(RV64.MEM_ADDRESS_WIDTH, '#', InstrType.entries.map { it.id }, instrParamsCanContainWordsBesideLabels = false) {

    override fun MutableList<Compiler.Token>.checkInstr(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: ELabel?): Boolean {
        for (paramType in ParamType.entries) {
            val result = paramType.tokenSeq.matchStart(*this.toTypedArray())
            if (!result.matches) continue
            val allTokens = result.sequenceMap.map { it.token }
            val nameToken = allTokens.firstOrNull() ?: continue
            val params = allTokens.drop(1)

            val instrType = InstrType.entries.firstOrNull { it.paramType == paramType && it.id.uppercase() == nameToken.content.uppercase() } ?: continue
            val eInstr = RV64Instr(instrType, paramType, nameToken, params, currentLabel)

            elements.add(eInstr)
            allTokens.forEach {
                this.remove(it)
            }
            return true
        }

        return false
    }

    enum class ParamType(val pseudo: Boolean, val exampleString: String, val tokenSeq: TokenSeq) {
        // NORMAL INSTRUCTIONS
        RD_I20(
            false, "rd, imm20", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit20()),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[MaskLabel.RD]
                return if (rd != null) {
                    paramMap.remove(MaskLabel.RD)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, imm
        RD_Off12(
            false, "rd, imm12(rs)", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit12()),
                Specific("("),
                Register(RV64.standardRegFile),
                Specific(")"),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[MaskLabel.RD]
                val rs1 = paramMap[MaskLabel.RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(MaskLabel.RD)
                    paramMap.remove(MaskLabel.RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t$immString(${arch.getRegByAddr(rs1)?.aliases?.first()})"
                } else {
                    "param missing"
                }
            }
        }, // rd, imm12(rs)
        RS2_Off12(
            false, "rs2, imm12(rs1)", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit12()),
                Specific("("),
                Register(RV64.standardRegFile),
                Specific(")"),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rs2 = paramMap[MaskLabel.RS2]
                val rs1 = paramMap[MaskLabel.RS1]
                return if (rs2 != null && rs1 != null) {
                    paramMap.remove(MaskLabel.RS2)
                    paramMap.remove(MaskLabel.RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rs2)?.aliases?.first()},\t$immString(${arch.getRegByAddr(rs1)?.aliases?.first()})"
                } else {
                    "param missing"
                }
            }
        }, // rs2, imm5(rs1)
        RD_RS1_RS2(
            false, "rd, rs1, rs2", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                Register(RV64.standardRegFile),
                Specific(","),
                Register(RV64.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[MaskLabel.RD]
                val rs1 = paramMap[MaskLabel.RS1]
                val rs2 = paramMap[MaskLabel.RS2]
                return if (rd != null && rs2 != null && rs1 != null) {
                    paramMap.remove(MaskLabel.RD)
                    paramMap.remove(MaskLabel.RS2)
                    paramMap.remove(MaskLabel.RS1)
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t${arch.getRegByAddr(rs2)?.aliases?.first()}"
                } else {
                    "param missing"
                }
            }
        }, // rd, rs1, rs2
        RD_RS1_I12(
            false, "rd, rs1, imm12", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit12()),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[MaskLabel.RD]
                val rs1 = paramMap[MaskLabel.RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(MaskLabel.RD)
                    paramMap.remove(MaskLabel.RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, rs, imm
        RD_RS1_SHAMT6(
            false, "rd, rs1, shamt6", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit6(), onlyUnsigned = true),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[MaskLabel.RD]
                val rs1 = paramMap[MaskLabel.RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(MaskLabel.RD)
                    paramMap.remove(MaskLabel.RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, rs, shamt
        RS1_RS2_I12(
            false, "rs1, rs2, imm12", TokenSeq(WordNoDotsAndUS, Space, Register(RV64.standardRegFile), Specific(","), Register(RV64.standardRegFile), Specific(","), SpecConst(Variable.Size.Bit12()), NewLine, ignoreSpaces = true)
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rs2 = paramMap[MaskLabel.RS2]
                val rs1 = paramMap[MaskLabel.RS1]
                return if (rs2 != null && rs1 != null) {
                    paramMap.remove(MaskLabel.RS2)
                    paramMap.remove(MaskLabel.RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rs1)?.aliases?.first()},\t${arch.getRegByAddr(rs2)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rs1, rs2, imm
        CSR_RD_OFF12_RS1(
            false, "rd, csr12, rs1", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                RegOrSpecConst(Variable.Size.Bit12(), notInRegFile = RV64.standardRegFile),
                Specific(","),
                Register(RV64.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[MaskLabel.RD]
                val csr = paramMap[MaskLabel.CSR]
                val rs1 = paramMap[MaskLabel.RS1]
                return if (rd != null && csr != null && rs1 != null) {
                    paramMap.remove(MaskLabel.RD)
                    paramMap.remove(MaskLabel.CSR)
                    paramMap.remove(MaskLabel.RS1)
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(csr.toHex(), RV64.CSR_REGFILE_NAME)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()}"
                } else {
                    "param missing"
                }
            }
        },
        CSR_RD_OFF12_UIMM5(
            false, "rd, offset, uimm5", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                RegOrSpecConst(Variable.Size.Bit12(), notInRegFile = RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit5()),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[MaskLabel.RD]
                val csr = paramMap[MaskLabel.CSR]
                return if (rd != null && csr != null) {
                    paramMap.remove(MaskLabel.RD)
                    paramMap.remove(MaskLabel.CSR)
                    val immString = paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toBin().toString() }
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(csr.toHex(), RV64.CSR_REGFILE_NAME)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        },

        // PSEUDO INSTRUCTIONS
        PS_RS1_RS2_Jlbl(
            true, "rs1, rs2, jlabel", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                Register(RV64.standardRegFile),
                Specific(","),
                Word,
                NewLine, ignoreSpaces = true
            )
        ),
        PS_RD_LI_I28Unsigned(
            true, "rd, imm28u", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit28(), signed = false),
                NewLine, ignoreSpaces = true
            )
        ), // rd, imm28 unsigned
        PS_RD_LI_I32Signed(
            true, "rd, imm32s", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit32(), signed = true),
                NewLine, ignoreSpaces = true
            )
        ), // rd, imm32
        PS_RD_LI_I40Unsigned(
            true, "rd, imm40u", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit40(), signed = false),
                NewLine, ignoreSpaces = true
            )
        ),
        PS_RD_LI_I52Unsigned(
            true, "rd, imm52u", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit52(), signed = false),
                NewLine, ignoreSpaces = true
            )
        ),
        PS_RD_LI_I64(
            true, "rd, imm64", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit64()),
                NewLine, ignoreSpaces = true
            )
        ), // rd, imm64
        PS_RS1_Jlbl(
            true, "rs, jlabel", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                Word,
                NewLine, ignoreSpaces = true
            )
        ), // rs, label
        PS_RD_Albl(
            true, "rd, alabel", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                Word,
                NewLine, ignoreSpaces = true
            )
        ), // rd, label
        PS_lbl(true, "jlabel", TokenSeq(WordNoDotsAndUS, Space, Word, NewLine, ignoreSpaces = true)),  // label
        PS_RD_RS1(
            true, "rd, rs", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                Register(RV64.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ), // rd, rs
        PS_RS1(true, "rs1", TokenSeq(WordNoDotsAndUS, Space, Register(RV64.standardRegFile), NewLine, ignoreSpaces = true)),
        PS_CSR_RS1(
            true, "csr, rs1", TokenSeq(
                WordNoDotsAndUS,
                Space,
                RegOrSpecConst(Variable.Size.Bit12(), notInRegFile = RV64.standardRegFile),
                Specific(","),
                Register(RV64.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ),
        PS_RD_CSR(
            true, "rd, csr", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV64.standardRegFile),
                Specific(","),
                RegOrSpecConst(Variable.Size.Bit12(), notInRegFile = RV64.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ),

        // NONE PARAM INSTR
        NONE(false, "none", TokenSeq(WordNoDotsAndUS, NewLine)),
        PS_NONE(true, "none", TokenSeq(WordNoDotsAndUS, NewLine));

        open fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<MaskLabel, Variable.Value.Bin>): String {
            return "pseudo param type"
        }
    }

    enum class InstrType(val id: String, val pseudo: Boolean, val paramType: ParamType, val opCode: RV64BinMapper.OpCode? = null, val memWords: Int = 1, val relative: InstrType? = null, val needFeatures: List<Int> = emptyList()) {
        LUI("LUI", false, ParamType.RD_I20, RV64BinMapper.OpCode("00000000000000000000 00000 0110111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap) // only for console information
                // get relevant parameters from binary map
                val rdAddr = paramMap[MaskLabel.RD]
                val imm20 = paramMap[MaskLabel.IMM20]
                if (rdAddr == null || imm20 == null) return

                // get relevant registers
                val rd = arch.getRegByAddr(rdAddr)
                val pc = arch.getRegContainer().pc
                if (rd == null) return

                // calculate
                val shiftedIMM = imm20.getResized(RV64.XLEN) shl 12 // from imm20 to imm32
                // change states
                rd.set(shiftedIMM)    // set register to imm32 value
                pc.set(pc.get() + Variable.Value.Hex("4"))
            }
        },
        AUIPC("AUIPC", false, ParamType.RD_I20, RV64BinMapper.OpCode("00000000000000000000 00000 0010111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                if (rdAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val imm20 = paramMap[MaskLabel.IMM20]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm20 != null) {
                        val shiftedIMM = imm20.getUResized(RV64.XLEN) shl 12
                        val sum = pc.get() + shiftedIMM
                        rd.set(sum)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        JAL("JAL", false, ParamType.RD_I20, RV64BinMapper.OpCode("00000000000000000000 00000 1101111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                if (rdAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val imm20 = paramMap[MaskLabel.IMM20]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm20 != null) {
                        val imm20str = imm20.getRawBinStr()

                        /**
                         *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                         *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                         *        Location       20 [      10 : 1               ] 11 [ 19 : 12             ]
                         */

                        val shiftedImm = Variable.Value.Bin(imm20str[0].toString() + imm20str.substring(12) + imm20str[11] + imm20str.substring(1, 11), Variable.Size.Bit20()).getResized(RV64.XLEN) shl 1

                        rd.set(pc.get() + Variable.Value.Hex("4"))
                        pc.set(pc.get() + shiftedImm)
                    }
                }
            }
        },
        JALR("JALR", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 000 00000 1100111", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val jumpAddr = rs1.get() + imm12.getResized(RV64.XLEN)
                        rd.set(pc.get() + Variable.Value.Hex("4"))
                        pc.set(jumpAddr)
                    }
                }
            }
        },
        ECALL("ECALL", false, ParamType.NONE, RV64BinMapper.OpCode("000000000000 00000 000 00000 1110011", arrayOf(MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.OPCODE))),
        EBREAK("EBREAK", false, ParamType.NONE, RV64BinMapper.OpCode("000000000001 00000 000 00000 1110011", arrayOf(MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.OPCODE))),
        BEQ(
            "BEQ", false, ParamType.RS1_RS2_I12,
            RV64BinMapper.OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())

                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toBin() == rs2.get().toBin()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BNE(
            "BNE", false, ParamType.RS1_RS2_I12,
            RV64BinMapper.OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toBin() != rs2.get().toBin()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BLT(
            "BLT", false, ParamType.RS1_RS2_I12,
            RV64BinMapper.OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toDec() < rs2.get().toDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BGE(
            "BGE", false, ParamType.RS1_RS2_I12,
            RV64BinMapper.OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toDec() >= rs2.get().toDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BLTU(
            "BLTU", false, ParamType.RS1_RS2_I12,
            RV64BinMapper.OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toUDec() < rs2.get().toUDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BGEU(
            "BGEU", false, ParamType.RS1_RS2_I12,
            RV64BinMapper.OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[MaskLabel.IMM7]
                    val imm5 = paramMap[MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                        if (rs1.get().toUDec() >= rs2.get().toUDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BEQ1("BEQ", true, ParamType.PS_RS1_RS2_Jlbl, relative = BEQ),
        BNE1("BNE", true, ParamType.PS_RS1_RS2_Jlbl, relative = BNE),
        BLT1("BLT", true, ParamType.PS_RS1_RS2_Jlbl, relative = BLT),
        BGE1("BGE", true, ParamType.PS_RS1_RS2_Jlbl, relative = BGE),
        BLTU1("BLTU", true, ParamType.PS_RS1_RS2_Jlbl, relative = BLTU),
        BGEU1("BGEU", true, ParamType.PS_RS1_RS2_Jlbl, relative = BGEU),
        LB("LB", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 000 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex()).toBin().getResized(RV64.XLEN)
                        rd.set(loadedByte)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LH("LH", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 001 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedHalfWord = arch.getMemory().load(memAddr.toHex(), 2).toBin().getResized(RV64.XLEN)
                        rd.set(loadedHalfWord)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LW("LW", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 010 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedWord = arch.getMemory().load(memAddr.toHex(), 4).toBin().getResized(RV64.XLEN)
                        rd.set(loadedWord)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LD("LD", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 011 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedWord = arch.getMemory().load(memAddr.toHex(), 8).toBin().getResized(RV64.XLEN)
                        rd.set(loadedWord)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LBU("LBU", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 100 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex())
                        rd.set(loadedByte.getUResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LHU("LHU", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 101 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex(), 2)
                        rd.set(loadedByte.getUResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LWU("LWU", false, ParamType.RD_Off12, RV64BinMapper.OpCode("000000000000 00000 110 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val imm12 = paramMap[MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                        val loadedWord = arch.getMemory().load(memAddr.toHex(), 4)
                        rd.set(loadedWord.getUResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SB("SB", false, ParamType.RS2_Off12, RV64BinMapper.OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                val imm5 = paramMap[MaskLabel.IMM5]
                val imm7 = paramMap[MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                        val memAddr = rs1.get().toBin().getResized(RV64.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Variable.Size.Bit8()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SH("SH", false, ParamType.RS2_Off12, RV64BinMapper.OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                val imm5 = paramMap[MaskLabel.IMM5]
                val imm7 = paramMap[MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                        val memAddr = rs1.get().toBin().getResized(RV64.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Variable.Size.Bit16()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SW("SW", false, ParamType.RS2_Off12, RV64BinMapper.OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                val imm5 = paramMap[MaskLabel.IMM5]
                val imm7 = paramMap[MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                        val memAddr = rs1.variable.get().toBin().getResized(RV64.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Variable.Size.Bit32()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SD("SD", false, ParamType.RS2_Off12, RV64BinMapper.OpCode("0000000 00000 00000 011 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                val imm5 = paramMap[MaskLabel.IMM5]
                val imm7 = paramMap[MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                        val memAddr = rs1.variable.get().toBin().getResized(RV64.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADDI("ADDI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 000 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV64.XLEN)
                        val sum = rs1.get().toBin() + paddedImm64
                        rd.set(sum)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADDIW("ADDIW", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 000 00000 0011011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm32 = imm12.getResized(Variable.Size.Bit32())
                        val sum = rs1.get().toBin().getResized(Variable.Size.Bit32()) + paddedImm32
                        rd.set(sum.toBin().getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLTI("SLTI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 010 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV64.XLEN)
                        rd.set(if (rs1.get().toDec() < paddedImm64.toDec()) Variable.Value.Bin("1", RV64.XLEN) else Variable.Value.Bin("0", RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLTIU("SLTIU", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 011 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV64.XLEN)
                        rd.set(if (rs1.get().toBin() < paddedImm64) Variable.Value.Bin("1", RV64.XLEN) else Variable.Value.Bin("0", RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        XORI("XORI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 100 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV64.XLEN)
                        rd.set(rs1.get().toBin() xor paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ORI("ORI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 110 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV64.XLEN)
                        rd.set(rs1.get().toBin() or paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ANDI("ANDI", false, ParamType.RD_RS1_I12, RV64BinMapper.OpCode("000000000000 00000 111 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV64.XLEN)
                        rd.set(rs1.get().toBin() and paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLLI(
            "SLLI", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("000000 000000 00000 001 00000 0010011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() ushl shamt6.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLLIW(
            "SLLIW", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("000000 000000 00000 001 00000 0011011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set((rs1.get().toBin().getUResized(Variable.Size.Bit32()) ushl shamt6.getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2)).getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRLI(
            "SRLI", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("000000 000000 00000 101 00000 0010011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() ushr shamt6.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRLIW(
            "SRLIW", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("000000 000000 00000 101 00000 0011011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set((rs1.get().toBin().getUResized(Variable.Size.Bit32()) ushr shamt6.getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2)).getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRAI(
            "SRAI", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("010000 000000 00000 101 00000 0010011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() shr shamt6.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRAIW(
            "SRAIW", false, ParamType.RD_RS1_SHAMT6,
            RV64BinMapper.OpCode("010000 000000 00000 101 00000 0011011", arrayOf(MaskLabel.FUNCT6, MaskLabel.SHAMT6, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt6 = paramMap[MaskLabel.SHAMT6]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt6 != null && rs1 != null) {
                        rd.set((rs1.get().toBin().getUResized(Variable.Size.Bit32()) shr shamt6.getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2)).getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADD(
            "ADD", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() + rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADDW(
            "ADDW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 000 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set((rs1.get().toBin().getResized(Variable.Size.Bit32()) + rs2.get().toBin().getResized(Variable.Size.Bit32())).toBin().getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SUB(
            "SUB", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() - rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SUBW(
            "SUBW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0100000 00000 00000 000 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set((rs1.get().toBin().getResized(Variable.Size.Bit32()) - rs2.get().toBin().getResized(Variable.Size.Bit32())).toBin().getResized(RV64.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLL(
            "SLL", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() ushl rs2.get().toBin().getUResized(Variable.Size.Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLLW(
            "SLLW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 001 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin().getUResized(Variable.Size.Bit32()) ushl rs2.get().toBin().getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLT(
            "SLT", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(if (rs1.get().toDec() < rs2.get().toDec()) Variable.Value.Bin("1", Variable.Size.Bit32()) else Variable.Value.Bin("0", Variable.Size.Bit32()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLTU(
            "SLTU", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(if (rs1.get().toBin() < rs2.get().toBin()) Variable.Value.Bin("1", Variable.Size.Bit32()) else Variable.Value.Bin("0", Variable.Size.Bit32()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        XOR(
            "XOR", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() xor rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRL(
            "SRL", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() ushr rs2.get().toBin().getUResized(Variable.Size.Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRLW(
            "SRLW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 101 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin().getUResized(Variable.Size.Bit32()) ushr rs2.get().toBin().getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRA(
            "SRA", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() shr rs2.get().toBin().getUResized(Variable.Size.Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRAW(
            "SRAW", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0100000 00000 00000 101 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin().getUResized(Variable.Size.Bit32()) shr rs2.get().toBin().getUResized(Variable.Size.Bit5()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        OR(
            "OR",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() or rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        AND(
            "AND", false, ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() and rs2.get().toBin())
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        // CSR Extension
        CSRRW(
            "CSRRW",
            false,
            ParamType.CSR_RD_OFF12_RS1,
            RV64BinMapper.OpCode("000000000000 00000 001 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(rs1.get())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRS(
            "CSRRS",
            false,
            ParamType.CSR_RD_OFF12_RS1,
            RV64BinMapper.OpCode("000000000000 00000 010 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(rs1.get().toBin() or csr.get().toBin())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRC(
            "CSRRC",
            false,
            ParamType.CSR_RD_OFF12_RS1,
            RV64BinMapper.OpCode("000000000000 00000 011 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() and rs1.get().toBin().inv())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRWI(
            "CSRRWI",
            false,
            ParamType.CSR_RD_OFF12_UIMM5,
            RV64BinMapper.OpCode("000000000000 00000 101 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.UIMM5, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val uimm5 = paramMap[MaskLabel.UIMM5]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(uimm5.getUResized(RV64.XLEN))

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRSI(
            "CSRRSI",
            false,
            ParamType.CSR_RD_OFF12_UIMM5,
            RV64BinMapper.OpCode("000000000000 00000 110 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.UIMM5, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val uimm5 = paramMap[MaskLabel.UIMM5]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() or uimm5.getUResized(RV64.XLEN))

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRCI(
            "CSRRCI",
            false,
            ParamType.CSR_RD_OFF12_UIMM5,
            RV64BinMapper.OpCode("000000000000 00000 111 00000 1110011", arrayOf(MaskLabel.CSR, MaskLabel.UIMM5, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val uimm5 = paramMap[MaskLabel.UIMM5]
                val csrAddr = paramMap[MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV64.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() and uimm5.getUResized(RV64.XLEN).inv())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        CSRW("CSRW", true, ParamType.PS_CSR_RS1, needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)),
        CSRR("CSRR", true, ParamType.PS_RD_CSR, needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)),

        // M Extension
        MUL(
            "MUL",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        MULH(
            "MULH",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 001 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2, false).ushr(RV64.XLEN.bitWidth).getResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        MULHSU(
            "MULHSU",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 010 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2, resizeToLargestParamSize = false, true).ushr(RV64.XLEN.bitWidth).getResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        MULHU(
            "MULHU",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 011 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = (factor1 * factor2).toBin().ushr(RV64.XLEN.bitWidth).getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        DIV(
            "DIV",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 100 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexDivSigned(factor2, dividendIsUnsigned = true)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        DIVU(
            "DIVU",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1 / factor2
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        REM(
            "REM",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 110 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexRemSigned(factor2)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        REMU(
            "REMU",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 111 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1 % factor2
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        // RV64 M Extension
        MULW(
            "MULW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 000 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2).getUResized(Variable.Size.Bit32()).getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        DIVW(
            "DIVW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 100 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin().getUResized(Variable.Size.Bit32())
                        val factor2 = rs2.get().toBin().getUResized(Variable.Size.Bit32())
                        val result = factor1.flexDivSigned(factor2, dividendIsUnsigned = true).getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        DIVUW(
            "DIVUW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 101 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin().getUResized(Variable.Size.Bit32())
                        val factor2 = rs2.get().toBin().getUResized(Variable.Size.Bit32())
                        val result = (factor1 / factor2).toBin().getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        REMW(
            "REMW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 110 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin().getUResized(Variable.Size.Bit32())
                        val factor2 = rs2.get().toBin().getUResized(Variable.Size.Bit32())
                        val result = factor1.flexRemSigned(factor2).getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        REMUW(
            "REMUW",
            false,
            ParamType.RD_RS1_RS2,
            RV64BinMapper.OpCode("0000001 00000 00000 111 00000 0111011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE)),
            needFeatures = listOf(RV64.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[MaskLabel.RD]
                val rs1Addr = paramMap[MaskLabel.RS1]
                val rs2Addr = paramMap[MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin().getUResized(Variable.Size.Bit32())
                        val factor2 = rs2.get().toBin().getUResized(Variable.Size.Bit32())
                        val result = (factor1 % factor2).toBin().getUResized(RV64.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        // Pseudo
        Nop("NOP", true, ParamType.PS_NONE),
        Mv("MV", true, ParamType.PS_RD_RS1),
        Li28Unsigned("LI", true, ParamType.PS_RD_LI_I28Unsigned, memWords = 2),
        Li32Signed("LI", true, ParamType.PS_RD_LI_I32Signed, memWords = 2),
        Li40Unsigned("LI", true, ParamType.PS_RD_LI_I40Unsigned, memWords = 4),
        Li52Unsigned("LI", true, ParamType.PS_RD_LI_I52Unsigned, memWords = 6),
        Li64("LI", true, ParamType.PS_RD_LI_I64, memWords = 8),
        La("LA", true, ParamType.PS_RD_Albl, memWords = 2),
        Not("NOT", true, ParamType.PS_RD_RS1),
        Neg("NEG", true, ParamType.PS_RD_RS1),
        Seqz("SEQZ", true, ParamType.PS_RD_RS1),
        Snez("SNEZ", true, ParamType.PS_RD_RS1),
        Sltz("SLTZ", true, ParamType.PS_RD_RS1),
        Sgtz("SGTZ", true, ParamType.PS_RD_RS1),
        Beqz("BEQZ", true, ParamType.PS_RS1_Jlbl),
        Bnez("BNEZ", true, ParamType.PS_RS1_Jlbl),
        Blez("BLEZ", true, ParamType.PS_RS1_Jlbl),
        Bgez("BGEZ", true, ParamType.PS_RS1_Jlbl),
        Bltz("BLTZ", true, ParamType.PS_RS1_Jlbl),
        BGTZ("BGTZ", true, ParamType.PS_RS1_Jlbl),
        Bgt("BGT", true, ParamType.PS_RS1_RS2_Jlbl),
        Ble("BLE", true, ParamType.PS_RS1_RS2_Jlbl),
        Bgtu("BGTU", true, ParamType.PS_RS1_RS2_Jlbl),
        Bleu("BLEU", true, ParamType.PS_RS1_RS2_Jlbl),
        J("J", true, ParamType.PS_lbl),
        JAL1("JAL", true, ParamType.PS_RS1_Jlbl, relative = JAL),
        JAL2("JAL", true, ParamType.PS_lbl, relative = JAL),
        Jr("JR", true, ParamType.PS_RS1),
        JALR1("JALR", true, ParamType.PS_RS1, relative = JALR),
        JALR2("JALR", true, ParamType.RD_Off12, relative = JALR),
        Ret("RET", true, ParamType.PS_NONE),
        Call("CALL", true, ParamType.PS_lbl, memWords = 2),
        Tail("TAIL", true, ParamType.PS_lbl, memWords = 2);

        open fun execute(arch: emulator.kit.Architecture, paramMap: Map<MaskLabel, Variable.Value.Bin>) {
            arch.getConsole().log("> $id {...}")
        }
    }

    class RV64Instr(val instrType: InstrType, val paramType: ParamType, nameToken: Compiler.Token, params: List<Compiler.Token>, parentLabel: ELabel?) : EInstr(nameToken, params, parentLabel)


}