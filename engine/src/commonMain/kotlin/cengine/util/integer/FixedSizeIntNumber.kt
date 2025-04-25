
package cengine.util.integer

/**
 * Sealed interface for integer numbers with a guaranteed fixed size (bitWidth/byteCount).
 */
sealed interface FixedSizeIntNumber<T : FixedSizeIntNumber<T>>: IntNumber<T> {

    /**
     * The number of bits required to represent this number.
     */
    val bitWidth: Int

    /**
     * The number of bytes required to represent this number.
     */
    val byteCount: Int

    operator fun inv(): T
    infix fun and(other: T): T
    infix fun or(other: T): T
    infix fun xor(other: T): T
    infix fun shl(bits: T): T
    infix fun shr(bits: T): T

    // Kotlin Int Operations
    infix fun and(other: Int): T
    infix fun or(other: Int): T
    infix fun xor(other: Int): T
    infix fun shl(bits: Int): T
    infix fun shr(bits: Int): T

    // Kotlin Int Operations
    infix fun and(other: Long): T
    infix fun or(other: Long): T
    infix fun xor(other: Long): T

    /**
     * @return [BitwiseOperationProvider] and ([bitWidth] mask of 1)
     */
    infix fun lowest(bitWidth: Int): T

    override val type: FixedSizeIntNumberT<T>

    /**
     * @param index 0 ..<[bitWidth]
     */
    fun bit(index: Int): T = (this shr index) and 1

    // Rotate Left
    infix fun rol(bits: T): T {
        val shift = bits.toInt() % bitWidth // Ensure the shift is within bounds
        if (shift == 0) return type.to(this)
        return (this shl shift) or (this shr (bitWidth - shift))
    }

    // Rotate Right
    infix fun ror(bits: T): T {
        val shift = bits.toInt() % bitWidth // Ensure the shift is within bounds
        if (shift == 0) return type.to(this)
        return (this shr shift) or (this shl (bitWidth - shift))
    }

    // Rotate Left (Int parameter)
    infix fun rol(bits: Int): T {
        val shift = bits % bitWidth // Ensure the shift is within bounds
        if (shift == 0) return type.to(this)
        if (shift < 0) return ror(-shift) // Handle negative rotation
        return (this shl shift) or (this shr (bitWidth - shift))
    }

    // Rotate Right (Int parameter)
    infix fun ror(bits: Int): T {
        val shift = bits % bitWidth // Ensure the shift is within bounds
        if (shift == 0) return type.to(this)
        if (shift < 0) return rol(-shift) // Handle negative rotation
        return (this shr shift) or (this shl (bitWidth - shift))
    }

    /**
     * Extends the sign bit of a given subset of bits to the full bit width of the value.
     * Only truly meaningful for Signed types, but calculable for Unsigned representations too.
     *
     * @param subsetBitWidth The number of bits in the subset to sign-extend. Must be in 1..[bitWidth]!
     * @return The sign-extended value.
     */
    fun signExtend(subsetBitWidth: Int): T {
        require(subsetBitWidth in 1..bitWidth) {
            "subsetBitWidth ($subsetBitWidth) must be in the range 1 to bitWidth ($bitWidth)."
        }
        if (subsetBitWidth == bitWidth) return type.to(this) // No extension needed

        val signBitMask = type.ONE shl (subsetBitWidth - 1)
        val isNegative = (this and signBitMask) != type.ZERO

        return if (isNegative) {
            // Extend with 1s
            val extensionMask = ((type.ONE shl (bitWidth - subsetBitWidth)) - type.ONE) shl subsetBitWidth
            this or extensionMask
        } else {
            // Sign bit is 0, just mask out upper bits if necessary (though typically result of AND/conversion already does this)
            val valueMask = (type.ONE shl subsetBitWidth) - type.ONE
            this and valueMask
        }
    }


    // --- Byte/Chunk Access ---
    // These make most sense for fixed-size types
    fun int8s(): List<Int8>
    fun uInt8s(): List<UInt8>

    fun int16s(): List<Int16> = int8s().chunked(2) { bytes ->
        require(bytes.size == 2) { "Need 2 bytes for Int16" }
        // Assuming Big Endian merge (adjust if Little Endian needed)
        (bytes[0].toInt16() shl 8) or (bytes[1].toInt16() and 0xFF)
    }

    fun int32s(): List<Int32> = int8s().chunked(4) { bytes ->
        require(bytes.size == 4) { "Need 4 bytes for Int32" }
        bytes.fold(Int32.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toInt32() and 0xFF)
        }
    }

    fun int64s(): List<Int64> = int8s().chunked(8) { bytes ->
        require(bytes.size == 8) { "Need 8 bytes for Int64" }
        bytes.fold(Int64.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toInt64() and 0xFF)
        }
    }

    fun int128s(): List<Int128> = int8s().chunked(16) { bytes ->
        require(bytes.size == 16) { "Need 16 bytes for Int128" }
        bytes.fold(Int128.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toInt128() and 0xFF)
        }
    }

    fun uInt16s(): List<UInt16> = uInt8s().chunked(2) { bytes ->
        require(bytes.size == 2) { "Need 2 bytes for UInt16" }
        (bytes[0].toUInt16() shl 8) or (bytes[1].toUInt16() and 0xFF) // Use unsigned mask
    }

    fun uInt32s(): List<UInt32> = uInt8s().chunked(4) { bytes ->
        require(bytes.size == 4) { "Need 4 bytes for UInt32" }
        bytes.fold(UInt32.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toUInt32() and 0xFF)
        }
    }

    fun uInt64s(): List<UInt64> = uInt8s().chunked(8) { bytes ->
        require(bytes.size == 8) { "Need 8 bytes for UInt64" }
        bytes.fold(UInt64.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toUInt64() and 0xFF)
        }
    }

    fun uInt128s(): List<UInt128> = uInt8s().chunked(16) { bytes ->
        require(bytes.size == 16) { "Need 16 bytes for UInt128" }
        bytes.fold(UInt128.ZERO) { acc, byte ->
            (acc shl 8) or (byte.toUInt128() and 0xFF) // Use correct mask
        }
    }

    // --- Padded String Representations ---
    fun uPaddedBin(): String
    fun uPaddedHex(): String

}