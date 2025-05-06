package emulator.kit.debugger

/**
 * Interface for a VSCode Debugger Extension Host.
 * This component typically runs as a server, waiting for a connection
 * from the VSCode debugger UI (the client). It handles communication
 * based on the Debug Adapter Protocol (DAP).
 */
interface DebuggerHost {

    /**
     * Indicates if the host is currently actively listening for connections or has an active connection.
     */
    val isRunning: Boolean

    /**
     * Indicates if a client (VSCode) is currently connected.
     */
    val isClientConnected: Boolean

    /**
     * Sets the listener that will receive events from this host.
     * Pass null to remove the listener.
     * Consider thread-safety if the listener implementation requires it, although
     * notifications should ideally be dispatched from a consistent context.
     */
    fun setListener(listener: DebuggerHostListener?)

    /**
     * Starts the debugger host, making it ready to accept a client connection.
     * For socket-based hosts, this typically means binding to a port and listening.
     * For stdio-based hosts, this might mean starting to read from stdin.
     *
     * This function should be non-blocking or manage its own background execution.
     *
     * @param configuration A map containing configuration details (e.g., "port" for sockets).
     * @throws Exception if the host cannot be started (e.g., port already in use).
     */
    suspend fun start(configuration: Map<String, Any>)

    /**
     * Sends a Debug Adapter Protocol message (JSON) to the connected client.
     * This should handle the necessary DAP message framing (e.g., Content-Length header).
     *
     * @param message The raw JSON message string to send.
     * @throws IllegalStateException if no client is currently connected.
     * @throws kotlinx.io.IOException if an error occurs during sending. // Use appropriate exception type
     */
    suspend fun sendMessage(message: String) // Make it suspend for async I/O

    /**
     * Stops the debugger host, disconnects any connected client, and releases resources.
     * This function should ensure graceful shutdown.
     */
    suspend fun stop() // Make it suspend for potentially async cleanup
}