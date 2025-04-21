package cengine.lang.obj.elf

import cengine.util.integer.UInt16.Companion.toUInt16
import cengine.util.integer.UInt32.Companion.toUInt32

/**
 * ELF Header
 *
 * @property e_ident The initial bytes mark the file as an object file and provide machine-independent data with which to decode and interpret the file's contents.
 * @property e_type This member identifies the object file type.
 * @property e_machine This member's value specifies the required architecture for an individual file.
 * @property e_version This member identifies the object file version.
 * @property e_entry This member gives the virtual address to which the system first transfers control, thus starting the process. If the file has no associated entry point, this member holds zero.
 * @property e_phoff This member holds the program header table's file offset in bytes. If the file has no
 * program header table, this member holds zero.
 * @property e_shoff This member holds the section header table's file offset in bytes. If the file has no
 * section header table, this member holds zero.
 * @property e_flags This member holds processor-specific flags associated with the file. Flag names
 * take the form [EF_machine_flag].
 * @property e_ehsize This member holds the ELF header's size in bytes.
 * @property e_phentsize This member holds the size in bytes of one entry in the file's program header table;
 * all entries are the same size.
 * @property e_phnum This member holds the number of entries in the program header table. Thus, the
 * product of [e_phentsize] and [e_phnum] gives the table's size in bytes. If a file
 * has no program header table, [e_phnum] holds value zero.
 * @property e_shentsize This member holds a section header's size in bytes. A section header is one entry
 * in the section header table; all entries are the same size.
 * @property e_shnum This member holds the number of entries in the section header table. Thus, the
 * product of [e_shentsize] and [e_shnum] gives the section header table's size in
 * bytes. If a file has no section header table, [e_shnum] holds value zero.
 * @property e_shstrndx This member holds the section header table index of the entry associated with the
 * section name string table. If the file has no section name string table, this member
 * holds the value [SHN_UNDEF]. See "Sections" and "String Table" below for more
 * information.
 *
 */
sealed class Ehdr : BinaryProvider {

    abstract var e_ident: E_IDENT
    abstract var e_type: Elf_Half
    abstract var e_machine: Elf_Half
    abstract var e_version: Elf_Word
    abstract var e_flags: Elf_Word
    abstract var e_ehsize: Elf_Half
    abstract var e_phentsize: Elf_Half
    abstract var e_phnum: Elf_Half
    abstract var e_shentsize: Elf_Half
    abstract var e_shnum: Elf_Half
    abstract var e_shstrndx: Elf_Half

    companion object {
        fun getELFType(type: Elf_Half): String = when (type) {
            ET_NONE -> "NONE (No file type)"
            ET_REL -> "REL (Relocatable file)"
            ET_EXEC -> "EXEC (Executable file)"
            ET_DYN -> "DYN (Shared object file)"
            ET_CORE -> "CORE (Core file)"
            else -> "UNKNOWN (0x${type.toString(16)})"
        }

        fun getELFMachine(machine: Elf_Half): String = when (machine) {
            EM_386 -> "Intel 80386"
            EM_VPP500 -> "VPP500"
            EM_SPARC -> "SPARC"
            EM_68K -> "68K"
            EM_88K -> "88K"
            EM_X86_64 -> "AMD x86-64 architecture"
            EM_ARM -> "ARM"
            EM_RISCV -> "RISCV"
            EM_CUSTOM_IKRMINI -> "IKR MINI"
            EM_CUSTOM_IKRRISC2 -> "IKR RISC-II"
            EM_CUSTOM_T6502 -> "T6502"
            else -> "UNKNOWN (0x${machine.toString(16)})"
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT): Ehdr {
            var currIndex = E_IDENT.EI_NIDENT.toInt()
            val e_type = byteArray.loadUInt16(eIdent, currIndex)
            currIndex += 2
            val e_machine = byteArray.loadUInt16(eIdent, currIndex)
            currIndex += 2
            val e_version = byteArray.loadUInt32(eIdent, currIndex)
            currIndex += 4

            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val e_entry = byteArray.loadUInt32(eIdent, currIndex)
                    currIndex += 4
                    val e_phoff = byteArray.loadUInt32(eIdent, currIndex)
                    currIndex += 4
                    val e_shoff = byteArray.loadUInt32(eIdent, currIndex)
                    currIndex += 4

                    val e_flags = byteArray.loadUInt32(eIdent, currIndex)
                    currIndex += 4

                    val e_ehsize = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_phentsize = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_phnum = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_shentsize = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_shnum = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_shstrndx = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    return ELF32_Ehdr(eIdent, e_type, e_machine, e_version, e_entry, e_phoff, e_shoff, e_flags, e_ehsize, e_phentsize, e_phnum, e_shentsize, e_shnum, e_shstrndx)
                }

                E_IDENT.ELFCLASS64 -> {
                    val e_entry = byteArray.loadUInt64(eIdent, currIndex)
                    currIndex += 8
                    val e_phoff = byteArray.loadUInt64(eIdent, currIndex)
                    currIndex += 8
                    val e_shoff = byteArray.loadUInt64(eIdent, currIndex)
                    currIndex += 8

                    val e_flags = byteArray.loadUInt32(eIdent, currIndex)
                    currIndex += 4

                    val e_ehsize = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_phentsize = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_phnum = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_shentsize = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_shnum = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    val e_shstrndx = byteArray.loadUInt16(eIdent, currIndex)
                    currIndex += 2

                    return ELF64_Ehdr(eIdent, e_type, e_machine, e_version, e_entry, e_phoff, e_shoff, e_flags, e_ehsize, e_phentsize, e_phnum, e_shentsize, e_shnum, e_shstrndx)
                }

                else -> throw NotInELFFormatException
            }

        }

        /**
         * [e_type]
         */

        /**
         * No file type
         */
        val ET_NONE: Elf_Half = 0U.toUInt16()

        /**
         * Relocatable file
         */
        val ET_REL: Elf_Half = 1U.toUInt16()

        /**
         * Executable file
         */
        val ET_EXEC: Elf_Half = 2U.toUInt16()

        /**
         * Shared Object file
         */
        val ET_DYN: Elf_Half = 3U.toUInt16()

        /**
         * Core file
         */
        val ET_CORE: Elf_Half = 4U.toUInt16()

        /**
         * Processor-specific
         */
        val ET_LOPROC: Elf_Half = 0xFF00U.toUInt16()

        /**
         * Processor-specific
         */
        val ET_HIPROC: Elf_Half = 0xFFFFU.toUInt16()

        /**
         * [e_machine]
         */

        /**
         * AT&T WE 32100
         */
        val EM_M32: Elf_Half = 1U.toUInt16()

        /**
         * SPARC
         */
        val EM_SPARC: Elf_Half = 2U.toUInt16() // SPARC

        /**
         * Intel Architecture
         */
        val EM_386: Elf_Half = 3U.toUInt16() // Intel Architecture

        /**
         * Motorola 68000
         */
        val EM_68K: Elf_Half = 4U.toUInt16() // Motorola 68000

        /**
         * Motorola 88000
         */
        val EM_88K: Elf_Half = 5U.toUInt16() // Motorola 88000

        /**
         * Intel 80860
         */
        val EM_860: Elf_Half = 7U.toUInt16()

        /**
         * MIPS RS3000 Big-Endian
         */
        val EM_MIPS: Elf_Half = 8U.toUInt16()

        /**
         * MIPS RS4000 Big-Endian
         */
        val EM_MIPS_RS4_BE: Elf_Half = 10U.toUInt16()

        /**
         * Fujitsu VPP500
         */
        val EM_VPP500: Elf_Half = 17U.toUInt16()

        /**
         * ARM
         */
        val EM_ARM: Elf_Half = 40U.toUInt16()

        /**
         * X86 64Bit
         */
        val EM_X86_64: Elf_Half = 62U.toUInt16()

        /**
         * RISC-V
         */
        val EM_RISCV: Elf_Half = 243U.toUInt16()

        // ...

        // FREE TO USE MACHINE TYPES 0xFF00 - 0xFFFF

        val EM_CUSTOM_IKRRISC2: Elf_Half = 0xFF00U.toUInt16()

        val EM_CUSTOM_IKRMINI: Elf_Half = 0xFF01U.toUInt16()

        val EM_CUSTOM_T6502: Elf_Half = 0xFF02U.toUInt16()


        /**
         * Invalid version
         */
        val EV_NONE: Elf_Word = 0U.toUInt32()

        /**
         * Current version
         */
        val EV_CURRENT: Elf_Word = 1U.toUInt32()
    }

    abstract override fun toString(): String

}