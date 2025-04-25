package cengine.lang.obj.elf

import cengine.util.Endianness
import cengine.util.buffer.Buffer8
import cengine.util.integer.UInt64
import cengine.util.integer.UInt8

/**
 * Symbol Table
 *
 * An object file's symbol table holds information needed to locate and relocate a program's
 * symbolic definitions and references.  A symbol table index is a subscript into this array. Index
 * 0 both designates the first entry in the table and serves as the undefined symbol index. The
 * contents of the initial entry are specified later in this section.
 *
 * @param st_name This member holds an index into the object file's symbol string table, which holds
 * the character representations of the symbol names.
 *
 * @param st_value This member gives the value of the associated symbol. Depending on the context,
 * this may be an absolute value, an address, and so on; details appear below.
 *
 * @param st_size Many symbols have associated sizes. For example, a data object's size is the number
 * of bytes contained in the object. This member holds 0 if the symbol has no size or
 * an unknown size.
 *
 * @param st_info This member specifies the symbol's type and binding attributes. A list of the values
 * and meanings appears below. The following code shows how to manipulate the
 * values.
 *
 * @param st_other This member currently holds 0 and has no defined meaning.
 *
 * @param st_shndx Every symbol table entry is "defined'' in relation to some section; this member holds
 * the relevant section header table index. As Figure 1-7 and the related text describe,
 * some section indexes indicate special meanings.
 *
 */
data class ELF64_Sym(
    override var st_name: Elf_Word,
    override var st_info: Elf_Byte,
    override var st_other: Elf_Byte = UInt8.ZERO,
    override var st_shndx: Elf_Half,
    var st_value: Elf_Xword = UInt64.ZERO,
    var st_size: Elf_Xword = UInt64.ZERO,
): Sym() {
    override fun build(endianness: Endianness): Array<UInt8> {
        val b = Buffer8(endianness)

        b.put(st_name)
        b.put(st_info)
        b.put(st_other)
        b.put(st_shndx)
        b.put(st_value)
        b.put(st_size)

        return b.toTypedArray()
    }
    override fun byteSize(): Int = 24
}
