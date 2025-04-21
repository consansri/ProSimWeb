package debug

/**
 * [DebugTools] holds simple constants which can be switched to quickly log debug information.
 */
object DebugTools {

    /**
     * EXTENDABLE
     */

    const val RV64_showLIDecisions = false

    /**
     * CENGINE
     */

    const val ENGINE_showFileWatcherInfo = true

    /**
     * KIT
     */

    const val KIT_showMemoryInfo = false
    const val KIT_showCacheInfo = false

    const val KIT_showRuleChecks = false
    const val KIT_showPSITree = false
    const val KIT_showPostFixExpressions = false

    val KIT_ValBinaryToolsDivisionLoopLimit: Long? = null
    const val KIT_showValCheckWarnings = false
    const val KIT_showValTypeConversionInfo = false
    const val KIT_showValBinaryToolsCalculations = false
    const val KIT_showValDecToolsCalculations = false
    const val KIT_showValDecToolsCalculationDetails = false

}