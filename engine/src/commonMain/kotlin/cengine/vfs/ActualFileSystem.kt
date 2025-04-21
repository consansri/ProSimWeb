package cengine.vfs

/**
 * Platform-specific implementation of the actual file system operations.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object ActualFileSystem {

    /**
     * Reads the content of a file.
     *
     * @param path The relative path of the file to read.
     * @return The content of the file as ByteArray.
     */
    fun readFile(path: FPath): ByteArray

    /**
     * Writes content to a file.
     *
     * @param path The relative path of the file to write.
     * @param content The content to write to the file.
     */
    fun writeFile(path: FPath, content: ByteArray)

    /**
     * Deletes a file or directory.
     *
     * @param path The relative path of the file or directory to delete.
     */
    fun deleteFile(path: FPath)

    /**
     * Creates a file or directory.
     *
     * @param path The relative path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    fun createFile(path: FPath, isDirectory: Boolean)

    /**
     * Lists the contents of a directory.
     *
     * @param path The relative path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    fun listDirectory(path: FPath): List<String>

    /**
     * Checks if a path represents a directory.
     *
     * @param path The relative path to check.
     * @return True if the path is a directory, false otherwise.
     */
    fun isDirectory(path: FPath): Boolean

    /**
     * Checks if a file or directory exists.
     *
     * @param path The relative path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    fun exists(path: FPath): Boolean

    /**
     * Returns the [FPath] to the app state directory.
     */
    fun getAppStateDir(): FPath

}