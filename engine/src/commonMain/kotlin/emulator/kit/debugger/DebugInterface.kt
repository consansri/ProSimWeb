package emulator.kit.debugger

import cengine.util.integer.IntNumber

/**
 * Interface for controlling the emulation through a debugger.
 * This interface provides methods for setting and clearing breakpoints,
 * as well as methods for controlling the execution of the emulator.
 */
interface DebugInterface {
    /**
     * Sets a breakpoint at the specified address.
     * @param address The address where the breakpoint should be set.
     * @return True if the breakpoint was set successfully, false otherwise.
     */
    fun setBreakpoint(address: IntNumber<*>): Boolean

    /**
     * Clears a breakpoint at the specified address.
     * @param address The address where the breakpoint should be cleared.
     * @return True if the breakpoint was cleared successfully, false otherwise.
     */
    fun clearBreakpoint(address: IntNumber<*>): Boolean

    /**
     * Clears all breakpoints.
     */
    fun clearAllBreakpoints()

    /**
     * Checks if a breakpoint is set at the specified address.
     * @param address The address to check.
     * @return True if a breakpoint is set at the specified address, false otherwise.
     */
    fun isBreakpointSet(address: IntNumber<*>): Boolean

    /**
     * Continues execution until a breakpoint is hit or the program terminates.
     */
    fun continueExecution()

    /**
     * Executes a single instruction.
     */
    fun step()

    /**
     * Executes a specified number of instructions.
     * @param steps The number of instructions to execute.
     */
    fun step(steps: Long)

    /**
     * Pauses the execution of the emulator.
     */
    fun pause()

    /**
     * Resets the emulator to its initial state.
     */
    fun reset()
}