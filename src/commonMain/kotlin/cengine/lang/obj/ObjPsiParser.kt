package cengine.lang.obj

import cengine.lang.obj.elf.ELF32File
import cengine.lang.obj.elf.ELF64File
import cengine.lang.obj.elf.ELFFile
import cengine.lang.obj.elf.E_IDENT
import cengine.psi.PsiManager
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile

object ObjPsiParser : PsiParser<ObjPsiFile> {
    override suspend fun parse(file: VirtualFile, manager: PsiManager<*, *>): ObjPsiFile {
        val content = file.getContent()

        // Try ELF

        val e_ident = E_IDENT.extractFrom(content)

        when (e_ident.ei_class) {
            E_IDENT.ELFCLASS32 -> return ELF32File(file, manager)
            E_IDENT.ELFCLASS64 -> return ELF64File(file, manager)
        }

        // Return InvalidObjFile

        return InvalObjFile(file, manager)
    }
}