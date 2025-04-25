package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.UInt16

/**
 * A buffer for [UInt16] values.
 *
 * This class is used to create a buffer that can store [UInt16] values. It is
 * backed by a [MutableList] of [UInt16] values and provides methods to interact with
 * the buffer.
 */
class Buffer16(endianness: Endianness) : Buffer<UInt16>(endianness, UInt16){

}