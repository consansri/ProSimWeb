package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Int32

/**
 * A buffer for [Int32] values.
 *
 * This class is used to create a buffer that can store [Int32] values. It is
 * backed by a [MutableList] of [Int32] values and provides methods to interact with
 * the buffer.
 */
class Int32Buffer(endianness: Endianness, initial: Array<Int> = emptyArray()) : Buffer<Int32>(endianness, Int32){

}