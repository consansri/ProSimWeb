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
import cengine.util.integer.UInt16
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32
import cengine.util.integer.UInt64.Companion.toUInt64
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
            LUI -> {
                baseRegs[decoded.rd.toInt()] = (decoded.imm20uType shl 12)
                incPCBy4()
            }

            AUIPC -> {
                val result = pc + (decoded.imm20uType shl 12).toInt()
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            JAL -> {
                baseRegs[decoded.rd] = pc + 4
                pc += decoded.jTypeOffset.toInt()
            }

            JALR -> {
                baseRegs[decoded.rd] = pc + 4
                pc = baseRegs[decoded.rs1] + decoded.iTypeImm.toUInt32() and (-1 shl 1)
            }

            ECALL -> {
                // Environment call - Triggers a synchronous trap
                console.log("ECALL triggered at 0x${pc.toString(16)}")
                // Basic Machine Mode Trap Handling:
                csrRegs[RvRegT.RvCsrT.MachineT.X341] = pc                 // Save PC of ECALL instruction
                csrRegs[RvRegT.RvCsrT.MachineT.X342] = 11u.toUInt32()                  // Cause: Environment call from M-mode (assuming M-mode)
                // TODO: Implement privilege level detection and use corresponding cause codes (8=U, 9=S)
                // TODO: Update MSTATUS (e.g., save MIE to MPIE, clear MIE)
                pc = csrRegs[RvRegT.RvCsrT.MachineT.X305] // Jump to Machine Trap Vector Handler
            }

            EBREAK -> {
                // Environment breakpoint - Triggers a synchronous trap
                console.log("EBREAK triggered at 0x${pc.toString(16)}")
                // Basic Machine Mode Trap Handling:
                csrRegs[RvRegT.RvCsrT.MachineT.X341] = pc                 // Save PC of EBREAK instruction
                csrRegs[RvRegT.RvCsrT.MachineT.X342] = 3u.toUInt32()                   // Cause: Breakpoint
                // TODO: Update MSTATUS
                pc = csrRegs[RvRegT.RvCsrT.MachineT.X305] // Jump to Machine Trap Vector Handler
            }

            BEQ -> {
                if (baseRegs[decoded.rs1] == baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            BNE -> {
                if (baseRegs[decoded.rs1] != baseRegs[decoded.rs2]) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            BLT -> {
                if (baseRegs[decoded.rs1].toInt() < baseRegs[decoded.rs2].toInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            BGE -> {
                if (baseRegs[decoded.rs1].toInt() >= baseRegs[decoded.rs2].toInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            BLTU -> {
                if (baseRegs[decoded.rs1].toUInt() < baseRegs[decoded.rs2].toUInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            BGEU -> {
                if (baseRegs[decoded.rs1].toUInt() >= baseRegs[decoded.rs2].toUInt()) {
                    pc += decoded.bTypeOffset.toInt()
                } else {
                    incPCBy4()
                }
            }

            LB -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toInt8().toInt32().toUnsigned()
                incPCBy4()
            }

            LH -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toInt16().toInt32().toUnsigned()
                incPCBy4()
            }

            LW -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadWord(address, tracker = tracker)
                incPCBy4()
            }

            LBU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadInstance(address, tracker = tracker).toUInt32()
                incPCBy4()
            }

            LHU -> {
                val address = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = dataMemory.loadHalf(address, tracker = tracker).toUInt32()
                incPCBy4()
            }

            SB -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2].toUInt8(), tracker = tracker)
                incPCBy4()
            }

            SH -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2].toUInt16(), tracker = tracker)
                incPCBy4()
            }

            SW -> {
                val address = baseRegs[decoded.rs1] + decoded.sTypeImm.toInt()
                dataMemory.storeEndianAwareValue(address, baseRegs[decoded.rs2], tracker = tracker)
                incPCBy4()
            }

            ADDI -> {
                val result = baseRegs[decoded.rs1] + decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            SLTI -> {
                if (baseRegs[decoded.rs1].toInt() < decoded.iTypeImm.toInt()) {
                    baseRegs[decoded.rd] = UInt32.ONE
                } else {
                    baseRegs[decoded.rd] = UInt32.ZERO
                }
                incPCBy4()
            }

            SLTIU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1] < decoded.iTypeImm.toUInt32()) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            XORI -> {
                val result = baseRegs[decoded.rs1] xor decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            ORI -> {
                val result = baseRegs[decoded.rs1] or decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            ANDI -> {
                val result = baseRegs[decoded.rs1] and decoded.iTypeImm.toInt()
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            SLLI -> {
                val result = baseRegs[decoded.rs1] shl decoded.shamtRV32
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            SRLI -> {
                val result = baseRegs[decoded.rs1] shr decoded.shamtRV32
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            SRAI -> {
                val result = baseRegs[decoded.rs1].toInt32() shr decoded.shamtRV32.toInt()
                baseRegs[decoded.rd] = result.toUInt32()
                incPCBy4()
            }

            ADD -> {
                val result = baseRegs[decoded.rs1] + baseRegs[decoded.rs2]
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            SUB -> {
                val result = baseRegs[decoded.rs1] - baseRegs[decoded.rs2]
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            SLL -> {
                val result = (baseRegs[decoded.rs1] shl baseRegs[decoded.rs2])
                baseRegs[decoded.rd] = result
                incPCBy4()
            }

            SLT -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toInt() < baseRegs[decoded.rs2].toInt()) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            SLTU -> {
                baseRegs[decoded.rd] = if (baseRegs[decoded.rs1].toUInt() < baseRegs[decoded.rs2].toUInt()) {
                    UInt32.ONE
                } else {
                    UInt32.ZERO
                }
                incPCBy4()
            }

            XOR -> {
                val result = baseRegs[decoded.rs1] xor baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            SRL -> {
                val result = baseRegs[decoded.rs1] shr baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            SRA -> {
                val result = baseRegs[decoded.rs1].toInt32() shr baseRegs[decoded.rs2].toInt()
                baseRegs[decoded.rd.toInt()] = result.toUnsigned()
                incPCBy4()
            }

            OR -> {
                val result = baseRegs[decoded.rs1] or baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            AND -> {
                val result = baseRegs[decoded.rs1] and baseRegs[decoded.rs2]
                baseRegs[decoded.rd.toInt()] = result
                incPCBy4()
            }

            FENCE -> {

            }

            FENCEI -> {

            }

            CSRRW -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = baseRegs[decoded.rs1]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            CSRRS -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = baseRegs[decoded.rs1] or csrRegs[decoded.iTypeImm.toUInt32()]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            CSRRC -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = baseRegs[decoded.rs1] and csrRegs[decoded.iTypeImm.toUInt32()]
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            CSRRWI -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = decoded.rs1
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            CSRRSI -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = t or decoded.rs1.toInt()
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            CSRRCI -> {
                val t = csrRegs[decoded.iTypeImm.toUInt32()]
                csrRegs[decoded.iTypeImm.toUInt32()] = t and decoded.rs1.inv().toInt()
                baseRegs[decoded.rd] = t

                incPCBy4()
            }

            MUL -> {
                val result = (baseRegs[decoded.rs1].toInt32() * baseRegs[decoded.rs2].toInt32())
                baseRegs[decoded.rd.toInt()] = result.toUnsigned()
                incPCBy4()
            }

            MULH -> {
                val a = baseRegs[decoded.rs1].toLong()
                val b = baseRegs[decoded.rs2].toLong()
                val result = (a * b).shr(32)

                baseRegs[decoded.rd.toInt()] = result.toInt().toUInt32()
                incPCBy4()
            }

            MULHSU -> {
                val a = baseRegs[decoded.rs1].toInt().toLong()
                val b = baseRegs[decoded.rs2].toUInt().toLong()
                val result = (a * b).shr(32).toInt()

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            MULHU -> {
                val a = baseRegs[decoded.rs1].toUInt().toULong()
                val b = baseRegs[decoded.rs2].toUInt().toULong()
                val result = (a * b).shr(32).toUInt()

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            DIV -> {
                val a = baseRegs[decoded.rs1].toInt()
                val b = baseRegs[decoded.rs2].toInt()
                val result = a / b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            DIVU -> {
                val a = baseRegs[decoded.rs1].toUInt()
                val b = baseRegs[decoded.rs2].toUInt()
                val result = a / b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            REM -> {
                val a = baseRegs[decoded.rs1].toInt()
                val b = baseRegs[decoded.rs2].toInt()
                val result = a % b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
            }

            REMU -> {
                val a = baseRegs[decoded.rs1].toUInt()
                val b = baseRegs[decoded.rs2].toUInt()
                val result = a % b

                baseRegs[decoded.rd.toInt()] = result.toUInt32()
                incPCBy4()
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

            else -> {
                console.error("Invalid or unknown instruction binary: 0x${instrBin.toString(16)} at PC=0x${pc.toString(16)}")
                // Trigger Illegal Instruction Trap
                csrRegs[RvRegT.RvCsrT.MachineT.X341] = pc
                csrRegs[RvRegT.RvCsrT.MachineT.X342] = 2u.toUInt32() // Cause: Illegal instruction
                csrRegs[RvRegT.RvCsrT.MachineT.X343] = instrBin // Store offending instruction
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
            LWU, LD, SD, ADDIW, SLLIW, SRLIW, SRAIW, ADDW, SUBW, SLLW, SRLW, SRAW, MULW, DIVW, DIVUW, REMW, REMUW -> true
            else -> false
        }

        return ExecutionResult(true, typeIsReturnFromSubroutine = isReturnFromSubroutine, typeIsBranchToSubroutine = isBranchToSubroutine, typeIsException = isException)
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