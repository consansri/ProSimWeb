package cengine.vfs

/**
 * Interface for receiving notifications about file-system changes.
 */
interface FileChangeListener {
    /**
     * Called when a file's content has changed.
     *
     * @param file The [VirtualFile] that changed.
     */
    fun onFileChanged(file: VirtualFile)

    /**
     * Called when a file or directory is created.
     *
     * @param file The newly created [VirtualFile].
     */
    fun onFileCreated(file: VirtualFile)

    /**
     * Called when a file or directory is deleted.
     *
     * @param file The deleted [VirtualFile].
     */
    fun onFileDeleted(file: VirtualFile)
}