package cengine.lang.mif

import cengine.lang.Runner
import cengine.lang.mif.ast.MifPsiFile
import cengine.lang.obj.ObjRunner
import cengine.lang.vhdl.toVHDL
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.VirtualFile
import nativeError
import nativeLog

object MifRunner : Runner<MifLang>(MifLang, "mifc") {

    override suspend fun run(project: Project, vararg attrs: String): Boolean {

        var target = Target.VHDL
        var filename: String? = null // file.name.removeSuffix(lang.fileSuffix)
        var filepath: FPath? = null
        var constname = "mem"

        for (i in attrs.indices) {
            val attr = attrs[i]

            when (attr) {
                DEFAULT_FILEPATH_ATTR -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    if (next.isNotEmpty()) {
                        filepath = FPath.delimited(next)
                    } else {
                        nativeError("${this::class.simpleName} expected filepath!")
                        return false
                    }
                }

                "-t", "--target" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    target = Target.entries.firstOrNull {
                        it.name == next.uppercase()
                    } ?: continue
                }

                "-n", "--name" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    if (next.isNotEmpty() && !next.startsWith("-")) {
                        constname = next
                    }
                }

                "-fn", "--filename" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    if (next.isNotEmpty() && !next.startsWith("-")) {
                        filename = next
                    }
                }

                "-h", "--help" -> {
                    nativeLog("""
                        
                        -------------------------------------------------------- $name help --------------------------------------------------------
                            Arguments:
                                $DEFAULT_FILEPATH_ATTR  : file to run
                                -t, --target            : set the target (${Target.entries.joinToString { it.name }})
                                -n, --name              : change name of constant
                                -fn, --filename         : change output filename (without type suffix)
                                -h, --help              : show help
                             
                        -------------------------------------------------------- $name help --------------------------------------------------------
                    """.trimIndent())
                }

                else -> {
                    nativeError("${name}: Invalid Argument $attr (display valid arguments with -h or --help)!")
                }
            }
        }

        val file = resolveFilePath(project, filepath) ?: return false

        if (filename == null) filename = file.name.removeSuffix(lang.fileSuffix)

        val manager = project.getManager(file)
        val psiFile = manager?.updatePsi(file) as? MifPsiFile

        if (manager == null) {
            nativeError("${this::class.simpleName} Unable to find manager for ${file.name}!")
            return false
        }

        if (psiFile == null) {
            nativeError("${this::class.simpleName} Unable to find or create PsiFile for ${file.name}!")
            return false
        }

        when (target) {
            Target.VHDL -> {
                val outputPath = FPath.of(project.fileSystem, MifLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".vhd")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val fileContent = psiFile.toVHDL(filename, constname)
                outputFile.setAsUTF8String(fileContent)
            }
        }

        return true
    }

    enum class Target {
        VHDL
    }
}