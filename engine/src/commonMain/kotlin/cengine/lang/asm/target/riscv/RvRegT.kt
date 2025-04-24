package cengine.lang.asm.target.riscv

import cengine.lang.asm.psi.AsmRegisterT
import cengine.psi.parser.PsiBuilder
import cengine.util.integer.UInt32
import cengine.util.integer.UInt32.Companion.toUInt32

sealed interface RvRegT : AsmRegisterT {

    val uint32: UInt32 get() = address.toUInt32()

    enum class IntT(vararg additional: String) : RvRegT {
        ZERO, RA, SP, GP, TP, T0, T1, T2, S0("fp"),
        S1, A0, A1, A2, A3, A4, A5, A6, A7, S2, S3,
        S4, S5, S6, S7, S8, S9, S10, S11, T3, T4, T5, T6
        ;

        companion object {
            val rvIntRegMap = IntT.entries.flatMap { entry -> entry.recognizable.map { name -> name to entry } }.associate { it }

            fun PsiBuilder.parseRvIntReg(): Boolean {
                val marker = mark()
                val text = getTokenText()
                val regType = rvIntRegMap[text]
                if (regType != null) {
                    advance()
                    marker.done(regType)
                } else {
                    error("Expected a risc-v integer register!")
                    marker.drop()

                }

                return regType != null
            }
        }

        override val address: UInt
            get() = ordinal.toUInt()

        override val recognizable: List<String> = listOf(name.lowercase(), "x$ordinal") + additional
    }

    enum class FloatT(vararg additional: String) : RvRegT {
        FT0, FT1, FT2, FT3, FT4, FT5, FT6, FT7, FS0, FS1, FA0,
        FA1, FA2, FA3, FA4, FA5, FA6, FA7, FS2, FS3, FS4, FS5,
        FS6, FS7, FS8, FS9, FS10, FS11, FT8, FT9, FT10, FT11
        ;

        companion object {
            val rvFloatRegMap = FloatT.entries.flatMap { entry -> entry.recognizable.map { name -> name to entry } }.associate { it }

            fun PsiBuilder.parseRvFloatReg(): Boolean {
                val marker = mark()
                val text = getTokenText()
                val regType = rvFloatRegMap[text]
                if (regType != null) {
                    advance()
                    marker.done(regType)
                } else {
                    error("Expected a risc-v float register!")
                    marker.drop()
                }

                return regType != null
            }
        }

        override val address: UInt
            get() = ordinal.toUInt()

        override val recognizable: List<String> = listOf( name.lowercase(), "f$ordinal") + additional
    }

    enum class VectorT : RvRegT {
        V0, V1, V2, V3, V4, V5, V6, V7,
        V8, V9, V10, V11, V12, V13, V14, V15,
        V16, V17, V18, V19, V20, V21, V22, V23,
        V24, V25, V26, V27, V28, V29, V30, V31
        ;

        companion object {
            val rvVectorRegMap = VectorT.entries.flatMap { entry -> entry.recognizable.map { name -> name to entry } }.associate { it }
            val allVectorNames = rvVectorRegMap.keys.toTypedArray() // Needed for the placeholder implementation

            fun PsiBuilder.parseRvVectorReg(): Boolean {
                val marker = mark()
                val text = getTokenText() // Use currentToken if available
                if (text != null) {
                    val regType = rvVectorRegMap[text]
                    if (regType != null) {
                        advance() // Use advanceLexer()
                        // Use the enum entry itself as the element type
                        marker.done(regType)
                        return true
                    }
                }
                // Error case
                error("Expected a risc-v vector register (v0-v31)!")
                return false
            }
        }

        override val address: UInt
            get() = ordinal.toUInt()

        // Generate names like "v0", "v1", ... "v31"
        override val recognizable: List<String> = listOf("v$ordinal")
    }

    data object VectorMaskT : RvRegT {
        override val address: UInt = 0U
        override val recognizable: List<String> = listOf("vm")

        /**
         * Parses the vector mask specifier, which is conventionally the token "vm".
         * This function expects to be called *after* the preceding comma has been consumed.
         */
        fun PsiBuilder.parseVectorMask(): Boolean {
            val marker = mark()

            // We expect the literal 'vm' token based on common assembler syntax convention,
            // representing masked execution (vm=0) using v0 implicitly.

            if (expect("vm")) {
                marker.done(VectorMaskT)
                return true
            } else {
                marker.drop()
                return false
            }
        }
    }

    sealed interface RvCsrT: RvRegT {

        companion object{
            val rv64CsrRegs: List<RvCsrT> = UnprivilegedT.entries + SupervisorT.entries + MachineT.entries + DebugT.entries
            val rv32CsrRegs = rv64CsrRegs + Rv32OnlyT.entries
            val allCsr = rv64CsrRegs + Rv32OnlyT.entries

            val rv64CsrRegMap = rv64CsrRegs.flatMap { entry -> entry.recognizable.map { name -> name to entry } }.associate { it }
            val rv32CsrRegMap = rv32CsrRegs.flatMap { entry -> entry.recognizable.map { name -> name to entry } }.associate { it }
            val allCsrRegMap = allCsr.flatMap { entry -> entry.recognizable.map { name -> name to entry } }.associate { it }

            fun PsiBuilder.parseRv64CsrReg(): Boolean {
                val text = getTokenText()
                val regType = rv64CsrRegMap[text]
                if (regType != null) {
                    val marker = mark()
                    advance()
                    marker.done(regType)
                }

                return regType != null
            }

            fun PsiBuilder.parseRv32CsrReg(): Boolean {
                val text = getTokenText()
                val regType = rv32CsrRegMap[text]
                if (regType != null) {
                    val marker = mark()
                    advance()
                    marker.done(regType)
                }

                return regType != null
            }

            fun PsiBuilder.parseRvCsrReg(): Boolean{
                val text = getTokenText()
                val regType = allCsrRegMap[text]
                if (regType != null) {
                    val marker = mark()
                    advance()
                    marker.done(regType)
                }

                return regType != null
            }
        }


        enum class DebugT(alias: String, val description: String) : RvCsrT {
            X7B0("dcsr", "Debug control and status register"),
            X7B1("dpc", "Debug PC"),
            X7B2("dscratch0", "Debug scratch register 0"),
            X7B3("dscratch1", "Debug scratch register 1")

            ;

            override val address: UInt = name.removePrefix("X").toUInt(16)

            override val recognizable: List<String> = listOf(alias, name.lowercase())
        }

        enum class MachineT(alias: String, val description: String): RvCsrT {
            // Machine Information Registers
            XF11("mvendorid", "Vendor ID"),
            XF12("marchid", "Architecture ID"),
            XF13("mimpid", "Implementation ID"),
            XF14("mhartid", "Hardware thread ID"),
            XF15("mconfigptr", "Pointer to configuration data structure"),
            // Machine Trap Setup
            X300("mstatus", "Machine status register"),
            X301("misa", "ISA and extensions"),
            X302("medeleg", "Machine exception delegation register"),
            X303("mideleg", "Machine interrupt delegation register"),
            X304("mie", "Machine interrupt-enable register"),
            X305("mtvec", "Machine trap-handler base address"),
            X306("mcounteren", "Machine counter enable"),
            X310("mstatush", "Additional machine status register (RV32)"),
            // Machine Trap Handling
            X340("mscratch", "Scratch register for machine trap handlers"),
            X341("mepc", "Machine exception program counter"),
            X342("mcause", "Machine trap cause"),
            X343("mtval", "Machine bad address or instruction"),
            X344("mip", "Machine interrupt pending"),
            X34A("mtinst", "Machine trap instruction (transformed)"),
            X34B("mtval2", "Machine bad guest physical address"),
            // Machine Configuration
            X30A("menvcfg", "Machine environment configuration register"),
            X747("mseccfg", "Machine security configuration register"),
            // Machine Memory Protection
            X3A0("pmpcfg0", "Physical memory protection configuration"),
            X3A2("pmpcfg2", "Physical memory protection configuration"),
            X3A4("pmpcfg4", "Physical memory protection configuration"),
            X3A6("pmpcfg6", "Physical memory protection configuration"),
            X3A8("pmpcfg8", "Physical memory protection configuration"),
            X3AA("pmpcfg10", "Physical memory protection configuration"),
            X3AC("pmpcfg12", "Physical memory protection configuration"),
            X3AE("pmpcfg14", "Physical memory protection configuration"),

            X3B0("pmpaddr0", "Physical memory protection address register"),
            X3B1("pmpaddr1", "Physical memory protection address register"),
            X3B2("pmpaddr2", "Physical memory protection address register"),
            X3B3("pmpaddr3", "Physical memory protection address register"),
            X3B4("pmpaddr4", "Physical memory protection address register"),
            X3B5("pmpaddr5", "Physical memory protection address register"),
            X3B6("pmpaddr6", "Physical memory protection address register"),
            X3B7("pmpaddr7", "Physical memory protection address register"),
            X3B8("pmpaddr8", "Physical memory protection address register"),
            X3B9("pmpaddr9", "Physical memory protection address register"),
            X3BA("pmpaddr10", "Physical memory protection address register"),
            X3BB("pmpaddr11", "Physical memory protection address register"),
            X3BC("pmpaddr12", "Physical memory protection address register"),
            X3BD("pmpaddr13", "Physical memory protection address register"),
            X3BE("pmpaddr14", "Physical memory protection address register"),
            X3BF("pmpaddr15", "Physical memory protection address register"),

            X3C0("pmpaddr16", "Physical memory protection address register"),
            X3C1("pmpaddr17", "Physical memory protection address register"),
            X3C2("pmpaddr18", "Physical memory protection address register"),
            X3C3("pmpaddr19", "Physical memory protection address register"),
            X3C4("pmpaddr20", "Physical memory protection address register"),
            X3C5("pmpaddr21", "Physical memory protection address register"),
            X3C6("pmpaddr22", "Physical memory protection address register"),
            X3C7("pmpaddr23", "Physical memory protection address register"),
            X3C8("pmpaddr24", "Physical memory protection address register"),
            X3C9("pmpaddr25", "Physical memory protection address register"),
            X3CA("pmpaddr26", "Physical memory protection address register"),
            X3CB("pmpaddr27", "Physical memory protection address register"),
            X3CC("pmpaddr28", "Physical memory protection address register"),
            X3CD("pmpaddr29", "Physical memory protection address register"),
            X3CE("pmpaddr30", "Physical memory protection address register"),
            X3CF("pmpaddr31", "Physical memory protection address register"),

            X3D0("pmpaddr32", "Physical memory protection address register"),
            X3D1("pmpaddr33", "Physical memory protection address register"),
            X3D2("pmpaddr34", "Physical memory protection address register"),
            X3D3("pmpaddr35", "Physical memory protection address register"),
            X3D4("pmpaddr36", "Physical memory protection address register"),
            X3D5("pmpaddr37", "Physical memory protection address register"),
            X3D6("pmpaddr38", "Physical memory protection address register"),
            X3D7("pmpaddr39", "Physical memory protection address register"),
            X3D8("pmpaddr40", "Physical memory protection address register"),
            X3D9("pmpaddr41", "Physical memory protection address register"),
            X3DA("pmpaddr42", "Physical memory protection address register"),
            X3DB("pmpaddr43", "Physical memory protection address register"),
            X3DC("pmpaddr44", "Physical memory protection address register"),
            X3DD("pmpaddr45", "Physical memory protection address register"),
            X3DE("pmpaddr46", "Physical memory protection address register"),
            X3DF("pmpaddr47", "Physical memory protection address register"),

            X3E0("pmpaddr48", "Physical memory protection address register"),
            X3E1("pmpaddr49", "Physical memory protection address register"),
            X3E2("pmpaddr50", "Physical memory protection address register"),
            X3E3("pmpaddr51", "Physical memory protection address register"),
            X3E4("pmpaddr52", "Physical memory protection address register"),
            X3E5("pmpaddr53", "Physical memory protection address register"),
            X3E6("pmpaddr54", "Physical memory protection address register"),
            X3E7("pmpaddr55", "Physical memory protection address register"),
            X3E8("pmpaddr56", "Physical memory protection address register"),
            X3E9("pmpaddr57", "Physical memory protection address register"),
            X3EA("pmpaddr58", "Physical memory protection address register"),
            X3EB("pmpaddr59", "Physical memory protection address register"),
            X3EC("pmpaddr60", "Physical memory protection address register"),
            X3ED("pmpaddr61", "Physical memory protection address register"),
            X3EE("pmpaddr62", "Physical memory protection address register"),
            X3EF("pmpaddr63", "Physical memory protection address register"),
            // Machine Counter/Timers
            XB00("mcycle", "Machine cycle counter"),
            XB02("minstret", "Machine instructions-retired counter"),
            XB03("mhpmcounter3", "Machine performance-monitoring counter"),
            XB04("mhpmcounter4", "Machine performance-monitoring counter"),
            XB05("mhpmcounter5", "Machine performance-monitoring counter"),
            XB06("mhpmcounter6", "Machine performance-monitoring counter"),
            XB07("mhpmcounter7", "Machine performance-monitoring counter"),
            XB08("mhpmcounter8", "Machine performance-monitoring counter"),
            XB09("mhpmcounter9", "Machine performance-monitoring counter"),
            XB0A("mhpmcounter10", "Machine performance-monitoring counter"),
            XB0B("mhpmcounter11", "Machine performance-monitoring counter"),
            XB0C("mhpmcounter12", "Machine performance-monitoring counter"),
            XB0D("mhpmcounter13", "Machine performance-monitoring counter"),
            XB0E("mhpmcounter14", "Machine performance-monitoring counter"),
            XB0F("mhpmcounter15", "Machine performance-monitoring counter"),
            XB10("mhpmcounter16", "Machine performance-monitoring counter"),
            XB11("mhpmcounter17", "Machine performance-monitoring counter"),
            XB12("mhpmcounter18", "Machine performance-monitoring counter"),
            XB13("mhpmcounter19", "Machine performance-monitoring counter"),
            XB14("mhpmcounter20", "Machine performance-monitoring counter"),
            XB15("mhpmcounter21", "Machine performance-monitoring counter"),
            XB16("mhpmcounter22", "Machine performance-monitoring counter"),
            XB17("mhpmcounter23", "Machine performance-monitoring counter"),
            XB18("mhpmcounter24", "Machine performance-monitoring counter"),
            XB19("mhpmcounter25", "Machine performance-monitoring counter"),
            XB1A("mhpmcounter26", "Machine performance-monitoring counter"),
            XB1B("mhpmcounter27", "Machine performance-monitoring counter"),
            XB1C("mhpmcounter28", "Machine performance-monitoring counter"),
            XB1D("mhpmcounter29", "Machine performance-monitoring counter"),
            XB1E("mhpmcounter30", "Machine performance-monitoring counter"),
            XB1F("mhpmcounter31", "Machine performance-monitoring counter"),
            // Machine Counter Setup
            X320("mcountinhibit", "Machine counter-inhibit register"),
            X323("mhpmevent3", "Machine performance-monitoring event selector"),
            X324("mhpmevent4", "Machine performance-monitoring event selector"),
            X325("mhpmevent5", "Machine performance-monitoring event selector"),
            X326("mhpmevent6", "Machine performance-monitoring event selector"),
            X327("mhpmevent7", "Machine performance-monitoring event selector"),
            X328("mhpmevent8", "Machine performance-monitoring event selector"),
            X329("mhpmevent9", "Machine performance-monitoring event selector"),
            X32A("mhpmevent10", "Machine performance-monitoring event selector"),
            X32B("mhpmevent11", "Machine performance-monitoring event selector"),
            X32C("mhpmevent12", "Machine performance-monitoring event selector"),
            X32D("mhpmevent13", "Machine performance-monitoring event selector"),
            X32E("mhpmevent14", "Machine performance-monitoring event selector"),
            X32F("mhpmevent15", "Machine performance-monitoring event selector"),
            X330("mhpmevent16", "Machine performance-monitoring event selector"),
            X331("mhpmevent17", "Machine performance-monitoring event selector"),
            X332("mhpmevent18", "Machine performance-monitoring event selector"),
            X333("mhpmevent19", "Machine performance-monitoring event selector"),
            X334("mhpmevent20", "Machine performance-monitoring event selector"),
            X335("mhpmevent21", "Machine performance-monitoring event selector"),
            X336("mhpmevent22", "Machine performance-monitoring event selector"),
            X337("mhpmevent23", "Machine performance-monitoring event selector"),
            X338("mhpmevent24", "Machine performance-monitoring event selector"),
            X339("mhpmevent25", "Machine performance-monitoring event selector"),
            X33A("mhpmevent26", "Machine performance-monitoring event selector"),
            X33B("mhpmevent27", "Machine performance-monitoring event selector"),
            X33C("mhpmevent28", "Machine performance-monitoring event selector"),
            X33D("mhpmevent29", "Machine performance-monitoring event selector"),
            X33E("mhpmevent30", "Machine performance-monitoring event selector"),
            X33F("mhpmevent31", "Machine performance-monitoring event selector"),
            // Debug/Trace Registers
            X7A0("tselect", "Debug/Trace trigger register select"),
            X7A1("tdata1", "First Debug/Trace trigger data register"),
            X7A2("tdata2", "Second Debug/Trace trigger data register"),
            X7A3("tdata3", "Third Debug/Trace trigger data register"),
            X7A8("mcontext", "Machine-mode context register");

            override val address: UInt = name.removePrefix("X").toUInt(16)

            override val recognizable: List<String> = listOf(alias, name.lowercase())
        }

        enum class SupervisorT(alias: String, val description: String) : RvCsrT {
            X100("sstatus","Supervisor status register"),
            X104("sie", "Supervisor interrupt-enable register"),
            X105("stvec", "Supervisor trap handler base address"),
            X106("scounteren", "Supervisor counter enable"),
            X10A("senvcfg", "Supervisor environment configuration register"),
            X140("sscratch", "Scratch register for supervisor trap handlers"),
            X141("sepc", "Supervisor exception program counter"),
            X142("scause", "Supervisor trap cause"),
            X143("stval", "Supervisor bad address or instruction"),
            X144("sip", "Supervisor interrupt pending"),
            X180("satp", "Supervisor address translation and protection"),
            X5A8("scontext", "Supervisor-mode context register")

            ;

            override val address: UInt = name.removePrefix("X").toUInt(16)

            override val recognizable: List<String> = listOf(alias, name.lowercase())
        }

        /**
         * Machine-Level CSR
         */
        enum class UnprivilegedT(alias: String, val description: String = "") : RvCsrT {
            X001("fflags", "Floating-Point Accrued Exceptions"),
            X002("frm", "Floating-Point Dynamic Rounding Mode"),
            X003("fcsr", "Floating-Point Control and Status Register (frm + fflags)"),

            XC00("cycle", "Cycle counter for RDCYCLE instruction"),
            XC01("time", "Timer for RDTIME instruction"),
            XC02("instret", "Instructions-retired counter for RDINSTRET instruction"),

            XC03("hpmcounter3", "Performance-monitoring counter"),
            XC04("hpmcounter4", "Performance-monitoring counter"),
            XC05("hpmcounter5", "Performance-monitoring counter"),
            XC06("hpmcounter6", "Performance-monitoring counter"),
            XC07("hpmcounter7", "Performance-monitoring counter"),
            XC08("hpmcounter8", "Performance-monitoring counter"),
            XC09("hpmcounter9", "Performance-monitoring counter"),
            XC0A("hpmcounter10", "Performance-monitoring counter"),
            XC0B("hpmcounter11", "Performance-monitoring counter"),
            XC0C("hpmcounter12", "Performance-monitoring counter"),
            XC0D("hpmcounter13", "Performance-monitoring counter"),
            XC0E("hpmcounter14", "Performance-monitoring counter"),
            XC0F("hpmcounter15", "Performance-monitoring counter"),
            XC10("hpmcounter16", "Performance-monitoring counter"),
            XC11("hpmcounter17", "Performance-monitoring counter"),
            XC12("hpmcounter18", "Performance-monitoring counter"),
            XC13("hpmcounter19", "Performance-monitoring counter"),
            XC14("hpmcounter20", "Performance-monitoring counter"),
            XC15("hpmcounter21", "Performance-monitoring counter"),
            XC16("hpmcounter22", "Performance-monitoring counter"),
            XC17("hpmcounter23", "Performance-monitoring counter"),
            XC18("hpmcounter24", "Performance-monitoring counter"),
            XC19("hpmcounter25", "Performance-monitoring counter"),
            XC1A("hpmcounter26", "Performance-monitoring counter"),
            XC1B("hpmcounter27", "Performance-monitoring counter"),
            XC1C("hpmcounter28", "Performance-monitoring counter"),
            XC1D("hpmcounter29", "Performance-monitoring counter"),
            XC1E("hpmcounter30", "Performance-monitoring counter"),
            XC1F("hpmcounter31", "Performance-monitoring counter"),
            ;

            override val address: UInt = name.removePrefix("X").toUInt(16)

            override val recognizable: List<String> = listOf(alias, name.lowercase())

        }

        enum class Rv32OnlyT(alias: String, val description: String): RvCsrT {
            X3A1("pmpcfg1", "Physical memory protection configuration, RV32 only"),
            X3A3("pmpcfg3","Physical memory protection configuration, RV32 only"),

            XB80("mcycleh", "Upper 32 bits of mcycle, RV32I only"),
            XB82("minstreth", "Upper 32 bits of minstret, RV32I only"),
            XB83("mhpmcounter3h", "Upper 32 bits of mhpmcounter3, RV32I only"),
            XB84("mhpmcounter4h", "Upper 32 bits of mhpmcounter4, RV32I only"),
            XB85("mhpmcounter5h", "Upper 32 bits of mhpmcounter5, RV32I only"),
            XB86("mhpmcounter6h", "Upper 32 bits of mhpmcounter6, RV32I only"),
            XB87("mhpmcounter7h", "Upper 32 bits of mhpmcounter7, RV32I only"),
            XB88("mhpmcounter8h", "Upper 32 bits of mhpmcounter8, RV32I only"),
            XB89("mhpmcounter9h", "Upper 32 bits of mhpmcounter9, RV32I only"),
            XB8A("mhpmcounter10h", "Upper 32 bits of mhpmcounter10, RV32I only"),
            XB8B("mhpmcounter11h", "Upper 32 bits of mhpmcounter11, RV32I only"),
            XB8C("mhpmcounter12h", "Upper 32 bits of mhpmcounter12, RV32I only"),
            XB8D("mhpmcounter13h", "Upper 32 bits of mhpmcounter13, RV32I only"),
            XB8E("mhpmcounter14h", "Upper 32 bits of mhpmcounter14, RV32I only"),
            XB8F("mhpmcounter15h", "Upper 32 bits of mhpmcounter15, RV32I only"),
            XB90("mhpmcounter16h", "Upper 32 bits of mhpmcounter16, RV32I only"),
            XB91("mhpmcounter17h", "Upper 32 bits of mhpmcounter17, RV32I only"),
            XB92("mhpmcounter18h", "Upper 32 bits of mhpmcounter18, RV32I only"),
            XB93("mhpmcounter19h", "Upper 32 bits of mhpmcounter19, RV32I only"),
            XB94("mhpmcounter20h", "Upper 32 bits of mhpmcounter20, RV32I only"),
            XB95("mhpmcounter21h", "Upper 32 bits of mhpmcounter21, RV32I only"),
            XB96("mhpmcounter22h", "Upper 32 bits of mhpmcounter22, RV32I only"),
            XB97("mhpmcounter23h", "Upper 32 bits of mhpmcounter23, RV32I only"),
            XB98("mhpmcounter24h", "Upper 32 bits of mhpmcounter24, RV32I only"),
            XB99("mhpmcounter25h", "Upper 32 bits of mhpmcounter25, RV32I only"),
            XB9A("mhpmcounter26h", "Upper 32 bits of mhpmcounter26, RV32I only"),
            XB9B("mhpmcounter27h", "Upper 32 bits of mhpmcounter27, RV32I only"),
            XB9C("mhpmcounter28h", "Upper 32 bits of mhpmcounter28, RV32I only"),
            XB9D("mhpmcounter29h", "Upper 32 bits of mhpmcounter29, RV32I only"),
            XB9E("mhpmcounter30h", "Upper 32 bits of mhpmcounter30, RV32I only"),
            XB9F("mhpmcounter31h", "Upper 32 bits of mhpmcounter31, RV32I only"),

            XC80("cycleh", "Upper 32 bits of cycle, RV32I only"),
            XC81("timeh", "Upper 32 bits of time, RV32I only"),
            XC82("instreth", "Upper 32 bits of instret, RV32I only"),
            XC83("hpmcounter3h", "Upper 32 bits of hpmcounter3, RV32I only"),
            XC84("hpmcounter4h", "Upper 32 bits of hpmcounter4, RV32I only"),
            XC85("hpmcounter5h", "Upper 32 bits of hpmcounter5, RV32I only"),
            XC86("hpmcounter6h", "Upper 32 bits of hpmcounter6, RV32I only"),
            XC87("hpmcounter7h", "Upper 32 bits of hpmcounter7, RV32I only"),
            XC88("hpmcounter8h", "Upper 32 bits of hpmcounter8, RV32I only"),
            XC89("hpmcounter9h", "Upper 32 bits of hpmcounter9, RV32I only"),
            XC8A("hpmcounter10h", "Upper 32 bits of hpmcounter10, RV32I only"),
            XC8B("hpmcounter11h", "Upper 32 bits of hpmcounter11, RV32I only"),
            XC8C("hpmcounter12h", "Upper 32 bits of hpmcounter12, RV32I only"),
            XC8D("hpmcounter13h", "Upper 32 bits of hpmcounter13, RV32I only"),
            XC8E("hpmcounter14h", "Upper 32 bits of hpmcounter14, RV32I only"),
            XC8F("hpmcounter15h", "Upper 32 bits of hpmcounter15, RV32I only"),
            XC90("hpmcounter16h", "Upper 32 bits of hpmcounter16, RV32I only"),
            XC91("hpmcounter17h", "Upper 32 bits of hpmcounter17, RV32I only"),
            XC92("hpmcounter18h", "Upper 32 bits of hpmcounter18, RV32I only"),
            XC93("hpmcounter19h", "Upper 32 bits of hpmcounter19, RV32I only"),
            XC94("hpmcounter20h", "Upper 32 bits of hpmcounter20, RV32I only"),
            XC95("hpmcounter21h", "Upper 32 bits of hpmcounter21, RV32I only"),
            XC96("hpmcounter22h", "Upper 32 bits of hpmcounter22, RV32I only"),
            XC97("hpmcounter23h", "Upper 32 bits of hpmcounter23, RV32I only"),
            XC98("hpmcounter24h", "Upper 32 bits of hpmcounter24, RV32I only"),
            XC99("hpmcounter25h", "Upper 32 bits of hpmcounter25, RV32I only"),
            XC9A("hpmcounter26h", "Upper 32 bits of hpmcounter26, RV32I only"),
            XC9B("hpmcounter27h", "Upper 32 bits of hpmcounter27, RV32I only"),
            XC9C("hpmcounter28h", "Upper 32 bits of hpmcounter28, RV32I only"),
            XC9D("hpmcounter29h", "Upper 32 bits of hpmcounter29, RV32I only"),
            XC9E("hpmcounter30h", "Upper 32 bits of hpmcounter30, RV32I only"),
            XC9F("hpmcounter31h", "Upper 32 bits of hpmcounter31, RV32I only"),

            ;

            override val address: UInt = name.removePrefix("X").toUInt(16)

            override val recognizable: List<String> = listOf(alias, name.lowercase())


        }



    }


}