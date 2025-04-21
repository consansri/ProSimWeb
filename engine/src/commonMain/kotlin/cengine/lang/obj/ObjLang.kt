package cengine.lang.obj

import cengine.console.IOContext
import cengine.console.SysOut
import cengine.lang.LanguageService
import cengine.lang.PsiSupport
import cengine.lang.Runner
import cengine.lang.obj.elf.ELF32File
import cengine.lang.obj.elf.ELF64File
import cengine.lang.obj.elf.E_IDENT
import cengine.psi.PsiManager
import cengine.psi.core.PsiParser
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile

object ObjLang : LanguageService() {

    override val name: String = "ObjLang"
    override val fileSuffix: String = ".o"

    override val runConfig: Runner<ObjLang> = ObjRunner
    override val psiParser: PsiParser = ObjPsiParser
    override val psiSupport: PsiSupport? = null

    override fun createManager(vfs: VFileSystem): PsiManager<ObjLang> = PsiManager(
        vfs,
        PsiManager.Mode.BINARY,
        this
    ) {

    }

    object ObjPsiParser : PsiParser {
        override suspend fun parse(file: VirtualFile, ioContext: IOContext): ObjPsiFile {
            val content = file.getContent()

            // Try ELF

            try {
                val e_ident = E_IDENT.extractFrom(content)

                when (e_ident.ei_class) {
                    E_IDENT.ELFCLASS32 -> return ELF32File(file)
                    E_IDENT.ELFCLASS64 -> return ELF64File(file)
                }
            } catch (e: Exception) {
                SysOut.error(e.toString())
            }

            // Return InvalidObjFile

            return InvalObjFile(file)
        }
    }

}