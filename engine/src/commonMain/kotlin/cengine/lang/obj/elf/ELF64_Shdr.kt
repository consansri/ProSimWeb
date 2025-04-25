package cengine.lang.obj.elf

import cengine.util.Endianness
import cengine.util.buffer.Buffer8
import cengine.util.integer.UInt32
import cengine.util.integer.UInt64
import cengine.util.integer.UInt8

data class ELF64_Shdr(
    override var sh_name: Elf_Word = UInt32.ZERO,
    override var sh_type: Elf_Word = SHT_NULL,
    var sh_flags: Elf_Xword = UInt64.ZERO,
    var sh_addr: Elf64_Addr = UInt64.ZERO,
    var sh_offset: Elf64_Off = UInt64.ZERO,
    var sh_size: Elf_Xword = UInt64.ZERO,
    override var sh_link: Elf_Word = SHN_UNDEF.toUInt32(),
    override var sh_info: Elf_Word = UInt32.ZERO,
    var sh_addralign: Elf_Xword = UInt64.ZERO,
    var sh_entsize: Elf_Xword = UInt64.ZERO
) : Shdr() {

    companion object {
        fun create(nameIndex: Elf_Word, type: Elf_Word, flags: Elf_Xword): ELF64_Shdr {
            return ELF64_Shdr(
                sh_name = nameIndex,
                sh_type = type,
                sh_flags = flags
            )
        }
    }

    override fun build(endianness: Endianness): Array<UInt8> {
        val b = Buffer8(endianness)

        b.put(sh_name)
        b.put(sh_type)
        b.put(sh_flags)
        b.put(sh_addr)
        b.put(sh_offset)
        b.put(sh_size)
        b.put(sh_link)
        b.put(sh_info)
        b.put(sh_addralign)
        b.put(sh_entsize)

        return b.toTypedArray()
    }

    override fun byteSize(): Int = 64
    override fun getEntSize(): Elf_Xword = sh_entsize

    override fun toString(): String = "$sh_name-${getSectionType(sh_type)}-${getSectionFlags(sh_flags)}"
}
