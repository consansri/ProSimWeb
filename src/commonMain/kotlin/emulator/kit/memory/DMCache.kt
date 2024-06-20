package emulator.kit.memory

import emulator.kit.common.IConsole
import kotlin.math.log
import kotlin.math.roundToInt

/**
 * Represents a direct-mapped cache implementation that extends the Cache class.
 *
 * @property backingMemory The backing memory for the cache.
 * @property console The console for logging messages.
 * @property tagBits The number of bits used for tag in the cache address.
 * @property rowBits The number of bits used for row in the cache address.
 * @property offsetBits The number of bits used for offset in the cache address.
 * @property name The name of the direct-mapped cache.
 *
 * @constructor Creates a DirectMappedCache with the specified parameters.
 *
 * @throws Exception if the combination of tag, row, and offset widths does not match the address size.
 *
 * @see Cache
 */
class DMCache(
    backingMemory: Memory,
    console: IConsole,
    val rowBits: Int,
    val offsetBits: Int,
    override val name: String = "Cache (DM)"
) : Cache(
    backingMemory,
    console,
    indexBits = rowBits,
    offsetBits = offsetBits,
    blockCount = 1,
    replaceAlgo = Model.ReplaceAlgo.RANDOM
){
    constructor(backingMemory: Memory, console: IConsole, cacheSize: CacheSize, name: String = "Cache"): this(
        backingMemory,
        console,
        log((cacheSize.bytes / CacheSize.BYTECOUNT_IN_ROW).toDouble(), 2.0).roundToInt(),
        log((CacheSize.BYTECOUNT_IN_ROW / backingMemory.instanceSize.getByteCount()).toDouble(),2.0).roundToInt(),
        "$name($cacheSize DM)"
    )
}