package emulator.kit.memory

import cengine.util.newint.IntNumber
import kotlin.math.log
import kotlin.math.roundToInt

class SACache<ADDR: IntNumber<*>, INSTANCE: IntNumber<*>>(
    backingMemory: Memory<ADDR, INSTANCE>,
    rowBits: Int,
    offsetBits: Int,
    blockCount: Int,
    replaceAlgo: ReplaceAlgo,
    toAddr: IntNumber<*>.() -> ADDR,
    toInstance: IntNumber<*>.() -> INSTANCE,
    override val name: String = "Cache (SA)"
) : Cache<ADDR, INSTANCE>(
    backingMemory,
    rowBits,
    blockCount,
    offsetBits,
    replaceAlgo,
    toAddr,
    toInstance
) {
    constructor(backingMemory: Memory<ADDR, INSTANCE>,blockCount: Int, cacheSize: CacheSize, replaceAlgo: ReplaceAlgo, toAddr: IntNumber<*>.() -> ADDR, toInstance: IntNumber<*>.() -> INSTANCE, name: String = "Cache") : this(
        backingMemory,
        log(((cacheSize.bytes / CacheSize.BYTECOUNT_IN_ROW) / blockCount).toDouble(), 2.0).roundToInt(),
        log((CacheSize.BYTECOUNT_IN_ROW / backingMemory.init.byteCount).toDouble(), 2.0).roundToInt(),
         blockCount,
         replaceAlgo,
        toAddr,
        toInstance,
        name = "$name($cacheSize ${blockCount}SA ${replaceAlgo})"
    )
}