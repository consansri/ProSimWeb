package cengine.console

import config.BuildConfig

sealed interface IOContext {

    companion object {
        const val PREFIX_ERROR = "error: "
        const val PREFIX_WARN = "warn: "
        const val PREFIX_INFO = "info: "
        const val PREFIX_DEBUG = "debug: "
        const val PREFIX_LOG = "log: "
        const val PREFIX_USAGE = "usage: "
    }

    fun stream(message: String)

    fun streamln(message: String = "") = stream(message + "\n")

    fun error(message: String, vararg elements: Any?) = streamln(PREFIX_ERROR + message + elements.joinToString("") { it.toString() })

    fun warn(message: String, vararg elements: Any?) = streamln(PREFIX_WARN + message + elements.joinToString("") { it.toString() })

    fun info(message: String, vararg elements: Any?) = streamln(PREFIX_INFO + message + elements.joinToString("") { it.toString() })

    fun debug(message: () -> String) {
        if (BuildConfig.DEBUG) streamln(PREFIX_DEBUG + message())
    }

    fun log(message: String, vararg elements: Any?) = streamln(PREFIX_LOG + message + elements.joinToString("") { it.toString() })

    fun usage(message: String, vararg elements: Any?) = streamln(PREFIX_USAGE + message + elements.joinToString("") { it.toString() })

}