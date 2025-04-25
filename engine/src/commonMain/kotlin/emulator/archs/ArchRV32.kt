package emulator.archs


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.riscv.RvDisassembler
import cengine.util.Endianness
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
        val decoded = RvDisassembler.RVInstrInfoProvider(instrBin) { toUInt32() }

        // EX
        when (decoded.type) {
            RvDisassembler.InstrType.LUI -> {
                baseRegs[decoded.rd.toInt()] = (decoded.imm20uType shl 12)
                incPCBy4()
            }

            RvDisassembler.InstrType.AUIPC -> {
                val result = pc + (decoded.imm20uType shl 12).toInt()
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.JAL -> {
                baseRegs[decoded.rd] = pc + 4
                pc += decoded.jTypeOffset.toInt()
            }

            RvDisassembler.InstrType.JALR -> {
                baseRegs[decoded.rd] = pc + 4
                pc = baseRegs[decoded.rs1] + decoded.imm12iType.signExtend(12) and (-1 shl 1)
            }

            RvDisassembler.InstrType.ECALL -> {

            }

            RvDisassembler.InstrType.EBREAK -> {

            }

            RvDisassembler.InstrType.BEQ -> {
                if (baseRegs[decoded.rs1] == baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RvDisassembler.InstrType.BNE -> {
                if (baseRegs[decoded.rs1] != baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RvDisassembler.InstrType.BLT -> {
                if (baseRegs[decoded.rs1].toInt() < baseRegs[decoded.rs2].toInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RvDisassembler.InstrType.BGE -> {
                if (baseRegs[decoded.rs1].toInt() >= baseRegs[decoded.rs2].toInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RvDisassembler.InstrType.BLTU -> {
                if (baseRegs[decoded.rs1].toUInt() < baseRegs[decoded.rs2].toUInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RvDisassembler.InstrType.BGEU -> {
                if (baseRegs[decoded.rs1].toUInt() >= baseRegs[decoded.rs2].toUInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            RvDisassembler.InstrType.LB -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toInt8().toInt32().toUnsigned()
                incPCBy4()
            }

            RvDisassembler.InstrType.LH -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toInt16().toInt32().toUnsigned()
                incPCBy4()
            }

            RvDisassembler.InstrType.LW -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadWord(address, tracker = tracker)
                incPCBy4()
            }

            RvDisassembler.InstrType.LBU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.LHU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.SB -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2].toUInt8(), tracker = tracker)
                incPCBy4()
            }

            RvDisassembler.InstrType.SH -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2].toUInt16(), tracker = tracker)
                incPCBy4()
            }

            RvDisassembler.InstrType.SW -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2], tracker = tracker)
                incPCBy4()
            }

            RvDisassembler.InstrType.ADDI -> {
                val result = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SLTI -> {
                if (baseRegs[decoded.rs1].toInt() < decoded.iTypeImm.toInt()) {
                    baseRegs[decoded.rd] = UInt32.ONE
                } else {
                    baseRegs[decoded.rd] = UInt32.ZERO
                }
                incPCBy4()
            }

            RvDisassembler.InstrType.SLTIU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1] < decoded.imm12iType) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            RvDisassembler.InstrType.XORI -> {
                val result = baseRegs[decoded.rs1] xor decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.ORI -> {
                val result = baseRegs[decoded.rs1] or decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.ANDI -> {
                val result = baseRegs[decoded.rs1] and decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SLLI -> {
                val result = baseRegs[decoded.rs1] shl decoded.shamt.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SRLI -> {
                val result = baseRegs[decoded.rs1] shr decoded.shamt.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SRAI -> {
                val result = baseRegs[decoded.rs1].toInt32() shr decoded.shamt.toInt()
                baseRegs[decoded.rd] = result.toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.ADD -> {
                val result = baseRegs[decoded.rs1] + baseRegs[decoded.rs2]
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SUB -> {
                val result = baseRegs[decoded.rs1] - baseRegs[decoded.rs2]
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SLL -> {
                val result = (baseRegs[decoded.rs1] shl baseRegs[decoded.rs2])
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SLT -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toInt() < baseRegs[decoded.rs2].toInt()) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            RvDisassembler.InstrType.SLTU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toUInt() < baseRegs[decoded.rs2].toUInt()) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            RvDisassembler.InstrType.XOR -> {
                val result = baseRegs[decoded.rs1] xor baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SRL -> {
                val result = baseRegs[decoded.rs1] shr baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.SRA -> {
                val result = baseRegs[decoded.rs1].toInt32() shr baseRegs[decoded.rs2].toInt()
                baseRegs[decoded.rd.toInt()] = result.toUnsigned()
                incPCBy4()
            }

            RvDisassembler.InstrType.OR -> {
                val result = baseRegs[decoded.rs1] or baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.AND -> {
                val result = baseRegs[decoded.rs1] and baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            RvDisassembler.InstrType.FENCE -> {

            }

            RvDisassembler.InstrType.FENCEI -> {

            }

            RvDisassembler.InstrType.CSRRW -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RvDisassembler.InstrType.CSRRS -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1] or csrRegs[decoded.imm12iType]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RvDisassembler.InstrType.CSRRC -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1] and csrRegs[decoded.imm12iType]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RvDisassembler.InstrType.CSRRWI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = decoded.rs1
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RvDisassembler.InstrType.CSRRSI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = t or decoded.rs1.toInt()
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RvDisassembler.InstrType.CSRRCI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = t and decoded.rs1.inv().toInt()
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            RvDisassembler.InstrType.MUL -> {
                val result = (baseRegs[decoded.rs1].toInt32() * baseRegs[decoded.rs2].toInt32())
                baseRegs[decoded.rd.toInt()] = result.toUnsigned()
                incPCBy4()
            }

            RvDisassembler.InstrType.MULH -> {
                val a = baseRegs[decoded.rs1].toLong()
                val b = baseRegs[decoded.rs2].toLong()
                val result = (a * b).shr(32)

                baseRegs[decoded.rd.toInt()] = result.toInt().toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.MULHSU -> {
                val a = baseRegs[decoded.rs1].toInt().toLong()
                val b = baseRegs[decoded.rs2].toUInt().toLong()
                val result = (a * b).shr(32).toInt()

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.MULHU -> {
                val a = baseRegs[decoded.rs1].toUInt().toULong()
                val b = baseRegs[decoded.rs2].toUInt().toULong()
                val result = (a * b).shr(32).toUInt()

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.DIV -> {
                val a = baseRegs[decoded.rs1].toInt()
                val b = baseRegs[decoded.rs2].toInt()
                val result = a / b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.DIVU -> {
                val a = baseRegs[decoded.rs1].toUInt()
                val b = baseRegs[decoded.rs2].toUInt()
                val result = a / b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.REM -> {
                val a = baseRegs[decoded.rs1].toInt()
                val b = baseRegs[decoded.rs2].toInt()
                val result = a % b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            RvDisassembler.InstrType.REMU -> {
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
            RvDisassembler.InstrType.JALR -> true
            else -> false
        }
        val isBranchToSubroutine = when (decoded.type) {
            RvDisassembler.InstrType.JAL -> true
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
            ArchConfig.Setting.Enumeration("Instruction Cache", Setting.entries, Setting.NONE) { arch, setting ->
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
            ArchConfig.Setting.Enumeration("Data Cache", Setting.entries, Setting.NONE) { arch, setting ->
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
        override val DISASSEMBLER: AsmDisassembler = RvDisassembler { toUInt32() }

    }

}