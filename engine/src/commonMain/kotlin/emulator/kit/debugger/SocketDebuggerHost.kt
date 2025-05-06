package emulator.kit.debugger

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readFully
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.cancellation.CancellationException

private const val CONTENT_LENGTH_HEADER = "Content-Length"
private const val HEADER_SEPARATOR = "\r\n"
private const val BODY_SEPARATOR = "\r\n\r\n"

/**
 * Configuration key for the port number.
 */
const val CONFIG_PORT = "port"
/**
 * Configuration key for the host address (defaults to "127.0.0.1").
 */
const val CONFIG_HOST = "host"

/**
 * A socket-based implementation of the VSDebuggerHost using Ktor.
 * Listens on a specified TCP port for a single client connection.
 */
@OptIn(ExperimentalAtomicApi::class)
class SocketDebuggerHost(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default // IO dispatcher might be better: Dispatchers.IO
) : DebuggerHost {

    // Use atomic references for thread-safe state management
    private val _isRunning = AtomicBoolean(false)
    private val _isClientConnected = AtomicBoolean(false)
    private val _listener = AtomicReference<DebuggerHostListener?>(null)

    // Coroutine scope for managing background tasks (listening, reading, writing)
    private var hostScope: CoroutineScope? = null
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var clientWriteChannel: ByteWriteChannel? = null

    // Flow for handling backpressure on send operations if needed
    // Alternatively, use a simple lock or ensure writeChannel handles suspension.
    // Using a SharedFlow as a signal channel to coordinate sends.
    private val sendSignal = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1, // Allow one send attempt to buffer
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val isRunning: Boolean get() = _isRunning.load()
    override val isClientConnected: Boolean get() = _isClientConnected.load()

    override fun setListener(listener: DebuggerHostListener?) {
        _listener.store(listener)
    }

    @Throws(IllegalArgumentException::class)
    override suspend fun start(configuration: Map<String, Any>) {
        if (_isRunning.load()) {
            println("Debugger host is already running.")
            return
        }

        val port = configuration[CONFIG_PORT] as? Int
            ?: throw IllegalArgumentException("Missing or invalid '$CONFIG_PORT' configuration.")
        val host = configuration[CONFIG_HOST] as? String ?: "127.0.0.1"

        // Create a new scope for this host instance
        val newScope = CoroutineScope(SupervisorJob() + dispatcher)
        hostScope = newScope // Store the scope

        try {
            // SelectorManager should be managed appropriately (e.g., singleton or passed in)
            // Creating a new one each time might be inefficient but is simple here.
            // Consider Dispatchers.IO for selector manager if available on platform
            val selectorManager = SelectorManager(dispatcher)
            val socketBuilder = aSocket(selectorManager).tcp()
            serverSocket = socketBuilder.bind(host, port) {
                reuseAddress = true // Often useful for development
            }
            _isRunning.store(true)
            println("Debugger host listening on $host:$port")

            // Launch the acceptor loop in the background
            newScope.launch {
                try {
                    println("Waiting for client connection...")
                    // Accept only one connection for typical DAP scenarios
                    val acceptedSocket = serverSocket!!.accept() // Suspending call
                    clientSocket = acceptedSocket // Store the client socket
                    _isClientConnected.store(true)
                    println("Client connected from: ${acceptedSocket.remoteAddress}")
                    _listener.load()?.onClientConnected()

                    // Get channels for reading and writing
                    val readChannel = acceptedSocket.openReadChannel()
                    clientWriteChannel = acceptedSocket.openWriteChannel(autoFlush = true)
                    sendSignal.tryEmit(Unit) // Signal that sending is now possible

                    // Launch the reading loop
                    handleClientCommunication(readChannel, acceptedSocket)

                } catch (e: CancellationException) {
                    println("Acceptor job cancelled.")
                    // Allow cleanup in finally block
                } catch (e: IOException) { // Catch specific bind/accept errors
                    if (_isRunning.load()) { // Avoid error if stop() was called
                        println("Error accepting connection: ${e.message}")
                        _listener.load()?.onError(e)
                        gracefulStopInternal() // Attempt cleanup
                    }
                } catch (e: Exception) { // Catch unexpected errors
                    if (_isRunning.load()) {
                        println("Unexpected error during connection accept/setup: ${e.message}")
                        e.printStackTrace() // Log stacktrace for debugging
                        _listener.load()?.onError(e)
                        gracefulStopInternal()
                    }
                } finally {
                    // If the accept loop finishes unexpectedly but the host wasn't stopped,
                    // ensure cleanup. This might happen if only one connection is accepted.
                    // In a real scenario, you might want a loop to accept multiple clients
                    // if the protocol allows, or specific logic for single client disconnect.
                    // For DAP, typically one client connects and stays.
                    println("Acceptor loop finished.")
                    // If client didn't connect and loop ends, ensure host stops
                    // if (!_isClientConnected.value && _isRunning.value) {
                    //    gracefulStopInternal() // Or handle differently?
                    //}
                }
            }
        }  catch (e: Exception) {
            println("Failed to start debugger host: ${e.message}")
            hostScope?.cancel()
            hostScope = null
            _isRunning.store(false)
            _listener.load()?.onError(e)
            throw RuntimeException("Failed to start debugger host", e)
        }
    }

    /**
     * Handles reading DAP messages from the client.
     */
    private suspend fun handleClientCommunication(readChannel: ByteReadChannel, socket: Socket) {
        try {
            while (currentCoroutineContext().isActive && !readChannel.isClosedForRead) {
                val headers = mutableMapOf<String, String>()
                var contentLength = -1

                // 1. Read Headers
                while (true) {
                    val line = readChannel.readUTF8Line() // Reads until \n or \r\n
                    if (line.isNullOrBlank()) { // Empty line signifies end of headers
                        break
                    }
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        headers[key] = value
                        if (key.equals(CONTENT_LENGTH_HEADER, ignoreCase = true)) {
                            contentLength = value.toIntOrNull() ?: -1
                        }
                    }
                }

                // 2. Validate Content-Length
                if (contentLength < 0) {
                    println("Error: Missing or invalid $CONTENT_LENGTH_HEADER")
                    // Consider disconnecting or logging error
                    continue // Or break? Depending on desired strictness
                }

                // 3. Read Body
                if (contentLength > 0) {
                    val buffer = ByteArray(contentLength)
                    readChannel.readFully(buffer, 0, contentLength)
                    val messageJson = buffer.decodeToString() // Assumes UTF-8

                    // 4. Notify Listener
                    _listener.load()?.onReceiveMessage(messageJson)
                } else {
                    // Handle case of Content-Length: 0 if necessary
                    _listener.load()?.onReceiveMessage("{}") // Or empty string, depending on DAP spec?
                }
            }
        } catch (e: CancellationException) {
            println("Client reader job cancelled.")
            // Handled by finally block
        } catch (e: ClosedReceiveChannelException) {
            println("Client disconnected (read channel closed).")
            handleDisconnect(null) // Normal disconnect
        } catch (e: IOException) {
            println("IO error reading from client: ${e.message}")
            handleDisconnect(e) // Disconnect due to error
        } catch (e: Exception) {
            println("Unexpected error reading from client: ${e.message}")
            e.printStackTrace()
            handleDisconnect(e) // Disconnect due to error
        } finally {
            println("Client read loop finished.")
            // Ensure disconnect state is set if loop exits for any reason other than stop()
            if (_isClientConnected.load()) {
                handleDisconnect(null) // Assume disconnect if loop ends while connected
            }
        }
    }


    override suspend fun sendMessage(message: String) {
        if (!_isClientConnected.load() || clientSocket == null || clientWriteChannel == null) {
            throw IllegalStateException("Cannot send message: No client connected.")
        }

        // Wait until the write channel is available (handles initial connection)
        // This basic signal might not be robust enough for high-throughput backpressure.
        // Ktor's write channels often handle suspension correctly.
        // Consider removing the signal flow if writeByteAvailable seems sufficient.
        // sendSignal.first() // Wait for the signal that channel is ready

        val writeChannel = clientWriteChannel ?: throw IllegalStateException("Write channel not available.")

        try {
            // DAP Framing: Content-Length header + body
            val messageBytes = message.encodeToByteArray() // UTF-8
            val header = "$CONTENT_LENGTH_HEADER: ${messageBytes.size}$BODY_SEPARATOR"
            val headerBytes = header.encodeToByteArray() // ASCII/UTF-8

            // Use suspending write operations
            writeChannel.writeFully(headerBytes, 0, headerBytes.size)
            writeChannel.writeFully(messageBytes, 0, messageBytes.size)
            // writeChannel.flush() // autoFlush should handle this, but explicit flush can be added if needed

            // println("Sent ${headerBytes.size + messageBytes.size} bytes.") // Debugging
        } catch (e: IOException) {
            println("Error sending message: ${e.message}")
            handleDisconnect(e) // Assume connection is lost on send error
            throw e // Re-throw IO exception
        } catch (e: Exception) {
            println("Unexpected error sending message: ${e.message}")
            e.printStackTrace()
            handleDisconnect(e)
            throw RuntimeException("Failed to send message", e) // Wrap unexpected errors
        }
    }

    override suspend fun stop() {
        if (!_isRunning.load()) return
        println("Stopping debugger host...")
        gracefulStopInternal()
        println("Debugger host stopped.")
    }

    /** Called when the client disconnects or an error forces disconnection. */
    private fun handleDisconnect(error: Throwable?) {
        if (_isClientConnected.exchange(false)) { // Only notify once
            println("Handling disconnect. Error: ${error?.message}")
            val listener = _listener.load()
            // Execute listener callback outside of critical section if possible,
            // potentially launching it in a separate coroutine to avoid deadlocks.
            // Using launch here to avoid blocking the disconnect flow.
            hostScope?.launch {
                listener?.onClientDisconnected(error)
            }

            // Clean up client-specific resources
            closeClientSocket()
            clientWriteChannel = null

            // Optional: Decide whether to stop the whole host on client disconnect.
            // For DAP, usually yes, the session ends.
            // gracefulStopInternal() // Uncomment if host should stop when client disconnects.
            // If we don't stop the host here, it might go back to waiting for a new connection
            // if the accept loop was designed for it. The current one isn't.
            // Consider stopping the host fully if the primary client disconnects.
            hostScope?.launch { gracefulStopInternal() } // Stop the host when client leaves
        }
    }


    /** Internal shutdown logic, attempts to close resources gracefully. */
    private suspend fun gracefulStopInternal() {
        if (!_isRunning.exchange(false)) {
            // Already stopping or stopped
            return
        }
        println("Executing internal graceful stop...")

        // 1. Cancel all coroutines in the host's scope
        hostScope?.cancel("Debugger host stopping") // This cancels acceptor, reader jobs etc.
        hostScope = null // Release the scope reference

        // 2. Close client socket (might be redundant if handleDisconnect called it)
        closeClientSocket()

        // 3. Close server socket
        try {
            serverSocket?.close()
            println("Server socket closed.")
        } catch (e: Exception) {
            println("Error closing server socket: ${e.message}")
            // Log error, but continue shutdown
        } finally {
            serverSocket = null
        }

        _isClientConnected.store(false) // Ensure client state is false
        clientWriteChannel = null

        // Note: Listener isn't notified of shutdown here, only errors or disconnects.
        // Add a specific onHostStopped() event if needed.
    }

    private fun closeClientSocket() {
        try {
            clientSocket?.close() // Ktor socket close is suspend fun, but can be called non-suspendingly? Check docs. Often safe.
            // If clientSocket.close() requires suspension:
            // hostScope?.launch { clientSocket?.close() } // Or runBlocking { } if absolutely needed (avoid)
            println("Client socket closed.")
        } catch (e: Exception) {
            println("Error closing client socket: ${e.message}")
        } finally {
            clientSocket = null
        }
    }
}