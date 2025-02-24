package emulator.archs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cengine.lang.asm.Disassembler
import cengine.lang.asm.ast.target.riscv.RVDisassembler
import cengine.lang.asm.ast.target.riscv.RVDisassembler.InstrType.JAL
import cengine.lang.asm.ast.target.riscv.RVDisassembler.InstrType.JALR
import cengine.util.Endianness
import cengine.util.integer.BigInt.Companion.toBigInt
import cengine.util.integer.Int64.Companion.toInt64
import cengine.util.integer.UInt16
import cengine.util.integer.UInt32
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import cengine.util.integer.UInt8
import emulator.archs.riscv.riscv64.RV64BaseRegs
import emulator.archs.riscv.riscv64.RV64CSRRegs
import emulator.kit.ArchConfig
import emulator.kit.MicroSetup
import emulator.kit.memory.*
import emulator.kit.memory.Cache.Setting
import emulator.kit.optional.BasicArchImpl

class ArchRV64 : BasicArchImpl<UInt64, UInt8>(UInt64, UInt8) {

    override val config: ArchConfig = ArchRV64
    override val pcState: MutableState<UInt64> = mutableStateOf(UInt64.ZERO)


    private var pc by pcState

    override val memory: MainMemory<UInt64, UInt8> = MainMemory(Endianness.LITTLE, UInt64, UInt8)

    var instrMemory: Memory<UInt64, UInt8> = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    var dataMemory: Memory<UInt64, UInt8> = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    private val baseRegs = RV64BaseRegs()
    private val csrRegs = RV64CSRRegs()

    private fun Memory<UInt64, UInt8>.loadDWord(addr: UInt64, tracker: Memory.AccessTracker): UInt64 {
        val bytes = loadEndianAwareBytes(addr, 8, tracker)
        return UInt64.fromUInt32(
            UInt32.fromUInt16(UInt16.fromUInt8(bytes[0], bytes[1]), UInt16.fromUInt8(bytes[2], bytes[3])),
            UInt32.fromUInt16(UInt16.fromUInt8(bytes[4], bytes[5]), UInt16.fromUInt8(bytes[6], bytes[7]))
        )
    }

    private fun Memory<UInt64, UInt8>.loadWord(addr: UInt64, tracker: Memory.AccessTracker): UInt32 {
        val bytes = loadEndianAwareBytes(addr, 4, tracker)
        return UInt32.fromUInt16(UInt16.fromUInt8(bytes[0], bytes[1]), UInt16.fromUInt8(bytes[2], bytes[3]))
    }

    private fun Memory<UInt64, UInt8>.loadHalf(addr: UInt64, tracker: Memory.AccessTracker): UInt16 {
        val bytes = loadEndianAwareBytes(addr, 2, tracker)
        return UInt16.fromUInt8(bytes[0], bytes[1])
    }

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        // IF
        val instrBin = instrMemory.loadWord(pc, tracker)

        // DC
        val decoded = RVDisassembler.RVInstrInfoProvider(instrBin) { toUInt64().toBigInt() }

        // EX
        when (decoded.type) {
            RVDisassembler.InstrType.LUI -> {
                baseRegs[decoded.rd] = (decoded.imm20uType.toLong() shl 12).toInt64()
                pc += 4
            }

            RVDisassembler.InstrType.AUIPC -> {
                baseRegs[decoded.rd] = pc + (decoded.imm20uType shl 12).toInt()
                pc += 4
            }

            RVDisassembler.InstrType.JAL -> {
                baseRegs[decoded.rd] = pc + 4
                pc += decoded.jTypeOffset
            }

            RVDisassembler.InstrType.JALR -> {
                baseRegs[decoded.rd] = pc + 4

                pc = baseRegs[decoded.rs1] + (decoded.iTypeImm and (-1L shl 1))
            }

            RVDisassembler.InstrType.ECALL -> {

            }

            RVDisassembler.InstrType.EBREAK -> {

            }

            RVDisassembler.InstrType.BEQ -> {
                if (baseRegs[decoded.rs1] == baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            RVDisassembler.InstrType.BNE -> {
                if (baseRegs[decoded.rs1] != baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            RVDisassembler.InstrType.BLT -> {
                if (baseRegs[decoded.rs1].toLong() < baseRegs[decoded.rs2].toLong()) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            RVDisassembler.InstrType.BGE -> {
                if (baseRegs[decoded.rs1].toLong() >= baseRegs[decoded.rs2].toLong()) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            RVDisassembler.InstrType.BLTU -> {
                if (baseRegs[decoded.rs1].toULong() < baseRegs[decoded.rs2].toULong()) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            RVDisassembler.InstrType.BGEU -> {
                if (baseRegs[decoded.rs1].toULong() >= baseRegs[decoded.rs2].toULong()) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            RVDisassembler.InstrType.LB -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toInt8().toInt64()
                pc += 4
            }

            RVDisassembler.InstrType.LH -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toInt16().toInt64()
                pc += 4
            }

            RVDisassembler.InstrType.LW -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadWord(address, tracker = tracker).toInt32().toInt64()
                pc += 4
            }

            RVDisassembler.InstrType.LD -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadDWord(address, tracker = tracker)
                pc += 4
            }

            RVDisassembler.InstrType.LBU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toUInt64()
                pc += 4
            }

            RVDisassembler.InstrType.LHU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toUInt64()
                pc += 4
            }

            RVDisassembler.InstrType.LWU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadWord(address, tracker = tracker).toUInt64()
                pc += 4
            }

            RVDisassembler.InstrType.SB -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm
                dataMemory.storeEndianAware(address, baseRegs[decoded.rs2].toUInt8(), tracker = tracker)
                pc += 4
            }

            RVDisassembler.InstrType.SH -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm
                dataMemory.storeEndianAware(address, baseRegs[decoded.rs2].toUInt16(), tracker = tracker)
                pc += 4
            }

            RVDisassembler.InstrType.SW -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm
                dataMemory.storeEndianAware(address, baseRegs[decoded.rs2].toUInt32(), tracker = tracker)
                pc += 4
            }

            RVDisassembler.InstrType.SD -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm
                dataMemory.storeEndianAware(address, baseRegs[decoded.rs2], tracker = tracker)
                pc += 4
            }

            RVDisassembler.InstrType.ADDI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] + decoded.iTypeImm
                pc += 4
            }

            RVDisassembler.InstrType.ADDIW -> {
                val result = baseRegs[decoded.rs1].toInt32() + decoded.iTypeImm.toInt64().toInt32()
                baseRegs[decoded.rd] = result.toInt64()
                pc += 4
            }

            RVDisassembler.InstrType.SLTI -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toInt64() < decoded.iTypeImm.toInt64()) {
                    UInt64.ONE
                } else {
                    UInt64.ZERO
                }
                pc += 4
            }

            RVDisassembler.InstrType.SLTIU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toULong() < decoded.iTypeImm.toULong()) {
                    UInt64.ONE
                } else {
                    UInt64.ZERO
                }
                pc += 4
            }

            RVDisassembler.InstrType.XORI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] xor decoded.iTypeImm
                pc += 4
            }

            RVDisassembler.InstrType.ORI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] or decoded.iTypeImm
                pc += 4
            }

            RVDisassembler.InstrType.ANDI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] and decoded.iTypeImm
                pc += 4
            }

            RVDisassembler.InstrType.SLLI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] shl decoded.shamt.toInt()
                pc += 4
            }

            RVDisassembler.InstrType.SLLIW -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toUInt32() shl decoded.shamt.toInt()
                pc += 4
            }

            RVDisassembler.InstrType.SRLI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] shr decoded.shamt.toInt()
                pc += 4
            }

            RVDisassembler.InstrType.SRLIW -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toUInt32() shr decoded.shamt.toInt()
                pc += 4
            }

            RVDisassembler.InstrType.SRAI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toInt64() shr decoded.shamt.toInt()
                pc += 4
            }

            RVDisassembler.InstrType.SRAIW -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toInt32() shr decoded.shamt.toInt()
                pc += 4
            }

            RVDisassembler.InstrType.ADD -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] + baseRegs[decoded.rs2]
                pc += 4
            }

            RVDisassembler.InstrType.ADDW -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toInt32() + baseRegs[decoded.rs2].toInt32()
                pc += 4
            }

            RVDisassembler.InstrType.SUB -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] - baseRegs[decoded.rs2]
                pc += 4
            }

            RVDisassembler.InstrType.SUBW -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toInt32() - baseRegs[decoded.rs2].toInt32()
                pc += 4
            }

            RVDisassembler.InstrType.SLL -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] shl baseRegs[decoded.rs2]
                pc += 4
            }

            RVDisassembler.InstrType.SLLW -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toInt32() shl baseRegs[decoded.rs2].toInt32()
                pc += 4
            }

            RVDisassembler.InstrType.SLT -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toLong() < baseRegs[decoded.rs2].toLong()) {
                    UInt64.ONE
                } else {
                    UInt64.ZERO
                }
                pc += 4
            }

            RVDisassembler.InstrType.SLTU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toULong() < baseRegs[decoded.rs2].toULong()) {
                    UInt64.ONE
                } else {
                    UInt64.ONE
                }
                pc += 4
            }

            RVDisassembler.InstrType.XOR -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] xor baseRegs[decoded.rs2]
                pc += 4
            }

            RVDisassembler.InstrType.SRL -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] shr baseRegs[decoded.rs2]
                pc += 4
            }

            RVDisassembler.InstrType.SRLW -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toUInt32() shr baseRegs[decoded.rs2].toInt()
                pc += 4
            }

            RVDisassembler.InstrType.SRA -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toInt64() shr baseRegs[decoded.rs2].toInt()
                pc += 4
            }

            RVDisassembler.InstrType.SRAW -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1].toInt32() shr baseRegs[decoded.rs2].toULong().toInt()
                pc += 4
            }

            RVDisassembler.InstrType.OR -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] or baseRegs[decoded.rs2]
                pc += 4
            }

            RVDisassembler.InstrType.AND -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] and baseRegs[decoded.rs2]
                pc += 4
            }

            RVDisassembler.InstrType.FENCE -> {

            }

            RVDisassembler.InstrType.FENCEI -> {

            }

            RVDisassembler.InstrType.CSRRW -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1]
                baseRegs[decoded.rd] = t

                pc += 4
            }

            RVDisassembler.InstrType.CSRRS -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1] or t
                baseRegs[decoded.rd] = t

                pc += 4
            }

            RVDisassembler.InstrType.CSRRC -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = baseRegs[decoded.rs1] and t
                baseRegs[decoded.rd] = t

                pc += 4
            }

            RVDisassembler.InstrType.CSRRWI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = decoded.rs1
                baseRegs[decoded.rd] = t

                pc += 4
            }

            RVDisassembler.InstrType.CSRRSI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = (t.toULong() or decoded.rs1.toULong()).toUInt64()
                baseRegs[decoded.rd] = t

                pc += 4
            }

            RVDisassembler.InstrType.CSRRCI -> {
                val t = csrRegs[decoded.imm12iType]
                csrRegs[decoded.imm12iType] = (t.toULong() and decoded.rs1.toULong().inv()).toUInt64()
                baseRegs[decoded.rd] = t

                pc += 4
            }

            RVDisassembler.InstrType.MUL -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] * baseRegs[decoded.rs2]
                pc += 4
            }

            RVDisassembler.InstrType.MULH -> {
                val a = baseRegs[decoded.rs1].toLong().toBigInt()
                val b = baseRegs[decoded.rs2].toLong().toBigInt()

                baseRegs[decoded.rd] = ((a * b) shr 64).toUInt64()
                pc += 4
            }

            RVDisassembler.InstrType.MULHSU -> {
                val a = baseRegs[decoded.rs1].toLong().toBigInt()
                val b = baseRegs[decoded.rs2].toBigInt()

                baseRegs[decoded.rd] = ((a * b) shr 64).toUInt64()
                pc += 4
            }

            RVDisassembler.InstrType.MULHU -> {
                val a = baseRegs[decoded.rs1].toBigInt()
                val b = baseRegs[decoded.rs2].toBigInt()

                baseRegs[decoded.rd] = ((a * b) shr 64).toUInt64()
                pc += 4
            }

            RVDisassembler.InstrType.DIV -> {
                val a = baseRegs[decoded.rs1].toInt64()
                val b = baseRegs[decoded.rs2].toInt64()

                baseRegs[decoded.rd] = a / b
                pc += 4
            }

            RVDisassembler.InstrType.DIVU -> {
                val a = baseRegs[decoded.rs1]
                val b = baseRegs[decoded.rs2]

                baseRegs[decoded.rd] = a / b
                pc += 4
            }

            RVDisassembler.InstrType.REM -> {
                val a = baseRegs[decoded.rs1].toInt64()
                val b = baseRegs[decoded.rs2].toInt64()

                baseRegs[decoded.rd] = a % b
                pc += 4
            }

            RVDisassembler.InstrType.REMU -> {
                val a = baseRegs[decoded.rs1]
                val b = baseRegs[decoded.rs2]

                baseRegs[decoded.rd] = a % b
                pc += 4
            }

            RVDisassembler.InstrType.MULW -> {
                val a = baseRegs[decoded.rs1].toInt32()
                val b = baseRegs[decoded.rs2].toInt32()

                baseRegs[decoded.rd] = (a * b).toInt64()
                pc += 4
            }

            RVDisassembler.InstrType.DIVW -> {
                val a = baseRegs[decoded.rs1].toInt32()
                val b = baseRegs[decoded.rs2].toInt32()

                baseRegs[decoded.rd] = (a / b).toInt64()
                pc += 4
            }

            RVDisassembler.InstrType.DIVUW -> {
                val a = baseRegs[decoded.rs1].toUInt32()
                val b = baseRegs[decoded.rs2].toUInt32()

                baseRegs[decoded.rd] = a / b
                pc += 4
            }

            RVDisassembler.InstrType.REMW -> {
                val a = baseRegs[decoded.rs1].toInt32()
                val b = baseRegs[decoded.rs2].toInt32()

                baseRegs[decoded.rd] = (a % b).toInt64()
                pc += 4
            }

            RVDisassembler.InstrType.REMUW -> {
                val a = baseRegs[decoded.rs1].toUInt32()
                val b = baseRegs[decoded.rs2].toUInt32()

                baseRegs[decoded.rd] = a % b
                pc += 4
            }

            null -> {
                console.error("Invalid Instruction!")
                return ExecutionResult(false)
            }
        }

        val isReturnFromSubroutine = when (decoded.type) {
            JALR -> true
            else -> false
        }
        val isBranchToSubroutine = when (decoded.type) {
            JAL -> true
            else -> false
        }

        return ExecutionResult(true, typeIsReturnFromSubroutine = isReturnFromSubroutine, typeIsBranchToSubroutine = isBranchToSubroutine)
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
        MicroSetup.append(baseRegs)
        MicroSetup.append(csrRegs)
    }

    override fun resetPC() {
        pc = UInt64.ZERO
    }

    companion object : ArchConfig {
        override val DESCR: ArchConfig.Description = ArchConfig.Description("RV64I", "RISC-V 64Bit")
        override val SETTINGS: List<ArchConfig.Setting<*>> = listOf(
            ArchConfig.Setting.Enumeration("Instruction Cache", Setting.entries, Setting.NONE) { arch, setting ->
                if (arch is ArchRV64) {
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
                if (arch is ArchRV64) {
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
        override val DISASSEMBLER: Disassembler = RVDisassembler { toUInt64().toBigInt() }
    }
}