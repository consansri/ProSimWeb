package cengine.lang.obj.elf

import cengine.util.Endianness
import cengine.util.buffer.Int8Buffer
import cengine.util.integer.Int8
import cengine.util.integer.UInt32

/**
 * ELF Program Header
 *
 * An executable or shared object file's program header table is an array of structures, each
 * describing a segment or other information the system needs to prepare the program for
 * execution. An object file segment contains one or more sections. Program headers are
 * meaningful only for executable and shared object files. A file specifies its own program header
 * size with the ELF header's [e_phentsize] and  [e_phnum] members.
 *
 * @param p_type This member tells what kind of segment this array element describes or how to
 * interpret the array element's information. Type values and their meanings appear
 * below.
 *
 * @param p_offset This member gives the offset from the beginning of the file at which the first byte
 * of the segment resides.
 *
 * @param p_vaddr This member gives the virtual address at which the first byte of the segment resides
 * in memory.
 *
 * @param p_paddr On systems for which physical addressing is relevant, this member is reserved for
 * the segment's physical address. This member requires operating system specific
 * information, which is described in the appendix at the end of Book III.
 *
 * @param p_filesz This member gives the number of bytes in the file image of the segment; it may be
 * zero.
 *
 * @param p_memsz This member gives the number of bytes in the memory image of the segment; it
 * may be zero.
 *
 * @param p_flags This member gives flags relevant to the segment. Defined flag values appear below.
 *
 * @param p_align Loadable process segments must have congruent values for [p_vaddr] and
 * [p_offset], modulo the page size. This member gives the value to which the
 * segments are aligned in memory and in the file. Values 0 and 1 mean that no
 * alignment is required. Otherwise, [p_align] should be a positive, integral power of
 * 2, and [p_addr] should equal [p_offset], modulo [p_align].
 *
 *
 */
data class ELF32_Phdr(
    override var p_type: Elf_Word,
    var p_offset: Elf32_Off = UInt32.ZERO,
    var p_vaddr: Elf32_Addr = UInt32.ZERO,
    var p_paddr: Elf32_Addr = UInt32.ZERO,
    var p_filesz: Elf_Word = UInt32.ZERO,
    var p_memsz: Elf_Word = UInt32.ZERO,
    override var p_flags: Elf_Word,
    var p_align: Elf_Word
) : Phdr() {


    override fun build(endianness: Endianness): Array<Int8> {
        val b = Int8Buffer(endianness)

        b.put(p_type)
        b.put(p_offset)
        b.put(p_vaddr)
        b.put(p_paddr)
        b.put(p_filesz)
        b.put(p_memsz)
        b.put(p_flags)
        b.put(p_align)

        val array = b.toTypedArray()

        return array
    }

    override fun byteSize(): Int = 32

}