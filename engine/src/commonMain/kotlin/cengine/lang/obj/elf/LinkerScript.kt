package cengine.lang.obj.elf

import cengine.util.integer.BigInt
import cengine.util.integer.UInt64

interface LinkerScript {

    val textStart: BigInt?
    val dataStart: BigInt?
    val rodataStart: BigInt?
    val segmentAlign: UInt64

}