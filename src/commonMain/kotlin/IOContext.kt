sealed interface IOContext {

    companion object{
        const val PREFIX_ERROR = "error: "
        const val PREFIX_WARN = "warn: "
        const val PREFIX_INFO = "info: "
        const val PREFIX_DEBUG = "debug: "
        const val PREFIX_LOG = "log: "
    }

    fun stream(message: String)

    fun streamln(message: String = "") = stream(message + "\n")

    fun error(message: String) = streamln(PREFIX_ERROR + message)

    fun warn(message: String) = streamln(PREFIX_WARN + message)

    fun info(message: String) = streamln(PREFIX_INFO + message)

    fun debug(message: String) = streamln(PREFIX_DEBUG + message)

    fun log(message: String) = streamln(PREFIX_LOG + message)

}