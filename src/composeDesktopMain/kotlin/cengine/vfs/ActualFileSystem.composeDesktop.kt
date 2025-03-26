package cengine.vfs

import SysOut
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Platform-specific implementation of the actual file system operations.
 *
 * @property rootPath The root path of this file system.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ActualFileSystem actual constructor(actual val rootPath: FPath) {

    /**
     * Reads the content of a file.
     *
     * @param path The relative path of the file to read.
     * @return The content of the file as ByteArray.
     */
    actual fun readFile(path: FPath): ByteArray {
        return try {
            Files.readAllBytes(Paths.get(getAbsolutePath(path)))
        } catch (e: FileAlreadyExistsException) {
            ByteArray(0)
        }
    }

    /**
     * Writes content to a file.
     *
     * @param path The relative path of the file to write.
     * @param content The content to write to the file.
     */
    actual fun writeFile(path: FPath, content: ByteArray) {
        Files.write(Paths.get(getAbsolutePath(path)), content)
    }

    /**
     * Deletes a file or directory.
     *
     * @param path The relative path of the file or directory to delete.
     */
    actual fun deleteFile(path: FPath) {
        try {
            Files.delete(Paths.get(getAbsolutePath(path)))
        } catch (e: Exception) {
            // if not existent, then it shouldn't need to be deleted.
        }
    }

    /**
     * Creates a file or directory.
     *
     * @param path The relative path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    actual fun createFile(path: FPath, isDirectory: Boolean) {
        if (!Files.exists(Paths.get(getAbsolutePath(path)))) {
            if (isDirectory) {
                Files.createDirectory(Paths.get(getAbsolutePath(path)))
            } else {
                Files.createFile(Paths.get(getAbsolutePath(path)))
            }
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @param path The relative path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    actual fun listDirectory(path: FPath): List<String> {
        return Files.list(Paths.get(getAbsolutePath(path))).use { stream ->
            stream.map { it.fileName.toString() }.toList()
        }
    }

    /**
     * Checks if a path represents a directory.
     *
     * @param path The relative path to check.
     * @return True if the path is a directory, false otherwise.
     */
    actual fun isDirectory(path: FPath): Boolean = Files.isDirectory(Paths.get(getAbsolutePath(path)))

    /**
     * Checks if a file or directory exists.
     *
     * @param path The relative path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    actual fun exists(path: FPath): Boolean = Files.exists(Paths.get(getAbsolutePath(path)))

    /**
     * Converts a relative path to an absolute path using [rootPath].
     *
     * @param path The relative path to convert.
     * @return The absolute path.
     */
    actual fun getAbsolutePath(path: FPath): String {
        return if (path.isEmpty()) {
            SysOut.info("Path is empty $path!")
            Paths.get("/").normalize().toString()
        } else {
            Paths.get(path.first(), *path.withoutFirst().parts).normalize().toString()
        }
    }


}