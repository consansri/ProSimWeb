package cengine.lang.obj

import cengine.lang.Runner
import cengine.lang.mif.MifConverter
import cengine.lang.obj.ObjRunner.Type
import cengine.lang.obj.elf.ELFFile
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.VirtualFile


/**
 * Attributes:
 * -type [Type] or -t [Type]
 */
object ObjRunner : Runner<ObjLang>(ObjLang, "convert") {

    override suspend fun global(project: Project, vararg attrs: String) {

    }

    override suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String) {

        var type = Type.MIF

        for (i in attrs.indices) {
            val attr = attrs[i]

            when (attr) {
                "-t", "-type" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    type = Type.entries.firstOrNull {
                        it.name == next.uppercase()
                    } ?: continue
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
        }
    }


    enum class Type {
        MIF
    }

}