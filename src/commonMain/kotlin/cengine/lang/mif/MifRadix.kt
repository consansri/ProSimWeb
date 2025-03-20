package cengine.lang.mif

enum class MifRadix(val radix: Int) {
    HEX(16),
    OCT(8),
    BIN(2),
    DEC(10);

    companion object {
        fun getRadix(string: String): MifRadix {
            return entries.firstOrNull { it.name == string.uppercase() } ?: HEX
        }
    }
}