package cengine.lang.obj

import cengine.lang.Runner
import cengine.lang.mif.toMif
import cengine.lang.obj.ObjRunner.Target
import cengine.lang.obj.elf.ELFFile
import cengine.lang.vhdl.toVHDL
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.VirtualFile
import nativeError
import nativeLog


/**
 * Attributes:
 * -type [Target] or -t [Target]
 */
object ObjRunner : Runner<ObjLang>(ObjLang, "objc") {

    override suspend fun global(project: Project, vararg attrs: String) {

    }

    override suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String) {

        var target = Target.MIF
        var filename = file.name.removeSuffix(lang.fileSuffix)
        var constname = "mem"
        var addrWidth: Int? = null

        val manager = project.getManager(file)
        if (manager == null) {
            nativeError("${this::class.simpleName} Unable to find manager for ${file.name}!")
            return
        }
        val objFile = manager.getPsiFile(file) as? ELFFile ?: manager.updatePsi(file) as? ELFFile
        if (objFile == null) {
            nativeError("${this::class.simpleName} Unable to find or create PsiFile for ${file.name}!")
            return
        }

        for (i in attrs.indices) {
            val attr = attrs[i]

            when (attr) {
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

                "-aw", "--address-width" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    val aw = next.toIntOrNull()
                    if (next.isNotEmpty() && aw != null) {
                        addrWidth = aw
                    }
                }

                "-h", "--help" -> {
                    nativeLog("""
                        
                        -------------------------------------------------------- $name help --------------------------------------------------------
                            Arguments:
                                -t, --target            : set the target (${Target.entries.joinToString { it.name }})
                                -n, --name              : change name of constant
                                -fn, --filename         : change output filename (without type suffix)
                                -aw, --address-width    : customize address-width (in bits)
                                -h, --help              : show help
                             
                        -------------------------------------------------------- $name help --------------------------------------------------------
                    """.trimIndent())
                }

                else -> {
                    nativeError("$name: Invalid Argument $attr (display valid arguments with -h or --help)!")
                }
            }
        }

        when (target) {
            Target.MIF -> {
                val outputPath = FPath.of(project.fileSystem, ObjLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".mif")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val fileContent = objFile.toMif(addrWidth)
                outputFile.setAsUTF8String(fileContent)
            }

            Target.VHDL -> {
                val outputPath = FPath.of(project.fileSystem, ObjLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".vhd")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val fileContent = objFile.toVHDL(filename, constname)
                outputFile.setAsUTF8String(fileContent)
            }
        }
    }


    enum class Target {
        MIF,
        VHDL
    }

}