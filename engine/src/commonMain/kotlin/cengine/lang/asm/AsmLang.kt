package cengine.lang.asm

import cengine.console.SysOut
import cengine.lang.LanguageService
import cengine.lang.PsiSupport
import cengine.lang.Runner
import cengine.lang.asm.features.AsmAnnotator
import cengine.lang.asm.features.AsmCompleter
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.psi.elements.PsiFile
import cengine.vfs.VFileSystem

class AsmLang(val spec: AsmSpec<*>) : LanguageService() {
    override val name: String = "Assembly"
    override val fileSuffix: String = ".s"

    override val runConfig: Runner<*> = AsmRunner(this)
    override val psiSupport: PsiSupport = PsiSupport(
        spec.createLexerSet(),
        spec.createParser(),
        PsiFile,
        AsmCompleter(spec),
        AsmAnnotator(spec),
    )

    override fun init(project: Project) {
        super.init(project)

        // Only if project is empty
        if (project.fileSystem.root.getChildren().isEmpty()) {
            SysOut.log("create $name example")
            val filePath = project.fileSystem.root.path + "example$fileSuffix"
            val file = project.fileSystem.createFile(filePath)
            file.setAsUTF8String(spec.contentExample)
        }
    }

    override fun createManager(vfs: VFileSystem): PsiManager<*> {
        return PsiManager<AsmLang>(vfs, PsiManager.Mode.TEXT, this) {

        }
    }
}