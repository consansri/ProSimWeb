package cengine.lang.obj

import cengine.lang.Runner
import cengine.lang.mif.MifConverter
import cengine.lang.obj.ObjRunner.Type
import cengine.lang.obj.elf.ELFFile
import cengine.lang.vhdl.toVHDL
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.VirtualFile


/**
 * Attributes:
 * -type [Type] or -t [Type]
 */
object ObjRunner : Runner<ObjLang>(ObjLang, "convert") {
    override val defaultAttrs: List<String> = listOf("-t", Type.MIF.name, "-cn", "mem")

    override suspend fun global(project: Project, vararg attrs: String) {

    }

    override suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String) {

        var type = Type.MIF
        var filename = file.name.removeSuffix(lang.fileSuffix)
        var constname = "mem"

        for (i in attrs.indices) {
            val attr = attrs[i]

            when (attr) {
                "-t", "--type" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    type = Type.entries.firstOrNull {
                        it.name == next.uppercase()
                    } ?: continue
                }

                "-cn", "--constant" -> {
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

                else -> {

                }
            }
        }

        when (type) {
            Type.MIF -> {
                val outputPath = FPath.of(project.fileSystem, ObjLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".mif")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val manager = project.getManager(file) ?: return
                val objFile = manager.getPsiFile(file) as? ELFFile ?: return

                val fileContent = MifConverter.parseElf(objFile).build()
                outputFile.setAsUTF8String(fileContent)
            }

            Type.VHDL -> {
                val outputPath = FPath.of(project.fileSystem, ObjLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".vhd")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val manager = project.getManager(file) ?: return
                val objFile = manager.getPsiFile(file) as? ELFFile ?: return

                val fileContent = objFile.toVHDL(filename, constname)
                outputFile.setAsUTF8String(fileContent)
            }
        }
    }


    enum class Type {
        MIF,
        VHDL
    }

}