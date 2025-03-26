package ui.uilib.console

import cengine.vfs.FPath.Companion.toFPath


data class ShellCmd(val keyword: String, val onPrompt: ShellContext.(attrs: List<String>) -> Unit) {

    companion object {
        val BASE = listOf(
            ShellCmd("clear") { attrs ->
                clear()
            },
            ShellCmd("ls") { attrs ->
                // List names of children in the current directory
                streamln(directory.getChildren().joinToString { it.name })
            },
            ShellCmd("cd") { attrs ->
                // Change directory command
                // No argument: go to the root of the virtual file system.
                if (attrs.isEmpty()) {
                    directory = project.fileSystem.root
                } else if (attrs.size == 1) {
                    directory = project.fileSystem[directory, attrs[0].toFPath()] ?: return@ShellCmd error("Invalid path ${attrs[0]}")
                } else {
                    streamln("Error: Expecting one relative filepath argument but received: ${attrs.joinToString(" ") { it }}")
                }
            },
            ShellCmd("pwd") { attrs ->
                // Print working directory command.
                // This assumes that 'directory' has a method getAbsolutePath() returning a string.
                streamln(directory.path.toString())
            },
            ShellCmd("mkdir") { attrs ->
                // Create a new directory.
                if (attrs.size != 1) {
                    usage("mkdir <directory_name>")
                } else {
                    val destPath = attrs[0].toFPath()
                    val parentDir = project.fileSystem[directory, destPath] ?: return@ShellCmd error("Invalid path $destPath")

                    if (parentDir.getChildren().any { it.name == destPath.last() }) {
                        streamln("Error: A file or directory named '${attrs[0]}' already exists")
                    } else {
                        // Create a new virtual directory. (Assuming you have a VirtualDirectory class.)
                        project.fileSystem.createFile(parentDir.path + destPath.last(), true)
                    }
                }
            },
            ShellCmd("rm") { attrs ->
                // Usage: rm [-r] <target>
                if (attrs.isEmpty()) return@ShellCmd usage("rm [-r] <target>")

                var recursive = false
                var targetPath: String? = null

                if (attrs[0] == "-r") {
                    recursive = true
                    if (attrs.size > 1) {
                        targetPath = attrs[1]
                    } else {
                        return@ShellCmd usage("rm -r <target>")
                    }
                } else {
                    targetPath = attrs[0]
                }

                // Find the target file/directory in the current directory.
                val target = project.fileSystem[directory, targetPath.toFPath()] ?: return@ShellCmd error("No such file or directory: $targetPath")

                if (target.isDirectory && !recursive) {
                    return@ShellCmd error("$targetPath is a directory. Use rm -r to remove directories.")
                }

                // Remove the target.
                project.fileSystem.deleteFile(target.path, true)
            },
            ShellCmd("touch") { attrs ->
                if (attrs.size != 1) {
                    usage("touch <filename>")
                    return@ShellCmd
                }
                // Convert the provided argument to an FPath.
                val filePath = attrs[0].toFPath()

                if (filePath.isEmpty()) {
                    usage("touch <filename>")
                    return@ShellCmd
                }

                // Resolve the parent directory relative to the current directory.
                val parentDir = project.fileSystem[directory, filePath.withoutLast()] ?: return@ShellCmd error("Invalid path: ${filePath.withoutLast()}")

                project.fileSystem.createFile(parentDir.path + filePath.last(), false)
            },
            ShellCmd("cat") { attrs ->
                if (attrs.size != 1) {
                    usage("cat <filename>")
                    return@ShellCmd
                }

                val file = project.fileSystem[directory, attrs[0].toFPath()] ?: return@ShellCmd usage("cat <filename>")
                if (file.isDirectory) {
                    error("File is a directory!")
                    return@ShellCmd
                }

                streamln(file.getAsUTF8String())
            }
        )
    }

}