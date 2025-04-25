package cengine.vfs

import Constants
import Keys
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Platform-specific implementation of the actual file system operations.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalEncodingApi::class)
actual object ActualFileSystem {

    /**
     * Reads the content of a file.
     *
     * @param path The absolute path of the file to read.
     * @return The content of the file as ByteArray.
     */
    actual fun readFile(path: FPath): ByteArray {
        val fileKey = getFileKey(path)
        val dirKey = getDirKey(path)

        if (localStorage[dirKey] != null) {
            throw IsDirectoryException(path) // Throw exception for directory
        }

        val encodedContent = localStorage[fileKey]
            ?: throw NoSuchFileException(path) // Throw if neither dir nor file key exists

        return try {
            Base64.decode(encodedContent)
        } catch (e: IllegalArgumentException) { // Base64.decode throws this on error
            throw Base64EncodingException(path, e)
        }
    }

    /**
     * Writes content to a file.
     *
     * @param path The absolute path of the file to write.
     * @param content The content to write to the file.
     */
    actual fun writeFile(path: FPath, content: ByteArray) {
        val fileKey = getFileKey(path)
        val dirKey = getDirKey(path)

        if (localStorage[dirKey] != null) {
            throw IsDirectoryException(path) // Cannot write content to a directory path
        }

        // Ensure parent directory exists (optional, depending on desired strictness)
        path.withoutLast().let { if (it.isEmpty() || !isDirectory(it)) throw NoSuchFileException(it) } // Basic check

        localStorage[fileKey] = Base64.encode(content)
        // If a directory marker existed for some reason, remove it? Or handle earlier.
    }

    /**
     * Deletes a file or directory.
     *
     * @param path The absolute path of the file or directory to delete.
     */
    actual fun deleteFile(path: FPath) {
        val fileKey = getFileKey(path)
        val dirKey = getDirKey(path)

        if (localStorage[dirKey] != null) {
            // It's a directory, check if empty before deleting marker (Fix Option A)
            if (listDirectory(path).isNotEmpty()) {
                throw DirectoryNotEmptyException(path)
            }
            localStorage.removeItem(dirKey)
        } else if (localStorage[fileKey] != null) {
            // It's a file
            localStorage.removeItem(fileKey)
        } else {
            // Path doesn't exist - either throw NoSuchFileException or do nothing (current)
            // throw NoSuchFileException(path) // Option to be stricter
        }
        // Note: Recursive deletion (Fix Option B) is more complex and not shown here.
    }

    /**
     * Creates a file or directory.
     *
     * @param path The absolute path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    actual fun createFile(path: FPath, isDirectory: Boolean) {
        if (exists(path)) {
            throw FileAlreadyExistsException(path) // Don't overwrite
        }

        // Ensure parent exists (important!)
        val parent = path.withoutLast()
        if (parent.isNotEmpty() && !isDirectory(parent)) {
            // If a parent exists but isn't a directory, OR parent doesn't exist at all
            if (exists(parent) || !createMissingParentDirectories(parent)) {
                throw NoSuchFileException(parent) // Indicate parent issue
            }
        }


        val key = if (isDirectory) getDirKey(path) else getFileKey(path)

        // Store empty string marker (Base64 of empty ByteArray)
        localStorage[key] = "" // Empty string is fine, Base64("") decodes to empty ByteArray

    }

    // Helper for createFile to ensure parents exist
    private fun createMissingParentDirectories(path: FPath): Boolean {
        if (exists(path)) {
            return isDirectory(path) // Return true if exists and is a directory
        }
        val parent = path.withoutLast()
        if (parent.isNotEmpty()) {
            if (!createMissingParentDirectories(parent)) {
                return false // Failed to create ancestor
            }
        }
        // Now create this directory level
        return try {
            localStorage[getDirKey(path)] = ""
            true
        } catch (e: Exception) {
            false /* Quota or other error */
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @param path The absolute path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    actual fun listDirectory(path: FPath): List<String> {
        val filePrefix = getFileKey(path) + FPath.DELIMITER
        val dirPrefix = getDirKey(path) + FPath.DELIMITER

        val allKeys = getLocalStorageKeys()

        val dirPaths = allKeys
            .filter { it.startsWith(dirPrefix) }
            .mapNotNull { it.removePrefix(dirPrefix).split(FPath.DELIMITER).firstOrNull() }

        val filePaths = allKeys
            .filter { it.startsWith(filePrefix) }
            .mapNotNull { it.removePrefix(filePrefix).split(FPath.DELIMITER).firstOrNull() }

        val allPaths = (dirPaths + filePaths).distinct()

        //cengine.console.SysOut.log("ACTUAL ListDirectories: path=$path prefix=$prefix ->\n ${paths}")
        return allPaths
    }

    /**
     * Checks if a path represents a directory.
     *
     * @param path The absolute path to check.
     * @return True if the path is a directory, false otherwise.
     */
    actual fun isDirectory(path: FPath): Boolean {
        return localStorage[getDirKey(path)] != null
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param path The absolute path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    actual fun exists(path: FPath): Boolean {
        return localStorage[getFileKey(path)] != null || localStorage[getDirKey(path)] != null
    }

    /**
     * Returns the [FPath] to the app state directory.
     */
    actual fun getAppStateDir(): FPath {
        val appName = Constants.NAME
        val companyName = Constants.ORG

        return FPath(companyName, appName)
    }

    private fun getFileKey(path: FPath): String = Keys.FILE_PREFIX + path
    private fun getDirKey(path: FPath): String = Keys.DIR_PREFIX + path
    private fun getLocalStorageKeys(): List<String> {
        val keys = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            localStorage.key(i)?.let { keys.add(it) }
        }
        return keys
    }


}