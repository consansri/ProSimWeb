package extendable

import StyleConst.Main.Editor.HL

object ArchConst {
    /*
     *    !! use this Object only in Development Phase to test instances even if other Instances are not integrated yet !!
     */

    /*
        NOT OVERRIDABLE!
     */

    // Architecture Assembly Constructor
    object StandardHL {
        val comment = HL.base05.getFlag()
        val register = HL.orange.getFlag()
        val word = HL.magenta.getFlag()
        val alphaNum = HL.violet.getFlag()
        val instruction = HL.blue.getFlag()
        val symbol = HL.cyan.getFlag()
        val bin = HL.blue.getFlag()
        val hex = HL.blue.getFlag()
        val dec = HL.blue.getFlag()
        val udec =HL.blue.getFlag()
        val ascii = HL.green.getFlag()
        val string = HL.green.getFlag()

        val error = HL.red.getFlag()
        val whiteSpace = HL.whitespace.getFlag()
    }

    // GLOBAL
    const val PROSIMNAME = "ProSimWeb"

    // FILEHANDLER
    const val UNDO_STATE_COUNT = 32
    const val REDO_STATE_COUNT = 32
    const val UNDO_DELAY_MILLIS = 1000L

    // COMPILER
    const val COMPILER_TOKEN_PSEUDOID = -100

    // REGISTER
    val REGISTER_VALUETYPES = arrayOf(RegTypes.BIN, RegTypes.HEX, RegTypes.UDEC, RegTypes.DEC)

    // TRANSCRIPT PARAM SPLIT SYMBOL
    val TRANSCRIPT_PARAMSPLIT = ",\t"

    // ADDRESS
    const val hex = 0b10111001

    // TYPE IDENTIFICATION
    const val PRESTRING_HEX = "0x"
    const val PRESTRING_BINARY = "0b"
    const val PRESTRING_DECIMAL = ""
    const val PRESTRING_UDECIMAL = "u"

    // REGEX SPLITTER
    val LINEBREAKS = listOf("\n", "\r", "\r\n")

    // STATES
    const val STATE_UNCHECKED = "unchecked"
    const val STATE_HASERRORS = "hasErrors"
    const val STATE_EXECUTABLE = "buildable"
    const val STATE_EXECUTION = "execution"

    enum class TranscriptHeaders{
        addr,
        label,
        instr,
        params
    }

    enum class RegTypes {
        HEX,
        BIN,
        DEC,
        UDEC
    }

}