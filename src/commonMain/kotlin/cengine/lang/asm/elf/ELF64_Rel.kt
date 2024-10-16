package cengine.lang.asm.elf

import cengine.lang.asm.elf.ELF64_Rel.Companion.R_SYM
import cengine.lang.asm.elf.ELF64_Rel.Companion.R_TYPE
import cengine.util.ByteBuffer
import cengine.util.Endianness


/**
 * ELF Relocation Entry
 *
 * Relocation is the process of connecting symbolic references with symbolic definitions. For
 * example, when a program calls a function, the associated call instruction must transfer control
 * to the proper destination address at execution. In other words, relocatable files must have
 * information that describes how to modify their section contents, thus allowing executable and
 * shared object files to hold the right information for a process's program image. Relocation
 * entries are these data.
 *
 * @param r_offset This member gives the location at which to apply the relocation action. For
 * a relocatable file, the value is the byte offset from the beginning of the
 * section to the storage unit affected by the relocation. For an executable file
 * or a shared object, the value is the virtual address of the storage unit affected
 * by the relocation.
 *
 * @param r_info This member gives both the symbol table index with respect to which the
 * relocation must be made, and the type of relocation to apply. For example,
 * a call instruction's relocation entry would hold the symbol table index of
 * the function being called. If the index is STN_UNDEF, the undefined symbol
 * index, the relocation uses 0 as the "symbol value.''  Relocation types are
 * processor-specific; descriptions of their behavior appear in the processor
 * supplement. When the text in the processor supplement refers to a
 * relocation entry's relocation type or symbol table index, it means the result
 * of applying [R_TYPE] or  [R_SYM], respectively, to the
 * entry's [r_info] member.
 *
 */
data class ELF64_Rel(
    var r_offset: Elf64_Addr,
    var r_info: Elf_Xword
): Rel {

    companion object{
        const val SIZE = 16
        fun R_SYM(i: Elf_Xword) = i.shr(32)
        fun R_TYPE(i: Elf_Xword) = i.and(0xffffffffUL)
        fun R_INFO(s: Elf_Xword, t: Elf_Xword) = s.shl(32) + t.and(0xffffffffUL)
    }

    override fun build(endianness: Endianness): ByteArray {
        val b = ByteBuffer(endianness)

        b.put(r_offset)
        b.put(r_info)

        return b.toByteArray()
    }

    override fun byteSize(): Int = SIZE
}