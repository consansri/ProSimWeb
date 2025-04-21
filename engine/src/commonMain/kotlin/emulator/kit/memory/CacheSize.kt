package emulator.kit.memory

enum class CacheSize(val bytes: Long, private val uiName: String) {
    KiloByte_1(1024, "1KB"),
    KiloByte_2(2048, "2KB"),
    KiloByte_4(4096,"4KB"),
    KiloByte_8(8192, "8KB"),
    KiloByte_16(16384,"16KB"),
    KiloByte_32(32768, "32KB"),
    KiloByte_64(65536, "64KB"),
    KiloByte_128(131072, "128KB"),
    KiloByte_256(262144, "256KB"),
    KiloByte_512(524288, "512KB"),
    MegaByte_1(1048576, "1MB");

    override fun toString(): String {
        return uiName
    }

    companion object{
        const val BYTECOUNT_IN_ROW = 16
    }

}