package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Int16

/**
 * A buffer for [Int16] values.
 *
 * This class is used to create a buffer that can store [Int16] values. It is
 * backed by a [MutableList] of [Int16] values and provides methods to interact with
 * the buffer.
 */
class Int16Buffer(endianness: Endianness, initial: Array<Int16> = emptyArray()) : Buffer<Int16>(endianness, Int16){

}