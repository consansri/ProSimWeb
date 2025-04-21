package emulator.archs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cengine.lang.asm.AsmDisassembler
import cengine.lang.asm.target.ikrmini.IkrMiniDisassembler
import cengine.lang.asm.target.ikrmini.IkrMiniDisassembler.InstrType.*
import cengine.util.Endianness
import cengine.util.integer.UInt16
import cengine.util.integer.UInt16.Companion.toUInt16
import cengine.util.integer.UInt32
import emulator.archs.ikrmini.IKRMiniBaseRegs
import emulator.kit.ArchConfig
import emulator.kit.MicroSetup
import emulator.kit.memory.*
import emulator.kit.optional.BasicArchImpl

class ArchIKRMini : BasicArchImpl<UInt16, UInt16>(UInt16, UInt16) {

    override val config: ArchConfig = ArchIKRMini

    override val pcState: MutableState<UInt16> = mutableStateOf(UInt16.ZERO)
    private var pc by pcState

    override val memory: MainMemory<UInt16, UInt16> = MainMemory(Endianness.BIG, UInt16, UInt16)

    var instrMemory: Memory<UInt16, UInt16> = memory
        set(value) {
            field = value
            resetMicroArch()
        }
    var dataMemory: Memory<UInt16, UInt16> = memory
        set(value) {
            field = value
            resetMicroArch()
        }

    private val alu = ALU()

    private val baseRegs = IKRMiniBaseRegs()
    private var ac: UInt16
        get() = baseRegs[0]
        set(value) {
            baseRegs[0] = value
        }

    private var nzvc: UInt16
        get() = baseRegs[1]
        set(value) {
            baseRegs[1] = value
        }

    override fun executeNext(tracker: Memory.AccessTracker): ExecutionResult {
        val opc = instrMemory.loadInstance(pc, tracker)
        val second = instrMemory.loadInstance(pc + 1, tracker)
        val third = instrMemory.loadInstance(pc + 2, tracker)

        val decoded = IkrMiniDisassembler.IKRMiniInstrProvider(opc, second, third)
        when (decoded.type) {
            LOAD_IMM -> {
                ac = second
                decoded.type.incPC()
            }

            LOAD_DIR -> {
                ac = dataMemory.loadInstance(second, tracker)
                decoded.type.incPC()
            }

            LOAD_IND -> {
                ac = dataMemory.loadInstance(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            LOAD_IND_OFF -> {
                ac = dataMemory.loadInstance(dataMemory.loadInstance(second, tracker) + third)
                decoded.type.incPC()
            }

            LOADI -> {
                ac = dataMemory.loadInstance(ac, tracker)
                decoded.type.incPC()
            }

            STORE_DIR -> {
                dataMemory.storeInstance(second, ac, tracker)
                decoded.type.incPC()
            }

            STORE_IND -> {
                dataMemory.storeInstance(dataMemory.loadInstance(second, tracker), ac, tracker)
                decoded.type.incPC()
            }

            STORE_IND_OFF -> {
                dataMemory.storeInstance(dataMemory.loadInstance(second, tracker) + third, ac, tracker)
                decoded.type.incPC()
            }

            AND_IMM -> {
                ac = alu.and(ac, second)
                decoded.type.incPC()
            }

            AND_DIR -> {
                ac = alu.and(ac, dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            AND_IND -> {
                ac = alu.and(ac, dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            AND_IND_OFF -> {
                ac = alu.and(ac, (dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            OR_IMM -> {
                ac = alu.or(ac, second)
                decoded.type.incPC()
            }

            OR_DIR -> {
                ac = alu.or(ac, dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            OR_IND -> {
                ac = alu.or(ac, dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            OR_IND_OFF -> {
                ac = alu.or(ac, (dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            XOR_IMM -> {
                ac = alu.xor(ac, second)
                decoded.type.incPC()
            }

            XOR_DIR -> {
                ac = alu.xor(ac, dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            XOR_IND -> {
                ac = alu.xor(ac, dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            XOR_IND_OFF -> {
                ac = alu.xor(ac, (dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            ADD_IMM -> {
                ac = alu.add(ac, second)
                decoded.type.incPC()
            }

            ADD_DIR -> {
                ac = alu.add(ac, dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            ADD_IND -> {
                ac = alu.add(ac, dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            ADD_IND_OFF -> {
                ac = alu.add(ac, (dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            ADDC_IMM -> {
                ac = alu.addc(ac, second)
                decoded.type.incPC()
            }

            ADDC_DIR -> {
                ac = alu.addc(ac, dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            ADDC_IND -> {
                ac = alu.addc(ac, dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            ADDC_IND_OFF -> {
                ac = alu.addc(ac, (dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            SUB_IMM -> {
                ac = alu.sub(ac, second)
                decoded.type.incPC()
            }

            SUB_DIR -> {
                ac = alu.sub(ac, dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            SUB_IND -> {
                ac = alu.sub(ac, dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            SUB_IND_OFF -> {
                ac = alu.sub(ac, (dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            SUBC_IMM -> {
                ac = alu.subc(ac, second)
                decoded.type.incPC()
            }

            SUBC_DIR -> {
                ac = alu.subc(ac, dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            SUBC_IND -> {
                ac = alu.subc(ac, dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            SUBC_IND_OFF -> {
                ac = alu.subc(ac, (dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            LSL -> {
                ac = alu.lsl(ac)
                decoded.type.incPC()
            }

            LSL_DIR -> {
                ac = alu.lsl(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            LSL_IND -> {
                ac = alu.lsl(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            LSL_IND_OFF -> {
                ac = alu.lsl((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            LSR -> {
                ac = alu.lsr(ac)
                decoded.type.incPC()
            }

            LSR_DIR -> {
                ac = alu.lsr(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            LSR_IND -> {
                ac = alu.lsr(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            LSR_IND_OFF -> {
                ac = alu.lsr((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            ROL -> {
                ac = alu.rol(ac)
                decoded.type.incPC()
            }

            ROL_DIR -> {
                ac = alu.rol(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            ROL_IND -> {
                ac = alu.rol(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            ROL_IND_OFF -> {
                ac = alu.rol((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            ROR -> {
                ac = alu.ror(ac)
                decoded.type.incPC()
            }

            ROR_DIR -> {
                ac = alu.ror(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            ROR_IND -> {
                ac = alu.ror(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            ROR_IND_OFF -> {
                ac = alu.ror((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            ASL -> {
                ac = alu.asl(ac)
                decoded.type.incPC()
            }

            ASL_DIR -> {
                ac = alu.asl(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            ASL_IND -> {
                ac = alu.asl(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            ASL_IND_OFF -> {
                ac = alu.asl((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            ASR -> {
                ac = alu.asr(ac)
                decoded.type.incPC()
            }

            ASR_DIR -> {
                ac = alu.asr(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            ASR_IND -> {
                ac = alu.asr(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            ASR_IND_OFF -> {
                ac = alu.asr((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            RCL -> {
                ac = alu.rcl(ac)
                decoded.type.incPC()
            }

            RCL_IMM -> {
                ac = alu.rcl(second)
                decoded.type.incPC()
            }

            RCL_DIR -> {
                ac = alu.rcl(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            RCL_IND -> {
                ac = alu.rcl(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            RCL_IND_OFF -> {
                ac = alu.rcl((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            RCR -> {
                ac = alu.rcr(ac)
                decoded.type.incPC()
            }

            RCR_IMM -> {
                ac = alu.rcr(second)
                decoded.type.incPC()
            }

            RCR_DIR -> {
                ac = alu.rcr(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            RCR_IND -> {
                ac = alu.rcr(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            RCR_IND_OFF -> {
                ac = alu.rcr((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            NOT -> {
                ac = alu.not(ac)
                decoded.type.incPC()
            }

            NOT_DIR -> {
                ac = alu.not(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            NOT_IND -> {
                ac = alu.not(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            NOT_IND_OFF -> {
                ac = alu.not((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            NEG_DIR -> {
                ac = alu.neg(dataMemory.loadInstance(second, tracker))
                decoded.type.incPC()
            }

            NEG_IND -> {
                ac = alu.neg(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)))
                decoded.type.incPC()
            }

            NEG_IND_OFF -> {
                ac = alu.neg((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third))
                decoded.type.incPC()
            }

            CLR -> {
                ac = UInt16.ZERO
                nzvc = UInt16(0b0100U)
                decoded.type.incPC()
            }

            INC -> {
                ac = alu.add(ac, UInt16.ONE)
                decoded.type.incPC()
            }

            INC_DIR -> {
                ac = alu.add(dataMemory.loadInstance(second, tracker), UInt16.ONE)
                decoded.type.incPC()
            }

            INC_IND -> {
                ac = alu.add(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)), UInt16.ONE)
                decoded.type.incPC()
            }

            INC_IND_OFF -> {
                ac = alu.add((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third), UInt16.ONE)
                decoded.type.incPC()
            }

            DEC -> {
                ac = alu.sub(ac, UInt16.ONE)
                decoded.type.incPC()
            }

            DEC_DIR -> {
                ac = alu.sub(dataMemory.loadInstance(second, tracker), UInt16.ONE)
                decoded.type.incPC()
            }

            DEC_IND -> {
                ac = alu.sub(dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)), UInt16.ONE)
                decoded.type.incPC()
            }

            DEC_IND_OFF -> {
                ac = alu.sub((dataMemory.loadInstance(dataMemory.loadInstance(second, tracker)) + third), UInt16.ONE)
                decoded.type.incPC()
            }

            BSR -> {
                ac = pc + UInt16(2U)
                pc = second
            }

            JMP -> {
                pc = ac
            }

            BRA -> {
                pc = second
            }

            BHI -> {
                if (Condition.HI.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BLS -> {
                if (Condition.LS.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BCC -> {
                if (Condition.CC.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BCS -> {
                if (Condition.CS.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BNE -> {
                if (Condition.NE.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BEQ -> {
                if (Condition.EQ.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BVC -> {
                if (Condition.VC.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BVS -> {
                if (Condition.VS.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BPL -> {
                if (Condition.PL.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BMI -> {
                if (Condition.MI.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BGE -> {
                if (Condition.GE.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BLT -> {
                if (Condition.LT.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BGT -> {
                if (Condition.GT.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            BLE -> {
                if (Condition.LE.test(nzvc.toUInt())) {
                    pc = second
                } else {
                    decoded.type.incPC()
                }
            }

            null -> ExecutionResult(false)
        }

        val isReturnFromSubroutine = when (decoded.type) {
            JMP -> true
            else -> false
        }

        val isBranchToSubroutine = when (decoded.type) {
            BSR -> true
            else -> false
        }

        return ExecutionResult(true, typeIsReturnFromSubroutine = isReturnFromSubroutine, typeIsBranchToSubroutine = isBranchToSubroutine)
    }

    override fun setupMicroArch() {
        MicroSetup.append(memory)
        if (instrMemory != memory) MicroSetup.append(instrMemory)
        if (dataMemory != memory) MicroSetup.append(dataMemory)
        MicroSetup.append(baseRegs)
    }

    private fun IkrMiniDisassembler.InstrType.incPC() {
        pc += length
    }

    override fun resetPC() {
        pc = UInt16.ZERO
    }

    private inner class ALU {

        fun lsl(a: UInt16): UInt16 {
            val result = a shl 1
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun lsr(a: UInt16): UInt16 {
            val result = a shr 1
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun rol(a: UInt16): UInt16 {
            val result = a rol 1
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun ror(a: UInt16): UInt16 {
            val result = a ror 1
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun asl(a: UInt16): UInt16 {
            val result = a.toInt16() shl 1
            nzvc = flags(isNegative(result.toUInt16()), isZero(result.toUInt16()))
            return result.toUInt16()
        }

        fun asr(a: UInt16): UInt16 {
            val result = a.toInt16() shr 1
            nzvc = flags(isNegative(result.toUInt16()), isZero(result.toUInt16()))
            return result.toUInt16()
        }

        fun rcl(a: UInt16): UInt16 {
            val carry = carry()
            val nextCarry = a shr 15 == UInt16.ONE
            val result = (a and UInt16(0b0111111111111111U) or (carry shl 15)) rol 1
            nzvc = flags(isNegative(result), isZero(result), c = nextCarry)
            return result
        }

        fun rcr(a: UInt16): UInt16 {
            val carry = carry()
            val nextCarry = a.toUInt() and 1U == 1U
            val result = (a and UInt16(0b1111111111111110U) or carry) rol 1
            nzvc = flags(isNegative(result), isZero(result), c = nextCarry)
            return result
        }

        fun not(a: UInt16): UInt16 {
            val result = a.inv()
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun neg(a: UInt16): UInt16 {
            val result = (-a.toInt16()).toUInt16()
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun and(a: UInt16, b: UInt16): UInt16 {
            val result = a and b
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun or(a: UInt16, b: UInt16): UInt16 {
            val result = a or b
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun xor(a: UInt16, b: UInt16): UInt16 {
            val result = a xor b
            nzvc = flags(isNegative(result), isZero(result))
            return result
        }

        fun add(a: UInt16, b: UInt16): UInt16 {
            val result = a + b

            val isOverflow = a.toShort() < 0 == b.toShort() < 0
                    && result.toShort() < 0 != a.toShort() < 0

            nzvc = flags(isNegative(result), isZero(result), isOverflow, result shr 16 == UInt16.ONE)
            return result
        }

        fun sub(a: UInt16, b: UInt16): UInt16 {
            val result = a + b

            val isOverflow = a.toShort() < 0 != b.toShort() < 0
                    && result.toShort() < 0 != a.toShort() < 0

            nzvc = flags(isNegative(result), isZero(result), isOverflow, a < b)
            return result
        }

        fun addc(a: UInt16, b: UInt16): UInt16 {
            val result = a + b + (nzvc and 1)

            val isOverflow = a.toShort() < 0 == b.toShort() < 0
                    && result.toShort() < 0 != a.toShort() < 0

            nzvc = flags(isNegative(result), isZero(result), isOverflow, result shr 16 == UInt16.ONE)
            return result

        }

        fun subc(a: UInt16, b: UInt16): UInt16 {
            val result = a.toUInt32() - b.toUInt32() + nzvc.toUInt32() and UInt32.ONE

            val isOverflow = a.toShort() < 0 != b.toShort() < 0
                    && result.toShort() < 0 != a.toShort() < 0

            nzvc = flags(isNegative(result.toUInt16()), isZero(result.toUInt16()), isOverflow, a < b)
            return result.toUInt16()
        }

        private fun carry(): UInt16 = nzvc and 1

        private fun isZero(result: UInt16): Boolean = result.toUInt() == 0U

        private fun isNegative(result: UInt16): Boolean = result.toUInt() shr 15 == 1U

        private fun flags(n: Boolean, z: Boolean, v: Boolean = false, c: Boolean = false): UInt16 {
            var result = 0U
            if (n) {
                result += 1U shl 3
            }

            if (z) {
                result += 1U shl 2
            }

            if (v) {
                result += 1U shl 1
            }

            if (c) {
                result += 1U
            }
            return result.toUShort().toUInt16()
        }

    }

    private enum class Condition {
        HI,
        LS,
        CC,
        CS,
        NE,
        EQ,
        VC,
        VS,
        PL,
        MI,
        GE,
        LT,
        GT,
        LE;

        fun test(nzvc: UInt): Boolean {
            val n = (nzvc shr 3) and 1U == 1U
            val z = (nzvc shr 2) and 1U == 1U
            val v = (nzvc shr 1) and 1U == 1U
            val c = nzvc and 1U == 1U

            return when (this) {
                HI -> !(c || z)
                LS -> c || z
                CC -> !c
                CS -> c
                NE -> !z
                EQ -> z
                VC -> !v
                VS -> v
                PL -> !n
                MI -> n
                GE -> !((n && !v) || (!n && v))
                LT -> (n && !v) || (!n && v)
                GT -> !((n && !v) || (!n && v) || z)
                LE -> (n && !v) || (!n && v) || z
            }
        }
    }


    companion object : ArchConfig {
        override val DESCR = ArchConfig.Description(
            "IKR Mini",
            "IKR Minimalprozessor"
        )

        override val SETTINGS = listOf(
            ArchConfig.Setting.Enumeration("Instruction Cache", Cache.Setting.entries, Cache.Setting.NONE) { arch, setting ->
                if (arch is ArchIKRMini) {
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
                if (arch is ArchIKRMini) {
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
        override val DISASSEMBLER: AsmDisassembler = IkrMiniDisassembler

    }

}