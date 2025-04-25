package emulator.archs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cengine.console.SysOut
import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.ikrrisc2.IkrR2Disassembler
import cengine.lang.asm.target.ikrrisc2.IkrR2Disassembler.InstrType.*
import cengine.util.Endianness
import cengine.util.integer.Int32.Companion.toInt32
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32
import emulator.archs.ikrrisc2.IKRR2BaseRegs
import emulator.kit.ArchConfig
import emulator.kit.MicroSetup
import emulator.kit.memory.*
import emulator.kit.optional.BasicArchImpl

class ArchIKRRisc2 : BasicArchImpl<UInt32, UInt32>(UInt32, UInt32) {

    override val config: ArchConfig = ArchIKRRisc2

    override val pcState: MutableState<UInt32> = mutableStateOf(UInt32.ZERO)
    private var pc by pcState

    override val memory: MainMemory<UInt32, UInt32> = MainMemory(Endianness.BIG, UInt32, UInt32)

    var instrMemory: Memory<UInt32, UInt32> = memory
        set(value) {
            field = value
            resetMicroArch()
        }
    var dataMemory: Memory<UInt32, UInt32> = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    private val baseRegs = IKRR2BaseRegs()

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        val loaded = instrMemory.loadInstance(pc, tracker = tracker)
        val decoded = IkrR2Disassembler.IKRR2InstrProvider(loaded)

        when (decoded.type) {
            ADD -> {
                baseRegs[decoded.rc] = baseRegs[decoded.ra] + baseRegs[decoded.rb]
                pc += 1
            }

            ADDI -> {
                val imm16 = decoded.imm16.toShort().toInt().toInt32().toUInt32()

                baseRegs[decoded.rc] = baseRegs[decoded.rb] + imm16
                pc += 1
            }

            ADDLI -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb] + decoded.imm16.toInt()
                pc += 1
            }

            ADDHI -> {
                val imm16 = decoded.imm16.toInt() shl 16

                baseRegs[decoded.rc] = baseRegs[decoded.rb] + imm16
                pc += 1
            }

            ADDX -> {
                val carry = (baseRegs[decoded.ra] + baseRegs[decoded.rb]) shr 32

                baseRegs[decoded.rc] = carry
                pc += 1
            }

            SUB -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb] - baseRegs[decoded.ra]
                pc += 1
            }

            SUBX -> {
                baseRegs[decoded.rc] = if (baseRegs[decoded.rb].toUInt() < baseRegs[decoded.ra].toUInt()) UInt32.ONE else UInt32.ZERO
                pc += 1
            }

            CMPU -> {
                val comparison = baseRegs[decoded.rb].toUInt().compareTo(baseRegs[decoded.ra].toUInt())

                baseRegs[decoded.rc] = comparison.toUInt32()
                pc += 1
            }

            CMPS -> {
                val comparison = baseRegs[decoded.rb].toInt().compareTo(baseRegs[decoded.ra].toInt())

                baseRegs[decoded.rc] = comparison.toUInt32()
                pc += 1
            }

            CMPUI -> {
                val imm16 = decoded.imm16
                val comparison = baseRegs[decoded.rb].compareTo(imm16)

                baseRegs[decoded.rc] = comparison.toUInt32()
                pc += 1
            }

            CMPSI -> {
                val imm16 = decoded.imm16.toInt32().signExtend(16)
                val comparison = baseRegs[decoded.rb].toInt32().compareTo(imm16)

                baseRegs[decoded.rc] = comparison.toUInt32()
                pc += 1
            }

            AND -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb] and baseRegs[decoded.ra]
                pc += 1
            }

            AND0I -> {
                val imm16 = decoded.imm16
                baseRegs[decoded.rc] = baseRegs[decoded.rb] and imm16
                pc += 1
            }

            AND1I -> {
                val imm16 = decoded.imm16 or (UInt32.createBitMask(16) shl 16)
                val result = baseRegs[decoded.rb] and imm16

                baseRegs[decoded.rc] = result
                pc += 1
            }

            OR -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb] or baseRegs[decoded.ra]
                pc += 1
            }

            ORI -> {
                val imm16 = decoded.imm16

                baseRegs[decoded.rc] = baseRegs[decoded.rb] or imm16
                pc += 1
            }

            XOR -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb] xor baseRegs[decoded.ra]
                pc += 1
            }

            XORI -> {
                val imm16 = decoded.imm16
                val result = baseRegs[decoded.rb] xor imm16

                baseRegs[decoded.rc] = result
                pc += 1
            }

            LSL -> {
                val result = baseRegs[decoded.rb] shl 1

                baseRegs[decoded.rc] = result
                pc += 1
            }

            LSR -> {
                val result = baseRegs[decoded.rb] shr 1

                baseRegs[decoded.rc] = result
                pc += 1
            }

            ASL -> {
                val result = baseRegs[decoded.rb].toInt32() shl 1

                baseRegs[decoded.rc] = result.toUInt32()
                pc += 1
            }

            ASR -> {
                val result = baseRegs[decoded.rb].toInt32() shr 1

                baseRegs[decoded.rc] = result.toUInt32()
                pc += 1
            }

            ROL -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb].rol(1)
                pc += 1
            }

            ROR -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb].ror(1)
                pc += 1
            }

            EXTB -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb].toInt8().toInt32().toUInt32()
                pc += 1
            }

            EXTH -> {
                baseRegs[decoded.rc] = baseRegs[decoded.rb].toInt16().toInt32().toUInt32()
                pc += 1
            }

            SWAPB -> {
                val origin = baseRegs[decoded.rb]
                val b0 = origin and 0xff
                val b1 = origin shr 8 and 0xff
                val b2 = origin shr 16 and 0xff
                val b3 = origin shr 24 and 0xff

                val result = b2 shl 8 or b3 shl 8 or b0 shl 8 or b1

                baseRegs[decoded.rc] = result
                pc += 1
            }

            SWAPH -> {
                val origin = baseRegs[decoded.rb]
                val h0 = origin and 0xffff
                val h1 = origin shr 16 and 0xffff

                val result = h0 shl 16 or h1

                baseRegs[decoded.rc] = result
                pc += 1
            }

            NOT -> {
                val result = baseRegs[decoded.rb].inv()

                baseRegs[decoded.rc] = result
                pc += 1
            }

            LDD -> {
                val disp = decoded.disp16.toShort().toInt()
                val address = baseRegs[decoded.rb] + disp

                val fetched = dataMemory.loadInstance(address, tracker = tracker)

                baseRegs[decoded.rc] = fetched
                pc += 1
            }

            LDR -> {
                val address = baseRegs[decoded.rb] + baseRegs[decoded.ra]

                val fetched = dataMemory.loadInstance(address, tracker = tracker)

                baseRegs[decoded.rc] = fetched
                pc += 1
            }

            STD -> {
                val disp = decoded.disp16.toShort().toInt()
                val address = baseRegs[decoded.rb] + disp

                dataMemory.storeInstance(address, baseRegs[decoded.rc], tracker = tracker)

                pc += 1
            }

            STR -> {
                val address = baseRegs[decoded.rb] + baseRegs[decoded.ra]

                dataMemory.storeInstance(address, baseRegs[decoded.rc], tracker = tracker)

                pc += 1
            }

            BEQ -> {
                if (baseRegs[decoded.rc].equals(0)) {
                    pc += decoded.disp18
                } else {
                    pc += 1
                }
            }

            BNE -> {
                if (!baseRegs[decoded.rc].equals(0)) {
                    pc += decoded.disp18
                } else {
                    pc += 1
                }
            }

            BLT -> {
                if (baseRegs[decoded.rc].toInt32() < 0) {
                    pc += decoded.disp18
                } else {
                    pc += 1
                }
            }

            BGT -> {
                if (baseRegs[decoded.rc].toInt32() > 0) {
                    pc += decoded.disp18
                } else {
                    pc += 1
                }
            }

            BLE -> {
                if (baseRegs[decoded.rc].toInt32() <= 0) {
                    pc += decoded.disp18
                } else {
                    pc += 1
                }
            }

            BGE -> {
                if (baseRegs[decoded.rc].toInt32() >= 0) {
                    pc += decoded.disp18
                } else {
                    pc += 1
                }
            }

            BRA -> {
                pc += decoded.disp26
            }

            BSR -> {
                baseRegs[31] = pc + 1 // Save return address
                pc += decoded.disp26
            }

            JMP -> {
                pc = baseRegs[decoded.rb]
            }

            JSR -> {
                baseRegs[31] = pc + 1

                pc = baseRegs[decoded.rb]
            }

            null -> {
                console.error("Invalid Instruction!")
                return ExecutionResult(false)
            }
        }

        val isBranchToSubRoutine = when (decoded.type) {
            JSR -> true
            BSR -> true
            else -> false
        }

        val isReturnFromSubRoutine = when (decoded.type) {
            JMP -> true
            else -> false
        }

        return ExecutionResult(true, isReturnFromSubRoutine, isBranchToSubRoutine)
    }


    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
        MicroSetup.append(baseRegs)
    }

    override fun resetPC() {
        pc = UInt32.ZERO
    }

    companion object: ArchConfig {
        override val SETTINGS = listOf(
            ArchConfig.Setting.Enumeration("Instruction Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
                if (arch is ArchIKRRisc2) {
                    arch.instrMemory = when (setting.get()) {
                        Cache.Setting.NONE -> arch.memory
                        Cache.Setting.DirectedMapped -> DMCache(arch.memory, CacheSize.KiloByte_32, "Instruction")
                        Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM, "Instruction")
                        Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU, "Instruction")
                        Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO, "Instruction")
                        Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM, "Instruction")
                        Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU, "Instruction")
                        Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO, "Instruction")
                    }
                }
            },
            ArchConfig.Setting.Enumeration("Data Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
                if (arch is ArchIKRRisc2) {
                    arch.dataMemory = when (setting.get()) {
                        Cache.Setting.NONE -> arch.memory
                        Cache.Setting.DirectedMapped -> DMCache(arch.memory, CacheSize.KiloByte_32, "Data")
                        Cache.Setting.FullAssociativeRandom -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM, "Data")
                        Cache.Setting.FullAssociativeLRU -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU, "Data")
                        Cache.Setting.FullAssociativeFIFO -> FACache(arch.memory, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO, "Data")
                        Cache.Setting.SetAssociativeRandom -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.RANDOM, "Data")
                        Cache.Setting.SetAssociativeLRU -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.LRU, "Data")
                        Cache.Setting.SetAssociativeFIFO -> SACache(arch.memory, 4, CacheSize.KiloByte_32, Cache.ReplaceAlgo.FIFO, "Data")
                    }
                }
            }
        )
        override val DISASSEMBLER: AsmDisassembler = IkrR2Disassembler
        override val DESCR = ArchConfig.Description("IKR-RISC2", "IKR RISC-II")
    }

}