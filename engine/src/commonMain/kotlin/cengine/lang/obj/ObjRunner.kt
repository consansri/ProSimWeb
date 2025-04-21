package cengine.lang.obj

import cengine.console.ConsoleContext
import cengine.lang.Runner
import cengine.lang.mif.toMif
import cengine.lang.obj.elf.ELFFile
import cengine.lang.vhdl.toVHDL
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.FPath.Companion.toFPath


/**
 * Attributes:
 * -type [Target] or -t [Target]
 */
object ObjRunner : Runner<ObjLang>("objc") {

    override val lang: ObjLang get() = ObjLang

    override suspend fun ConsoleContext.runWithContext(project: Project, vararg attrs: String): Boolean {
        var target: Target? = null
        var filepath: FPath? = null
        var filename: String? = null // file.name.removeSuffix(lang.fileSuffix)
        var constname = "mem"
        var addrWidth = 32
        var dataWidth = 32
        var chunkSize = 4

        var index = 0
        while (index in attrs.indices) {
            when (val attr = attrs[index]) {
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

                "-aw", "--address-width" -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    val aw = next.toIntOrNull()
                    if (next.isNotEmpty() && aw != null) {
                        addrWidth = aw
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
                                -aw, --address-width    : customize address-width in bits (defaults to 32)
                                -cs, --chunk-size       : customize chunk-size (defaults to 4)
                                -h, --help              : show help
                             
                        -------------------------------------------------------- $name help --------------------------------------------------------
                    """.trimIndent()
                    )
                }

                else -> {
                    error("$name: Invalid Argument $attr (display valid arguments with -h or --help)!")
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
            usage("-f <path>")
            return false
        }

        if (filename == null) filename = file.name.removeSuffix(lang.fileSuffix)

        val manager = project.getManager(file)
        if (manager == null) {
            error("unable to find manager: ${file.name}")
            return false
        }
        val objFile = manager.getPsiFile(file) as? ELFFile ?: manager.updatePsi(file) as? ELFFile
        if (objFile == null) {
            error("unable to find or create PsiFile: ${file.name}")
            return false
        }

        if (target == null) {
            error("invalid target (targets: ${Target.entries})")
            usage("-t <target>")
            return false
        }

        when (target) {
            Target.MIF -> {
                val outputPath = directory.path + FPath(file.name.removeSuffix(lang.fileSuffix) + ".mif")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val fileContent = objFile.toMif(this, addrWidth, dataWidth, chunkSize)
                outputFile.setAsUTF8String(fileContent)
                log("generated ${outputFile.path}")
            }

            Target.VHDL -> {
                val outputPath = directory.path + FPath(file.name.removeSuffix(lang.fileSuffix) + ".vhd")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val fileContent = objFile.toVHDL(this, filename, constname, dataWidth, chunkSize)
                outputFile.setAsUTF8String(fileContent)
                log("generated ${outputFile.path}")
            }
        }

        return true
    }

    enum class Target {
        MIF,
        VHDL
    }

}