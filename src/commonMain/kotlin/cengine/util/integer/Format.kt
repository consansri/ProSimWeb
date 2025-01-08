package cengine.util.integer

import com.ionspin.kotlin.bignum.integer.BigInteger


/**
 * Radix and its associated format.
 */
enum class Format(val radix: Int) {

    /**
     * Binary (base 2)
     */
    BIN(2) {
        override fun format(number: IntNumber<*>): String = number.zeroPaddedBin()
        override fun valid(char: Char): Boolean = when (char) {
            '1', '0' -> true
            else -> false
        }
    },
    /**
     * Octal (base 8)
     */
    OCT(8) {
        override fun format(number: IntNumber<*>): String = number.toString(8)
        override fun valid(char: Char): Boolean = when (char) {
            in '0'..'7' -> true
            else -> false
        }
    },
    /**
     * Decimal (base 10)
     */
    DEC(10) {
        override fun format(number: IntNumber<*>): String = number.toString()
        override fun valid(char: Char): Boolean = char.isDigit()
    },
    /**
     * Hexadecimal (base 16)
     */
    HEX(16) {
        override fun format(number: IntNumber<*>): String = number.zeroPaddedHex()
        override fun valid(char: Char): Boolean = when (char) {
            in '0'..'9', 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f' -> true
            else -> false
        }
    };

    /**
     * Get the next format.
     */
    fun next(): Format {
        val length = entries.size
        val currIndex = entries.indexOf(this)
        val nextIndex = (currIndex + 1) % length
        return entries[nextIndex]
    }

    /**
     * Get a string representing the given number in this format.
     */
    abstract fun format(number: IntNumber<*>): String

    /**
     * Check if the given character is valid in this format.
     */
    abstract fun valid(char: Char): Boolean

    /**
     * Filter the given string to only include characters that are valid in this format.
     */
    fun filter(string: String): String = string.filter { valid(it) }

    /**
     * Parse the given string into a number in this format.
     */
    fun parse(string: String): IntNumber<*> = BigInt(BigInteger.parseString(string, radix))

}