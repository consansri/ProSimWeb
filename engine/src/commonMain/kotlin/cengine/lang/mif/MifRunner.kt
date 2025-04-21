package cengine.lang.mif

import cengine.console.ConsoleContext
import cengine.lang.Runner
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.FPath.Companion.toFPath

object MifRunner : Runner<MifLang>("mifc") {
    override val lang: MifLang
        get() = MifLang

    override suspend fun ConsoleContext.runWithContext(project: Project, vararg attrs: String): Boolean {

        var target: Target? = null
        var filename: String? = null // file.name.removeSuffix(lang.fileSuffix)
        var filepath: FPath? = null
        var dataWidth = 32
        var chunkSize = 4
        var constname = "mem"

        var index = 0
        while (index in attrs.indices) {
            val attr = attrs[index]

            when (attr) {
                DEFAULT_FILEPATH_ATTR -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    if (next.isNotEmpty()) {
                        filepath = next.toFPath()
                    } else {
                        error("expected filepath")
                        usage("$DEFAULT_FILEPATH_ATTR <path>")
                        return false
                    }
                    index++
                }

                "-t", "--target" -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    target = Target.entries.firstOrNull {
                        it.name == next.uppercase()
                    } ?: continue
                    index++
                }

                "-n", "--name" -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    if (next.isNotEmpty() && !next.startsWith("-")) {
                        constname = next
                    }
                    index++
                }

                "-fn", "--filename" -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    if (next.isNotEmpty() && !next.startsWith("-")) {
                        filename = next
                    }
                    index++
                }

                "-dw", "--data-width" -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    val dw = next.toIntOrNull()
                    if (next.isNotEmpty() && dw != null) {
                        dataWidth = dw
                    }
                    index++
                }

                "-cs", "--chunk-size" -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    val cs = next.toIntOrNull()
                    if (next.isNotEmpty() && cs != null) {
                        chunkSize = cs
                    }
                    index++
                }

                "-h", "--help" -> {
                    streamln(
                        """
                        -------------------------------------------------------- $name help --------------------------------------------------------
                            Arguments:
                                $DEFAULT_FILEPATH_ATTR  : file to run
                                -t, --target            : set the target (${Target.entries.joinToString { it.name }})
                                -n, --name              : change name of constant
                                -fn, --filename         : change output filename (without type suffix)
                                -dw, --data-width       : customize data-width in bits (defaults to 32)
                                -cs, --chunk-size       : customize chunk-size (defaults to 4)
                                -h, --help              : show help
                             
                        -------------------------------------------------------- $name help --------------------------------------------------------
                    """.trimIndent()
                    )
                }

                else -> {
                    error("invalid argument $attr (display valid arguments with -h or --help)")
                }
            }

            index++
        }

        if (filepath == null) {
            error("filepath is missing")
            usage("$DEFAULT_FILEPATH_ATTR <path>")
            return false
        }

        val file = project.fileSystem[directory, filepath]
        if (file == null) {
            error("filepath invalid or missing")
            usage("$DEFAULT_FILEPATH_ATTR <path>")
            return false
        }

        if (filename == null) filename = file.name.removeSuffix(lang.fileSuffix)

        val manager = project.getManager(file)
        val psiFile = manager?.updatePsi(file)

        if (manager == null) {
            error("unable to find manager: ${file.name}")
            return false
        }

        if (psiFile == null) {
            error("unable to find or create PsiFile: ${file.name}")
            return false
        }

        if (target == null) {
            error("invalid target (targets: ${Target.entries})")
            usage("-t <target>")
            return false
        }

        when (target) {
            Target.VHDL -> {
                val outputPath = directory.path + FPath(file.name.removeSuffix(lang.fileSuffix) + ".vhd")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                TODO()

                /*val fileContent = psiFile.toVHDL(this, filename, constname, dataWidth, chunkSize)
                outputFile.setAsUTF8String(fileContent)*/
                log("generated ${outputFile.path}")
            }
        }

        return true
    }

    enum class Target {
        VHDL
    }
}