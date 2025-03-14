package cengine.lang.mif

import cengine.lang.Runner
import cengine.project.Project
import cengine.vfs.VirtualFile

object MifRunner : Runner<MifLang>(MifLang, "mif") {
    override val defaultAttrs: List<String> = emptyList()

    override suspend fun global(project: Project, vararg attrs: String) {

    }

    override suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String) {
        val manager = project.getManager(file) ?: return
        val psiFile = manager.updatePsi(file)
    }
}