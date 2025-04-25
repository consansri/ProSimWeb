package emulator.archs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.riscv.RvDisassembler
import cengine.lang.asm.target.riscv.RvDisassembler.InstrType.*
import cengine.lang.asm.target.riscv.RvRegT
import cengine.util.Endianness
import cengine.util.integer.*
import cengine.util.integer.BigInt.Companion.toBigInt
import cengine.util.integer.Int64.Companion.toInt64
import cengine.util.integer.UInt32.Companion.toUInt32
import cengine.util.integer.UInt64.Companion.toUInt64
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
        val decoded = RvDisassembler.RVInstrInfoProvider(instrBin) { toUInt64() }

        // EX
        when (decoded.type) {
            LUI -> {
                baseRegs[decoded.rd] = (decoded.imm20uType.toLong() shl 12).toInt64().toUInt64()
                pc += 4
            }

            AUIPC -> {
                baseRegs[decoded.rd] = pc + (decoded.imm20uType shl 12).toInt()
                pc += 4
            }

            JAL -> {
                baseRegs[decoded.rd] = pc + 4
                pc += decoded.jTypeOffset
            }

            JALR -> {
                baseRegs[decoded.rd] = pc + 4

                pc = baseRegs[decoded.rs1] + (decoded.iTypeImm and (-1L shl 1))
            }

            ECALL -> {
                // Environment call - Triggers a synchronous trap
                console.log("ECALL triggered at 0x${pc.toString(16)}")
                // Basic Machine Mode Trap Handling:
                csrRegs[RvRegT.RvCsrT.MachineT.X341] = pc                 // Save PC of ECALL instruction
                csrRegs[RvRegT.RvCsrT.MachineT.X342] = 11uL.toUInt64()                  // Cause: Environment call from M-mode (assuming M-mode)
                // TODO: Implement privilege level detection and use corresponding cause codes (8=U, 9=S)
                // TODO: Update MSTATUS (e.g., save MIE to MPIE, clear MIE)
                pc = csrRegs[RvRegT.RvCsrT.MachineT.X305] // Jump to Machine Trap Vector Handler
            }

            EBREAK -> {
                // Environment breakpoint - Triggers a synchronous trap
                console.log("EBREAK triggered at 0x${pc.toString(16)}")
                // Basic Machine Mode Trap Handling:
                csrRegs[RvRegT.RvCsrT.MachineT.X341] = pc                 // Save PC of EBREAK instruction
                csrRegs[RvRegT.RvCsrT.MachineT.X342] = 3uL.toUInt64()                   // Cause: Breakpoint
                // TODO: Update MSTATUS
                pc = csrRegs[RvRegT.RvCsrT.MachineT.X305] // Jump to Machine Trap Vector Handler
            }

            BEQ -> {
                if (baseRegs[decoded.rs1] == baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            BNE -> {
                if (baseRegs[decoded.rs1] != baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            BLT -> {
                if (baseRegs[decoded.rs1].toLong() < baseRegs[decoded.rs2].toLong()) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            BGE -> {
                if (baseRegs[decoded.rs1].toLong() >= baseRegs[decoded.rs2].toLong()) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            BLTU -> {
                if (baseRegs[decoded.rs1].toULong() < baseRegs[decoded.rs2].toULong()) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            BGEU -> {
                if (baseRegs[decoded.rs1].toULong() >= baseRegs[decoded.rs2].toULong()) {
                    pc += decoded.bTypeOffset
                } else {
                    pc += 4
                }
            }

            LB -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toInt8().toInt64().toUInt64()
                pc += 4
            }

            LH -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toInt16().toInt64().toUInt64()
                pc += 4
            }

            LW -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadWord(address, tracker = tracker).toInt32().toInt64().toUInt64()
                pc += 4
            }

            LD -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadDWord(address, tracker = tracker)
                pc += 4
            }

            LBU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toUInt64()
                pc += 4
            }

            LHU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toUInt64()
                pc += 4
            }

            LWU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm
                baseRegs[decoded.rd] = dataMemory.loadWord(address, tracker = tracker).toUInt64()
                pc += 4
            }

            SB -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2].toUInt8(), tracker = tracker)
                pc += 4
            }

            SH -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2].toUInt16(), tracker = tracker)
                pc += 4
            }

            SW -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2].toUInt32(), tracker = tracker)
                pc += 4
            }

            SD -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2], tracker = tracker)
                pc += 4
            }

            ADDI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] + decoded.iTypeImm
                pc += 4
            }

            ADDIW -> {
                val result = baseRegs[decoded.rs1].toInt32() + decoded.iTypeImm.toInt64().toInt32()
                baseRegs[decoded.rd] = result.toInt64().toUInt64()
                pc += 4
            }

            SLTI -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toInt64() < decoded.iTypeImm.toInt64()) {
                    UInt64.ONE
                } else {
                    UInt64.ZERO
                }
                pc += 4
            }

            SLTIU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toULong() < decoded.iTypeImm.toULong()) {
                    UInt64.ONE
                } else {
                    UInt64.ZERO
                }
                pc += 4
            }

            XORI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] xor decoded.iTypeImm
                pc += 4
            }

            ORI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] or decoded.iTypeImm
                pc += 4
            }

            ANDI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] and decoded.iTypeImm
                pc += 4
            }

            SLLI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] shl decoded.shamtRV64
                pc += 4
            }

            SLLIW -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toUInt32() shl decoded.shamtRV32).toUInt64()
                pc += 4
            }

            SRLI -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] shr decoded.shamtRV64
                pc += 4
            }

            SRLIW -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toUInt32() shr decoded.shamtRV32).toUInt64()
                pc += 4
            }

            SRAI -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toInt64() shr decoded.shamtRV64.toInt()).toUInt64()
                pc += 4
            }

            SRAIW -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toInt32() shr decoded.shamtRV32.toInt()).toUInt64()
                pc += 4
            }

            ADD -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] + baseRegs[decoded.rs2]
                pc += 4
            }

            ADDW -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toInt32() + baseRegs[decoded.rs2].toInt32()).toUInt64()
                pc += 4
            }

            SUB -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] - baseRegs[decoded.rs2]
                pc += 4
            }

            SUBW -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toInt32() - baseRegs[decoded.rs2].toInt32()).toUInt64()
                pc += 4
            }

            SLL -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] shl baseRegs[decoded.rs2]
                pc += 4
            }

            SLLW -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toInt32() shl baseRegs[decoded.rs2].toInt32()).toUInt64()
                pc += 4
            }

            SLT -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toLong() < baseRegs[decoded.rs2].toLong()) {
                    UInt64.ONE
                } else {
                    UInt64.ZERO
                }
                pc += 4
            }

            SLTU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toULong() < baseRegs[decoded.rs2].toULong()) {
                    UInt64.ONE
                } else {
                    UInt64.ONE
                }
                pc += 4
            }

            XOR -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] xor baseRegs[decoded.rs2]
                pc += 4
            }

            SRL -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] shr baseRegs[decoded.rs2]
                pc += 4
            }

            SRLW -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toUInt32() shr baseRegs[decoded.rs2].toInt()).toUInt64()
                pc += 4
            }

            SRA -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toInt64() shr baseRegs[decoded.rs2].toInt()).toUInt64()
                pc += 4
            }

            SRAW -> {
                baseRegs[decoded.rd] = (baseRegs[decoded.rs1].toInt32() shr baseRegs[decoded.rs2].toULong().toInt()).toUInt64()
                pc += 4
            }

            OR -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] or baseRegs[decoded.rs2]
                pc += 4
            }

            AND -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] and baseRegs[decoded.rs2]
                pc += 4
            }

            FENCE -> {

            }

            FENCEI -> {

            }

            CSRRW -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = baseRegs[decoded.rs1]
                baseRegs[decoded.rd] = t

                pc += 4
            }

            CSRRS -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = baseRegs[decoded.rs1] or t
                baseRegs[decoded.rd] = t

                pc += 4
            }

            CSRRC -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = baseRegs[decoded.rs1] and t
                baseRegs[decoded.rd] = t

                pc += 4
            }

            CSRRWI -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = decoded.rs1.toUInt64()
                baseRegs[decoded.rd] = t

                pc += 4
            }

            CSRRSI -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = (t.toULong() or decoded.rs1.toULong()).toUInt64()
                baseRegs[decoded.rd] = t

                pc += 4
            }

            CSRRCI -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = (t.toULong() and decoded.rs1.toULong().inv()).toUInt64()
                baseRegs[decoded.rd] = t

                pc += 4
            }

            MUL -> {
                baseRegs[decoded.rd] = baseRegs[decoded.rs1] * baseRegs[decoded.rs2]
                pc += 4
            }

            MULH -> {
                val a = baseRegs[decoded.rs1].toLong().toBigInt()
                val b = baseRegs[decoded.rs2].toLong().toBigInt()

                baseRegs[decoded.rd] = BigInt((a * b).value shr 64).toUInt64()
                pc += 4
            }

            MULHSU -> {
                val a = baseRegs[decoded.rs1].toLong().toBigInt()
                val b = baseRegs[decoded.rs2].toBigInt()

                baseRegs[decoded.rd] = BigInt((a * b).value shr 64).toUInt64()
                pc += 4
            }

            MULHU -> {
                val a = baseRegs[decoded.rs1].toBigInt()
                val b = baseRegs[decoded.rs2].toBigInt()

                baseRegs[decoded.rd] = BigInt((a * b).value shr 64).toUInt64()
                pc += 4
            }

            DIV -> {
                val a = baseRegs[decoded.rs1].toInt64()
                val b = baseRegs[decoded.rs2].toInt64()

                baseRegs[decoded.rd] = (a / b).toUInt64()
                pc += 4
            }

            DIVU -> {
                val a = baseRegs[decoded.rs1]
                val b = baseRegs[decoded.rs2]

                baseRegs[decoded.rd] = a / b
                pc += 4
            }

            REM -> {
                val a = baseRegs[decoded.rs1].toInt64()
                val b = baseRegs[decoded.rs2].toInt64()

                baseRegs[decoded.rd] = (a % b).toUInt64()
                pc += 4
            }

            REMU -> {
                val a = baseRegs[decoded.rs1]
                val b = baseRegs[decoded.rs2]

                baseRegs[decoded.rd] = a % b
                pc += 4
            }

            MULW -> {
                val a = baseRegs[decoded.rs1].toInt32()
                val b = baseRegs[decoded.rs2].toInt32()

                baseRegs[decoded.rd] = (a * b).toInt64().toUnsigned()
                pc += 4
            }

            DIVW -> {
                val a = baseRegs[decoded.rs1].toInt32()
                val b = baseRegs[decoded.rs2].toInt32()

                baseRegs[decoded.rd] = (a / b).toInt64().toUnsigned()
                pc += 4
            }

            DIVUW -> {
                val a = baseRegs[decoded.rs1].toUInt32()
                val b = baseRegs[decoded.rs2].toUInt32()

                baseRegs[decoded.rd] = (a / b).toUInt64()
                pc += 4
            }

            REMW -> {
                val a = baseRegs[decoded.rs1].toInt32()
                val b = baseRegs[decoded.rs2].toInt32()

                baseRegs[decoded.rd] = (a % b).toInt64().toUnsigned()
                pc += 4
            }

            REMUW -> {
                val a = baseRegs[decoded.rs1].toUInt32()
                val b = baseRegs[decoded.rs2].toUInt32()

                baseRegs[decoded.rd] = (a % b).toUInt64()
                pc += 4
            }

            // --- System Return/Fence/Wait ---
            SRET -> {
                // Return from Supervisor Mode Trap
                // Simplified: Set PC from SEPC. Full impl needs SSTATUS/privilege handling.
                pc = csrRegs[RvRegT.RvCsrT.SupervisorT.X141]
                // TODO: Restore privilege level from SSTATUS.SPP
                // TODO: Restore SSTATUS.SIE from SSTATUS.SPIE, set SSTATUS.SPIE = 1
                console.log("SRET executed (simplified: pc <- sepc)")
            }

            MRET -> {
                // Return from Machine Mode Trap
                // Simplified: Set PC from MEPC. Full impl needs MSTATUS/privilege handling.
                pc = csrRegs[RvRegT.RvCsrT.MachineT.X341]
                // TODO: Restore privilege level from MSTATUS.MPP
                // TODO: Restore MSTATUS.MIE from MSTATUS.MPIE, set MSTATUS.MPIE = 1
                console.log("MRET executed (simplified: pc <- mepc)")
            }

            WFI -> {
                // Wait For Interrupt - Hint, often treated as NOP in simple emulators
                // TODO: Implement interrupt checking logic if needed. If interrupt pending, WFI does nothing. If not, halt until interrupt.
                console.log("WFI executed (treated as NOP)")
                pc += 4
            }

            SFENCE_VMA -> {
                // Supervisor Memory Management Fence - NOP in models without TLB/VM
                // TODO: Implement TLB flush/sync logic if VM is modeled.
                // Operands decoded.rs1 (ASID) and decoded.rs2 (vaddr) can be used.
                console.log("SFENCE.VMA executed (treated as NOP)")
                pc += 4
            }

            null -> {
                console.error("Invalid or unknown instruction binary: 0x${instrBin.toString(16)} at PC=0x${pc.toString(16)}")
                // Trigger Illegal Instruction Trap
                csrRegs[RvRegT.RvCsrT.MachineT.X341] = pc
                csrRegs[RvRegT.RvCsrT.MachineT.X342] = 2uL.toUInt64() // Cause: Illegal instruction
                csrRegs[RvRegT.RvCsrT.MachineT.X343] = instrBin.toUInt64() // Store offending instruction
                pc = csrRegs[RvRegT.RvCsrT.MachineT.X305]
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

        val isException = when (decoded.type) {
            ECALL, EBREAK, null -> true
            else -> false
        }

        return ExecutionResult(true, typeIsReturnFromSubroutine = isReturnFromSubroutine, typeIsBranchToSubroutine = isBranchToSubroutine, typeIsException = isException)
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
        override val DISASSEMBLER: AsmDisassembler = RvDisassembler { toUInt64() }
    }
}