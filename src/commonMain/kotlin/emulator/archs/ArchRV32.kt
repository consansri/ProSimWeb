package emulator.archs


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cengine.lang.asm.Disassembler
import cengine.lang.asm.ast.target.riscv.RVDisassembler
import cengine.util.Endianness
import cengine.util.integer.Int32.Companion.toInt32
import cengine.util.integer.UInt16
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32
import cengine.util.integer.UInt8
import emulator.archs.riscv.riscv32.RV32BaseRegs
import emulator.archs.riscv.riscv32.RV32CSRRegs
import emulator.kit.ArchConfig
import emulator.kit.MicroSetup
import emulator.kit.memory.*
import emulator.kit.memory.Cache.Setting
import emulator.kit.optional.BasicArchImpl

class ArchRV32 : BasicArchImpl<UInt32, UInt8>(UInt32, UInt8) {

    override val config: ArchConfig = ArchRV32
    override val pcState: MutableState<UInt32> = mutableStateOf(UInt32.ZERO)
    private var pc by pcState

    // Memories

    override val memory: MainMemory<UInt32, UInt8> = MainMemory(Endianness.LITTLE, UInt32, UInt8)

    var instrMemory: Memory<UInt32, UInt8> = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    var dataMemory: Memory<UInt32, UInt8> = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    // Reg Files

    private val baseRegs = RV32BaseRegs()
    private val csrRegs = RV32CSRRegs()

    private fun Memory<UInt32, UInt8>.loadWord(addr: UInt32, tracker: Memory.AccessTracker): UInt32 {
        val bytes = loadEndianAwareBytes(addr, 4, tracker)
        return UInt32.fromUInt16(UInt16.fromUInt8(bytes[0], bytes[1]), UInt16.fromUInt8(bytes[2], bytes[3]))
    }

    private fun Memory<UInt32, UInt8>.loadHalf(addr: UInt32, tracker: Memory.AccessTracker): UInt16 {
        val bytes = loadEndianAwareBytes(addr, 2, tracker)
        return UInt16.fromUInt8(bytes[0], bytes[1])
    }

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        // Shortcuts

        // IF
        val instrBin = instrMemory.loadWord(pc, tracker)

        // DC
        val decoded = RVDisassembler.RVInstrInfoProvider(instrBin) { toUInt32().toBigInt() }

        // EX
        when (decoded.type) {
            RVDisassembler.InstrType.LUI -> {
                baseRegs[decoded.rd.toInt()] = (decoded.imm20uType shl 12)
                incPCBy4()
            }

            RVDisassembler.InstrType.AUIPC -> {
                val result = pc + (decoded.imm20uType shl 12).toInt()
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.JAL -> {
                baseRegs[decoded.rd] = pc + 4
                pc += decoded.jTypeOffset.toInt()
            }

            RVDisassembler.InstrType.JALR -> {
                baseRegs[decoded.rd] = pc + 4
                pc = baseRegs[decoded.rs1] + decoded.imm12iType.signExtend(12) and (-1 shl 1)
            }

            RVDisassembler.InstrType.ECALL -> {

            }

            RVDisassembler.InstrType.EBREAK -> {

            }

            RVDisassembler.InstrType.BEQ -> {
                if (baseRegs[decoded.rs1] == baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BNE -> {
                if (baseRegs[decoded.rs1] != baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BLT -> {
                if (baseRegs[decoded.rs1].toInt() < baseRegs[decoded.rs2].toInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BGE -> {
                if (baseRegs[decoded.rs1].toInt() >= baseRegs[decoded.rs2].toInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BLTU -> {
                if (baseRegs[decoded.rs1].toUInt() < baseRegs[decoded.rs2].toUInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.BGEU -> {
                if (baseRegs[decoded.rs1].toUInt() >= baseRegs[decoded.rs2].toUInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RVDisassembler.InstrType.LB -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toInt8().toInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.LH -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toInt16().toInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.LW -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadWord(address, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.LBU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.LHU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.SB -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAware(address, baseRegs[decoded.rs2].toUInt8(), tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.SH -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAware(address, baseRegs[decoded.rs2].toUInt16(), tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.SW -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAware(address, baseRegs[decoded.rs2], tracker = tracker)
                incPCBy4()
            }

            RVDisassembler.InstrType.ADDI -> {
                val result = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SLTI -> {
                if (baseRegs[decoded.rs1].toInt() < decoded.iTypeImm.toInt()) {
                    baseRegs[decoded.rd] = UInt32.ONE
                } else {
                    baseRegs[decoded.rd] = UInt32.ZERO
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.SLTIU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1] < decoded.imm12iType) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.XORI -> {
                val result = baseRegs[decoded.rs1] xor decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.ORI -> {
                val result = baseRegs[decoded.rs1] or decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.ANDI -> {
                val result = baseRegs[decoded.rs1] and decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SLLI -> {
                val result = baseRegs[decoded.rs1] shl decoded.shamt.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SRLI -> {
                val result = baseRegs[decoded.rs1] shr decoded.shamt.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SRAI -> {
                val result = baseRegs[decoded.rs1].toInt32() shr decoded.shamt.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.ADD -> {
                val result = baseRegs[decoded.rs1] + baseRegs[decoded.rs2]
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SUB -> {
                val result = baseRegs[decoded.rs1] - baseRegs[decoded.rs2]
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SLL -> {
                val result = (baseRegs[decoded.rs1] shl baseRegs[decoded.rs2])
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SLT -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toInt() < baseRegs[decoded.rs2].toInt()) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.SLTU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toUInt() < baseRegs[decoded.rs2].toUInt()) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            RVDisassembler.InstrType.XOR -> {
                val result = baseRegs[decoded.rs1] xor baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SRL -> {
                val result = baseRegs[decoded.rs1] shr baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.SRA -> {
                val result = baseRegs[decoded.rs1].toInt() shr baseRegs[decoded.rs2].toInt()
                baseRegs[decoded.rd.toInt()] = result.toInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.OR -> {
                val result = baseRegs[decoded.rs1] or baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.AND -> {
                val result = baseRegs[decoded.rs1] and baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.FENCE -> {

            }

            RVDisassembler.InstrType.FENCEI -> {

            }

            RVDisassembler.InstrType.CSRRW -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRS -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1] or csrRegs[decoded.imm12iType]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRC -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1] and csrRegs[decoded.imm12iType]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRWI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = decoded.rs1
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRSI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = t or decoded.rs1.toInt()
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RVDisassembler.InstrType.CSRRCI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = t and decoded.rs1.inv().toInt()
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RVDisassembler.InstrType.MUL -> {
                val result = (baseRegs[decoded.rs1].toInt32() * baseRegs[decoded.rs2].toInt32())
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RVDisassembler.InstrType.MULH -> {
                val a = baseRegs[decoded.rs1].toLong()
                val b = baseRegs[decoded.rs2].toLong()
                val result = (a * b).shr(32).toInt()

                baseRegs[decoded.rd.toInt()] = result.toInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.MULHSU -> {
                val a = baseRegs[decoded.rs1].toInt().toLong()
                val b = baseRegs[decoded.rs2].toUInt().toLong()
                val result = (a * b).shr(32).toInt()

                baseRegs[decoded.rd.toInt()] = result.toInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.MULHU -> {
                val a = baseRegs[decoded.rs1].toUInt().toULong()
                val b = baseRegs[decoded.rs2].toUInt().toULong()
                val result = (a * b).shr(32).toUInt()

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.DIV -> {
                val a = baseRegs[decoded.rs1].toInt()
                val b = baseRegs[decoded.rs2].toInt()
                val result = a / b

                baseRegs[decoded.rd.toInt()] = result.toInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.DIVU -> {
                val a = baseRegs[decoded.rs1].toUInt()
                val b = baseRegs[decoded.rs2].toUInt()
                val result = a / b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.REM -> {
                val a = baseRegs[decoded.rs1].toInt()
                val b = baseRegs[decoded.rs2].toInt()
                val result = a % b

                baseRegs[decoded.rd.toInt()] = result.toInt32()
                incPCBy4()
            }

            RVDisassembler.InstrType.REMU -> {
                val a = baseRegs[decoded.rs1].toUInt()
                val b = baseRegs[decoded.rs2].toUInt()
                val result = a % b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            null -> {
                console.error("Invalid Instruction!")
                return ExecutionResult(false)
            }

            else -> {
                console.error("${decoded.type} is not a ${description.name} instruction!")
                return ExecutionResult(false)
            }
        }

        val isReturnFromSubroutine = when (decoded.type) {
            RVDisassembler.InstrType.JALR -> true
            else -> false
        }
        val isBranchToSubroutine = when (decoded.type) {
            RVDisassembler.InstrType.JAL -> true
            else -> false
        }

        return ExecutionResult(true, typeIsReturnFromSubroutine = isReturnFromSubroutine, typeIsBranchToSubroutine = isBranchToSubroutine)
    }


    private fun incPCBy4() {
        pc += 4
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
        MicroSetup.append(baseRegs)
        MicroSetup.append(csrRegs)
    }

    override fun resetPC() {
        pc = UInt32.ZERO
    }

    companion object : ArchConfig {
        override val DESCR: ArchConfig.Description = ArchConfig.Description("RV32I", "RISC-V 32Bit")
        override val SETTINGS: List<ArchConfig.Setting<*>> = listOf(
            ArchConfig.Setting.Enumeration("Instruction Cache", Cache.Setting.entries, Setting.NONE) { arch, setting ->
                if (arch is ArchRV32) {
                    arch.instrMemory = when (setting.get()) {
                        Setting.NONE -> arch.memory
                        Setting.DirectedMapped -> DMCache(arch.memory, CacheSize.KiloByte_32, "Instruction")
                        Setting.FullAssociativeRandom -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM, "Instruction")
                        Setting.FullAssociativeLRU -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU, "Instruction")
                        Setting.FullAssociativeFIFO -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO, "Instruction")
                        Setting.SetAssociativeRandom -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM, "Instruction")
                        Setting.SetAssociativeLRU -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU, "Instruction")
                        Setting.SetAssociativeFIFO -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO, "Instruction")
                    }
                }
            },
            ArchConfig.Setting.Enumeration("Data Cache", Cache.Setting.entries, Setting.NONE) { arch, setting ->
                if (arch is ArchRV32) {
                    arch.dataMemory = when (setting.get()) {
                        Setting.NONE -> arch.memory
                        Setting.DirectedMapped -> DMCache(arch.memory, CacheSize.KiloByte_32, "Data")
                        Setting.FullAssociativeRandom -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM, "Data")
                        Setting.FullAssociativeLRU -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU, "Data")
                        Setting.FullAssociativeFIFO -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO, "Data")
                        Setting.SetAssociativeRandom -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM, "Data")
                        Setting.SetAssociativeLRU -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU, "Data")
                        Setting.SetAssociativeFIFO -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO, "Data")
                    }
                }
            }
        )
        override val DISASSEMBLER: Disassembler = RVDisassembler { toUInt32().toBigInt() }

    }

}