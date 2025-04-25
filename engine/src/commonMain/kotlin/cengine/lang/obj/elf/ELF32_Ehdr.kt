package cengine.lang.obj.elf

import cengine.util.Endianness
import cengine.util.buffer.Buffer8
import cengine.util.integer.UInt8
import cengine.util.integer.hexDump


/**
 * ELF Header
 *
 * @param e_ident The initial bytes mark the file as an object file and provide machine-independent data with which to decode and interpret the file's contents.
 * @param e_type This member identifies the object file type.
 * @param e_machine This member's value specifies the required architecture for an individual file.
 * @param e_version This member identifies the object file version.
 * @param e_entry This member gives the virtual address to which the system first transfers control, thus starting the process. If the file has no associated entry point, this member holds zero.
 * @param e_phoff This member holds the program header table's file offset in bytes. If the file has no
 * program header table, this member holds zero.
 * @param e_shoff This member holds the section header table's file offset in bytes. If the file has no
 * section header table, this member holds zero.
 * @param e_flags This member holds processor-specific flags associated with the file. Flag names
 * take the form [EF_machine_flag].
 * @param e_ehsize This member holds the ELF header's size in bytes.
 * @param e_phentsize This member holds the size in bytes of one entry in the file's program header table;
 * all entries are the same size.
 * @param e_phnum This member holds the number of entries in the program header table. Thus, the
 * product of [e_phentsize] and [e_phnum] gives the table's size in bytes. If a file
 * has no program header table, [e_phnum] holds value zero.
 * @param e_shentsize This member holds a section header's size in bytes. A section header is one entry
 * in the section header table; all entries are the same size.
 * @param e_shnum This member holds the number of entries in the section header table. Thus, the
 * product of [e_shentsize] and [e_shnum] gives the section header table's size in
 * bytes. If a file has no section header table, [e_shnum] holds value zero.
 * @param e_shstrndx This member holds the section header table index of the entry associated with the
 * section name string table. If the file has no section name string table, this member
 * holds the value [SHN_UNDEF]. See "Sections" and "String Table" below for more
 * information.
 *
 */
data class ELF32_Ehdr(
    override var e_ident: E_IDENT,
    override var e_type: Elf_Half,
    override var e_machine: Elf_Half,
    override var e_version: Elf_Word = EV_CURRENT,
    var e_entry: Elf32_Addr,
    var e_phoff: Elf32_Off,
    var e_shoff: Elf32_Off,
    override var e_flags: Elf_Word,
    override var e_ehsize: Elf_Half,
    override var e_phentsize: Elf_Half,
    override var e_phnum: Elf_Half,
    override var e_shentsize: Elf_Half,
    override var e_shnum: Elf_Half,
    override var e_shstrndx: Elf_Half
) : Ehdr() {

    /*constructor(
        ei_class: Elf_Byte,
        ei_data: Elf_Byte,
        e_type: Elf_Half,

    ): this(
        E_IDENT(ei_class = ei_class, ei_data = ei_data),
        e_type
    )*/

    override fun byteSize(): Int = e_ident.byteSize() + 36

    override fun build(endianness: Endianness): Array<UInt8> {
        val buffer = Buffer8(endianness)

        buffer.putAll(e_ident.build(endianness))
        buffer.put(e_type)
        buffer.put(e_machine)
        buffer.put(e_version)
        buffer.put(e_entry)
        buffer.put(e_phoff)
        buffer.put(e_shoff)
        buffer.put(e_flags)
        buffer.put(e_ehsize)
        buffer.put(e_phentsize)
        buffer.put(e_phnum)
        buffer.put(e_shentsize)
        buffer.put(e_shnum)
        buffer.put(e_shstrndx)

        return buffer.toTypedArray()
    }

    override fun toString(): String {
        return "Elf Header:\n" +
                " Magic:                             ${e_ident.build(Endianness.BIG).hexDump()}\n" +
                " Class:                             ${E_IDENT.getElfClass(e_ident.ei_class)}\n" +
                " Data:                              ${E_IDENT.getElfData(e_ident.ei_data)}\n" +
                " Version:                           ${e_ident.ei_version}\n" +
                " OS/ABI:                            ${E_IDENT.getOsAbi(e_ident.ei_osabi)}\n" +
                " ABI Version:                       ${e_ident.ei_abiversion}\n" +
                " Type:                              ${getELFType(e_type)}\n" +
                " Machine:                           ${getELFMachine(e_machine)}\n" +
                " Version:                           $e_version\n" +
                " Entry point address:               $e_entry\n" +
                " Start of program headers:          $e_phoff (bytes into file)\n" +
                " Start of section headers:          $e_shoff (bytes into file)\n" +
                " Flags:                             0x${e_flags.toString(16)}\n" +
                " Size of this header:               $e_ehsize (bytes)\n" +
                " Size of program headers:           $e_phentsize (bytes)\n" +
                " Number of program headers:         $e_phnum\n" +
                " Size of section headers:           $e_shentsize (bytes)\n" +
                " Number of section headers:         $e_shnum\n" +
                " Section header string table index: $e_shstrndx\n"
    }

}
