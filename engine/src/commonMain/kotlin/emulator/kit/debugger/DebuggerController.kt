package emulator.kit.debugger

import cengine.util.integer.BigInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

/**
 * Controller class that connects the DebugInterface with the DebuggerHost.
 * This class implements the DebuggerHostListener interface to receive events from the DebuggerHost,
 * and uses the DebugInterface to control the emulator based on messages received from the VSCode debugger.
 */
class DebuggerController(
    private val debugInterface: DebugInterface,
    private val host: DebuggerHost
) : DebuggerHostListener {

    // Coroutine scope for handling asynchronous operations
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        host.setListener(this)
    }

    /**
     * Called when a complete Debug Adapter Protocol message (JSON) is received from the client (VSCode).
     * Parses the message and calls the appropriate method on the DebugInterface.
     * @param message The raw JSON message string.
     */
    override fun onReceiveMessage(message: String) {
        try {
            val json = Json.parseToJsonElement(message).jsonObject
            val command = json["command"]?.jsonPrimitive?.content ?: return

            when (command) {
                "continue" -> {
                    debugInterface.continueExecution()
                    sendResponse("continued")
                }
                "step" -> {
                    debugInterface.step()
                    sendResponse("stepped")
                }
                "stepMultiple" -> {
                    val steps = json["steps"]?.jsonPrimitive?.long ?: 1
                    debugInterface.step(steps)
                    sendResponse("steppedMultiple", mapOf("steps" to steps))
                }
                "pause" -> {
                    debugInterface.pause()
                    sendResponse("paused")
                }
                "reset" -> {
                    debugInterface.reset()
                    sendResponse("reset")
                }
                "setBreakpoint" -> {
                    val address = json["address"]?.jsonPrimitive?.content ?: return
                    // Convert address string to IntNumber
                    try {
                        // Assuming address is a hex string
                        val addressValue = BigInt.parse(address, 16)
                        val success = debugInterface.setBreakpoint(addressValue)
                        sendResponse("breakpointSet", mapOf("success" to success, "address" to address))
                    } catch (e: Exception) {
                        sendErrorResponse("Invalid address format: $address")
                    }
                }
                "clearBreakpoint" -> {
                    val address = json["address"]?.jsonPrimitive?.content ?: return
                    // Convert address string to IntNumber
                    try {
                        // Assuming address is a hex string
                        val addressValue = BigInt.parse(address, 16)
                        val success = debugInterface.clearBreakpoint(addressValue)
                        sendResponse("breakpointCleared", mapOf("success" to success, "address" to address))
                    } catch (e: Exception) {
                        sendErrorResponse("Invalid address format: $address")
                    }
                }
                "clearAllBreakpoints" -> {
                    debugInterface.clearAllBreakpoints()
                    sendResponse("allBreakpointsCleared")
                }
                else -> {
                    // Unknown command
                    sendErrorResponse("Unknown command: $command")
                }
            }
        } catch (e: Exception) {
            sendErrorResponse("Error processing message: ${e.message}")
        }
    }

    /**
     * Called when the client (VSCode) successfully connects to the host.
     */
    override fun onClientConnected() {
        // Send initialization message to the client
        scope.launch {
            val initMessage = buildJsonObject {
                put("type", "initialized")
                put("message", "Debugger connected")
            }
            host.sendMessage(initMessage.toString())
        }
    }

    /**
     * Called when the client disconnects or the connection is lost.
     * @param error An optional Throwable if disconnection was due to an error.
     */
    override fun onClientDisconnected(error: Throwable?) {
        // Clean up resources or perform any actions needed when client disconnects
    }

    /**
     * Called when an operational error occurs within the host itself (e.g., binding error).
     * @param error The Throwable representing the error.
     */
    override fun onError(error: Throwable) {
        // Handle host errors
    }

    /**
     * Sends a response to the client.
     * @param type The type of response.
     * @param data Additional data to include in the response.
     */
    private fun sendResponse(type: String, data: Map<String, Any> = emptyMap()) {
        val response = buildJsonObject {
            put("type", type)
            for ((key, value) in data) {
                when (value) {
                    is String -> put(key, value)
                    is Number -> put(key, value.toDouble())
                    is Boolean -> put(key, value)
                    else -> put(key, value.toString())
                }
            }
        }
        try {
            scope.launch {
                host.sendMessage(response.toString())
            }
        } catch (e: Exception) {
            // Log error
            println("Error sending response: ${e.message}")
        }
    }

    /**
     * Sends an error response to the client.
     * @param errorMessage The error message to send.
     */
    private fun sendErrorResponse(errorMessage: String) {
        sendResponse("error", mapOf("message" to errorMessage))
    }

    /**
     * Starts the debugger host with the specified configuration.
     * @param configuration A map containing configuration details (e.g., "port" for sockets).
     */
    suspend fun start(configuration: Map<String, Any>) {
        host.start(configuration)
    }

    /**
     * Stops the debugger host.
     */
    suspend fun stop() {
        host.stop()
    }
}
