package cengine.lang.obj.elf

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.asm.ast.AsmCodeGenerator.*
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.obj.elf.Shdr.Companion.SHF_ALLOC
import cengine.psi.PsiManager
import cengine.util.Endianness
import cengine.util.buffer.Int8Buffer
import cengine.util.buffer.Int8Buffer.Companion.toASCIIByteArray
import cengine.util.buffer.Int8Buffer.Companion.toASCIIString
import cengine.util.integer.*
import cengine.util.integer.Int8.Companion.toInt8
import cengine.util.integer.UInt16.Companion.toUInt16
import cengine.util.integer.UInt32.Companion.toUInt32
import cengine.util.integer.UInt64.Companion.toUInt64
import com.ionspin.kotlin.bignum.integer.BigInteger

abstract class ELFGenerator(
    val e_type: Elf_Half,
    val ei_class: Elf_Byte,
    val ei_data: Elf_Byte,
    val ei_osabi: Elf_Byte,
    val ei_abiversion: Elf_Byte,
    val e_machine: Elf_Half,
    var e_flags: Elf_Word,
    linkerScript: LinkerScript,
    psiManager: PsiManager<*, *>,
) : AsmCodeGenerator<ELFGenerator.ELFSection>(linkerScript, psiManager) {

    override val fileSuffix: String
        get() = ".o"

    val endianness: Endianness = when (ei_data) {
        E_IDENT.ELFDATA2LSB -> Endianness.LITTLE
        E_IDENT.ELFDATA2MSB -> Endianness.BIG
        else -> {
            throw ELFBuilderException("Invalid Data Encoding $ei_data.")
        }
    }

    /**
     * ELF IDENTIFICATION & CLASSIFICATION
     */
    protected val e_ident: E_IDENT = E_IDENT(ei_class = ei_class, ei_data = ei_data, ei_osabi = ei_osabi, ei_abiversion = ei_abiversion)

    protected var entryPoint: Elf_Xword = UInt64.ZERO

    protected val segments: MutableList<Segment> = mutableListOf()

    /**
     * Sections Stored in the following order.
     *
     * 1. [nullSec]: (Empty Always at the beginning on ELF Sections)
     * 2. [text]: Executable code.
     * 3. [rodata]: Read-only data.
     * 4. [data]: Initialized writable data.
     * 5. [bss]: Uninitialized writable data.
     * 6. [plt] and [got]: Procedure linkage and global offset tables for dynamic linking.
     * 7. [dynsym] and [dynstr]: Dynamic symbol and string tables for dynamic linking.
     * 8. [rel] or [rela]: Relocation section.
     * 9. [shStrTab], [strTab], [symTab]: Section and symbol tables (for linking and debugging).
     */
    override val sections: MutableList<ELFSection> = mutableListOf()

    val shStrTab = SHStrTab()
    val strTab = StrTab()

    private val nullSec = getOrCreateSection("")
    private val text = getOrCreateSection(".text", Shdr.SHT_text, Shdr.SHF_text.toUInt64())

    override var currentSection: ELFSection = text

    init {
        symbols.add(Symbol.Label("", nullSec, Symbol.Binding.LOCAL, BigInt(BigInteger.ZERO)))
    }

    override fun writeFile(): ByteArray {
        val buffer = Int8Buffer(endianness)

        sections.add(SymTab(strTab))
        sections.add(strTab)
        sections.add(shStrTab)

        val shdrs = calculateShdrs()

        val phdrSize = segments.firstOrNull()?.phdr?.byteSize()?.toULong() ?: 0U
        val phdrCount = segments.size.toULong()

        val ehdr = createELFHeader()

        val totalEhdrBytes = ehdr.byteSize().toUInt64()
        val totalPhdrBytes = (phdrSize * phdrCount).toUInt64()
        val totalSectionBytes = sections.sumOf { it.content.size.toULong() }.toUInt64()
        val phoff = totalEhdrBytes
        val shoff = totalEhdrBytes + totalPhdrBytes + totalSectionBytes

        buffer.writeELFHeader(ehdr, phoff, shoff)
        buffer.writePHDRs(totalEhdrBytes + totalPhdrBytes)
        buffer.writeSections(shdrs)
        buffer.writeSHDRs(shdrs.map { it.second })

        return buffer.toByteArray()
    }

    private fun calculateShdrs(): List<Pair<ELFSection, Shdr>> {
        return sections.map {
            it to when (val shdr = it.createHeader()) {
                is ELF32_Shdr -> {
                    shdr.sh_size = it.content.size.toUInt32()
                    shdr
                }

                is ELF64_Shdr -> {
                    shdr.sh_size = it.content.size.toUInt64()
                    shdr
                }
            }
        }
    }

    private fun createELFHeader(): Ehdr {
        val ehdr = when (e_ident.ei_class) {
            E_IDENT.ELFCLASS32 -> ELF32_Ehdr(
                e_ident = e_ident,
                e_type = e_type,
                e_machine = e_machine,
                e_flags = e_flags,
                e_ehsize = UInt16.ZERO, // assign later
                e_phentsize = Phdr.size(ei_class),
                e_phnum = segments.size.toUInt16(),
                e_shentsize = Shdr.size(ei_class),
                e_shnum = sections.size.toUInt16(),
                e_shstrndx = sections.indexOf(shStrTab).toUInt16(),
                e_entry = entryPoint.toUInt32(),
                e_phoff = UInt32.ZERO, // assign laterc
                e_shoff = UInt32.ZERO // assign later
            )

            E_IDENT.ELFCLASS64 -> ELF64_Ehdr(
                e_ident = e_ident,
                e_type = e_type,
                e_machine = e_machine,
                e_flags = e_flags,
                e_ehsize = UInt16.ZERO, // assign later
                e_phentsize = Phdr.size(ei_class),
                e_phnum = segments.size.toUInt16(),
                e_shentsize = Shdr.size(ei_class),
                e_shnum = sections.size.toUInt16(),
                e_shstrndx = sections.indexOf(shStrTab).toUInt16(),
                e_entry = entryPoint,
                e_phoff = UInt64.ZERO, // assign later
                e_shoff = UInt64.ZERO // assign later
            )

            else -> throw InvalidElfClassException(e_ident.ei_class)
        }
        ehdr.e_ehsize = ehdr.byteSize().toUInt16()
        return ehdr
    }

    private fun Int8Buffer.writeELFHeader(ehdr: Ehdr, phoff: UInt64, shoff: UInt64) {
        when (ehdr) {
            is ELF32_Ehdr -> {
                if (segments.isNotEmpty()) ehdr.e_phoff = phoff.toUInt32()
                if (sections.isNotEmpty()) ehdr.e_shoff = shoff.toUInt32()
            }

            is ELF64_Ehdr -> {
                if (segments.isNotEmpty()) ehdr.e_phoff = phoff
                if (sections.isNotEmpty()) ehdr.e_shoff = shoff
            }
        }

        putAll(ehdr.build(endianness))
    }

    private fun Int8Buffer.writePHDRs(fileIndexOfDataStart: UInt64) {
        // Serialize the program header (Phdr) into the byte buffer
        segments.forEach { segment ->
            // Set FileOffset of Segment Start
            val firstSection = segment.sections.firstOrNull()
            segment.p_offset = if (firstSection != null) {
                fileIndexOfDataStart + sections.takeWhile { it != firstSection }.sumOf { it.content.size.toULong() }.toUInt64()
            } else fileIndexOfDataStart

            putAll(segment.phdr.build(endianness))
        }
    }

    private fun Int8Buffer.writeSections(shdr: List<Pair<ELFSection, Shdr>>) {
        shdr.forEach { (section, header) ->
            val start = size
            putAll(section.content.toArray())
            when (header) {
                is ELF32_Shdr -> {
                    header.sh_offset = start.toUInt32()
                }

                is ELF64_Shdr -> {
                    header.sh_offset = start.toUInt64()
                }
            }
        }
    }

    private fun Int8Buffer.writeSHDRs(shdrs: List<Shdr>) {
        shdrs.forEachIndexed { index, shdr ->
            putAll(shdr.build(endianness))
        }
    }

    protected fun createAndAddSegment(p_type: Elf_Word, p_flags: Elf_Word, p_align: UInt64 = UInt64.ONE): Segment {
        val phdr = when (ei_class) {
            E_IDENT.ELFCLASS32 -> {
                ELF32_Phdr(
                    p_type = p_type,
                    p_flags = p_flags,
                    p_align = p_align.toUInt32()
                )
            }

            E_IDENT.ELFCLASS64 -> {
                ELF64_Phdr(
                    p_type = p_type,
                    p_flags = p_flags,
                    p_align = p_align
                )
            }

            else -> throw InvalidElfClassException(ei_class)
        }
        return Segment(phdr).also { segment ->
            segments.add(segment)
        }
    }

    final override fun createNewSection(name: String, type: UInt32, flags: UInt64, link: ELFSection?, info: String?): ELFSection = ELFSection(name, type, flags, link, info)

    fun Symbol<ELFSection>.toSym(): Sym {
        val binding = when (this.binding) {
            Symbol.Binding.LOCAL -> Sym.STB_LOCAL
            Symbol.Binding.GLOBAL -> Sym.STB_GLOBAL
            Symbol.Binding.WEAK -> Sym.STB_WEAK
        }
        val sym =
            when (this) {
                is Symbol.Abs -> {
                    Sym.createEmpty(ei_class, strTab.addString(this.name), sections.indexOf(section).toUInt16(), type = Sym.STT_NUM, binding = binding).apply {
                        setValue(value.value.ulongValue().toUInt64())
                    }
                }

                is Symbol.Label -> {
                    Sym.createEmpty(ei_class, strTab.addString(this.name), sections.indexOf(section).toUInt16(), type = Sym.STT_NOTYPE, binding = binding).apply {
                        setValue(address().value.ulongValue().toUInt64())
                    }
                }
            }
        return sym
    }

    // CLASSES


    /**
     * This section holds a symbol table, as "Symbol Table'' in this chapter
     * describes. If a file has a loadable segment that includes the symbol table,
     * the section's attributes will include the [SHF_ALLOC] bit; otherwise, that bit
     * will be off.
     */
    inner class SymTab(strTab: StrTab) : ELFSection(
        ".symtab",
        Shdr.SHT_SYMTAB,
        link = strTab,
        entSize = Sym.size(ei_class).toUInt64()
    ) {
        override val name = ".symtab"
        override val content: Int8Buffer = Int8Buffer(endianness).apply {
            symbols.forEach {
                putAll(it.toSym().build(endianness))
            }
        }
        override val reservations: MutableList<InstrReservation> = mutableListOf()
    }

    /**
     * This section holds section names.
     */
    inner class SHStrTab : ELFSection(
        ".shstrtab",
        Shdr.SHT_STRTAB
    ) {
        override val name = ".shstrtab"
        override val content: Int8Buffer = Int8Buffer(endianness)
        override val reservations: MutableList<InstrReservation> = mutableListOf()
        private val stringIndexMap = mutableMapOf<String, UInt32>()

        fun addString(str: String): UInt32 {
            return stringIndexMap.getOrPut(str) {
                val currentOffset = content.size
                content.putAll(str.toASCIIByteArray() + (0).toInt8())
                currentOffset.toUInt32()
            }
        }

        operator fun get(string: String): UInt32? = stringIndexMap[string]

        fun getStringAt(index: UInt): String = content.getZeroTerminated(index.toInt()).toASCIIString()
    }

    /**
     * This section holds strings, most commonly the strings that represent the names
     * associated with symbol table entries. If a file has a loadable segment that
     * includes the symbol string table, the section's attributes will include the
     * [SHF_ALLOC] bit; otherwise, that bit will be off.
     */
    inner class StrTab : ELFSection(
        ".strtab",
        Shdr.SHT_STRTAB
    ) {

        override val name = ".strtab"
        override val content: Int8Buffer = Int8Buffer(endianness)
        override val reservations: MutableList<InstrReservation> = mutableListOf()
        private val stringIndexMap = mutableMapOf<String, UInt32>()

        fun addString(str: String): UInt32 {
            return stringIndexMap.getOrPut(str) {
                val currentOffset = content.size
                content.putAll(str.toASCIIByteArray() + 0.toInt8())
                currentOffset.toUInt32()
            }
        }

        operator fun get(string: String): UInt32? = stringIndexMap[string]

        fun getStringAt(index: UInt): String = content.getZeroTerminated(index.toInt()).toASCIIString()
    }

    open inner class ELFSection(
        override val name: String,
        override var type: UInt32,
        override var flags: UInt64 = UInt64.ZERO,
        override var link: Section? = null,
        override var info: String? = null,
        private val entSize: UInt64? = null,
    ) : Section {
        override var address: BigInt = BigInt.ZERO

        fun createHeader(): Shdr = Shdr.create(ei_class).apply {
            sh_name = shStrTab[name] ?: shStrTab.addString(name)
            sh_type = type

            setAddr(address.value.ulongValue().toUInt64())
            setFlags(flags)

            val currLink = link
            if (currLink != null) {
                sh_link = sections.indexOf(currLink).toUInt32()
            }
            val currInfo = info
            if (currInfo != null) {
                sh_info = strTab[currInfo] ?: strTab.addString(currInfo)
            }
            if (entSize != null) {
                setEntSize(entSize)
            }
        }

        override val content: Int8Buffer = Int8Buffer(endianness)
        override val reservations: MutableList<InstrReservation> = mutableListOf()

        override fun toString(): String = print()
    }

    // INTERFACES


    class Segment(val phdr: Phdr) {
        val sections: MutableList<ELFSection> = mutableListOf()

        var p_offset: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_offset = value.toUInt32()
                    is ELF64_Phdr -> phdr.p_offset = value
                }
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_offset.toUInt64()
                is ELF64_Phdr -> phdr.p_offset
            }

        var p_vaddr: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_vaddr = value.toUInt32()
                    is ELF64_Phdr -> phdr.p_vaddr = value
                }
                calculateSectionAddresses()
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_vaddr.toUInt64()
                is ELF64_Phdr -> phdr.p_vaddr
            }

        var p_paddr: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_paddr = value.toUInt32()
                    is ELF64_Phdr -> phdr.p_paddr = value
                }
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_paddr.toUInt64()
                is ELF64_Phdr -> phdr.p_paddr
            }

        val p_align: Elf_Xword
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_align.toUInt64()
                is ELF64_Phdr -> phdr.p_align
            }

        var p_filesz: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_filesz = value.toUInt32()
                    is ELF64_Phdr -> phdr.p_filesz = value
                }
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_filesz.toUInt64()
                is ELF64_Phdr -> phdr.p_filesz
            }

        var p_memsz: Elf_Xword
            set(value) {
                when (phdr) {
                    is ELF32_Phdr -> phdr.p_memsz = value.toUInt32()
                    is ELF64_Phdr -> phdr.p_memsz = value
                }
            }
            get() = when (phdr) {
                is ELF32_Phdr -> phdr.p_memsz.toUInt64()
                is ELF64_Phdr -> phdr.p_memsz
            }

        // Adds a section to this segment
        fun addSection(section: ELFSection) {
            sections.add(section)
            calculateSegmentSize()
            calculateSectionAddresses()
        }

        // Calculates the size of the segment based on the sections added
        private fun calculateSegmentSize() {
            val size = sections.sumOf { it.content.size.toULong() }.toUInt64()
            p_filesz = size
            p_memsz = size
        }

        // Sets the addresses of each section within the segment based on the segment's start address
        private fun calculateSectionAddresses() {
            var currentAddress = p_vaddr
            sections.forEach { section ->
                section.address = currentAddress.toBigInt()
                currentAddress += section.content.size.toUInt64()
            }
        }

        override fun toString(): String {
            return "${Phdr.getProgramHeaderType(phdr.p_type)},${Phdr.getProgramHeaderFlags(phdr.p_flags)}: ${sections.joinToString { it.print() }}"
        }
    }

    // EXCEPTIONS

    open class ELFBuilderException(message: String) : Exception(message)

    class InvalidElfClassException(ei_class: Elf_Byte) : ELFBuilderException("Invalid ELF Class $ei_class.")
    class InvalidElfDataException(ei_data: Elf_Byte) : ELFBuilderException("Invalid ELF Data $ei_data type.")


}