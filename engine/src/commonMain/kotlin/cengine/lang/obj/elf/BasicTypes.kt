package cengine.lang.obj.elf

import cengine.util.Endianness
import cengine.util.integer.*
import cengine.util.integer.Int8.Companion.toInt8
import cengine.util.integer.UInt16.Companion.toUInt16
import cengine.util.integer.UInt32.Companion.toUInt32
import cengine.util.integer.UInt64.Companion.toUInt64

typealias Elf32_Addr = UInt32
typealias Elf64_Addr = UInt64

typealias Elf32_Off = UInt32
typealias Elf64_Off = UInt64

typealias Elf_Byte = UInt8

typealias Elf_Half = UInt16

typealias Elf_Sword = Int32

typealias Elf_Word = UInt32

typealias Elf_Sxword = Int64

typealias Elf_Xword = UInt64

// Helper functions to ensure 4-byte alignment
fun alignTo4Int8s(value: Int): Int = (value + 3) and (-4)
fun alignTo4Int8s(value: Int64): Int64 = (value + 3L) and (-4L)

// Helper functions to load multiple bytes into other types
fun ByteArray.loadUInt8(index: Int): UInt8 = this[index].toInt8().toUInt8()

fun ByteArray.loadInt8(index: Int): Int8 = this[index].toInt8()

fun ByteArray.loadUInt16(endianness: Endianness, index: Int): UInt16{
    val bytes = this.copyOfRange(index, index + UInt16.BYTES)
    val result = if (endianness == Endianness.BIG) {
        bytes.joinToString("") { it.toInt8().toUInt8().zeroPaddedHex() }.toUShort(16)
    } else {
        bytes.reversed().joinToString("") { it.toInt8().toUInt8().zeroPaddedHex() }.toUShort(16)
    }
    return result.toUInt16()
}

fun ByteArray.loadUInt16(e_ident: E_IDENT, index: Int): UInt16 = loadUInt16(e_ident.endianness, index)

fun ByteArray.loadInt16(e_ident: E_IDENT, index: Int): Int16 = loadUInt16(e_ident, index).toInt16()

fun ByteArray.loadUInt32(endianness: Endianness, index: Int): UInt32 {
    val bytes = this.copyOfRange(index, index + UInt.SIZE_BYTES)
    val result = if (endianness == Endianness.BIG) {
        bytes.joinToString("") { it.toInt8().toUInt8().zeroPaddedHex() }.toUInt(16)
    } else {
        bytes.reversed().joinToString("") { it.toInt8().toUInt8().zeroPaddedHex() }.toUInt(16)
    }
    return result.toUInt32()
}

fun ByteArray.loadUInt32(e_ident: E_IDENT, index: Int): UInt32 = loadUInt32(e_ident.endianness, index)

fun ByteArray.loadInt32(e_ident: E_IDENT, index: Int): Int32 = this.loadUInt32(e_ident, index).toInt32()

fun ByteArray.loadUInt64(endianness: Endianness, index: Int): UInt64 {
    val bytes = this.copyOfRange(index, index + UInt64.BYTES)
    val result = if (endianness == Endianness.BIG) {
        bytes.joinToString("") { it.toInt8().toUInt8().zeroPaddedHex() }.toULong(16)
    } else {
        bytes.reversed().joinToString("") { it.toInt8().toUInt8().zeroPaddedHex() }.toULong(16)
    }
    return result.toUInt64()
}

fun ByteArray.loadUInt64(e_ident: E_IDENT, index: Int): UInt64 = loadUInt64(e_ident.endianness, index)

fun ByteArray.loadInt64(e_ident: E_IDENT, index: Int): Int64 = loadUInt64(e_ident, index).toInt64()


// Exceptions
object NotInELFFormatException : Exception("File is not in ELF Format!")
