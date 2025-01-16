package emulator.kit

import androidx.compose.runtime.MutableState
import cengine.lang.asm.Initializer
import cengine.util.integer.IntNumber
import emulator.core.*
import emulator.kit.common.*
import emulator.kit.memory.MainMemory

/**
 *  Architecture Blueprint
 *
 *  Additional architectures need to build of this class.
 *  This Architecture gets a lot of it's logic through the constructor. To get the Processor in another State this architecture contains the main execution and syntax events.
 *  While the compilation process is fully integrated if the Syntax Logic is given through an abstracted Grammar and Assembly Class. The Execution Process needs to be implemented in the events.
 *  To make Debugging simpler i would recommend to implement a binary mapper which maps a certain instruction with it's parameters to a binary representation and the other way around.
 *  I would recommend you to look at integration of RV32I Assembler, Grammar and Binary Mapper as an example.
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
 *  Possible Features
 *  @property cache Not Essential: Possibly given by Config
 *
 */
abstract class Architecture<ADDR : IntNumber<*>, INSTANCE : IntNumber<*>> {

    abstract val config: ArchConfig

    val console: IConsole = IConsole("Console")
    var initializer: Initializer? = null
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
     * Execution Event: single step
     * should be implemented by specific archs
     */
    open fun exeSingleStep() {
        console.clear()
    }

    /**
     * Execution Event: multi step
     * should be implemented by specific archs
     */
    open fun exeMultiStep(steps: Long) {
        console.clear()
    }

    /**
     * Execution Event: skip subroutine
     * should be implemented by specific archs
     */
    open fun exeSkipSubroutine() {
        console.clear()
    }

    /**
     * Execution Event: return from subroutine
     * should be implemented by specific archs
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
        initializer?.initialize(memory)
        nativeLog("${this::class.simpleName} resetting!")
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