package emulator.kit.common

import cengine.lang.asm.CodeStyle
import nativeError
import nativeInfo
import nativeLog
import nativeWarn
import kotlinx.datetime.Clock

/**
 * The usage of [IConsole] is mainly to hold messages from architecture components on runtime. It's used to resolve assembler errors and warnings.
 */
class IConsole(val name: String) {

    private val messageArray: MutableList<Message> = mutableListOf()

    fun info(message: String) {
        nativeInfo(message)
        messageArray.add(Message(MSGType.INFO, message))
    }

    fun log(message: String) {
        nativeLog(message)
        messageArray.add(Message(MSGType.LOG, message))
    }

    fun warn(message: String) {
        nativeWarn(message)
        messageArray.add(Message(MSGType.WARNING, message))
    }

    fun error(message: String) {
        nativeError(message)
        messageArray.add(Message(MSGType.ERROR, message))
    }

    fun compilerInfo(message: String) {
        messageArray.add(Message(MSGType.INFO, "o.O $message"))
    }

    fun exeInfo(message: String) {
        messageArray.add(Message(MSGType.INFO, "> $message"))
    }

    fun missingFeature(message: String) {
        messageArray.add(Message(MSGType.WARNING, "feature missing: $message"))
    }

    fun clear() {
        messageArray.clear()
    }

    fun getMessages(): List<Message> {
        return messageArray
    }

    data class Message(val type: MSGType, val message: String){
        val time = Clock.System.now()
    }

    enum class MSGType(val style: CodeStyle) {
        LOG(CodeStyle.BASE3),
        INFO(CodeStyle.BASE1),
        WARNING(CodeStyle.YELLOW),
        ERROR(CodeStyle.RED)
    }

}