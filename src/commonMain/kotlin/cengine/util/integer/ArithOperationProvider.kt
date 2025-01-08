package cengine.util.integer

/**
 * Provides arithmetic operations for [IntNumber]s
 */
interface ArithOperationProvider<in T: IntNumber<*>, out U: IntNumber<*>> {

    operator fun plus(other: T): U
    operator fun minus(other: T): U
    operator fun times(other: T): U
    operator fun div(other: T): U
    operator fun rem(other: T): U

    operator fun unaryMinus(): U

    // Kotlin Int Operations
    operator fun plus(other: Int): U
    operator fun minus(other: Int): U
    operator fun times(other: Int): U
    operator fun div(other: Int): U
    operator fun rem(other: Int): U

    // Kotlin Long Operations
    operator fun plus(other: Long): U
    operator fun minus(other: Long): U
    operator fun times(other: Long): U
    operator fun div(other: Long): U
    operator fun rem(other: Long): U

}