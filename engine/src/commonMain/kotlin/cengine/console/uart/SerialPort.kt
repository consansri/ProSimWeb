package cengine.console.uart

/**
 * Expected interface for platform-specific Serial/UART communication.
 */
interface SerialPort {
    /** Checks if the port is currently open and ready for communication. */
    val isOpen: Boolean

    /** Attempts to open and configure the serial port. */
    fun open(): Boolean

    /** Closes the serial port, releasing any resources. */
    fun close()

    /**
     * Writes raw byte data to the serial port.
     * @param data The ByteArray to send.
     * @return True if the write was successful (or queued successfully), false otherwise.
     */
    fun write(data: ByteArray): Boolean

    /**
     * Reads available byte data from the serial port.
     * This might be blocking or non-blocking depending on the actual implementation.
     * Consider async/Flow alternatives for non-blocking reads in real applications.
     *
     * @param maxBytes The maximum number of bytes to read into the buffer.
     * @return A ByteArray containing the data read (up to maxBytes), or null/empty if no data is available or an error occurred.
     */
    fun read(maxBytes: Int): ByteArray?

    // Optional: Add methods for flushing buffers if needed by implementations
    // fun flushInput()
    // fun flushOutput()
}
