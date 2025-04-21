package cengine.lang.mif

import cengine.util.integer.IntNumber

enum class MifRadix(val radix: Int) {
    HEX(16){
        override fun format(number: IntNumber<*>): String = number.zeroPaddedHex()
    },
    OCT(8) {
        override fun format(number: IntNumber<*>): String = number.toString(8)
    },
    BIN(2) {
        override fun format(number: IntNumber<*>): String = number.zeroPaddedBin()
    },
    DEC(10) {
        override fun format(number: IntNumber<*>): String = number.toString()
    };

    abstract fun format(number: IntNumber<*>): String

    companion object {
        fun getRadix(string: String): MifRadix {
            return entries.firstOrNull { it.name == string.uppercase() } ?: HEX
        }
    }
}