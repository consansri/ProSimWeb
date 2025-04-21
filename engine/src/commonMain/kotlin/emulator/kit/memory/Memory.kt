package emulator.kit.memory

import cengine.util.Endianness
import cengine.util.integer.IntNumber
import cengine.util.integer.IntNumberStatic
import cengine.util.integer.UInt8

/**
 * Represents a Memory class that provides functionality for loading, storing, and managing memory instances.
 * This class is abstract and provides methods for accessing memory properties and operations.
 *
 * @property name: String, the name of the memory
 * @property addressSize: Size, the size of the memory address
 * @property instanceSize: Size, the size of each memory instance
 * @property initHex: String, the initial binary value of the memory
 *
 * Methods:
 * - globalEndianess(): Endianess, returns the global endianess of the memory
 * - load(address: Value): Value, loads the value at the specified memory address
 * - store(address: Value, value: Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false): Unit,
 *   stores the value at the specified memory address with optional parameters for marking and readonly
 * - clear(): Unit, clears the memory
 * - store(address: Value, variable: Variable, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false): Unit,
 *   stores the value of the variable at the specified memory address with optional parameters for marking and readonly
 * - storeArray(address: Value, vararg values: Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false): Unit,
 *   stores an array of values starting at the specified memory address with optional parameters for marking and readonly
 * - load(address: Value, amount: Int): Bin, loads a specified amount of values starting at the memory address
 * - loadArray(address: Value, amount: Int): Array<Bin>, loads an array of values starting at the memory address
 * - getInitialBinary(): Variable, returns the initial binary value of the memory
 *
 * Nested Classes:
 * -  MemoryException: Exception, represents an exception specific to the Memory class
 *
 * Enums:
 * - Endianess: Represents the endianess of the memory (LittleEndian, BigEndian)
 * - InstanceType: Represents the type of memory instance with light and dark color values
 */
sealed class Memory<ADDR : IntNumber<*>, INSTANCE : IntNumber<*>>(
    val addrType: IntNumberStatic<ADDR>,
    val instanceType: IntNumberStatic<INSTANCE>,
) {

    abstract val name: String
    open val init: INSTANCE
        get() = instanceType.ZERO

    abstract fun globalEndianess(): Endianness

    protected fun IntNumber<*>.addr(): ADDR = addrType.to(this)
    protected fun IntNumber<*>.instance(): INSTANCE = instanceType.to(this)

    /**
     *
     */
    abstract fun loadInstance(address: ADDR, tracker: AccessTracker = AccessTracker()): INSTANCE
    abstract fun storeInstance(address: ADDR, value: INSTANCE, tracker: AccessTracker = AccessTracker())
    abstract fun clear()

    /**
     * @param byteAmount The amount of bytes which should be loaded.
     * @param tracker Only the first access will be tracked!
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

        val bytes = if (globalEndianess() == Endianness.LITTLE) {
            instances.reversed().flatMap { it.uInt8s() }
        } else {
            instances.flatMap { it.uInt8s() }
        }

        return bytes
    }

    /**
     * Aligned Store
     *
     * @param
     */
    fun storeEndianAware(address: IntNumber<*>, value: IntNumber<*>, tracker: AccessTracker = AccessTracker()) {
        val amount = value.byteCount / init.byteCount
        val alignedAddr = addrType.to(address - (address % amount).toInt())
        val instances = if (globalEndianess() == Endianness.LITTLE) {
            instanceType.split(value).reversed()
        } else {
            instanceType.split(value)
        }

        //cengine.console.SysOut.log("StoreEndianAware $address: $value -> $alignedAddr: $instances")

        storeInstanceArray(alignedAddr, instances, tracker)
    }

    fun storeArray(address: IntNumber<*>, values: Collection<IntNumber<*>>, tracker: AccessTracker = AccessTracker()) {
        var currAddr: IntNumber<*> = address
        for (value in values) {
            storeEndianAware(currAddr, value, tracker)
            currAddr += value.byteCount / init.byteCount
        }
    }

    fun storeInstanceArray(address: ADDR, values: Collection<INSTANCE>, tracker: AccessTracker = AccessTracker()) {
        var curraddr: IntNumber<*> = address
        for (value in values) {
            storeInstance(addrType.to(curraddr), value, tracker)
            curraddr++
        }
    }

    fun loadArray(address: IntNumber<*>, amount: Int, tracker: AccessTracker = AccessTracker()): List<INSTANCE> {
        val instances = mutableListOf<INSTANCE>()

        var instanceAddress: IntNumber<*> = address
        for (i in 0..<amount) {
            val value = loadInstance(addrType.to(instanceAddress), tracker)
            instances.add(value)
            instanceAddress = instanceAddress.inc()
        }

        return instances.toList()
    }

    class MemoryException(override val message: String) : Exception()

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