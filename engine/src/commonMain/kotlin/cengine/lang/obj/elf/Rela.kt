package cengine.lang.obj.elf

import cengine.util.integer.UInt16.Companion.toUInt16

sealed class Rela : BinaryProvider {

    companion object {
        fun size(ei_class: Elf_Byte): Elf_Half {
            return when(ei_class){
                E_IDENT.ELFCLASS32 -> ELF32_Rela.SIZE.toUInt16()
                E_IDENT.ELFCLASS64 -> ELF64_Rela.SIZE.toUInt16()
                else -> throw ELFGenerator.InvalidElfClassException(ei_class)
            }
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Rela {
            var currIndex = offset
            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val r_offset = byteArray.loadUInt32(eIdent, currIndex)
                    currIndex += 4
                    val r_info = byteArray.loadUInt32(eIdent, currIndex)
                    currIndex += 4
                    val r_addend = byteArray.loadInt32(eIdent, currIndex)
                    return ELF32_Rela(r_offset, r_info, r_addend)
                }

                E_IDENT.ELFCLASS64 -> {
                    val r_offset = byteArray.loadUInt64(eIdent, currIndex)
                    currIndex += 8
                    val r_info = byteArray.loadUInt64(eIdent, currIndex)
                    currIndex += 8
                    val r_addend = byteArray.loadInt64(eIdent, currIndex)
                    return ELF64_Rela(r_offset, r_info, r_addend)
                }

                else -> throw NotInELFFormatException
            }
        }
    }

}