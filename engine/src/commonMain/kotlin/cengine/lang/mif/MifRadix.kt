package cengine.lang.mif

enum class MifRadix(val base: Int) {
    HEX(16),
    OCT(8),
    BIN(2),
    DEC(10),
    UNS(10);

    companion object {
        fun fromString(string: String): MifRadix? {
            return entries.firstOrNull { it.name == string.uppercase() }
        }
    }
}