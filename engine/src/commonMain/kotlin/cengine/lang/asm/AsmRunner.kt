package cengine.lang.asm

import cengine.console.ConsoleContext
import cengine.console.SysOut
import cengine.editor.annotation.Severity
import cengine.lang.Runner
import cengine.lang.asm.gas.AsmBackend
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.psi.visitor.PsiNotationCollector
import cengine.vfs.FPath
import cengine.vfs.FPath.Companion.toFPath
import cengine.vfs.VirtualFile

class AsmRunner(override val lang: AsmLang) : Runner<AsmLang>(getRunnerName(lang.spec)) {

    companion object {
        const val ASM_RUNNER_PREFIX = "asm-"
        fun getRunnerName(spec: AsmSpec<*>): String = ASM_RUNNER_PREFIX + spec.shortName
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
                    executable(project, manager, file)
                }
            }
        }

        return true
    }

    private suspend fun ConsoleContext.executable(project: Project, manager: PsiManager<*>, file: VirtualFile) {
        val asmFile = manager.updatePsi(file, this)

        if (!asmFile.valid) {
            error("Invalid file: ${file.name} Fix Syntax issues first!")
            return
        }

        val generator = lang.spec.createGenerator()
        val backend = AsmBackend(project, asmFile, generator, this)

        val outputPath = directory.path + FPath(file.name.removeSuffix(lang.fileSuffix) + generator.outputFileSuffix)

        project.fileSystem.deleteFile(outputPath)
        val outputFile = project.fileSystem.createFile(outputPath)

        val content = backend.assemble() ?: run {
            error("Unable to assemble file")
            return@executable
        }

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