package cengine.lang.obj

import ConsoleContext
import cengine.lang.LanguageService
import cengine.lang.Runner
import cengine.lang.asm.AsmLang
import cengine.lang.mif.toMif
import cengine.lang.obj.ObjRunner.Target
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
        var target = Target.MIF
        var filepath: FPath? = null
        var filename: String? = null // file.name.removeSuffix(lang.fileSuffix)
        var constname = "mem"
        var addrWidth: Int? = null

        var index = 0
        while(index in attrs.indices){
            when (val attr = attrs[index]) {
                DEFAULT_FILEPATH_ATTR -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    if (next.isNotEmpty()) {
                        filepath = next.toFPath()
                    } else {
                        error("${this::class.simpleName} expected filepath!")
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

                "-aw", "--address-width" -> {
                    val next = attrs.getOrNull(index + 1) ?: continue
                    val aw = next.toIntOrNull()
                    if (next.isNotEmpty() && aw != null) {
                        addrWidth = aw
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
                                -aw, --address-width    : customize address-width (in bits)
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

        if(filepath == null){
            error("${name}: Filepath is missing.")
            return false
        }

        val file = project.fileSystem[directory, filepath]

        if (file == null) {
            error("$name: Filepath invalid or missing.")
            return false
        }

        if (filename == null) filename = file.name.removeSuffix(lang.fileSuffix)

        val manager = project.getManager(file)
        if (manager == null) {
            error("${this::class.simpleName} Unable to find manager for ${file.name}!")
            return false
        }
        val objFile = manager.getPsiFile(file) as? ELFFile ?: manager.updatePsi(file) as? ELFFile
        if (objFile == null) {
            error("${this::class.simpleName} Unable to find or create PsiFile for ${file.name}!")
            return false
        }

        when (target) {
            Target.MIF -> {
                val outputPath = FPath(ObjLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".mif")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val fileContent = objFile.toMif(addrWidth)
                outputFile.setAsUTF8String(fileContent)
            }

            Target.VHDL -> {
                val outputPath = FPath(ObjLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".vhd")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val fileContent = objFile.toVHDL(filename, constname)
                outputFile.setAsUTF8String(fileContent)
            }
        }

        return true
    }


    enum class Target {
        MIF,
        VHDL
    }

}