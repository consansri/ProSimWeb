package emulator.kit.optional

import Performance
import cengine.util.integer.IntNumber
import emulator.kit.memory.Memory
import emulator.kit.nativeError
import kotlin.time.measureTime

abstract class BasicArchImpl<ADDR: IntNumber<*>, INSTANCE: IntNumber<*>> : emulator.kit.Architecture<ADDR, INSTANCE>() {
    override fun exeContinuous() {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeContinuous()

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= Performance.MAX_INSTR_EXE_AMOUNT) {
                instrCount++
                result = executeNext(tracker)
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("continuous \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeSingleStep() {
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeSingleStep() // clears console
            if (!executeNext(tracker).valid) {
                nativeError("Couldn't execute instruction at ${pcState.value.toString(16)}!")
            }
        }

        Performance.updateExePerformance(1, measuredTime)

        console.exeInfo("single step \ntook ${measuredTime.inWholeMicroseconds} μs\n$tracker")
    }

    override fun exeMultiStep(steps: Long) {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeMultiStep(steps)

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount < steps) {
                instrCount++
                result = executeNext(tracker)
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("$steps steps \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeSkipSubroutine() {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeSkipSubroutine()

            var result = executeNext(tracker)
            instrCount++

            if (result.valid && result.typeIsBranchToSubroutine) {
                while (result.valid && instrCount <= 1000) {
                    instrCount++
                    result = executeNext(tracker)
                    if (result.typeIsReturnFromSubroutine) {
                        break
                    }
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("skip subroutine \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeReturnFromSubroutine() {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeReturnFromSubroutine()

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= 1000) {
                instrCount++
                result = executeNext(tracker)
                if (result.typeIsReturnFromSubroutine) {
                    break
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("return from subroutine \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }

    override fun exeUntilAddress(address: IntNumber<*>) {
        var instrCount = 0L
        val tracker = Memory.AccessTracker()
        val measuredTime = measureTime {
            super.exeUntilAddress(address)

            var result: ExecutionResult? = null

            while (result?.valid != false && instrCount <= 1000) {
                instrCount++
                result = executeNext(tracker)
                if (pcState.value == address) {
                    break
                }
            }
        }

        Performance.updateExePerformance(instrCount, measuredTime)

        console.exeInfo("until $address \ntook ${measuredTime.inWholeMicroseconds} μs [executed $instrCount instructions]\n$tracker")
    }


    abstract fun executeNext(tracker: Memory.AccessTracker): ExecutionResult


    data class ExecutionResult(val valid: Boolean, val typeIsReturnFromSubroutine: Boolean = false, val typeIsBranchToSubroutine: Boolean = false)


}