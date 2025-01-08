package cengine.psi.core

/**
 * Intervals represent a range of offsets in a file.
 * They are used for annotating the PSI tree.
 */
interface Interval {
    val range: IntRange
}