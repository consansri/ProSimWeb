package emulator.kit

import androidx.compose.runtime.MutableState
import cengine.console.SysOut
import cengine.lang.asm.AsmBinaryProvider
import cengine.util.integer.BigInt
import cengine.util.integer.IntNumber
import cengine.util.integer.IntNumberT
import cengine.util.integer.UnsignedFixedSizeIntNumber
import emulator.kit.common.IConsole
import emulator.kit.memory.MainMemory


/**
 *  Architecture Blueprint
 *
 *  Additional architectures need to build of this class.
 *  This Architecture gets a lot of its logic through the constructor. To get the Processor in another State, this architecture contains the main execution and syntax events.
 *  While the compilation process is fully integrated if the Syntax Logic is given through an abstracted Grammar and Assembly Class. The Execution Process needs to be implemented in the events.
 *  To make Debugging simpler, I would recommend implementing a binary mapper which maps a certain instruction with it's parameters to a binary representation and the other way around.
 *  I would recommend you look at the integration of RV32I Assembler, Grammar and Binary Mapper as an example.
 *
 *  @param config Specific config "file" which should be defined in an object which contains all configuration constants of the architecture.
 *  @param asmConfig Specific Grammar and Assembler class which is then given the Architecture through the asmConfig "file"
 *
 *  Essential Features
 *  @property description Essential: Given by Config
 *  @property regContainer Essential: Given by Config
 *  @property memory Essential: Given by Config
 *
 *  @property console Instantiated with Config name
 *  @property assembler Instantiated with AsmConfig grammar and assembly and ArchConst COMPILER_REGEX and StandardHL
 *
 *  @property features Holds specific Assembler features.
 *  @property settings Holds specific Architecture Setup settings.
 *
 *
 */
abstract class Architecture<ADDR : UnsignedFixedSizeIntNumber<ADDR>, INSTANCE : UnsignedFixedSizeIntNumber<INSTANCE>>(val addrType: IntNumberT<ADDR>, val instanceType: IntNumberT<INSTANCE>) {

    abstract val config: ArchConfig

    val console: IConsole = IConsole("Console")
    var initializer: AsmBinaryProvider? = null
    val description get() = config.DESCR
    val settings get() = config.SETTINGS
    val disassembler get() = config.DISASSEMBLER

    abstract val memory: MainMemory<ADDR, INSTANCE>
    abstract val pcState: MutableState<ADDR>

    init {
        // Starting with non-micro setup
        MicroSetup.clear()
    }

    /**
     * Execution Event: continuous
     * should be implemented by specific archs
     */
    open fun exeContinuous() {
        console.clear()
    }

    /**
     * Execution Event: a single step
     * should be implemented by specific archs
     */
    open fun exeSingleStep() {
        console.clear()
    }

    /**
     * Execution Event: multistep
     * should be implemented by specific archs
     */
    open fun exeMultiStep(steps: Long) {
        console.clear()
    }

    /**
     * Execution Event: specific archs
     * should implement skip subroutine
     */
    open fun exeSkipSubroutine() {
        console.clear()
    }

    /**
     * Execution Event: specific archs
     * should implement return from subroutine
     */
    open fun exeReturnFromSubroutine() {
        console.clear()
    }

    /**
     * Execution Event: until address
     * should be implemented by specific archs
     */
    open fun exeUntilAddress(address: IntNumber<*>) {
        console.clear()
    }

    /**
     * Reset Event
     * don't need to but could be implemented by specific archs
     */
    open fun exeReset() {
        MicroSetup.getMemoryInstances().forEach {
            it.clear()
        }
        MicroSetup.getRegFiles().forEach {
            it.clear()
        }
        resetPC()
        initializer?.let { memory.initialize(it)}
        pcState.value = addrType.to(initializer?.entry() ?: BigInt.ZERO)
        SysOut.debug { "${this::class.simpleName} resetting!" }
        console.exeInfo("resetting")
    }

    protected abstract fun resetPC()

    /**
     * Reset [MicroSetup]
     */
    fun resetMicroArch() {
        MicroSetup.clear()
        setupMicroArch()
    }

    /**
     * Setup [MicroSetup]
     * For visibility of certain micro architectural components.
     *
     * Append Components in use to [MicroSetup] to make them visible.
     */
    open fun setupMicroArch() {
        MicroSetup.append(memory)
    }

}