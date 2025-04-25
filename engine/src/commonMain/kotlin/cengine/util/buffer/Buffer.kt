package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.*

/**
 * Base class for mutable buffers holding elements of type [T] (a subtype of [IntNumber]).
 *
 * This buffer stores elements of a specific type `T` but allows getting, putting (adding),
 * and setting values of *any* `IntNumber` type.
 *
 * - When putting/setting a value with more bytes than `T`, the value is split into
 * multiple `T` elements according to the buffer's endianness.
 * - When putting/setting a value with fewer or equal bytes than `T`, the value is
 * converted to `T` (potentially extending or truncating).
 * - When getting a type with more bytes than `T`, multiple `T` elements are merged
 * according to the buffer's endianness.
 * - When getting a type with fewer or equal bytes than `T`, a single `T` element is
 * retrieved and converted to the target type.
 *
 * @param T The specific type of IntNumber stored natively in this buffer.
 * @property endianness The endianness used for splitting and merging multi-element values.
 * @property type Static information and operations for type T.
 */
// Note: Made non-abstract as the core logic can now be generalized.
// You could make it abstract again if subclasses need to override toArray() or other specifics.
abstract class Buffer<T : UnsignedFixedSizeIntNumber<*>>(
    // T constrained to IntNumber<T>
    endianness: Endianness,
    val type: UnsignedFixedSizeIntNumberT<T>,
) : Collection<T> { // Implement MutableList for full List API + mutations

    var endianness: Endianness = endianness
        private set
    protected val data: MutableList<T> = mutableListOf()

    // --- Helper Functions ---

    /**
     * Merges a list of `T` elements into a larger number `R`.
     * Requires `targetType.BYTES` to be a multiple of `type.BYTES`.
     */
    private fun <R : UnsignedFixedSizeIntNumber<R>> mergePartsTo(
        parts: List<T>,
        targetType: UnsignedFixedSizeIntNumberT<R>,
    ): R {
        if (parts.isEmpty()) {
            // Return zero of the target type if parts list is empty
            return targetType.to(type.ZERO) // Convert T's ZERO to R's ZERO
        }
        if (targetType.BYTES % type.BYTES != 0) {
            throw IllegalArgumentException("Target type byte count (${targetType.BYTES}) must be a multiple of buffer element byte count (${type.BYTES}) for merging.")
        }
        if (parts.size * type.BYTES != targetType.BYTES) {
            throw IllegalArgumentException("Incorrect number of parts (${parts.size}) provided to merge into target type ${targetType::class.simpleName} (expected ${targetType.BYTES / type.BYTES}).")
        }

        var combined: R = targetType.ZERO
        val partShift: R = targetType.of(type.BITS)

        // Adjust iteration order based on buffer's endianness
        val effectiveParts = if (endianness == Endianness.LITTLE) parts.reversed() else parts

        for (part in effectiveParts) {
            combined = combined shl partShift
            val partAsTargetType = targetType.to(part)
            combined = combined or partAsTargetType
        }

        return combined
    }

    /**
     * Splits a value into a list of `T` elements.
     * Relies on `type.split()`. Handles endianness by reversing the list if needed.
     * Assumes `type.split()` returns parts in big-endian order (most significant first).
     */
    private fun splitValue(value: UnsignedFixedSizeIntNumber<*>): List<T> {
        if (value.byteCount % type.BYTES != 0) {
            throw IllegalArgumentException("Value byte count (${value.byteCount}) must be a multiple of buffer element byte count (${type.BYTES}) for splitting.")
        }
        // Ensure split handles the input type correctly.
        val parts = type.split(value) // Returns List<T>
        return if (endianness == Endianness.LITTLE) parts.reversed() else parts
    }

    // --- Core Generic Get/Put/Set ---

    /**
     * Retrieves elements starting at `index`, merges them, and converts to type `R`.
     * Handles cases where R is larger, smaller, or equal in size to T.
     *
     * @param R The desired IntNumber type to retrieve.
     * @param index The starting index in the buffer (element index, not byte offset).
     * @param targetType The static type information for R.
     * @return The value at the index, converted to type R.
     * @throws IndexOutOfBoundsException if the required indices are out of range.
     */
    operator fun <R : UnsignedFixedSizeIntNumber<R>> get(index: Int, targetType: UnsignedFixedSizeIntNumberT<R>): R {
        if (index < 0) throw IndexOutOfBoundsException("Index cannot be negative: $index")

        if (targetType.BYTES <= type.BYTES) {
            // Target is smaller or equal: Get single element T and convert/truncate to R
            if (index >= size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
            return targetType.to(data[index])
        } else {
            // Target is larger: Merge multiple T elements
            val numParts = targetType.BYTES / type.BYTES
            if (targetType.BYTES % type.BYTES != 0) {
                throw IllegalArgumentException("Target type byte count (${targetType.BYTES}) must be a multiple of buffer element byte count (${type.BYTES}) for merging.")
            }
            val endIndex = index + numParts // Exclusive end index for subList
            if (endIndex > size) throw IndexOutOfBoundsException("Required elements exceed buffer size. Index: $index, Parts needed: $numParts, Size: $size")

            val partsToMerge = data.subList(index, endIndex)
            return mergePartsTo(partsToMerge, targetType)
        }
    }

    /**
     * Adds a value to the end of the buffer.
     * Handles splitting if the value is larger than T, or conversion if smaller/equal.
     *
     * @param value The IntNumber value to add.
     * @return `true` (conform to MutableCollection.add).
     */
    fun put(value: UnsignedFixedSizeIntNumber<*>): Boolean {
        if (value.byteCount <= type.BYTES) {
            // Smaller or equal size: Convert to T and add single element
            data.add(type.to(value))
        } else {
            // Larger size: Split into T elements and add all
            val parts = splitValue(value)
            data.addAll(parts)
        }
        return true
    }

    fun put(value: SignedFixedSizeIntNumber<*>): Boolean = put(value.toUnsigned())

    operator fun plusAssign(value: UnsignedFixedSizeIntNumber<*>) {
        put(value)
    }

    /**
     * Replaces elements starting at `index` with the given value.
     * Handles splitting if the value is larger than T, or conversion if smaller/equal.
     * If the value is larger, it overwrites multiple existing `T` elements.
     *
     * @param index The starting index in the buffer (element index, not byte offset).
     * @param value The IntNumber value to set.
     * @throws IndexOutOfBoundsException if the required indices are out of range.
     */
    operator fun set(index: Int, value: UnsignedFixedSizeIntNumber<*>) {
        if (index < 0) throw IndexOutOfBoundsException("Index cannot be negative: $index")

        if (value.byteCount <= type.BYTES) {
            // Smaller or equal size: Convert to T and set single element
            if (index >= size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
            data[index] = type.to(value)
        } else {
            // Larger size: Split into T elements and overwrite multiple elements
            val numParts = value.byteCount / type.BYTES
            if (value.byteCount % type.BYTES != 0) {
                throw IllegalArgumentException("Value byte count (${value.byteCount}) must be a multiple of buffer element byte count (${type.BYTES}) for splitting.")
            }
            val endIndex = index + numParts // Non-inclusive end index
            if (endIndex > size) throw IndexOutOfBoundsException("Cannot set value, exceeds buffer size. Index: $index, Parts needed: $numParts, Size: $size")

            val parts = splitValue(value)
            for (i in 0 until numParts) {
                data[index + i] = parts[i]
            }
        }
    }

    operator fun get(index: Int): T = data[index]

    // --- Specific Type Putters ---

    fun putBytes(bytes: ByteArray) {
        // How to add raw bytes depends heavily on T.
        // If T=Int8, add directly.
        // If T=UInt16, merge pairs of bytes, etc.
        // This is complex to generalize safely. Let's restrict or define behavior clearly.
        // Option 1: Only allow if T is Int8/UInt8
        if (type != Int8 && type != UInt8) {
            throw UnsupportedOperationException("putBytes is only directly supported for Int8Buffer or UInt8Buffer.")
        }
        // Option 2: Assume T=Int8/UInt8 equivalent behavior (potentially slow/complex)
        bytes.forEach { put(UInt8(it.toUByte())) } // Add byte by byte, letting 'put' handle conversion to T
    }

    operator fun plusAssign(bytes: ByteArray) {
        putBytes(bytes)
    }

    fun putAll(values: Collection<T>) {
        data.addAll(values)
    }

    fun putAll(values: Array<T>) {
        data.addAll(values)
    }

    operator fun set(index: Int, bytes: ByteArray) {
        // Similar complexity/restriction as putBytes
        if (type != Int8 && type != UInt8) {
            throw UnsupportedOperationException("setBytes is only directly supported for Int8Buffer or UInt8Buffer.")
        }
        if (index + bytes.size > size) {
            // This check assumes T is Int8/UInt8. If T is larger, the calculation is different.
            // Needs careful thought based on T's size if we generalize beyond Int8/UInt8.
            throw IndexOutOfBoundsException("Setting bytes would exceed buffer size.")
        }
        bytes.forEachIndexed { i, byte -> set(index + i, UInt8(byte.toUByte())) } // Set byte by byte
    }

    // --- MutableList<T> Implementation ---
    // Delegate standard MutableList methods to the internal `data` list

    override val size: Int get() = data.size
    override fun contains(element: T): Boolean = data.contains(element)
    override fun containsAll(elements: Collection<T>): Boolean = data.containsAll(elements)
    override fun isEmpty(): Boolean = data.isEmpty()
    override fun iterator(): MutableIterator<T> = data.iterator()

    // --- Utility / Conversion ---

    fun pad(length: Int) {
        if (length > 0) {
            repeat(length) {
                put(type.ZERO) // Add native zero type
            }
        }
    }

    fun getZeroTerminated(startIndex: Int): List<T> {
        if (startIndex < 0 || startIndex >= size) throw IndexOutOfBoundsException("startIndex: $startIndex, Size: $size")
        val result = mutableListOf<T>()
        var currentIndex = startIndex
        while (currentIndex < size) {
            val element = data[currentIndex]
            if (element == type.ZERO) {
                break
            }
            result.add(element)
            currentIndex++
        }
        return result
    }

    // toArray() might still need to be abstract or require reflection/reified types

    fun asList(): List<T> = data.toList() // Immutable view

    open fun dataAsString(index: Int, radix: Int = 10): String = get(index).toString(radix)

    fun mapAsString(radix: Int = 10): List<String> = List(size) { index -> dataAsString(index, radix) }

    override fun toString(): String = mapAsString(10).toString()

    // Optional: Equality and HashCode based on content
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Buffer<*>) return false // Compare with any Buffer type
        if (type != other.type) return false // Might relax this if content comparison is desired regardless of T
        if (data != other.data) return false
        return true
    }

    override fun hashCode(): Int {
        return 31 * type.hashCode() + data.hashCode()
    }
}