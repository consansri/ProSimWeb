package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Int32
import cengine.util.integer.UInt32

/**
 * A buffer for [UInt32] values.
 *
 * This class is used to create a buffer that can store [UInt32] values. It is
 * backed by a [MutableList] of [UInt32] values and provides methods to interact with
 * the buffer.
 */
class Buffer32(endianness: Endianness) : Buffer<UInt32>(endianness, UInt32){

}