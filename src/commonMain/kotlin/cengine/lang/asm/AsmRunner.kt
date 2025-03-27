package cengine.lang.asm

import ConsoleContext
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
import IOContext
import cengine.vfs.FPath.Companion.toFPath

class AsmRunner(override val lang: AsmLang) : Runner<AsmLang>(getRunnerName(lang.spec)) {

    companion object {
        const val ASM_RUNNER_PREFIX = "asm-"
        fun getRunnerName(spec: TargetSpec<*>): String = ASM_RUNNER_PREFIX + spec.shortName
    }

    override suspend fun ConsoleContext.runWithContext(project: Project, vararg attrs: String): Boolean {
        var target = Target.EXEC
        var filepath: FPath? = null

        var i = 0
        while (i in attrs.indices) {
            when (val attr = attrs[i]) {
                DEFAULT_FILEPATH_ATTR -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    if (next.isNotEmpty()) {
                        filepath = next.toFPath()
                    } else {
                        error("expected filepath")
                        usage("$DEFAULT_FILEPATH_ATTR <path>")
                        return false
                    }
                    i++
                }

                "-t", "--target" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    target = Target.entries.firstOrNull {
                        it.name == next.uppercase()
                    } ?: continue
                    i++
                }

                "-h", "--help" -> {
                    streamln(
                        """
                        -------------------------------------------------------- $name help --------------------------------------------------------
                            Arguments:
                                $DEFAULT_FILEPATH_ATTR  : file to run
                                -t, --target            : set the target (${Target.entries.joinToString { it.name }})
                                -h, --help              : show help
                             
                        -------------------------------------------------------- $name help --------------------------------------------------------
                    """.trimIndent()
                    )
                }

                else -> {
                    error("invalid argument $attr (display valid arguments with -h or --help)")
                }
            }
            i++
        }

        if (filepath == null) {
            error("filepath is missing")
            usage("$DEFAULT_FILEPATH_ATTR <path>")
            return false
        }

        val file = project.fileSystem[directory, filepath]
        if (file == null) {
            error("filepath is invalid: $filepath")
            usage("$DEFAULT_FILEPATH_ATTR <path>")
            return false
        }

        when (target) {
            Target.EXEC -> {
                val manager = project.getManager(file)
                if (manager != null) {
                    executable(project.fileSystem, manager, file)
                }
            }
        }

        return true
    }

    private suspend fun ConsoleContext.executable(vfs: VFileSystem, manager: PsiManager<*, *>, file: VirtualFile) {
        val asmFile = manager.updatePsi(file) as AsmFile

        val generator =  lang.spec.createGenerator(manager)

        val outputPath = directory.path + FPath(file.name.removeSuffix(lang.fileSuffix) + generator.fileSuffix)

        vfs.deleteFile(outputPath)
        val outputFile = vfs.createFile(outputPath)

        val content = generator.generate(asmFile.program)

        val collector = PsiNotationCollector()
        asmFile.accept(collector)

        collector.annotations.forEach {
            when (it.severity) {
                Severity.INFO -> SysOut.info(it.createConsoleMessage(asmFile))
                Severity.WARNING -> SysOut.warn(it.createConsoleMessage(asmFile))
                Severity.ERROR -> SysOut.error(it.createConsoleMessage(asmFile))
            }
        }

        if (collector.annotations.none { it.severity == Severity.ERROR }) {
            outputFile.setContent(content)
        }

        log("generated ${outputFile.path}")
    }

    enum class Target {
        EXEC
    }
}