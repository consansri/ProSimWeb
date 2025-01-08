package cengine.util.integer

/**
 * Provides unsigned operations for [IntNumber]s
 */
interface UnsignedExtension {

    operator fun compareTo(other: UInt): Int
    operator fun compareTo(other: ULong): Int

}