package emulator.kit.memory

import cengine.util.integer.IntNumber
import kotlin.math.log2
import kotlin.math.roundToInt

/**
 * Represents a direct-mapped cache implementation that extends the Cache class.
 *
 * @property backingMemory The backing memory for the cache.
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
class DMCache<ADDR : IntNumber<*>, INSTANCE : IntNumber<*>>(
    backingMemory: Memory<ADDR, INSTANCE>,
    rowBits: Int,
    offsetBits: Int,
    override val name: String = "Cache (DM)",
) : Cache<ADDR, INSTANCE>(
    backingMemory,
    rowBits,
    1,
    offsetBits,
    ReplaceAlgo.RANDOM,
) {
    constructor(backingMemory: Memory<ADDR, INSTANCE>, cacheSize: CacheSize, name: String = "Cache") : this(
        backingMemory,
        log2((cacheSize.bytes / CacheSize.BYTECOUNT_IN_ROW).toDouble()).roundToInt(),
        log2((CacheSize.BYTECOUNT_IN_ROW / backingMemory.init.byteCount).toDouble()).roundToInt(),
        "$name($cacheSize DM)"
    )
}