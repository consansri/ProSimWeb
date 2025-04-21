package emulator.kit.memory

import androidx.compose.runtime.mutableStateMapOf
import cengine.util.Endianness
import cengine.util.integer.IntNumber
import cengine.util.integer.IntNumberStatic

/**
 * Represents the main memory of a system.
 *
 * @property addressSize The size of the memory addresses.
 * @property instanceSize The size of each memory instance.
 * @property endianness The endianess of the memory.
 * @property name The name of the memory.
 * @property initHex The initial binary value for the memory.
 * @property endianness The endianess of the memory.
 * @property memList The list of memory instances.
 * @property editableValues The list of editable memory values.
 * @property ioBounds The input/output bounds of the memory.
 * @property entrysInRow The number of entries in each row of memory.
 *
 * @constructor Creates a MainMemory instance with the specified parameters.
 *
 * @param addressSize The size of the memory addresses.
 * @param instanceSize The size of each memory instance.
 * @param endianness The endianess of the memory.
 * @param name The name of the memory.
 */
class MainMemory<ADDR : IntNumber<*>, INSTANCE : IntNumber<*>>(
    endianness: Endianness,
    addrType: IntNumberStatic<ADDR>,
    instanceType: IntNumberStatic<INSTANCE>,
    override val name: String = "Memory",
) : Memory<ADDR, INSTANCE>(
    addrType, instanceType
) {

    var endianness: Endianness = Endianness.BIG
    val memList = mutableStateMapOf<ADDR, INSTANCE>()

    init {
        this.endianness = endianness
    }

    override fun globalEndianess(): Endianness = endianness

    override fun loadInstance(address: ADDR, tracker: AccessTracker): INSTANCE {
        return memList[address] ?: init
    }

    override fun storeInstance(address: ADDR, value: INSTANCE, tracker: AccessTracker) {
        memList.remove(address)
        memList[address] = value
    }

    override fun clear() {
        memList.clear()
    }
}