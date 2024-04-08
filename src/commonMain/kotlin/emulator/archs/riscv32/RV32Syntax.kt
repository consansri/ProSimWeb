package emulator.archs.riscv32

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.standards.StandardSyntax
import emulator.kit.types.Variable
import emulator.kit.assembly.Syntax.TokenSeq.Component.InSpecific.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.Specific
import emulator.kit.assembly.Syntax.TokenSeq.Component.SpecConst
import emulator.archs.riscv32.RV32BinMapper.OpCode
import emulator.archs.riscv32.RV32BinMapper.MaskLabel.*

class RV32Syntax : StandardSyntax(RV32.MEM_ADDRESS_WIDTH, '#', InstrType.entries.map { it.id }, instrParamsCanContainWordsBesideLabels = false) {
    override fun MutableList<Compiler.Token>.checkInstr(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: ELabel?): Boolean {
        for (paramType in ParamType.entries) {
            val result = paramType.tokenSeq.matchStart(*this.toTypedArray())
            if (!result.matches) continue
            val allTokens = result.sequenceMap.map { it.token }
            val nameToken = allTokens.firstOrNull() ?: continue
            val params = allTokens.drop(1)

            val instrType = InstrType.entries.firstOrNull { it.paramType == paramType && it.id.uppercase() == nameToken.content.uppercase() } ?: continue
            val eInstr = RV32Instr(instrType, nameToken, params, currentLabel)

            elements.add(eInstr)
            allTokens.forEach { this.remove(it) }
            return true
        }

        return false
    }

    /**
     * Syntax Types Holding the Instruction and Directive Definitions
     */

    enum class ParamType(val pseudo: Boolean, val exampleString: String, val tokenSeq: TokenSeq) {
        // NORMAL INSTRUCTIONS
        RD_I20(
            false, "rd, imm20", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit20()),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[RD]
                return if (rd != null) {
                    paramMap.remove(RD)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, imm
        RD_OFF12(
            false, "rd, imm12(rs)", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit12()),
                Specific("("),
                Register(RV32.standardRegFile),
                Specific(")"),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[RD]
                val rs1 = paramMap[RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(RD)
                    paramMap.remove(RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t$immString(${arch.getRegByAddr(rs1)?.aliases?.first()})"
                } else {
                    "param missing"
                }
            }
        }, // rd, imm12(rs)
        RS2_OFF12(false, "rs2, imm12(rs1)", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), SpecConst(Variable.Size.Bit12()), Specific("("), Register(RV32.standardRegFile), Specific(")"), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rs2 = paramMap[RS2]
                val rs1 = paramMap[RS1]
                return if (rs2 != null && rs1 != null) {
                    paramMap.remove(RS2)
                    paramMap.remove(RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rs2)?.aliases?.first()},\t$immString(${arch.getRegByAddr(rs1)?.aliases?.first()})"
                } else {
                    "param missing"
                }
            }
        }, // rs2, imm5(rs1)
        RD_RS1_RS2(
            false, "rd, rs1, rs2", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), NewLine, ignoreSpaces = true)
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[RD]
                val rs1 = paramMap[RS1]
                val rs2 = paramMap[RS2]
                return if (rd != null && rs2 != null && rs1 != null) {
                    paramMap.remove(RD)
                    paramMap.remove(RS2)
                    paramMap.remove(RS1)
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
                Register(RV32.standardRegFile),
                Specific(","),
                Register(RV32.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit12()),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[RD]
                val rs1 = paramMap[RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(RD)
                    paramMap.remove(RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, rs, imm
        RD_RS1_SHAMT5(
            false, "rd, rs1, shamt5", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                Register(RV32.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit5(), onlyUnsigned = true),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[RD]
                val rs1 = paramMap[RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(RD)
                    paramMap.remove(RS1)
                    val immString = "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toHex().getRawHexStr() }}"
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, rs, shamt
        RS1_RS2_I12(
            false, "rs1, rs2, imm12", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                Register(RV32.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit12()),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rs2 = paramMap[RS2]
                val rs1 = paramMap[RS1]
                return if (rs2 != null && rs1 != null) {
                    paramMap.remove(RS2)
                    paramMap.remove(RS1)
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
                Register(RV32.standardRegFile),
                Specific(","),
                TokenSeq.Component.RegOrSpecConst(Variable.Size.Bit12(), notInRegFile = RV32.standardRegFile),
                Specific(","),
                Register(RV32.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[RD]
                val csr = paramMap[CSR]
                val rs1 = paramMap[RS1]
                return if (rd != null && csr != null && rs1 != null) {
                    paramMap.remove(RD)
                    paramMap.remove(CSR)
                    paramMap.remove(RS1)
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(csr.toHex(), RV32.CSR_REGFILE_NAME)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()}"
                } else {
                    "param missing"
                }
            }
        },
        CSR_RD_OFF12_UIMM5(
            false, "rd, offset, uimm5", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                TokenSeq.Component.RegOrSpecConst(Variable.Size.Bit12(), notInRegFile = RV32.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit5()),
                NewLine, ignoreSpaces = true
            )
        ) {
            override fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
                val rd = paramMap[RD]
                val csr = paramMap[CSR]
                return if (rd != null && csr != null) {
                    paramMap.remove(RD)
                    paramMap.remove(CSR)
                    val immString = paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString("") { it.toBin().toString() }
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(csr.toHex(), RV32.CSR_REGFILE_NAME)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        },

        // PSEUDO INSTRUCTIONS
        PS_RS1_RS2_JLBL(
            true, "rs1, rs2, jlabel", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                Register(RV32.standardRegFile),
                Specific(","),
                Word,
                NewLine, ignoreSpaces = true
            )
        ),
        PS_RD_I32(
            true, "rd, imm32", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                SpecConst(Variable.Size.Bit32()),
                NewLine, ignoreSpaces = true
            )
        ), // rd, imm
        PS_RS1_JLBL(
            true, "rs, jlabel", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                Word,
                NewLine, ignoreSpaces = true
            )
        ), // rs, label
        PS_RD_ALBL(
            true, "rd, alabel", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                Word,
                NewLine, ignoreSpaces = true
            )
        ), // rd, label
        PS_JLBL(true, "jlabel", TokenSeq(WordNoDotsAndUS, Space, Word, NewLine, ignoreSpaces = true)),  // label
        PS_RD_RS1(
            true, "rd, rs", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                Register(RV32.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ), // rd, rs
        PS_RS1(true, "rs1", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), NewLine, ignoreSpaces = true)),
        PS_CSR_RS1(
            true, "csr, rs1", TokenSeq(
                WordNoDotsAndUS,
                Space,
                TokenSeq.Component.RegOrSpecConst(Variable.Size.Bit12(), notInRegFile = RV32.standardRegFile),
                Specific(","),
                Register(RV32.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ),
        PS_RD_CSR(
            true, "rd, csr", TokenSeq(
                WordNoDotsAndUS,
                Space,
                Register(RV32.standardRegFile),
                Specific(","),
                TokenSeq.Component.RegOrSpecConst(Variable.Size.Bit12(), notInRegFile = RV32.standardRegFile),
                NewLine, ignoreSpaces = true
            )
        ),

        // NONE PARAM INSTR
        NONE(false, "none", TokenSeq(WordNoDotsAndUS, NewLine, ignoreSpaces = true)),
        PS_NONE(true, "none", TokenSeq(WordNoDotsAndUS, NewLine, ignoreSpaces = true));

        open fun getTSParamString(arch: emulator.kit.Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Variable.Value.Bin>): String {
            return "pseudo param type"
        }
    }

    enum class InstrType(val id: String, val pseudo: Boolean, val paramType: ParamType, val opCode: OpCode? = null, val memWords: Int = 1, val relative: InstrType? = null, val needFeatures: List<Int> = emptyList()) {
        LUI("LUI", false, ParamType.RD_I20, OpCode("00000000000000000000 00000 0110111", arrayOf(IMM20, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap) // only for console information
                // get relevant parameters from binary map
                val rdAddr = paramMap[RD]
                val imm20 = paramMap[IMM20]
                if (rdAddr == null || imm20 == null) return

                // get relevant registers
                val rd = arch.getRegByAddr(rdAddr)
                val pc = arch.getRegContainer().pc
                if (rd == null) return

                // calculate
                val shiftedIMM = imm20.getResized(RV32.XLEN) shl 12 // from imm20 to imm32
                // change states
                rd.set(shiftedIMM)    // set register to imm32 value
                pc.set(pc.get() + Variable.Value.Hex("4"))
            }
        },
        AUIPC("AUIPC", false, ParamType.RD_I20, OpCode("00000000000000000000 00000 0010111", arrayOf(IMM20, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                if (rdAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val imm20 = paramMap[IMM20]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm20 != null) {
                        val shiftedIMM = imm20.getUResized(RV32.XLEN) shl 12
                        val sum = pc.get() + shiftedIMM
                        rd.set(sum)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        JAL("JAL", false, ParamType.RD_I20, OpCode("00000000000000000000 00000 1101111", arrayOf(IMM20, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                if (rdAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val imm20 = paramMap[IMM20]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm20 != null) {
                        val imm20str = imm20.getRawBinStr()

                        /**
                         *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                         *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                         *        Location       20 [      10 : 1               ] 11 [ 19 : 12             ]
                         */

                        val shiftedImm = Variable.Value.Bin(imm20str[0].toString() + imm20str.substring(12) + imm20str[11] + imm20str.substring(1, 11), Variable.Size.Bit20()).getResized(RV32.XLEN) shl 1

                        rd.set(pc.get() + Variable.Value.Hex("4"))
                        pc.set(pc.get() + shiftedImm)
                    }
                }
            }
        },
        JALR("JALR", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 000 00000 1100111", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val jumpAddr = rs1.get() + imm12.getResized(RV32.XLEN)
                        rd.set(pc.get() + Variable.Value.Hex("4"))
                        pc.set(jumpAddr)
                    }
                }
            }
        },
        ECALL("ECALL", false, ParamType.NONE, OpCode("000000000000 00000 000 00000 1110011", arrayOf(NONE, NONE, NONE, NONE, OPCODE))),
        EBREAK("EBREAK", false, ParamType.NONE, OpCode("000000000001 00000 000 00000 1110011", arrayOf(NONE, NONE, NONE, NONE, OPCODE))),
        BEQ(
            "BEQ", false, ParamType.RS1_RS2_I12,
            OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[IMM7]
                    val imm5 = paramMap[IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())

                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[IMM7]
                    val imm5 = paramMap[IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[IMM7]
                    val imm5 = paramMap[IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[IMM7]
                    val imm5 = paramMap[IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[IMM7]
                    val imm5 = paramMap[IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[IMM7]
                    val imm5 = paramMap[IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Variable.Size.Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Variable.Size.Bit5()).getRawBinStr()
                        val imm12 = Variable.Value.Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
                        if (rs1.get().toUDec() >= rs2.get().toUDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Variable.Value.Hex("4"))
                        }
                    }
                }
            }
        },
        BEQ1("BEQ", true, ParamType.PS_RS1_RS2_JLBL, relative = BEQ),
        BNE1("BNE", true, ParamType.PS_RS1_RS2_JLBL, relative = BNE),
        BLT1("BLT", true, ParamType.PS_RS1_RS2_JLBL, relative = BLT),
        BGE1("BGE", true, ParamType.PS_RS1_RS2_JLBL, relative = BGE),
        BLTU1("BLTU", true, ParamType.PS_RS1_RS2_JLBL, relative = BLTU),
        BGEU1("BGEU", true, ParamType.PS_RS1_RS2_JLBL, relative = BGEU),
        LB("LB", false, ParamType.RD_OFF12, OpCode("000000000000 00000 000 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val imm12 = paramMap[IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex()).toBin().getResized(RV32.XLEN)
                        rd.set(loadedByte)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LH("LH", false, ParamType.RD_OFF12, OpCode("000000000000 00000 001 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val imm12 = paramMap[IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedHalfWord = arch.getMemory().load(memAddr.toHex(), 2).toBin().getResized(RV32.XLEN)
                        rd.set(loadedHalfWord)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LW("LW", false, ParamType.RD_OFF12, OpCode("000000000000 00000 010 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val imm12 = paramMap[IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedWord = arch.getMemory().load(memAddr.toHex(), 4).toBin().getResized(RV32.XLEN)
                        rd.set(loadedWord)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LBU("LBU", false, ParamType.RD_OFF12, OpCode("000000000000 00000 100 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val imm12 = paramMap[IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex())
                        rd.set(loadedByte.getUResized(RV32.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        LHU("LHU", false, ParamType.RD_OFF12, OpCode("000000000000 00000 101 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val imm12 = paramMap[IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex(), 2)
                        rd.set(loadedByte.getUResized(RV32.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SB(
            "SB", false, ParamType.RS2_OFF12,
            OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                val imm5 = paramMap[IMM5]
                val imm7 = paramMap[IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV32.XLEN) shl 5) + imm5
                        val memAddr = rs1.get().toBin().getResized(RV32.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Variable.Size.Bit8()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SH(
            "SH", false, ParamType.RS2_OFF12,
            OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                val imm5 = paramMap[IMM5]
                val imm7 = paramMap[IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV32.XLEN) shl 5) + imm5
                        val memAddr = rs1.get().toBin().getResized(RV32.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Variable.Size.Bit16()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SW(
            "SW", false, ParamType.RS2_OFF12,
            OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
                val imm5 = paramMap[IMM5]
                val imm7 = paramMap[IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV32.XLEN) shl 5) + imm5
                        val memAddr = rs1.variable.get().toBin().getResized(RV32.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Variable.Size.Bit32()))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADDI("ADDI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 000 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV32.XLEN)
                        val sum = rs1.get().toBin() + paddedImm64
                        rd.set(sum)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLTI("SLTI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 010 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV32.XLEN)
                        rd.set(if (rs1.get().toDec() < paddedImm64.toDec()) Variable.Value.Bin("1", RV32.XLEN) else Variable.Value.Bin("0", RV32.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLTIU("SLTIU", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 011 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV32.XLEN)
                        rd.set(if (rs1.get().toBin() < paddedImm64) Variable.Value.Bin("1", RV32.XLEN) else Variable.Value.Bin("0", RV32.XLEN))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        XORI("XORI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 100 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV32.XLEN)
                        rd.set(rs1.get().toBin() xor paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ORI("ORI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 110 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV32.XLEN)
                        rd.set(rs1.get().toBin() or paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ANDI("ANDI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 111 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV32.XLEN)
                        rd.set(rs1.get().toBin() and paddedImm64)
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SLLI(
            "SLLI", false, ParamType.RD_RS1_SHAMT5,
            OpCode("0000000 00000 00000 001 00000 0010011", arrayOf(FUNCT7, SHAMT, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt5 = paramMap[SHAMT]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt5 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() ushl shamt5.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRLI(
            "SRLI", false, ParamType.RD_RS1_SHAMT5,
            OpCode("0000000 00000 00000 101 00000 0010011", arrayOf(FUNCT7, SHAMT, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt5 = paramMap[SHAMT]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt5 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() ushr shamt5.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        SRAI(
            "SRAI", false, ParamType.RD_RS1_SHAMT5,
            OpCode("0100000 00000 00000 101 00000 0010011", arrayOf(FUNCT7, SHAMT, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt5 = paramMap[SHAMT]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt5 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() shr shamt5.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        ADD(
            "ADD", false, ParamType.RD_RS1_RS2,
            OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
        SUB(
            "SUB", false, ParamType.RD_RS1_RS2,
            OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
        SLL(
            "SLL", false, ParamType.RD_RS1_RS2,
            OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
        SLT(
            "SLT", false, ParamType.RD_RS1_RS2,
            OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
            OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
            OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
            OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
        SRA(
            "SRA", false, ParamType.RD_RS1_RS2,
            OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
        OR(
            "OR", false, ParamType.RD_RS1_RS2,
            OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
            OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]
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
            "CSRRW", false, ParamType.CSR_RD_OFF12_RS1,
            OpCode("000000000000 00000 001 00000 1110011", arrayOf(CSR, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val csrAddr = paramMap[CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(rs1.get())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRS(
            "CSRRS", false, ParamType.CSR_RD_OFF12_RS1,
            OpCode("000000000000 00000 010 00000 1110011", arrayOf(CSR, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val csrAddr = paramMap[CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(rs1.get().toBin() or csr.get().toBin())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRC(
            "CSRRC", false, ParamType.CSR_RD_OFF12_RS1,
            OpCode("000000000000 00000 011 00000 1110011", arrayOf(CSR, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val csrAddr = paramMap[CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() and rs1.get().toBin().inv())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRWI(
            "CSRRWI", false, ParamType.CSR_RD_OFF12_UIMM5,
            OpCode("000000000000 00000 101 00000 1110011", arrayOf(CSR, UIMM5, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val uimm5 = paramMap[UIMM5]
                val csrAddr = paramMap[CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(uimm5.getUResized(RV32.XLEN))

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRSI(
            "CSRRSI", false, ParamType.CSR_RD_OFF12_UIMM5,
            OpCode("000000000000 00000 110 00000 1110011", arrayOf(CSR, UIMM5, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val uimm5 = paramMap[UIMM5]
                val csrAddr = paramMap[CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() or uimm5.getUResized(RV32.XLEN))

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },
        CSRRCI(
            "CSRRCI", false, ParamType.CSR_RD_OFF12_UIMM5,
            OpCode("000000000000 00000 111 00000 1110011", arrayOf(CSR, UIMM5, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val uimm5 = paramMap[UIMM5]
                val csrAddr = paramMap[CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() and uimm5.getUResized(RV32.XLEN).inv())

                        pc.set(pc.get() + Variable.Value.Hex("4"))
                    }
                }
            }
        },

        CSRW("CSRW", true, ParamType.PS_CSR_RS1, needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)),
        CSRR("CSRR", true, ParamType.PS_RD_CSR, needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)),

        // M Extension
        MUL(
            "MUL",
            false,
            ParamType.RD_RS1_RS2,
            OpCode("0000001 00000 00000 000 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]

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
            OpCode("0000001 00000 00000 001 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2, false).ushr(RV32.XLEN.bitWidth).getResized(RV32.XLEN)
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
            OpCode("0000001 00000 00000 010 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2, resizeToLargestParamSize = false, true).ushr(RV32.XLEN.bitWidth).getResized(RV32.XLEN)
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
            OpCode("0000001 00000 00000 011 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = (factor1 * factor2).toBin().ushr(RV32.XLEN.bitWidth).getUResized(RV32.XLEN)
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
            OpCode("0000001 00000 00000 100 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]

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
            OpCode("0000001 00000 00000 101 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]

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
            OpCode("0000001 00000 00000 110 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]

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
            OpCode("0000001 00000 00000 111 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RD]
                val rs1Addr = paramMap[RS1]
                val rs2Addr = paramMap[RS2]

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

        Nop("NOP", true, ParamType.PS_NONE),
        Mv("MV", true, ParamType.PS_RD_RS1),
        Li("LI", true, ParamType.PS_RD_I32, memWords = 2),
        La("LA", true, ParamType.PS_RD_ALBL, memWords = 2),
        Not("NOT", true, ParamType.PS_RD_RS1),
        Neg("NEG", true, ParamType.PS_RD_RS1),
        Seqz("SEQZ", true, ParamType.PS_RD_RS1),
        Snez("SNEZ", true, ParamType.PS_RD_RS1),
        Sltz("SLTZ", true, ParamType.PS_RD_RS1),
        Sgtz("SGTZ", true, ParamType.PS_RD_RS1),
        Beqz("BEQZ", true, ParamType.PS_RS1_JLBL),
        Bnez("BNEZ", true, ParamType.PS_RS1_JLBL),
        Blez("BLEZ", true, ParamType.PS_RS1_JLBL),
        Bgez("BGEZ", true, ParamType.PS_RS1_JLBL),
        Bltz("BLTZ", true, ParamType.PS_RS1_JLBL),
        BGTZ("BGTZ", true, ParamType.PS_RS1_JLBL),
        Bgt("BGT", true, ParamType.PS_RS1_RS2_JLBL),
        Ble("BLE", true, ParamType.PS_RS1_RS2_JLBL),
        Bgtu("BGTU", true, ParamType.PS_RS1_RS2_JLBL),
        Bleu("BLEU", true, ParamType.PS_RS1_RS2_JLBL),
        J("J", true, ParamType.PS_JLBL),
        JAL1("JAL", true, ParamType.PS_RS1_JLBL, relative = JAL),
        JAL2("JAL", true, ParamType.PS_JLBL, relative = JAL),
        Jr("JR", true, ParamType.PS_RS1),
        JALR1("JALR", true, ParamType.PS_RS1, relative = JALR),
        JALR2("JALR", true, ParamType.RD_OFF12, relative = JALR),
        Ret("RET", true, ParamType.PS_NONE),
        Call("CALL", true, ParamType.PS_JLBL, memWords = 2),
        Tail("TAIL", true, ParamType.PS_JLBL, memWords = 2);

        open fun execute(arch: emulator.kit.Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Variable.Value.Bin>) {
            arch.getConsole().log("> $id {...}")
        }
    }

    class RV32Instr(val type: InstrType, nameToken: Compiler.Token, params: List<Compiler.Token>, parentLabel: ELabel?) : EInstr(nameToken, params, parentLabel)
}