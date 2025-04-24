package cengine.util.integer

/** Contains utility functions previously in IntNumber.Companion */
object IntNumberUtils {

    fun IntRange.overlaps(other: IntRange): Boolean {
        return this.first <= other.last && other.first <= this.last
    }

    fun String.parse(radix: Int, type: IntNumberT<*>): IntNumber<*> = type.parse(this, radix)

    /**
     * @throws IllegalArgumentException if byteCount is too large for a fixed-size type.
     */
    fun nearestUnsignedFixedSizeT(byteCount: Int): UnsignedFixedSizeIntNumberT<*> {
        if (byteCount <= UInt8.BYTES) return UInt8
        if (byteCount <= UInt16.BYTES) return UInt16
        if (byteCount <= UInt32.BYTES) return UInt32
        if (byteCount <= UInt64.BYTES) return UInt64
        if (byteCount <= UInt128.BYTES) return UInt128
        throw IllegalArgumentException("Byte count $byteCount is too large for a fixed-size unsigned type!")
    }

    /**
     * @throws IllegalArgumentException if byteCount is too large for a fixed-size type.
     */
    fun nearestSignedFixedSizeT(byteCount: Int): SignedFixedSizeIntNumberT<*> {
        if (byteCount <= Int8.BYTES) return Int8
        if (byteCount <= Int16.BYTES) return Int16
        if (byteCount <= Int32.BYTES) return Int32
        if (byteCount <= Int64.BYTES) return Int64
        if (byteCount <= Int128.BYTES) return Int128
        throw IllegalArgumentException("Byte count $byteCount is too large for a fixed-size signed type!")
    }

    // Example: Parsing specifically for fixed-size unsigned
    fun String.parseFixedUInt(radix: Int, byteCount: Int): FixedSizeIntNumber<*>? {
        return runCatching {
            val type = nearestUnsignedFixedSizeT(byteCount)
            type.parse(this, radix) as FixedSizeIntNumber<*>
        }.getOrNull()
    }

    // Example: Parsing specifically for fixed-size signed
    fun String.parseFixedInt(radix: Int, byteCount: Int): FixedSizeIntNumber<*>? {
        return runCatching {
            val type = nearestSignedFixedSizeT(byteCount)
            type.parse(this, radix) as FixedSizeIntNumber<*>
        }.getOrNull()
    }

    // Original functions, potentially returning BigInt if no fixed-size matches
    fun nearestUType(byteCount: Int): UnsignedFixedSizeIntNumberT<*> = nearestUnsignedFixedSizeT(byteCount)
    fun nearestType(byteCount: Int): SignedFixedSizeIntNumberT<*> = nearestSignedFixedSizeT(byteCount)

    // Merging requires a specific *fixed size* target type T
    fun <T : FixedSizeIntNumber<T>> Collection<UInt8>.mergeToFixedIntNumbers(
        targetType: FixedSizeIntNumberT<T>,
        createFromBytes: (List<UInt8>) -> T, // Keep this lambda for flexibility if needed
    ): List<T> {
        val chunkSize = targetType.BYTES
        require(chunkSize > 0) { "Target type must have a fixed positive byte count." }
        require(this.size % chunkSize == 0) { "Collection size (${this.size}) must be a multiple of chunk size ($chunkSize)." }

        // Use the type's built-in chunking logic if possible, otherwise use createFromBytes
        // This example assumes createFromBytes handles endianness etc.
        return this.chunked(chunkSize) { chunk ->
            createFromBytes(chunk)
        }
    }

    fun Collection<IntNumber<*>>.toArray(): Array<IntNumber<*>> = this.toTypedArray()
}