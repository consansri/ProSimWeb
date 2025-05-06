package emulator.kit.debugger

/**
 * Listener interface for events occurring on the VSDebuggerHost.
 */
interface DebuggerHostListener {
    /**
     * Called when a complete Debug Adapter Protocol message (JSON) is received from the client (VSCode).
     * @param message The raw JSON message string.
     */
    fun onReceiveMessage(message: String)

    /**
     * Called when the client (VSCode) successfully connects to the host.
     */
    fun onClientConnected()

    /**
     * Called when the client disconnects or the connection is lost.
     * @param error An optional Throwable if disconnection was due to an error.
     */
    fun onClientDisconnected(error: Throwable? = null)

    /**
     * Called when an operational error occurs within the host itself (e.g., binding error).
     * @param error The Throwable representing the error.
     */
    fun onError(error: Throwable)
}