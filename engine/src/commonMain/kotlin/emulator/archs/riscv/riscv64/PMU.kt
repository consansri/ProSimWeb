package emulator.archs.riscv.riscv64

import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64

/**
 * All internal regs will be mapped to CSR Addresses, which should be chosen wisely.
 *
 * Address Range for Configuration Values starting with 0x8F0
 *
 *
 */
class PMU {

    /**
     * Contains 4, 8bit wide [CounterType]s for the 4 [counters]
     */
    var mainConfigReg: UInt = 0U

    /**
     *
     */
    val counters: Array<Counter> = Array(4) { Counter() }

    enum class CounterType {
        /**
         * Global Counters
         *
         * Measuring execution times and events independent of the current task.
         * This configuration is globally valid and does not need to be buffered.
         * An example in this case is a counter which records the time, a single task is running.
         */
        GLOBAL_ALL_TIME,
        GLOBAL_USR_TIME,
        GLOBAL_SINGLE_TASK_TIME,
        GLOBAL_ALL_TASK_TIME,
        GLOBAL_INTERRUPTS,
        GLOBAL_MISSED_INTERRUPTS_BY_TASK,
        GLOBAL_EXT_PORT_PATTERN_MATCHES,

        /**
         * Measuring execution times and events for every running tasks. Therefor, the counter values are different from task to task.
         * Hence, the counter values must be buffered at every task switch to guarantee valid values for each task.
         * However, the configuration is persistent (e.g., a single counter that measures every task's individual execution time and buffers the values on task switches).
         * Its configuration can be set once and is globally valid.
         */
        TASK_AWARE_TIME,
        TASK_AWARE_INTERRUPT,
        TASK_AWARE_EXT_PORT_PATTERN_MATCHES,

        /**
         * Measuring differently for every task.
         * In this case, counter values as well as configuration values have to be buffered due to the fact that configuration is different from task to task.
         * Here, a counter that measures if the program counter of a task is within a certain area is an example.
         * The difference is that configuration has to be set within the task itself for every individual task and therefor must be saved and restored on each task switch.
         */
        Task_BELONGING_TIME,

    }

    /**
     *
     * if needed by [CounterType]: [configReg1] and [configReg2] are used to apply more configuration for [CounterType].
     *
     * [configReg1], [configReg2] is RW (Read, Write)
     *
     * [value1], [value2] are RO (Read Only)
     *
     */
    data class Counter(
        var configReg1: UInt64 = 0U.toUInt64(),
        var configReg2: UInt64 = 0U.toUInt64(),
        var value1: UInt64 = 0U.toUInt64(),
        var value2: UInt64 = 0U.toUInt64(),
    )


}