package emulator.kit.common

import cengine.lang.asm.CodeStyle




import kotlinx.datetime.Clock

/**
 * The usage of [IConsole] is mainly to hold messages from architecture components on runtime. It's used to resolve assembler errors and warnings.
 */
class IConsole(val name: String) {

    private val messageArray: MutableList<Message> = mutableListOf()

    fun info(message: String) {
        SysOut.info(message)
        messageArray.add(Message(MSGType.INFO, message))
    }

    fun log(message: String) {
        SysOut.log(message)
        messageArray.add(Message(MSGType.LOG, message))
    }

    fun warn(message: String) {
        SysOut.warn(message)
        messageArray.add(Message(MSGType.WARNING, message))
    }

    fun error(message: String) {
        SysOut.error(message)
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