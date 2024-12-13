package cengine.lang.obj.elf

import cengine.lang.asm.ast.TargetSpec

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
    e_flags: Elf_Word,
    linkerScript: LinkerScript
) : ELFGenerator(Ehdr.ET_REL, ei_class, ei_data, ei_osabi, ei_abiversion, e_machine, e_flags, linkerScript) {

    constructor(spec: TargetSpec<ELFGenerator>, e_flags: Elf_Word = Elf_Word.ZERO) : this(spec.ei_class, spec.ei_data, spec.ei_osabi, spec.ei_abiversion, spec.e_machine, e_flags, spec.linkerScript)

    // PUBLIC METHODS


    override fun orderSectionsAndResolveAddresses() {

    }

}
