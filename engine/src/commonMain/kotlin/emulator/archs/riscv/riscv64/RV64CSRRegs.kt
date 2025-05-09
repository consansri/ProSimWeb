package emulator.archs.riscv.riscv64

import androidx.compose.runtime.mutableStateListOf
import cengine.lang.asm.target.riscv.RvRegT
import cengine.util.integer.UInt32.Companion.toUInt32
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import cengine.util.integer.UnsignedFixedSizeIntNumberT
import emulator.archs.riscv.RV
import emulator.archs.riscv.csr.CsrAddr
import emulator.archs.riscv.csr.PriviledgeLevels.PRIV_MACHINE
import emulator.archs.riscv.csr.StatusBits
import emulator.kit.register.FieldProvider
import emulator.kit.register.RegFile

class RV64CSRRegs : RegFile<UInt64> {
    override val name: String = "csr"

    override val type: UnsignedFixedSizeIntNumberT<UInt64>
        get() = UInt64

    override val indentificators: List<FieldProvider> = listOf(
        RV.CSRNameProvider(false)
    )

    override val descriptors: List<FieldProvider> = listOf(
        RV.CSRPrivilegeProvider
    )

    override val regValues = mutableStateListOf(*Array(4096) { UInt64.ZERO })

    override fun set(index: Int, value: UInt64) {
        regValues[index] = value
    }

    operator fun set(reg: RvRegT.RvCsrT, value: UInt64) {
        this[reg.address.toUInt32()] = value
    }

    operator fun get(reg: RvRegT.RvCsrT): UInt64 = this[reg.address.toUInt32()]
    
    override fun isVisible(index: Int): Boolean = when (index) {
        // User
        in 0..0xFF -> when (index) {
            in 0x0..0x5,
            in 0x40..0x44,
                -> true

            else -> false
        } // Standard read/write
        in 0x400..0x4ff -> false // Standard read/write
        in 0xC00..0xCBF -> when (index) {
            in 0xC00..0xC1F,
                -> true

            else -> false
        } // Standard read-only

        // Supervisor
        in 0x100..0x1FF -> when (index) {
            0x100,
            in 0x102..0x105,
            in 0x140..0x144,
            0x180,
                -> true

            else -> false
        } // Standard read/write
        in 0x500..0x5BF -> false // Standard read/write
        in 0x900..0x9BF -> false // Standard read/write shadows
        in 0xD00..0xDBF -> false // Standard read-only

        // Hypervisor
        in 0x200..0x2FF -> when (index) {
            0x200,
            in 0x202..0x205,
            in 0x240..0x244,
                -> true

            else -> false
        } // Standard read/write
        in 0x600..0x6BF -> false // Standard read/write
        in 0xa00..0xabf -> false // Standard read/write shadows
        in 0xe00..0xebf -> false // Standard read-only

        // Machine
        in 0x300..0x3FF -> when (index) {
            in 0x300..0x305,
            in 0x320..0x33f,
            in 0x340..0x344,
            in 0x380..0x385,
                -> true

            else -> false
        } // Standard read/write
        in 0x700..0x79f -> false // Standard read/write
        in 0x7a0..0x7af -> when (index) {
            in 0x7a0..0x7a3 -> true

            else -> false
        } // Standard read/write debug CSRs
        in 0x7b0..0x7bf -> when (index) {
            in 0x7b0..0x7b2 -> true

            else -> false
        } // Debug-mode-only CSRs

        in 0xb00..0xbbf -> when (index) {
            0xb00,
            in 0xb02..0xb1f,
                -> true

            else -> false
        } // Standard read/write shadows
        in 0xf00..0xfbf -> when (index) {
            in 0xf11..0xf14 -> true
            else -> false
        } // Standard read-only

        else -> false
    }

    override fun clear() {
        for (i in regValues.indices) {
            if (regValues[i] != UInt64.ZERO) regValues[i] = UInt64.ZERO
        }
    }
}