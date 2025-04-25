package emulator.archs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.t6502.T6502Disassembler
import cengine.util.Endianness
import cengine.util.integer.UInt16
import cengine.util.integer.UInt8
import emulator.archs.t6502.T6502BaseRegs
import emulator.kit.ArchConfig
import emulator.kit.MicroSetup
import emulator.kit.memory.*
import emulator.kit.optional.BasicArchImpl

/**
 * MOS Technology 6502 Architecture
 */
class ArchT6502 : BasicArchImpl<UInt16, UInt8>(UInt16, UInt8) {

    override val config: ArchConfig = ArchT6502

    override val pcState: MutableState<UInt16> = mutableStateOf(UInt16.ZERO)

    private var pc by pcState

    override val memory: MainMemory<UInt16, UInt8> = MainMemory(Endianness.LITTLE, UInt16, UInt8)

    var instrMemory: Memory<UInt16, UInt8> = memory
        set(value) {
            field = value
            resetMicroArch()
        }
    var dataMemory: Memory<UInt16, UInt8> = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    private val baseRegs = T6502BaseRegs()

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        instrMemory.loadInstances(pc, 3, tracker)



        TODO()
    }


    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
        MicroSetup.append(baseRegs)
    }

    override fun resetPC() {
        pc = UInt16.ZERO
    }

    companion object : ArchConfig {
        override val DESCR: ArchConfig.Description = ArchConfig.Description("T6502", "MOS Technology 6502")
        override val SETTINGS: List<ArchConfig.Setting<*>> = listOf(
            ArchConfig.Setting.Enumeration("Instruction Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
                if (arch is ArchT6502) {
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
                if (arch is ArchT6502) {
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
        override val DISASSEMBLER: AsmDisassembler = T6502Disassembler()

    }
}