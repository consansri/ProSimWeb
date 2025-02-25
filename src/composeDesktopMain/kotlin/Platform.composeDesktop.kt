actual fun nativeWarn(message: String) {
    println("Warning: $message")
}

actual fun nativeLog(message: String) {
    println("Log: $message")
}

actual fun nativeError(message: String) {
    println("Error: $message")
}

actual fun nativeInfo(message: String) {
    println("Info: $message")
}