package cengine.vfs

import Keys
import SysOut
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set
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
        val content = if (!isDirectory(path)) {
            Base64.decode(localStorage[getFileKey(path)] ?: "")
        } else {
            SysOut.error("Unable to read directory $path!")
            ByteArray(0)
        }
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
        if (!isDirectory(path)) {
            localStorage[getFileKey(path)] = Base64.encode(content)
        } else {
            SysOut.error("Unable to write to directory $path!")
        }
    }

    /**
     * Deletes a file or directory.
     *
     * @param path The relative path of the file or directory to delete.
     */
    actual fun deleteFile(path: FPath) {
        // SysOut.log("ACTUAL DeleteFile: $path")
        if (isDirectory(path)) {
            localStorage.removeItem(getDirKey(path))
        } else {
            localStorage.removeItem(getFileKey(path))
        }
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
            localStorage[getDirKey(path)] = Base64.encode(ByteArray(0))
        } else {
            localStorage[getFileKey(path)] = Base64.encode(ByteArray(0))
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @param path The relative path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    actual fun listDirectory(path: FPath): List<String> {
        val filePrefix = getFileKey(path) + FPath.DELIMITER
        val dirPrefix = getDirKey(path) + FPath.DELIMITER

        val allKeys = getLocalStorageKeys()

        val filePaths = allKeys
            .filter { it.startsWith(filePrefix) }
            .mapNotNull { it.removePrefix(filePrefix).split(FPath.DELIMITER).firstOrNull() }
            .distinct()

        val dirPaths = allKeys
            .filter { it.startsWith(dirPrefix) }
            .mapNotNull { it.removePrefix(dirPrefix).split(FPath.DELIMITER).firstOrNull() }
            .distinct()

        //SysOut.log("ACTUAL ListDirectories: path=$path prefix=$prefix ->\n ${paths}")
        return dirPaths + filePaths
    }

    /**
     * Checks if a path represents a directory.
     *
     * @param path The relative path to check.
     * @return True if the path is a directory, false otherwise.
     */
    actual fun isDirectory(path: FPath): Boolean {
        return localStorage[getDirKey(path)] != null
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param path The relative path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    actual fun exists(path: FPath): Boolean {
        if (localStorage[getFileKey(path)] != null) {
            // Exists as File
            return true
        }

        if (localStorage[getDirKey(path)] != null) {
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

    private fun getFileKey(path: FPath): String = Keys.FILE_PREFIX + getAbsolutePath(path)
    private fun getDirKey(path: FPath): String = Keys.DIR_PREFIX + getAbsolutePath(path)
    private fun getLocalStorageKeys(): List<String> {
        val keys = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            localStorage.key(i)?.let { keys.add(it) }
        }
        return keys
    }


}