package cengine.lang.mif

import cengine.lang.Runner
import cengine.lang.mif.ast.MifPsiFile
import cengine.lang.obj.ObjLang
import cengine.lang.obj.ObjRunner
import cengine.lang.vhdl.toVHDL
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.VirtualFile
import nativeError

object MifRunner : Runner<MifLang>(MifLang, "mifc") {
    override val defaultAttrs: List<String> = listOf("-t", Type.VHDL.name, "-n", "mem")

    override suspend fun global(project: Project, vararg attrs: String) {

    }

    override suspend fun onFile(project: Project, file: VirtualFile, vararg attrs: String) {
        val manager = project.getManager(file)
        val psiFile = manager?.updatePsi(file) as? MifPsiFile

        var type = Type.VHDL
        var filename = file.name.removeSuffix(lang.fileSuffix)
        var constname = "mem"

        if (manager == null) {
            nativeError("${this::class.simpleName} Unable to find manager for ${file.name}!")
            return
        }

        if (psiFile == null) {
            nativeError("${this::class.simpleName} Unable to find or create PsiFile for ${file.name}!")
            return
        }

        for (i in attrs.indices) {
            val attr = attrs[i]

            when (attr) {
                "-t", "--type" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    type = Type.entries.firstOrNull {
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

                else -> {

                }
            }
        }

        when (type) {
            Type.VHDL -> {
                val outputPath = FPath.of(project.fileSystem, MifLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".vhd")

                project.fileSystem.deleteFile(outputPath)
                val outputFile = project.fileSystem.createFile(outputPath)

                val fileContent = psiFile.toVHDL(filename, constname)
                outputFile.setAsUTF8String(fileContent)
            }
        }

    }

    enum class Type {
        VHDL
    }
}