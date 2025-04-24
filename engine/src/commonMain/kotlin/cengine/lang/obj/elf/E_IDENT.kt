package cengine.lang.obj.elf

import cengine.lang.obj.elf.Ehdr.Companion.EV_CURRENT
import cengine.util.Endianness
import cengine.util.buffer.Buffer8
import cengine.util.integer.Int8
import cengine.util.integer.UInt8
import cengine.util.integer.UInt8.Companion.toUInt8

/**
 * @param ei_mag0 File identification
 * @param ei_mag1 File identification
 * @param ei_mag2 File identification
 * @param ei_mag3 File identification
 * @param ei_class File class
 * @param ei_data Data encoding
 * @param ei_version File version
 * @param ei_osabi
 * @param ei_abiversion
 * @param ei_pad Start of padding bytes
 * @param ei_nident Size of [E_IDENT.build]
 */
data class E_IDENT(
    val ei_mag0: Elf_Byte = ELFMAG0,
    val ei_mag1: Elf_Byte = ELFMAG1,
    val ei_mag2: Elf_Byte = ELFMAG2,
    val ei_mag3: Elf_Byte = ELFMAG3,
    val ei_class: Elf_Byte,
    val ei_data: Elf_Byte,
    val ei_version: Elf_Byte = EV_CURRENT.toUInt8(),
    val ei_osabi: Elf_Byte = ZERO,
    val ei_abiversion: Elf_Byte = ZERO,
    val ei_pad: Elf_Byte = ZERO,
    val ei_nident: Elf_Byte = EI_NIDENT
) : BinaryProvider {

    val endianness: Endianness get() = when(ei_data){
        ELFDATA2MSB -> Endianness.BIG
        else -> Endianness.LITTLE
    }

    companion object {

        fun getOsAbi(ei_osabi: Elf_Byte): String{
            return when(ei_osabi){
                ELFOSABI_SYSV -> "UNIX - System V"
                else -> "?"
            }
        }

        fun getElfClass(ei_class: Elf_Byte): String{
            return when(ei_class){
                ELFCLASS32 -> "ELF32"
                ELFCLASS64 -> "ELF64"
                ELFCLASSNONE -> "NONE"
                else -> "INVALID"
            }
        }

        fun getElfData(ei_data: Elf_Byte): String{
            return when(ei_data){
                ELFDATA2MSB -> "2's complement, big endian"
                ELFDATA2LSB -> "2's complement, little endian"
                ELFDATANONE -> "NONE"
                else -> "INVALID"
            }
        }

        val ZERO: Elf_Byte = 0U.toUInt8()

        val EI_NIDENT: Elf_Byte = 16U.toUInt8()

        val ELFMAG0: Elf_Byte = 0x7fU.toUInt8()

        /**
         * E in 7 bit ascii
         */
        val ELFMAG1: Elf_Byte = 0x45U.toUInt8()

        /**
         * L in 7 bit ascii
         */
        val ELFMAG2: Elf_Byte = 0x4CU.toUInt8()

        /**
         * F in 7 bit ascii
         */
        val ELFMAG3: Elf_Byte = 0x46U.toUInt8()

        /**
         * Invalid class
         */
        val ELFCLASSNONE: Elf_Byte = 0U.toUInt8()

        /**
         * 32-bit objects
         */
        val ELFCLASS32: Elf_Byte = 1U.toUInt8()

        /**
         * 64-bit objects
         */
        val ELFCLASS64: Elf_Byte = 2U.toUInt8()

        /**
         * Invalid data encoding
         */
        val ELFDATANONE: Elf_Byte = 0U.toUInt8()

        /**
         * Least significant byte occupying the lowest address.
         */
        val ELFDATA2LSB: Elf_Byte = 1U.toUInt8()

        /**
         * The most significant byte occupying the lowest address.
         */
        val ELFDATA2MSB: Elf_Byte = 2U.toUInt8()

        /**
         * This indicates that the ELF file follows the System V ABI.
         */
        val ELFOSABI_SYSV: Elf_Byte = 0U.toUInt8()

        fun extractFrom(byteArray: ByteArray): E_IDENT {
            val ei_mag0 = byteArray.getOrNull(0)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException
            if (ei_mag0 != ELFMAG0) throw NotInELFFormatException

            val ei_mag1 = byteArray.getOrNull(1)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException
            if (ei_mag1 != ELFMAG1) throw NotInELFFormatException

            val ei_mag2 = byteArray.getOrNull(2)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException
            if (ei_mag2 != ELFMAG2) throw NotInELFFormatException

            val ei_mag3 = byteArray.getOrNull(3)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException
            if (ei_mag3 != ELFMAG3) throw NotInELFFormatException

            val ei_class = byteArray.getOrNull(4)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException
            if (!(ei_class == ELFCLASSNONE || ei_class == ELFCLASS32 || ei_class == ELFCLASS64)) throw Exception("Incompatible ELF Class $ei_class!")

            val ei_data = byteArray.getOrNull(5)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException
            if (!(ei_data == ELFDATANONE || ei_data == ELFDATA2MSB || ei_data == ELFDATA2LSB)) throw Exception("Incompatible ELF Data $ei_class!")

            val ei_version = byteArray.getOrNull(6)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException
            if (ei_version != EV_CURRENT.toUInt8()) throw Exception("Invalid ELF Version $ei_version!")

            val ei_osabi = byteArray.getOrNull(7)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException

            val ei_abiversion = byteArray.getOrNull(8)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException

            val ei_pad = byteArray.getOrNull(9)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException

            var paddIndex = 10
            while (byteArray.getOrNull(paddIndex) == 0.toByte()) {
                paddIndex++
            }
            val ei_nident = byteArray.getOrNull(paddIndex)?.toUByte()?.toUInt8() ?: throw NotInELFFormatException

            return E_IDENT(
                ei_mag0,
                ei_mag1,
                ei_mag2,
                ei_mag3,
                ei_class,
                ei_data,
                ei_version,
                ei_osabi,
                ei_abiversion,
                ei_pad,
                ei_nident
            )
        }

    }

    override fun build(endianness: Endianness): Array<UInt8> {
        val b = Buffer8(endianness)

        b.put(ei_mag0)
        b.put(ei_mag1)
        b.put(ei_mag2)
        b.put(ei_mag3)
        b.put(ei_class)
        b.put(ei_data)
        b.put(ei_version)
        b.put(ei_osabi)
        b.put(ei_abiversion)
        b.put(ei_pad)
        try {
            b.putBytes(ByteArray(ei_nident.toInt() - b.size) {
                ZERO.toByte()
            })
        }catch (e: Exception){
            b.putBytes(ByteArray(EI_NIDENT.toInt() - b.size){
                ZERO.toByte()
            })
        }

        return b.toTypedArray()
    }

    override fun byteSize(): Int = ei_nident.toInt()

    override fun toString(): String {
        return "ELF Class:${getElfClass(ei_class)} Data:${getElfData(ei_data)} Version:$ei_version"
    }
}