package cengine.lang.obj.elf

import cengine.lang.asm.AsmDisassembler
import cengine.lang.obj.ObjPsiFile
import cengine.util.integer.BigInt
import cengine.util.integer.Int16.Companion.toInt16
import cengine.util.integer.Int8.Companion.toInt8
import cengine.util.integer.IntNumber
import cengine.util.integer.UInt8.Companion.toUInt8
import cengine.vfs.VirtualFile
import emulator.kit.memory.Memory

sealed class ELFFile(file: VirtualFile) : ObjPsiFile(file, true) {

    val bytes = file.getContent()

    val e_ident: E_IDENT = eIdent(bytes)
    val ehdr: Ehdr = ehdr(bytes, e_ident)

    val sectionHeaders: List<Shdr> = readSectionHeaders()
    val programHeaders: List<Phdr> = readProgramHeaders()
    val symbolTable: List<Sym>? = readSymbolTable()
    val dynamicSection: List<Dyn>? = readDynamicSection()
    val relocationTables: Map<String, List<Rel>> = readRelocationTables()
    val relocationTablesWithAddend: Map<String, List<Rela>> = readRelocationTablesWithAddend()
    val noteHeaders: List<Nhdr>? = readNoteHeaders()
    val segmentToSectionGroup: List<Group> = groupSectionsBySegment()
    val shstrtab: Shdr? = sectionHeaders.getOrNull(ehdr.e_shstrndx.toInt())
    val strTab = sectionHeaders.firstOrNull { getSectionName(it) == ".strtab" }

    override fun initialize(memory: Memory<*, *>) {
        for (phdr in programHeaders) {

            val startAddr = when (phdr) {
                is ELF32_Phdr -> phdr.p_vaddr.toBigInt()
                is ELF64_Phdr -> phdr.p_vaddr.toBigInt()
            }

            val startOffset = when (phdr) {
                is ELF32_Phdr -> phdr.p_offset.toInt()
                is ELF64_Phdr -> phdr.p_offset.toInt()
            }

            val size = when (phdr) {
                is ELF32_Phdr -> phdr.p_filesz.toInt()
                is ELF64_Phdr -> phdr.p_filesz.toInt()
            }

            bytes.copyOfRange(startOffset, startOffset + size).forEachIndexed { index, byte ->
                memory.storeEndianAware(startAddr + index, byte.toInt8())
            }
        }
    }

    override fun entry(): IntNumber<*> = when(ehdr){
        is ELF32_Ehdr -> ehdr.e_entry
        is ELF64_Ehdr -> ehdr.e_entry
    }

    override fun contents(): Map<BigInt, Pair<List<IntNumber<*>>, List<AsmDisassembler.Label>>> {
        val contents = mutableMapOf<BigInt, Pair<List<IntNumber<*>>, List<AsmDisassembler.Label>>>()

        for (group in segmentToSectionGroup.filterIsInstance<Segment>()) {
            val phdr = group.phdr
            val segmentOffset = when (phdr) {
                is ELF32_Phdr -> phdr.p_offset.toUInt64()
                is ELF64_Phdr -> phdr.p_offset
            }

            val labels = group.sections.flatMap { shdr: Shdr ->
                val sectionIndex = sectionHeaders.indexOf(shdr)
                val symbols = symbolTable?.filter {
                    it.st_shndx == sectionIndex.toInt16().toUInt16()
                } ?: return@flatMap emptyList<AsmDisassembler.Label>()
                val sectionOffset = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_offset.toUInt64()
                    is ELF64_Shdr -> shdr.sh_offset
                }

                symbols.filter {
                    Sym.ELF_ST_TYPE(it.st_info) == Sym.STT_NOTYPE
                }.mapNotNull { sym ->
                    val symValue = when (sym) {
                        is ELF32_Sym -> sym.st_value.toUInt64()
                        is ELF64_Sym -> sym.st_value
                    }

                    val offset = sectionOffset - segmentOffset + symValue
                    val name = getStrTabString(sym.st_name.toInt()) ?: "[invalid]"
                    if (name.isEmpty()) return@mapNotNull null
                    AsmDisassembler.Label(offset.toInt(), name)
                }
            }

            val startAddr = when (phdr) {
                is ELF32_Phdr -> phdr.p_vaddr.toBigInt()
                is ELF64_Phdr -> phdr.p_vaddr.toBigInt()
            }

            val startOffset = when (phdr) {
                is ELF32_Phdr -> phdr.p_offset.toInt()
                is ELF64_Phdr -> phdr.p_offset.toInt()
            }

            val size = when (phdr) {
                is ELF32_Phdr -> phdr.p_filesz.toInt()
                is ELF64_Phdr -> phdr.p_filesz.toInt()
            }

            val segmentBytes = bytes.copyOfRange(startOffset, startOffset + size).map { byte: Byte ->
                byte.toUByte().toUInt8()
            }

            contents[startAddr] = segmentBytes to labels
        }

        return contents
    }

    private fun readSectionHeaders(): List<Shdr> {
        return when (ehdr) {
            is ELF32_Ehdr -> {
                val offset = ehdr.e_shoff.toInt()
                List(ehdr.e_shnum.toInt()) { index ->
                    shdr(bytes, e_ident, offset + index * ehdr.e_shentsize.toInt())
                }
            }

            is ELF64_Ehdr -> {
                val offset = ehdr.e_shoff.toInt()
                List(ehdr.e_shnum.toInt()) { index ->
                    shdr(bytes, e_ident, offset + index * ehdr.e_shentsize.toInt())
                }
            }
        }
    }

    private fun readProgramHeaders(): List<Phdr> {
        return when (ehdr) {
            is ELF32_Ehdr -> {
                val offset = ehdr.e_phoff.toInt()
                List(ehdr.e_phnum.toInt()) { index ->
                    phdr(bytes, e_ident, offset + index * ehdr.e_phentsize.toInt())
                }
            }

            is ELF64_Ehdr -> {
                val offset = ehdr.e_phoff.toInt()
                List(ehdr.e_phnum.toInt()) { index ->
                    phdr(bytes, e_ident, offset + index * ehdr.e_phentsize.toInt())
                }
            }
        }
    }

    private fun readSymbolTable(): List<Sym>? {
        val symtabSection = sectionHeaders.find { it.sh_type == Shdr.SHT_SYMTAB }
        return symtabSection?.let {
            when (it) {
                is ELF32_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        sym(bytes, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                is ELF64_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        sym(bytes, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }
            }
        }
    }

    private fun readDynamicSection(): List<Dyn>? {
        val dynamicSection = sectionHeaders.find { it.sh_type == Shdr.SHT_DYNAMIC }
        return dynamicSection?.let {
            when (it) {
                is ELF32_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        dyn(bytes, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                is ELF64_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        dyn(bytes, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }
            }

        }
    }

    private fun readRelocationTables(): Map<String, List<Rel>> {
        return sectionHeaders.filter { it.sh_type == Shdr.SHT_REL }
            .associate { section ->
                val sectionName = getSectionName(section)
                when (section) {
                    is ELF32_Shdr -> {
                        val offset = section.sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            rel(bytes, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }

                    is ELF64_Shdr -> {
                        val offset = section.sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            rel(bytes, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }
                }
            }

    }

    private fun readRelocationTablesWithAddend(): Map<String, List<Rela>> {
        return sectionHeaders.filter { it.sh_type == Shdr.SHT_RELA }
            .associate { section ->
                val sectionName = getSectionName(section)
                when (section) {
                    is ELF32_Shdr -> {
                        val offset = section.sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            rela(bytes, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }

                    is ELF64_Shdr -> {
                        val offset = section.sh_offset.toInt()
                        val relEntries = List(section.sh_size.toInt() / section.sh_entsize.toInt()) { index ->
                            rela(bytes, e_ident, offset + index * section.sh_entsize.toInt())
                        }
                        sectionName to relEntries
                    }
                }
            }

    }

    private fun readNoteHeaders(): List<Nhdr>? {
        val noteSection = sectionHeaders.find { it.sh_type == Shdr.SHT_NOTE }
        return noteSection?.let {
            when (it) {
                is ELF32_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        nhdr(bytes, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }

                is ELF64_Shdr -> {
                    val offset = it.sh_offset.toInt()
                    List(it.sh_size.toInt() / it.sh_entsize.toInt()) { index ->
                        nhdr(bytes, e_ident, offset + index * it.sh_entsize.toInt())
                    }
                }
            }
        }
    }

    abstract fun getSectionName(section: Shdr): String

    abstract fun getStrTabString(namendx: Int): String?

    private fun groupSectionsBySegment(): List<Group> {
        val segments = programHeaders.map { phdr ->
            val start = when (phdr) {
                is ELF32_Phdr -> phdr.p_offset.toUInt64()
                is ELF64_Phdr -> phdr.p_offset
            }
            val size = when (phdr) {
                is ELF32_Phdr -> phdr.p_filesz.toUInt64()
                is ELF64_Phdr -> phdr.p_filesz
            }

            val fileIndexRange = start.toLong()..<(start + size).toLong()

            val sections = sectionHeaders.filter { shdr ->
                when (shdr) {
                    is ELF32_Shdr -> shdr.sh_offset.toLong() in fileIndexRange
                    is ELF64_Shdr -> shdr.sh_offset.toLong() in fileIndexRange
                }

            }

            createSegment(phdr, sections)
        }
        val unmatched = (sectionHeaders - segments.flatMap { it.sections }.toSet()).map {
            createSection(it)
        }
        return (segments + unmatched).sortedBy { it.index }
    }

    fun nameOfSection(index: Int): String {
        val section = sectionHeaders.getOrNull(index) ?: return ""
        return getSectionName(section)
    }


    abstract fun createSegment(phdr: Phdr, sections: List<Shdr>): Segment
    abstract fun createSection(shdr: Shdr): Section

    sealed class Group(val index: Int)
    abstract class Segment(val phdr: Phdr, val sections: List<Shdr>, sectionHeaders: List<Shdr>) : Group(sectionHeaders.indexOf(sections.firstOrNull()))
    abstract class Section(val section: Shdr, sectionHeaders: List<Shdr>) : Group(sectionHeaders.indexOf(section))

    fun eIdent(byteArray: ByteArray): E_IDENT = E_IDENT.extractFrom(byteArray)

    abstract fun ehdr(byteArray: ByteArray, eIdent: E_IDENT): Ehdr

    abstract fun shdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Shdr

    abstract fun phdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Phdr

    abstract fun sym(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Sym

    abstract fun dyn(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Dyn

    abstract fun rel(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Rel

    abstract fun rela(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Rela

    fun nhdr(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Nhdr = Nhdr.extractFrom(byteArray, eIdent, offset)

    class InvalidElfComponent(thisComponent: Any, misfitting: Any?) : Exception("Invalid Elf Component: ${thisComponent::class.simpleName} isn't expecting ${misfitting?.let { it::class.simpleName.toString() }}!")

}