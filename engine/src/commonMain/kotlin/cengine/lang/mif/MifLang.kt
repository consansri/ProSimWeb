package cengine.lang.mif

import cengine.lang.LanguageService
import cengine.lang.PsiSupport
import cengine.lang.Runner
import cengine.psi.PsiManager
import cengine.vfs.VFileSystem

object MifLang : LanguageService() {

    override val name: String = "MIF"
    override val fileSuffix: String = ".mif"
    override val runConfig: Runner<MifLang> = MifRunner
    override val psiSupport: PsiSupport? = null

    override fun createManager(vfs: VFileSystem): PsiManager<*> = PsiManager<MifLang>(
        vfs,
        PsiManager.Mode.TEXT,
        this
    ) {

    }
}