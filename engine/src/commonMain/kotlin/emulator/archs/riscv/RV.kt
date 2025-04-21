package emulator.archs.riscv

import emulator.kit.register.FieldProvider

object RV {

    object BaseNameProvider : FieldProvider {
        override val name: String = "NAME"

        override fun get(id: Int): String = when (id) {
            0 -> "zero"
            1 -> "ra"
            2 -> "sp"
            3 -> "gp"
            4 -> "tp"

            5 -> "t0"
            6 -> "t1"
            7 -> "t2"

            28 -> "t3"
            29 -> "t4"
            30 -> "t5"
            31 -> "t6"

            10 -> "a0"
            11 -> "a1"
            12 -> "a2"
            13 -> "a3"
            14 -> "a4"
            15 -> "a5"
            16 -> "a6"
            17 -> "a7"

            8 -> "s0 fp"
            9 -> "s1"

            18 -> "s2"
            19 -> "s3"
            20 -> "s4"
            21 -> "s5"
            22 -> "s6"
            23 -> "s7"
            24 -> "s8"
            25 -> "s9"
            26 -> "s10"
            27 -> "s11"

            else -> ""
        }
    }

    object BaseCCProvider : FieldProvider {
        override val name: String = "CALLE"

        override fun get(id: Int): String = when (id) {
            1, in 5..7, in 10..17, in 28..31 -> "R"
            2, 8, 9, in 18..27 -> "E"
            else -> "-"
        }
    }

    object BaseProvider : FieldProvider {
        override val name: String = "DESCR"
        override fun get(id: Int): String = when (id) {
            0 -> "hardwired zero"
            1 -> "return address"
            2 -> "stack pointer"
            3 -> "global pointer"
            4 -> "thread pointer"

            5 -> "temporary register 0"
            6 -> "temporary register 1"
            7 -> "temporary register 2"

            28 -> "temporary register 3"
            29 -> "temporary register 4"
            30 -> "temporary register 5"
            31 -> "temporary register 6"

            10 -> "function argument 0 / return value 0"
            11 -> "function argument 1 / return value 1"
            12 -> "function argument 2"
            13 -> "function argument 3"
            14 -> "function argument 4"
            15 -> "function argument 5"
            16 -> "function argument 6"
            17 -> "function argument 7"

            8 -> "saved register 0 / frame pointer"
            9 -> "saved register 1"

            18 -> "saved register 2"
            19 -> "saved register 3"
            20 -> "saved register 4"
            21 -> "saved register 5"
            22 -> "saved register 6"
            23 -> "saved register 7"
            24 -> "saved register 8"
            25 -> "saved register 9"
            26 -> "saved register 10"
            27 -> "saved register 11"

            else -> ""
        }
    }

    class CSRNameProvider(val showRV32Regs: Boolean) : FieldProvider {
        override val name: String = "NAME"

        override fun get(id: Int): String = when (id) {

            // User Trap Setup
            0x000 -> "ustatus"
            0x004 -> "uie"
            0x005 -> "utvec"

            // User Trap Handling
            0x040 -> "uscratch"
            0x041 -> "uepc"
            0x042 -> "ucause"
            0x043 -> "ubadaddr"
            0x044 -> "uip"

            // User Floating-Point CSRs
            0x001 -> "fflags"
            0x002 -> "frm"
            0x003 -> "fcsr"

            // User Counter/Timers
            0xC00 -> "cycle"
            0xC01 -> "time"
            0xC02 -> "instret"
            in 0xC03..0xC1f -> "hpmcounter${id and 0b11111}"

            // Supervisor Trap Setup
            0x100 -> "sstatus"
            0x102 -> "sedeleg"
            0x103 -> "sideleg"
            0x104 -> "sie"
            0x105 -> "stvec"

            // Supervisor Trap Handling
            0x140 -> "sscratch"
            0x141 -> "sepc"
            0x142 -> "scause"
            0x143 -> "sbadaddr"
            0x144 -> "sip"

            // Supervisor Protection and Translation
            0x180 -> "sptbr"

            // Hypervisor Trap Setup
            0x200 -> "hstatus"
            0x202 -> "hedeleg"
            0x203 -> "hideleg"
            0x204 -> "hie"
            0x205 -> "htvec"

            // Hypervisor Trap Handling
            0x240 -> "hscratch"
            0x241 -> "hepc"
            0x242 -> "hcause"
            0x243 -> "hbadaddr"
            0x244 -> "hip"

            // Hypervisor Protection and Translation TBD

            // Machine Information Registers
            0xF11 -> "mvendorid"
            0xF12 -> "marchid"
            0xF13 -> "mimpid"
            0xF14 -> "mhartid"

            // Machine Trap Setup
            0x300 -> "mstatus"
            0x301 -> "misa"
            0x302 -> "medeleg"
            0x303 -> "mideleg"
            0x304 -> "mie"
            0x305 -> "mtvec"

            // Machine Trap Handling
            0x340 -> "mscratch"
            0x341 -> "mepc"
            0x342 -> "mcause"
            0x343 -> "mbadaddr"
            0x344 -> "mip"

            // Machine Protection and Translation
            0x380 -> "mbase"
            0x381 -> "mbound"
            0x382 -> "mibase"
            0x383 -> "mibound"
            0x384 -> "mdbase"
            0x385 -> "mdbound"

            // Machine Counter/Timers
            0xB00 -> "mcycle"
            0xB02 -> "minstret"
            in 0xB03..0xB1f -> "mhpmcounter${id and 0b11111}"

            // Machine Counter Setup
            0x320 -> "mucounteren"
            0x321 -> "mscounteren"
            0x322 -> "mhcounteren"
            in 0x323..0x33f -> "mhpmevent${id and 0b11111}"

            // Debug/Trace Registers (shared with Debug Mode)
            0x7a0 -> "tselect"
            0x7a1 -> "tdata1"
            0x7a2 -> "tdata2"
            0x7a3 -> "tdata3"

            // Debug Mode Registers
            0x7b0 -> "dcsr"
            0x7b1 -> "dpc"
            0x7b2 -> "dscratch"

            else -> if (showRV32Regs) {
                // RV32 Only
                when (id) {

                    // User Counter/Timers
                    0xC80 -> "cycleh"
                    0xC81 -> "timeh"
                    0xC82 -> "instreth"
                    in 0xC83..0xC9F -> "hpmcounter${id and 0b11111}h"

                    // Machine Counter/Timers
                    0xB80 -> "mcycleh"
                    0xB82 -> "minstreth"
                    in 0xB83..0xB9f -> "mhpmcounter${id and 0b11111}h"


                    else -> ""
                }
            } else ""
        }
    }

    object CSRPrivilegeProvider : FieldProvider {
        override val name: String = "PV"

        override fun get(id: Int): String {
            val relevant = id shr 8
            return when (relevant and 0b11) {
                0b00 -> "U"
                0b01 -> "S"
                0b10 -> "H"
                0b11 -> "M"
                else -> ""
            } + when (relevant shr 10) {
                0b11 -> "RO"
                else -> "RW"
            }
        }
    }
}