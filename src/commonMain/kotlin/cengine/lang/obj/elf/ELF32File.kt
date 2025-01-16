package cengine.lang.obj.elf

import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.vfs.VirtualFile

class ELF32File(file: VirtualFile, manager: PsiManager<*,*>) : ELFFile(file, manager) {

    init {
        if (ehdr !is ELF32_Ehdr) throw InvalidElfComponent(this, ehdr)
    }

    override fun getSectionName(section: Shdr): String {
        if (section !is ELF32_Shdr) throw InvalidElfComponent(this, section)
        if (shstrtab !is ELF32_Shdr) return "[shstrtab missing]"
        val stringTableOffset = shstrtab.sh_offset.toInt()
        val nameOffset = section.sh_name.toInt()
        return bytes.sliceArray(stringTableOffset + nameOffset until bytes.size)
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .decodeToString()
    }

    override fun getStrTabString(namendx: Int): String? {
        if (strTab == null) return null
        if (strTab !is ELF32_Shdr) return "[strTab missing]"
        val strTabOffset = strTab.sh_offset.toInt()
        return bytes.sliceArray(strTabOffset + namendx until bytes.size)
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .decodeToString()
    }


    override fun ehdr(byteArray: ByteArray, eIdent: E_IDENT): ELF32_Ehdr = Ehdr.extractFrom(byteArray, eIdent) as ELF32_Ehdr

    override fun shdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Shdr = Shdr.extractFrom(byteArray, eIdent, offset) as ELF32_Shdr

    override fun phdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Phdr = Phdr.extractFrom(byteArray, eIdent, offset) as ELF32_Phdr

    override fun sym(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Sym = Sym.extractFrom(byteArray, eIdent, offset) as ELF32_Sym

    override fun dyn(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Dyn = Dyn.extractFrom(byteArray, eIdent, offset) as ELF32_Dyn

    override fun rel(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Rel = Rel.extractFrom(byteArray, eIdent, offset) as ELF32_Rel

    override fun rela(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): ELF32_Rela = Rela.extractFrom(byteArray, eIdent, offset) as ELF32_Rela


    override fun createSegment(phdr: Phdr, sections: List<Shdr>): Segment = ELF32Segment(phdr as ELF32_Phdr, sections.filterIsInstance<ELF32_Shdr>())

    override fun createSection(shdr: Shdr): Section = ELF32Section(shdr as ELF32_Shdr)

    inner class ELF32Section(shdr: ELF32_Shdr) : Section(shdr, sectionHeaders)
    inner class ELF32Segment(phdr: ELF32_Phdr, sections: List<ELF32_Shdr>) : Segment(phdr, sections, sectionHeaders)
}