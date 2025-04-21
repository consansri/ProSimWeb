package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Int64

/**
 * A buffer for [Int64] values.
 *
 * This class is used to create a buffer that can store [Int64] values. It is
 * backed by a [MutableList] of [Int64] values and provides methods to interact with
 * the buffer.
 */
class Int64Buffer(endianness: Endianness, initial: Array<Int64> = emptyArray()) : Buffer<Int64>(endianness, Int64){

}