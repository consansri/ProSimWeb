package cengine.vfs

import Keys
import kotlinx.browser.localStorage
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
    @OptIn(ExperimentalEncodingApi::class)
    actual fun readFile(path: FPath): ByteArray {
        val content = Base64.decode(localStorage.getItem(getFileKey(path)) ?: "")
        // SysOut.log("ACTUAL ReadFile: $path ${content.size}")
        return content
    }

    /**
     * Writes content to a file.
     *
     * @param path The relative path of the file to write.
     * @param content The content to write to the file.
     */
    @OptIn(ExperimentalEncodingApi::class)
    actual fun writeFile(path: FPath, content: ByteArray) {
        // SysOut.log("ACTUAL WriteFile: $path ${content.size}")
        localStorage.setItem(getFileKey(path), Base64.encode(content))
    }

    /**
     * Deletes a file or directory.
     *
     * @param path The relative path of the file or directory to delete.
     */
    actual fun deleteFile(path: FPath) {
        // SysOut.log("ACTUAL DeleteFile: $path")
        localStorage.removeItem(getFileKey(path))
    }

    /**
     * Creates a file or directory.
     *
     * @param path The relative path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    @OptIn(ExperimentalEncodingApi::class)
    actual fun createFile(path: FPath, isDirectory: Boolean) {
        // SysOut.log("ACTUAL CreateFile: $path isDirectory=$isDirectory")
        if (isDirectory) {
            // don't save!
        } else {
            localStorage.setItem(getFileKey(path), Base64.encode(ByteArray(0)))
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @param path The relative path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    actual fun listDirectory(path: FPath): List<String> {
        val prefix = getDirPrefix(path)

        val paths = getLocalStorageKeys()
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix).split(FPath.DELIMITER).firstOrNull() ?: "" }
            .filter { it.isNotEmpty() }
            .distinct()

        //SysOut.log("ACTUAL ListDirectories: path=$path prefix=$prefix ->\n ${paths}")
        return paths
    }

    /**
     * Checks if a path represents a directory.
     *
     * @param path The relative path to check.
     * @return True if the path is a directory, false otherwise.
     */
    actual fun isDirectory(path: FPath): Boolean {
        val prefix = getDirPrefix(path)
        return getLocalStorageKeys().any { it.startsWith(prefix) && it != getFileKey(path) }
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param path The relative path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    actual fun exists(path: FPath): Boolean {
        if (localStorage.getItem(getFileKey(path)) != null) {
            // Exists as File
            return true
        }

        if (getLocalStorageKeys().any { it.startsWith(getDirPrefix(path)) }) {
            // Exists as Directory
            return true
        }

        return false
    }

    /**
     * Converts a relative path to an absolute path using [rootPath].
     *
     * @param path The relative path to convert.
     * @return The absolute path.
     */
    actual fun getAbsolutePath(path: FPath): String = path.toString()

    private fun getFileKey(path: FPath): String = "${Keys.FILE_PREFIX}${getAbsolutePath(path)}"
    private fun getDirPrefix(path: FPath): String = "${Keys.FILE_PREFIX}${getAbsolutePath(path)}${FPath.DELIMITER}"
    private fun getLocalStorageKeys(): List<String> {
        val keys = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            localStorage.key(i)?.let { keys.add(it) }
        }
        return keys
    }


}