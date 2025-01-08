package cengine.vfs

/**
 * Virtual File System (VFS)
 *
 * A flexible, platform-independent file system abstraction layer.
 * It provides a unified interface for file operations across different platforms,
 * allowing to work with files and directories consistently regardless of the
 * underlying storage mechanism.
 *
 * The main class that manages the virtual file system.
 *
 * @property actualFileSystem The platform-specific file system implementation.
 */
class VFileSystem(absRootPath: String) {
    val absRootPath = absRootPath.replace("\\", FPath.DELIMITER)
    private val actualFileSystem: ActualFileSystem = ActualFileSystem(absRootPath)
    val root: VirtualFile = RootDirectory(absRootPath.split(FPath.DELIMITER).last())
    private val fileCache = mutableMapOf<FPath, VirtualFile>()
    private val changeListeners = mutableListOf<FileChangeListener>()
    private val fileWatcher: FileWatcher = FileWatcher(this)

    init {
        initializeFileWatcher()
    }

    /**
     * FileWatcher Initialization
     */

    private fun initializeFileWatcher() {
        watchRecursively(FPath())
        fileWatcher.startWatching()
    }

    private fun watchRecursively(relativePath: FPath) {
        if (!actualFileSystem.isDirectory(relativePath)) return
        fileWatcher.watchDirectory(actualFileSystem.getAbsolutePath(relativePath))

        actualFileSystem.listDirectory(relativePath).forEach { childName ->
            val childPath = relativePath + childName
            watchRecursively(childPath)
        }
    }

    /**
     * File System Modification
     */

    /**
     * Converts an [absolutePath] to a relative [FPath].
     */
    fun toRelative(absolutePath: String): FPath = FPath.of(this, *absolutePath.removePrefix(absRootPath).split(FPath.DELIMITER).filter { it.isNotEmpty() }.toTypedArray())

    /**
     * Rename a file using [createFile] and [deleteFile] methods.
     */
    fun renameFile(path: FPath, newName: String): Boolean {
        val file = findFile(path) ?: return false
        val content = file.getContent()
        val isDirectory = file.isDirectory

        val renamedFile = createFile(file.path.withoutLast() + newName, isDirectory)
        if (content.isNotEmpty()) renamedFile.setContent(content)

        deleteFile(file.path)

        return true
    }

    /**
     * Finds a file or directory in the virtual file system.
     *
     * Use [DELIMITER] inside the path!
     *
     * @param relativePath The relative path of the file or directory to find.
     * @return The [VirtualFile] object if found, or null if not found.
     */
    fun findFile(relativePath: FPath): VirtualFile? {
        // nativeLog("FindFile")

        if (relativePath.isEmpty()) return null

        if (relativePath == root.path) return root

        val fromCache = fileCache[relativePath]
        if (fromCache != null) {
            // nativeLog("FoundFile from Cache: $relativePath -> ${fromCache.path}")
            return fromCache
        }

        if (actualFileSystem.exists(relativePath)) {
            return getOrCreateFile(relativePath, findFile(relativePath.withoutLast()))
        }

        return null
    }

    /**
     * Creates a new file or directory in the virtual file system.
     *
     * Use [DELIMITER] inside the path!
     *
     * @param relativePath The relative path where the new file or directory should be created.
     * @param isDirectory Whether to create a directory (true) or a file (false).
     * @return The newly created [VirtualFile] object.
     */
    fun createFile(relativePath: FPath, isDirectory: Boolean = false): VirtualFile {

        // nativeLog("Create $relativePath")
        // Create Parent Directory if non-existent

        val parentPath = relativePath.withoutLast()

        if (!actualFileSystem.exists(parentPath)) {
            actualFileSystem.createFile(parentPath, true)
        }

        val parent = findFile(parentPath)

        // Create File

        actualFileSystem.createFile(relativePath, isDirectory)

        val newFile = getOrCreateFile(relativePath, parent, isDirectory)
        fileCache[relativePath] = newFile
        notifyFileCreated(newFile)
        return newFile
    }

    /**
     * Deletes a file or directory from the virtual file system.
     *
     * Use [DELIMITER] inside the path!
     *
     * @param relativePath The relative path of the file or directory to delete.
     */
    fun deleteFile(relativePath: FPath) {
        // nativeLog("DeleteFile $relativePath")
        val deletedFile = findFile(relativePath)
        actualFileSystem.deleteFile(relativePath)
        fileCache.remove(relativePath)
        deletedFile?.let {
            notifyFileDeleted(it)
        }
    }

    /**
     * Should be called only if file exists!
     */
    private fun getOrCreateFile(relativePath: FPath, parent: VirtualFile?, isDirectory: Boolean = actualFileSystem.isDirectory(relativePath)): VirtualFile {
        // nativeLog("GetOrCreateFile: $relativePath")

        val created = fileCache.getOrPut(relativePath) {
            val name = relativePath.last()

            val newFile = VirtualFileImpl(name, relativePath, isDirectory, parent)
            newFile
        }

        return created
    }

    /**
     * Change Listeners
     */

    /**
     * Adds a listener for file system change events.
     *
     * @param listener The [FileChangeListener] to add.
     */
    fun addChangeListener(listener: FileChangeListener) {
        changeListeners.add(listener)
    }

    /**
     * Removes a previously added file system change listener.
     *
     * @param listener The [FileChangeListener] to remove.
     */
    fun removeChangeListener(listener: FileChangeListener) {
        changeListeners.remove(listener)
    }

    fun notifyFileChanged(file: VirtualFile) {
        file.hasChangedOnDisk()
        // nativeLog("Notify File Changed")
        changeListeners.forEach { it.onFileChanged(file) }
    }

    fun notifyFileCreated(file: VirtualFile) {
        changeListeners.forEach { it.onFileCreated(file) }
    }

    fun notifyFileDeleted(file: VirtualFile) {
        changeListeners.forEach { it.onFileDeleted(file) }
    }

    fun close() {
        changeListeners.clear()
    }

    /**
     * A special [VirtualFile] that represents the root directory of the virtual file system.
     *
     * @property name The name of the root directory.
     */
    inner class RootDirectory(override val name: String) : VirtualFile {
        override val path: FPath = FPath(name)
        override val isDirectory: Boolean = true
        override val parent: VirtualFile? = null
        override var onDiskChange: () -> Unit = {}

        /**
         * Returns a list of all files and directories in the root directory.
         */
        override fun getChildren(): List<VirtualFile> {
            // nativeLog("getChildren($path)")
            return actualFileSystem.listDirectory(path).map { getOrCreateFile(path + it, this) }
        }

        /**
         * Returns an empty byte array, as the root directory does not have any content.
         */
        override fun getContent(): ByteArray = ByteArray(0)

        /**
         * Throws an [UnsupportedOperationException], as the root directory cannot be modified.
         */
        override fun setContent(content: ByteArray) {
            throw UnsupportedOperationException()
        }

        /**
         * Returns the name of the root directory.
         */
        override fun toString(): String = name
    }

    /**
     * Implementation of [VirtualFile] that uses the [ActualFileSystem] to access the file system.
     *
     * @property name The name of the file or directory.
     * @property path The path of the file or directory.
     * @property isDirectory If this is a directory.
     * @property parent The parent directory of this file or directory, or null if this is the root directory.
     */
    inner class VirtualFileImpl(
        override val name: String,
        override val path: FPath,
        override val isDirectory: Boolean,
        override val parent: VirtualFile?
    ) : VirtualFile {
        override var onDiskChange: () -> Unit = {}

        /**
         * Returns a list of all files and directories in this directory.
         *
         * @return A list of [VirtualFile] objects.
         */
        override fun getChildren(): List<VirtualFile> {
            // nativeLog("getChildren($path)")
            return if (isDirectory) {
                actualFileSystem.listDirectory(path).map { getOrCreateFile(path + it, this) }
            } else {
                emptyList()
            }
        }

        /**
         * Returns the content of this file as a byte array.
         *
         * @return A byte array containing the content of this file.
         */
        override fun getContent(): ByteArray {
            return if (isDirectory) {
                ByteArray(0)
            } else {
                actualFileSystem.readFile(path)
            }
        }

        /**
         * Sets the content of this file.
         *
         * @param content The new content of this file as a byte array.
         */
        override fun setContent(content: ByteArray) {
            if (!isDirectory) {
                actualFileSystem.writeFile(path, content)
            }
        }

        /**
         * @return filename ([name])
         */
        override fun toString(): String = name
    }


    /**
     * Returns the root directory of this file system.
     *
     * @return [root] filename
     */
    override fun toString(): String {
        return root.toString()
    }


}