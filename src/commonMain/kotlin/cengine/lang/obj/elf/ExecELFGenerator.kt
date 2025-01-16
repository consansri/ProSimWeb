package cengine.lang.obj.elf

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.AsmFile
import cengine.psi.PsiManager
import cengine.psi.core.*
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import kotlin.experimental.*

/**
 * ELF File
 *
 * 1. [Ehdr]
 * 2. [Phdr]s
 * 3. [Section]s
 * 4. [Shdr]s
 */
class ExecELFGenerator(
    ei_class: Elf_Byte,
    ei_data: Elf_Byte,
    ei_osabi: Elf_Byte,
    ei_abiversion: Elf_Byte,
    e_machine: Elf_Half,
    e_flags: Elf_Word = Elf_Word.ZERO,
    linkerScript: LinkerScript,
    psiManager: PsiManager<*, *>
) : ELFGenerator(Ehdr.ET_EXEC, ei_class, ei_data, ei_osabi, ei_abiversion, e_machine, e_flags, linkerScript, psiManager) {

    private val segAlign get() = linkerScript.segmentAlign

    private val phdrSegment = createAndAddSegment(p_type = Phdr.PT_PHDR, p_flags = Phdr.PF_R, p_align = segAlign)

    private val textSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R or Phdr.PF_X, p_align = segAlign)

    private val dataSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R or Phdr.PF_W, p_align = segAlign)

    private val rodataSegment = createAndAddSegment(p_type = Phdr.PT_LOAD, p_flags = Phdr.PF_R, p_align = segAlign)

    override fun orderSectionsAndResolveAddresses() {
        // Assign Sections to Segments
        sections.forEach {
            it.assignToSegment()
        }

        // Order Sections
        segments.forEach {
            sections.removeAll(it.sections.toSet())
        }
        var currentMemoryAddress = UInt64.ZERO
        segments.forEach { segment ->
            sections.addAll(segment.sections)
            // apply padding
            if (currentMemoryAddress % segment.p_align != UInt64.ZERO) {
                val padding = segment.p_align - (currentMemoryAddress % segment.p_align)
                currentMemoryAddress += padding
            }

            // Assign Segment Address
            segment.p_vaddr = currentMemoryAddress
            segment.p_paddr = currentMemoryAddress
            currentMemoryAddress += segment.p_memsz
        }

        linkerScript.textStart?.let {
            textSegment.p_vaddr = it.value.ulongValue().toUInt64()
        }

        linkerScript.dataStart?.let {
            dataSegment.p_vaddr = it.value.ulongValue().toUInt64()
        }

        linkerScript.rodataStart?.let {
            rodataSegment.p_vaddr = it.value.ulongValue().toUInt64()
        }

        // Set Entry Point
        entryPoint = textSegment.p_vaddr
    }

    // PRIVATE METHODS

    private fun ELFSection.assignToSegment() {
        when {
            isText() -> textSegment.addSection(this)
            isData() -> dataSegment.addSection(this)
            isRoData() -> rodataSegment.addSection(this)
        }
    }

}