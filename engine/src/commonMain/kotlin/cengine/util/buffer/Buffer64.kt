package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.UInt64

/**
 * A buffer for [UInt64] values.
 *
 * This class is used to create a buffer that can store [UInt64] values. It is
 * backed by a [MutableList] of [UInt64] values and provides methods to interact with
 * the buffer.
 */
class Buffer64(endianness: Endianness) : Buffer<UInt64>(endianness, UInt64){

}