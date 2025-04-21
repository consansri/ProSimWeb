import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.time.Duration

/**
 * Object to manage performance-related metrics.
 */
object Performance {

    /**
     * Maximum number of instructions that can be executed.
     * This value is updated based on execution performance.
     */
    var MAX_INSTR_EXE_AMOUNT by mutableStateOf(1000L)

    /**
     * Updates the maximum instruction execution amount based on the
     * number of instructions executed and the elapsed time.
     *
     * @param instrCount The number of instructions executed.
     * @param ellapsed The duration over which the instructions were executed.
     */
    fun updateExePerformance(instrCount: Long, ellapsed: Duration) {
        if (instrCount > 0L && ellapsed.inWholeNanoseconds > 0L) {
            MAX_INSTR_EXE_AMOUNT = 1000000000 / (ellapsed.inWholeNanoseconds / instrCount)
        }
    }

}