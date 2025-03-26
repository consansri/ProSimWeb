package cengine.lang.mif

import ConsoleContext
import cengine.lang.Runner
import cengine.lang.mif.ast.MifPsiFile
import cengine.lang.vhdl.toVHDL
import cengine.project.Project
import cengine.vfs.FPath
import cengine.vfs.FPath.Companion.toFPath

object MifRunner : Runner<MifLang>("mifc") {
    override val lang: MifLang
        get() = MifLang

    override suspend fun ConsoleContext.runWithContext(project: Project, vararg attrs: String): Boolean {

        var target = Target.VHDL
        var filename: String? = null // file.name.removeSuffix(lang.fileSuffix)
        var filepath: FPath? = null
        var constname = "mem"

        var i = 0
        while(i in attrs.indices){
            val attr = attrs[i]

            when (attr) {
                DEFAULT_FILEPATH_ATTR -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    if (next.isNotEmpty()) {
                        filepath = next.toFPath()
                    } else {
                        error("${this::class.simpleName} expected filepath!")
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

                "-n", "--name" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    if (next.isNotEmpty() && !next.startsWith("-")) {
                        constname = next
                    }
                    i++
                }

                "-fn", "--filename" -> {
                    val next = attrs.getOrNull(i + 1) ?: continue
                    if (next.isNotEmpty() && !next.startsWith("-")) {
                        filename = next
                    }
                    i++
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
                                -h, --help              : show help
                             
                        -------------------------------------------------------- $name help --------------------------------------------------------
                    """.trimIndent()
                    )
                }

                else -> {
                    error("${name}: Invalid Argument $attr (display valid arguments with -h or --help)!")
                }
            }

            i++
        }

        if(filepath == null){
            error("${name}: filepath is missing!")
            return false
        }

        val file = project.fileSystem[directory, filepath]
        if (file == null) {
            error("${name}: Filepath invalid or missing.")
            return false
        }

        if (filename == null) filename = file.name.removeSuffix(lang.fileSuffix)

        val manager = project.getManager(file)
        val psiFile = manager?.updatePsi(file) as? MifPsiFile

        if (manager == null) {
            error("${this::class.simpleName} Unable to find manager for ${file.name}!")
            return false
        }

        if (psiFile == null) {
            error("${this::class.simpleName} Unable to find or create PsiFile for ${file.name}!")
            return false
        }

        when (target) {
            Target.VHDL -> {
                val outputPath = FPath(MifLang.OUTPUT_DIR, file.name.removeSuffix(lang.fileSuffix) + ".vhd")

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