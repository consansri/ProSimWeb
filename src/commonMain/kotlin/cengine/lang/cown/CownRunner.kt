package cengine.lang.cown

import cengine.lang.Runner
import cengine.project.Project
import cengine.vfs.VirtualFile

object CownRunner: Runner<CownLang>(CownLang,"CownRun") {
    override val defaultAttrs: List<String> = emptyList()

    override suspend fun global(project: Project, vararg attrs: String) {

    }

    override suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String) {

    }
}