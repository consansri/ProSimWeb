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
class VFileSystem(val absRootPath: FPath) {
    private val fileCache = mutableMapOf<FPath, VirtualFile>()
    private val changeListeners = mutableListOf<FileChangeListener>()
    private val fileWatcher: FileWatcher = FileWatcher(this)

    val root: VirtualFile = get(absRootPath) ?: createFile(absRootPath, true)

    init {
        initializeFileWatcher()
    }

    /**
     * FileWatcher Initialization
     */

    private fun initializeFileWatcher() {
        watchRecursively(absRootPath)
        fileWatcher.startWatching()
    }

    private fun watchRecursively(path: FPath) {
        if (!ActualFileSystem.isDirectory(path)) return
        fileWatcher.watchDirectory(path)

        ActualFileSystem.listDirectory(path).forEach { childName ->
            val childPath = path + childName
            watchRecursively(childPath)
        }
    }

    /**
     * File System Interaction
     */

    /**
     * Get File through relative [relativePath]
     */
    operator fun get(context: VirtualFile, relativePath: FPath): VirtualFile? {
        if (relativePath.isEmpty()) return context

        var file = context
        for (part in relativePath.parts) {
            file = when (part) {
                ".." -> {
                    file.parent() ?: return null
                }

                "." -> file

                else -> file.getChildren().firstOrNull { it.name == part } ?: return null
            }
        }

        return file
    }

    /**
     * Get File through absolute [path]
     */
    operator fun get(path: FPath): VirtualFile? {
        if (path.isEmpty()) return null
        if (!ActualFileSystem.exists(path)) return null

        return getOrCreateFile(path)
    }

    /**
     * File System Modification
     */

    /**
     * Rename a file using [createFile] and [deleteFile] methods.
     */
    fun renameFile(path: FPath, newName: String): Boolean {
        val file = get(path) ?: return false
        val content = file.getContent()
        val isDirectory = file.isDirectory

        val renamedFile = createFile(file.path.withoutLast() + newName, isDirectory)
        if (content.isNotEmpty()) renamedFile.setContent(content)

        deleteFile(file.path)

        return true
    }

    /**
     * Creates a new file or directory in the virtual file system.
     *
     * @param path The absolute path where the new file or directory should be created.
     * @param isDirectory Whether to create a directory (true) or a file (false).
     * @return The newly created [VirtualFile] object.
     */
    fun createFile(path: FPath, isDirectory: Boolean = false): VirtualFile {

        // cengine.console.SysOut.log("Create $path")
        // Create Parent Directory if non-existent

        val parentPath = path.withoutLast()

        if (!ActualFileSystem.exists(parentPath)) {
            ActualFileSystem.createFile(parentPath, true)
        }

        // Create File

        ActualFileSystem.createFile(path, isDirectory)

        val newFile = getOrCreateFile(path)
        fileCache[path] = newFile
        notifyFileCreated(newFile)
        return newFile
    }

    /**
     * Deletes a file or directory from the virtual file system.
     *
     * @param path The absolute path of the file or directory to delete.
     */
    fun deleteFile(path: FPath, recursive: Boolean = true) {
        // cengine.console.SysOut.log("DeleteFile $path")
        if (recursive && ActualFileSystem.isDirectory(path)) {
            ActualFileSystem.listDirectory(path).forEach { childName ->
                deleteFile(path + childName)
            }
        }

        val deletedFile = get(path)
        ActualFileSystem.deleteFile(path)
        fileCache.remove(path)
        deletedFile?.let {
            notifyFileDeleted(it)
        }
    }

    /**
     * Should be called only if file exists!
     */
    private fun getOrCreateFile(path: FPath): VirtualFile {
        // cengine.console.SysOut.log("GetOrCreateFile: $path")

        val created = fileCache.getOrPut(path) {
            VirtualFileImpl(path, ActualFileSystem.isDirectory(path))
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
        // cengine.console.SysOut.log("Notify File Changed")
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
     * Implementation of [VirtualFile] that uses the [ActualFileSystem] to access the file system.
     *
     * @property name The name of the file or directory.
     * @property path The path of the file or directory.
     * @property isDirectory If this is a directory.
     * @property parent The parent directory of this file or directory, or null if this is the root directory.
     */
    inner class VirtualFileImpl(
        override val path: FPath,
        override val isDirectory: Boolean
    ) : VirtualFile {
        override val name: String
            get() = path.last()

        override var onDiskChange: () -> Unit = {}

        override fun parent(): VirtualFile? {
            if(!ActualFileSystem.exists(path.withoutLast())) return null

            return getOrCreateFile(path.withoutLast())
        }

        /**
         * Returns a list of all files and directories in this directory.
         *
         * @return A list of [VirtualFile] objects.
         */
        override fun getChildren(): List<VirtualFile> {
            // cengine.console.SysOut.log("getChildren($path)")
            return if (isDirectory) {
                ActualFileSystem.listDirectory(path).map { getOrCreateFile(path + it) }
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
                ActualFileSystem.readFile(path)
            }
        }

        /**
         * Sets the content of this file.
         *
         * @param content The new content of this file as a byte array.
         */
        override fun setContent(content: ByteArray) {
            if (!isDirectory) {
                ActualFileSystem.writeFile(path, content)
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