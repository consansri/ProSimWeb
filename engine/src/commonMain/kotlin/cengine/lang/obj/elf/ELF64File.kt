package cengine.lang.obj.elf

import cengine.vfs.VirtualFile

class ELF64File(file: VirtualFile) : ELFFile(file) {

    init {
        if (ehdr !is ELF64_Ehdr) throw InvalidElfComponent(this, ehdr)
    }

    override fun getSectionName(section: Shdr): String {
        if (section !is ELF64_Shdr) throw InvalidElfComponent(this, section)
        if (shstrtab !is ELF64_Shdr) return "[shstrtab missing]"
        val stringTableOffset = shstrtab.sh_offset.toInt()
        val nameOffset = section.sh_name.toInt()
        return bytes.sliceArray(stringTableOffset + nameOffset until bytes.size)
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .decodeToString()
    }

    override fun getStrTabString(namendx: Int): String {
        if (strTab !is ELF64_Shdr) return "[strTab missing]"
        val strTabOffset = strTab.sh_offset.toInt()
        return bytes.sliceArray(strTabOffset + namendx until bytes.size)
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .decodeToString()
    }

    override fun ehdr(byteArray: ByteArray, eIdent: E_IDENT): ELF64_Ehdr = Ehdr.extractFrom(byteArray, eIdent) as ELF64_Ehdr

    override fun shdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Shdr = Shdr.extractFrom(byteArray, eIdent, offset) as ELF64_Shdr

    override fun phdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Phdr = Phdr.extractFrom(byteArray, eIdent, offset) as ELF64_Phdr

    override fun sym(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Sym = Sym.extractFrom(byteArray, eIdent, offset) as ELF64_Sym

    override fun dyn(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Dyn = Dyn.extractFrom(byteArray, eIdent, offset) as ELF64_Dyn

    override fun rel(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Rel = Rel.extractFrom(byteArray, eIdent, offset) as ELF64_Rel

    override fun rela(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF64_Rela = Rela.extractFrom(byteArray, eIdent, offset) as ELF64_Rela

    override fun createSegment(phdr: Phdr, sections: List<Shdr>): Segment = ELF64Segment(phdr as ELF64_Phdr, sections.filterIsInstance<ELF64_Shdr>())
    override fun createSection(shdr: Shdr): Section = ELF64Section(shdr as ELF64_Shdr)

    inner class ELF64Section(shdr: ELF64_Shdr) : Section(shdr, sectionHeaders)
    inner class ELF64Segment(phdr: ELF64_Phdr, sections: List<ELF64_Shdr>) : Segment(phdr, sections, sectionHeaders)
}