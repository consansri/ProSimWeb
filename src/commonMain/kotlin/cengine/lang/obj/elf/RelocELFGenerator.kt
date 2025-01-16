package cengine.lang.obj.elf

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.AsmFile
import cengine.psi.PsiManager

/**
 * ELF File
 *
 * 1. [Ehdr]
 * 2. [Phdr]s
 * 3. [Section]s
 * 4. [Shdr]s
 */
class RelocELFGenerator(
    ei_class: Elf_Byte,
    ei_data: Elf_Byte,
    ei_osabi: Elf_Byte,
    ei_abiversion: Elf_Byte,
    e_machine: Elf_Half,
    e_flags: Elf_Word = Elf_Word.ZERO,
    linkerScript: LinkerScript,
    psiManager: PsiManager<*, *>
) : ELFGenerator(Ehdr.ET_REL, ei_class, ei_data, ei_osabi, ei_abiversion, e_machine, e_flags, linkerScript, psiManager) {

    // PUBLIC METHODS


    override fun orderSectionsAndResolveAddresses() {

    }

}
