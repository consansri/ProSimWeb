package emulator.kit.memory

import cengine.lang.asm.AsmBinaryProvider
import cengine.util.Endianness
import cengine.util.integer.FixedSizeIntNumber
import cengine.util.integer.FixedSizeIntNumberT
import cengine.util.integer.IntNumber
import cengine.util.integer.IntNumberT
import cengine.util.integer.UInt8
import cengine.util.integer.UnsignedFixedSizeIntNumber
import cengine.util.integer.UnsignedFixedSizeIntNumberT

/**
 * Represents an abstract memory component within the processor emulator, such as main memory or a cache level.
 *
 * Provides a common interface and base functionality for loading and storing data, handling endianness,
 * and tracking access statistics. Concrete implementations (like `MassMemory` or `Cache`) must provide
 * implementations for instance-level access (`loadInstance`, `storeInstance`).
 *
 * @param ADDR The integer type used for memory addresses (e.g., UInt32).
 * @param INSTANCE The integer type representing a single fetchable/storable memory unit (e.g., UInt64 for 64-bit memory bus).
 * @property addrType Companion object/static context for the address type [ADDR]. Used for type conversions.
 * @property instanceType Companion object/static context for the instance type [INSTANCE]. Used for type conversions and properties like byte count.
 */
sealed class Memory<ADDR : UnsignedFixedSizeIntNumber<ADDR>, INSTANCE : UnsignedFixedSizeIntNumber<INSTANCE>>(
    val addrType: UnsignedFixedSizeIntNumberT<ADDR>,
    val instanceType: UnsignedFixedSizeIntNumberT<INSTANCE>,
) {

    /** The descriptive name of this memory component (e.g., "L1 Data Cache", "Main Memory"). */
    abstract val name: String

    /** The default initial value for memory instances if not otherwise specified. Defaults to zero. */
    open val init: INSTANCE
        get() = instanceType.ZERO

    /** Specifies the endianness (Little or Big Endian) used when loading/storing multi-byte values. */
    abstract fun globalEndianness(): Endianness

    // --- Type Conversion Helpers ---

    /** Converts a generic [IntNumber] to this memory's address type [ADDR]. */
    protected fun IntNumber<*>.addr(): ADDR = addrType.to(this)

    /** Converts a generic [IntNumber] to this memory's instance type [INSTANCE]. */
    protected fun IntNumber<*>.instance(): INSTANCE = instanceType.to(this)

    // --- Abstract Core Operations (to be implemented by subclasses) ---

    /**
     * Loads a single memory instance at the specified address.
     * This is the fundamental read operation that concrete memory types must implement.
     *
     * @param address The exact address of the instance to load.
     * @param tracker Optional tracker to record access statistics (hits, misses).
     * @return The memory instance value at the given address.
     */
    abstract fun loadInstance(address: ADDR, tracker: AccessTracker = AccessTracker()): INSTANCE

    /**
     * Stores a single memory instance at the specified address.
     * This is the fundamental write operation that concrete memory types must implement.
     *
     * @param address The exact address where the instance should be stored.
     * @param value The instance value to store.
     * @param tracker Optional tracker to record access statistics.
     */
    abstract fun storeInstance(address: ADDR, value: INSTANCE, tracker: AccessTracker = AccessTracker())

    /**
     * Resets the memory component to its initial state (e.g., clears caches, resets main memory).
     */
    abstract fun clear()

    /**
     * Loads a sequence of bytes starting from a given address, respecting the memory's [globalEndianess].
     * This may involve loading multiple underlying memory instances.
     *
     * **Alignment:** Loads the necessary instances containing the requested bytes, potentially starting
     * from an aligned address lower than the requested `startAddress`.
     * **Tracking:** Only the *first* underlying `loadInstance` call within this operation will update the [tracker].
     *
     * @param address The starting byte address for the load.
     * @param byteAmount The total number of bytes to load.
     * @param tracker Optional tracker for access statistics.
     * @return A list of [UInt8] bytes representing the loaded data in the correct order.
     * @throws IllegalArgumentException if byteAmount is not positive.
     */
    fun loadEndianAwareBytes(address: ADDR, byteAmount: Int, tracker: AccessTracker = AccessTracker()): List<UInt8> {
        val amount = byteAmount / init.byteCount

        val alignedAddr = address - (address % amount).toInt()

        val instances = (0..<amount).map {
            if (it == 0) {
                loadInstance(addrType.to(alignedAddr + it), tracker)
            } else {
                loadInstance(addrType.to(alignedAddr + it))
            }
        }

        val bytes = if (globalEndianness() == Endianness.LITTLE) {
            instances.reversed().flatMap { it.uInt8s() }
        } else {
            instances.flatMap { it.uInt8s() }
        }

        return bytes
    }

    /**
     * Stores a value (represented by an [IntNumber]) at a given address, respecting the memory's [globalEndianess].
     * The value is split into appropriate memory instances before storing.
     *
     * **Alignment:** The store operation targets the instances containing the bytes of the value,
     * potentially starting at an aligned address.
     * **Tracking:** All underlying `storeInstance` calls within this operation potentially update the [tracker].
     *
     * @param address The starting byte address for the store.
     * @param value The value to store. Its size must be a multiple of [instanceByteCount].
     * @param tracker Optional tracker for access statistics.
     * @throws IllegalArgumentException if the value's byte count is not compatible.
     */
    fun storeEndianAwareValue(address: IntNumber<*>, value: FixedSizeIntNumber<*>, tracker: AccessTracker = AccessTracker()) {
        val amount = value.byteCount / init.byteCount
        val alignedAddr = addrType.to(address - (address % amount).toInt())

        storeInstances(alignedAddr, splitValueToInstances(value), tracker)
    }

    /**
     * Splits a generic [IntNumber] value into a list of [INSTANCE]s based on [globalEndianess].
     */
    private fun splitValueToInstances(value: FixedSizeIntNumber<*>): List<INSTANCE> {
        // instanceType.split is assumed to split value into chunks of instanceType size
        val instances = instanceType.split(value) // Ensure correct type
        return if (globalEndianness() == Endianness.LITTLE) {
            instances.reversed() // Little Endian: Store least significant instance first
        } else {
            instances // Big Endian: Store most significant instance first
        }
    }

    // --- Bulk Operations ---

    /**
     * Stores a collection of values sequentially starting at the given address.
     * Handles endianness for each value.
     *
     * @param address The starting address for the first value.
     * @param values The collection of [IntNumber] values to store.
     * @param tracker Optional tracker for access statistics.
     */
    fun storeValues(address: IntNumber<*>, values: Collection<FixedSizeIntNumber<*>>, tracker: AccessTracker = AccessTracker()) {
        var currAddr: IntNumber<*> = address
        for (value in values) {
            storeEndianAwareValue(currAddr, value, tracker)
            currAddr += value.byteCount / instanceType.BYTES
        }
    }

    /**
     * Stores a collection of pre-formatted memory [INSTANCE]s sequentially.
     * This assumes the instances are already in the correct order for storage.
     *
     * @param address The address of the first instance.
     * @param instances The collection of [INSTANCE]s to store.
     * @param tracker Optional tracker for access statistics.
     */
    fun storeInstances(address: ADDR, instances: Collection<INSTANCE>, tracker: AccessTracker = AccessTracker()) {
        var curraddr: IntNumber<*> = address
        for (value in instances) {
            storeInstance(addrType.to(curraddr), value, tracker)
            curraddr++
        }
    }

    /**
     * Loads a specific number of memory [INSTANCE]s sequentially.
     *
     * @param address The address of the first instance to load.
     * @param amount The number of instances to load.
     * @param tracker Optional tracker for access statistics (tracks *each* instance load).
     * @return A list containing the loaded [INSTANCE]s.
     */
    fun loadInstances(address: IntNumber<*>, amount: Int, tracker: AccessTracker = AccessTracker()): List<INSTANCE> {
        val instances = mutableListOf<INSTANCE>()

        var instanceAddress: IntNumber<*> = address
        for (i in 0..<amount) {
            val value = loadInstance(addrType.to(instanceAddress), tracker)
            instances.add(value)
            instanceAddress = instanceAddress.inc()
        }

        return instances.toList()
    }

    fun initialize(provider: AsmBinaryProvider) {
        val sections = provider.contents()
        for ((address, content) in sections.entries) {
            val values = content.first
            if (values.isEmpty()) continue
            if (values.size == 1 && values[0] == values[0].type.ZERO) {
                continue
            }
            storeValues(
                address,
                values
            )
        }
    }

    // --- Utility Classes ---

    /** Custom exception for memory-related errors. */
    class MemoryException(override val message: String) : Exception()

    /**
     * Data class to track memory access statistics.
     *
     * @property hits Number of accesses that were resolved by this memory level (e.g., cache hit).
     * @property misses Number of accesses that required fetching from a lower level (e.g., cache miss).
     * @property writeBacks Number of times data was written back to a lower level (e.g., dirty cache line eviction).
     */
    data class AccessTracker(
        var hits: Int = 0,
        var misses: Int = 0,
        var writeBacks: Int = 0,
    ) {
        override fun toString(): String {
            return "$hits HITS and $misses MISSES (with $writeBacks write backs)"
        }
    }
}