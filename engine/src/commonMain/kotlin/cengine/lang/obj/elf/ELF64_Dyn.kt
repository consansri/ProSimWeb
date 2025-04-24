package cengine.lang.obj.elf

import cengine.util.Endianness
import cengine.util.buffer.Buffer8
import cengine.util.integer.Int8
import cengine.util.integer.UInt8

/**
 * Data class representing the Elf32_Dyn structure in the ELF format.
 *
 * @property d_tag The dynamic table entry type.
 * @property d_val The integer value associated with the dynamic table entry.
 * @property d_ptr The address associated with the dynamic table entry.
 */
data class ELF64_Dyn(
    var d_tag: Elf_Sxword,
    var d_val: Elf_Xword,
    var d_ptr: Elf64_Addr
): Dyn(){
    override fun build(endianness: Endianness): Array<UInt8> {
        val b = Buffer8(endianness)

        b.put(d_tag)
        b.put(d_val)
        b.put(d_ptr)

        return b.toTypedArray()
    }

    override fun byteSize(): Int = 24
}
