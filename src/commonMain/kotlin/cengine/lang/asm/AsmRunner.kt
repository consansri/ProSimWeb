package cengine.lang.asm

import cengine.editor.annotation.Severity
import cengine.lang.Runner
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.AsmFile
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.psi.impl.PsiNotationCollector
import cengine.vfs.FPath
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import nativeError
import nativeInfo
import nativeLog
import nativeWarn

class AsmRunner(lang: AsmLang) : Runner<AsmLang>(lang, getRunnerName(lang.spec)) {

    companion object{
        const val ASM_RUNNER_PREFIX = "asm-"
        fun getRunnerName(spec: TargetSpec<*>): String = ASM_RUNNER_PREFIX + spec.shortName
    }

    override suspend fun global(project: Project, vararg attrs: String) {

    }

    override suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String) {
        var target = Target.EXEC

        for (i in attrs.indices) {
            when (val attr = attrs[i]) {
                "-t", "--target" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    target = Target.entries.firstOrNull {
                        it.name == next.uppercase()
                    } ?: continue
                }

                "-h", "--help" -> {
                    nativeLog("""
                        
                        -------------------------------------------------------- $name help --------------------------------------------------------
                            Arguments:
                                -t, --target            : set the target (${Target.entries.joinToString { it.name }})
                                -h, --help              : show help
                             
                        -------------------------------------------------------- $name help --------------------------------------------------------
                    """.trimIndent())
                }

                else -> {
                    nativeError("${name}: Invalid Argument $attr (display valid arguments with -h or --help)!")
                }
            }
        }

        when (target) {
            Target.EXEC -> {
                val manager = project.getManager(file)
                if(manager != null){
                    executable(project.fileSystem, manager, file)
                }
            }
        }
    }

    private suspend fun executable(vfs: VFileSystem, manager: PsiManager<*,*>, file: VirtualFile) {
        val asmFile = manager.updatePsi(file) as AsmFile
        nativeLog("Updated PsiFile $asmFile ${manager.printCache()}")

        val generator = lang.spec.createGenerator(manager)

        val outputPath = FPath.of(vfs, AsmLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + generator.fileSuffix)

        vfs.deleteFile(outputPath)
        val outputFile = vfs.createFile(outputPath)

        val content = generator.generate(asmFile.program)

        val collector = PsiNotationCollector()
        asmFile.accept(collector)

        collector.annotations.forEach {
            when (it.severity) {
                Severity.INFO -> nativeInfo(it.createConsoleMessage(asmFile))
                Severity.WARNING -> nativeWarn(it.createConsoleMessage(asmFile))
                Severity.ERROR -> nativeError(it.createConsoleMessage(asmFile))
            }
        }

        if (collector.annotations.none { it.severity == Severity.ERROR }) {
            outputFile.setContent(content)
        }
    }

    enum class Target {
        EXEC
    }
}