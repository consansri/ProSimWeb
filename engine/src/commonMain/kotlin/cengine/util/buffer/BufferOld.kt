package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.*
import cengine.util.integer.Int8.Companion.toInt8

/**
 * Base class for mutable buffers holding elements of type [T] which is a subtype of [IntNumber].
 * Provides basic list-like operations and utility functions.
 *
 * @param T The specific type of IntNumber stored in this buffer.
 * @property endianness The endianness hint for potential byte-level operations (primarily used by subclasses).
 * @property type Static information and operations for type T.
 */
abstract class BufferOld<T : IntNumber<*>>(endianness: Endianness, val type: IntNumberStatic<T>) : Collection<T> {

    protected val data: MutableList<T> = mutableListOf()

    var endianness: Endianness = endianness
        private set

    override val size: Int get() = data.size
    override fun isEmpty(): Boolean = data.isEmpty()
    override fun contains(element: T): Boolean = data.contains(element)
    override fun containsAll(elements: Collection<T>): Boolean = data.containsAll(elements)
    override fun iterator(): Iterator<T> = data.iterator()

    // -------------------------- CONVERSION

    abstract fun toArray(): Array<T>
    fun asList(): List<T> = data.toList()

    // -------------------------- GET

    operator fun get(index: Int): T = data[index]

    // BYTE

    fun getInt8(index: Int): Int8 = getUInt8(index).toInt8()

    abstract fun getUInt8(index: Int): UInt8

    // SHORT
    fun getInt16(index: Int): Int16 = getUInt16(index).toInt16()

    abstract fun getUInt16(index: Int): UInt16

    // INT

    fun getInt32(index: Int): Int32 = getUInt32(index).toInt32()

    abstract fun getUInt32(index: Int): UInt32

    // LONG

    fun getInt64(index: Int): Int64 = getUInt64(index).toInt64()

    abstract fun getUInt64(index: Int): UInt64

    // STRING

    fun getZeroTerminated(index: Int): List<T> {
        val result = mutableListOf<T>()
        var currentIndex = index

        while (currentIndex < size && this[currentIndex] != type.ZERO) {
            result.add(get(currentIndex))
            currentIndex++
        }

        return result
    }

    // -------------------------- PUT

    fun pad(length: Int) {
        data.addAll(List(length) { type.ZERO })
    }

    // BYTEARRAY

    fun putBytes(bytes: ByteArray) {
        putInt8s(bytes.map { it.toInt8() }.toTypedArray())
    }

    fun putAll(values: Array<T>) {
        data.addAll(values)
    }

    fun putAll(values: Collection<T>) {
        data.addAll(values)
    }

    abstract fun putUInt8s(bytes: Array<UInt8>)

    fun putInt8s(bytes: Array<Int8>) {
        putUInt8s(bytes.map { it.toUInt8() }.toTypedArray())
    }

    // BYTE

    fun put(value: Int8) {
        put(value.toUInt8())
    }

    abstract fun put(value: UInt8)

    // SHORT

    fun put(value: Int16) {
        put(value.toUInt16())
    }

    abstract fun put(value: UInt16)

    // INT

    fun put(value: Int32) {
        put(value.toUInt32())
    }

    abstract fun put(value: UInt32)

    // LONG

    fun put(value: Int64) {
        put(value.toUInt64())
    }

    abstract fun put(value: UInt64)

    // BigInt

    fun put(value: BigInt) = putBytes(value.value.toByteArray())

    // -------------------------- SET

    // ByteArray

    fun setBytes(index: Int, bytes: ByteArray) {
        setAll(index, bytes.map { type.to(it.toInt8()) })
    }

    fun setAll(index: Int, values: Collection<T>) {
        data.addAll(index, values)
    }

    fun setUInt8s(index: Int, bytes: Array<UInt8>) {
        setAll(index, bytes.map { type.to(it) })
    }

    fun setInt8s(index: Int, bytes: Array<Int8>) {
        setUInt8s(index, bytes.map { it.toUInt8() }.toTypedArray())
    }

    // Byte

    operator fun set(index: Int, value: Int8) {
        this[index] = value.toUInt8()
    }

    abstract operator fun set(index: Int, value: UInt8)

    // Short

    operator fun set(index: Int, value: Int16) {
        this[index] = value.toUInt16()
    }

    abstract operator fun set(index: Int, value: UInt16)

    // Int

    operator fun set(index: Int, value: Int32) {
        this[index] = value.toUInt32()
    }

    abstract operator fun set(index: Int, value: UInt32)

    // Long

    operator fun set(index: Int, value: Int64) {
        this[index] = value.toUInt64()
    }

    abstract operator fun set(index: Int, value: UInt64)

    // BigInt

    operator fun set(index: Int, value: BigInt) = setBytes(index, value.value.toByteArray())

    // toString

    abstract fun dataAsString(index: Int, radix: Int): String

    fun mapAsString(radix: Int) = indices.map { index -> dataAsString(index, radix) }
}