package cengine.util

/**
 * Specifies the byte order of multi-byte values.
 *
 * @property LITTLE Little-endian, where the least significant byte is stored first.
 * @property BIG Big-endian, where the most significant byte is stored first.
 */
enum class Endianness {
    /**
     * Little-endian, where the least significant byte is stored first.
     */
    LITTLE,

    /**
     * Big-endian, where the most significant byte is stored first.
     */
    BIG
}