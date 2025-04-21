package cengine.lang.obj.elf

import cengine.util.integer.Int64.Companion.toInt64

/**
 * Data class representing the Elf32_Dyn structure in the ELF format.
 *
 * @property d_tag The dynamic table entry type.
 * @property d_val The integer value associated with the dynamic table entry.
 * @property d_ptr The address associated with the dynamic table entry.
 */
sealed class Dyn : BinaryProvider {

    companion object {
        fun getDynamicType(tag: Elf_Sxword): String = when (tag) {
            DT_NULL -> "NULL"
            DT_NEEDED -> "NEEDED"
            DT_PLTRELSZ -> "PLTRELSZ"
            DT_PLTGOT -> "PLTGOT"
            DT_HASH -> "HASH"
            DT_STRTAB -> "STRTAB"
            DT_SYMTAB -> "SYMTAB"
            DT_RELA -> "RELA"
            DT_RELASZ -> "RELASZ"
            DT_RELAENT -> "RELAENT"
            DT_STRSZ -> "STRSZ"
            DT_SYMENT -> "SYMENT"
            DT_INIT -> "INIT"
            DT_FINI -> "FINI"
            DT_SONAME -> "SONAME"
            DT_RPATH -> "RPATH"
            DT_SYMBOLIC -> "SYMBOLIC"
            DT_REL -> "REL"
            DT_RELSZ -> "RELSZ"
            DT_RELENT -> "RELENT"
            DT_PLTREL -> "PLTREL"
            DT_DEBUG -> "DEBUG"
            DT_TEXTREL -> "TEXTREL"
            DT_JMPREL -> "JMPREL"
            DT_BIND_NOW -> "BIND_NOW"
            else -> "UNKNOWN (${tag})"
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Dyn {
            var currIndex = offset
            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val d_tag = byteArray.loadInt32(eIdent, currIndex)
                    currIndex += 4
                    val d_un = byteArray.loadUInt32(eIdent, currIndex)
                    currIndex += 4
                    val d_ptr = byteArray.loadUInt32(eIdent, currIndex)

                    return ELF32_Dyn(d_tag, d_un, d_ptr)
                }

                E_IDENT.ELFCLASS64 -> {
                    val d_tag = byteArray.loadInt64(eIdent, currIndex)
                    currIndex += 8
                    val d_un = byteArray.loadUInt64(eIdent, currIndex)
                    currIndex += 8
                    val d_ptr = byteArray.loadUInt64(eIdent, currIndex)
                    return ELF64_Dyn(d_tag, d_un, d_ptr)
                }

                else -> throw NotInELFFormatException
            }
        }


        /**
         * Marks end of dynamic section.
         */
        val DT_NULL = 0L.toInt64()

        /**
         * String table offset to name of a needed library.
         */
        val DT_NEEDED = 1L.toInt64()

        /**
         * Size in bytes of PLT relocation entries.
         */
        val DT_PLTRELSZ = 2L.toInt64()

        /**
         * Address of PLT and/or GOT.
         */
        val DT_PLTGOT = 3L.toInt64()

        /**
         * Address of symbol hash table.
         */
        val DT_HASH = 4L.toInt64()

        /**
         * Address of string table.
         */
        val DT_STRTAB = 5L.toInt64()

        /**
         * Address of symbol table.
         */
        val DT_SYMTAB = 6L.toInt64()

        /**
         * Address of Rela relocation table.
         */
        val DT_RELA = 7L.toInt64()

        /**
         * Size in bytes of the Rela relocation table.
         */
        val DT_RELASZ = 8L.toInt64()

        /**
         * Size in bytes of a Rela relocation table entry.
         */
        val DT_RELAENT = 9L.toInt64()

        /**
         * Size in bytes of string table.
         */
        val DT_STRSZ = 10L.toInt64()

        /**
         * Size in bytes of a symbol table entry.
         */
        val DT_SYMENT = 11L.toInt64()

        /**
         * Address of the initialization function.
         */
        val DT_INIT = 12L.toInt64()

        /**
         * Address of the termination function.
         */
        val DT_FINI = 13L.toInt64()

        /**
         * String table offset to name of shared object.
         */
        val DT_SONAME = 14L.toInt64()

        /**
         * String table offset to search path for direct and indirect library dependencies.
         */
        val DT_RPATH = 15L.toInt64()

        /**
         * Alert linker to search this shared object before the executable for symbols.
         */
        val DT_SYMBOLIC = 16L.toInt64()

        /**
         * Address of Rel relocation table.
         */
        val DT_REL = 17L.toInt64()

        /**
         * Size in bytes of Rel relocation table.
         */
        val DT_RELSZ = 18L.toInt64()

        /**
         * Size in bytes of a Rel table entry.
         */
        val DT_RELENT = 19L.toInt64()

        /**
         * Type of relocation entry to which the PLT refers (Rela or Rel).
         */
        val DT_PLTREL = 20L.toInt64()

        /**
         * Undefined use for debugging.
         */
        val DT_DEBUG = 21L.toInt64()

        /**
         * Absence of this entry indicates that no relocation entries should apply to a nonwritable segment.
         */
        val DT_TEXTREL = 22L.toInt64()

        /**
         * Address of relocation entries associated solely with the PLT.
         */
        val DT_JMPREL = 23L.toInt64()

        /**
         * Instruct dynamic linker to process all relocations before transferring control to the executable.
         */
        val DT_BIND_NOW = 24L.toInt64()

        /**
         * String table offset to search path for direct library dependencies.
         */
        val DT_RUNPATH = 25L.toInt64()

        /**
         * Lower bound of processor-specific semantic values.
         */
        val DT_LOPROC = 0x70000000L.toInt64()

        /**
         * Upper bound of processor-specific semantic values.
         */
        val DT_HIPROC = 0x7fffffffL.toInt64()
    }

}