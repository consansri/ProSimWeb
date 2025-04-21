package cengine.vfs

import cengine.system.getSystemLineBreak

/**
 * Represents a file or directory in the virtual file system.
 */
interface VirtualFile {
    /**
     * The name of the file or directory.
     */
    val name: String

    /**
     * The full path of the file or directory within the file system.
     */
    val path: FPath

    /**
     * Indicates whether this is a directory
     */
    val isDirectory: Boolean

    /**
     * Indicates whether this is a file
     */
    val isFile: Boolean
        get() = !isDirectory

    /**
     * To add an execution event when the file changed through another application.
     */
    var onDiskChange: () -> Unit

    /**
     * Will be triggered through the [VFileSystem].
     */
    fun hasChangedOnDisk(){
        onDiskChange()
    }

    /**
     * Retrieves the parent directory, or null if this is the root.
     */
    fun parent(): VirtualFile?

    /**
     * Retrieves the child files and directories.
     *
     * @return A list of child [VirtualFile] objects, or an empty list if this is not a directory.
     */
    fun getChildren(): List<VirtualFile>

    /**
     * Retrieves the content of the file.
     *
     * @return The file content as a ByteArray, or an empty ByteArray if this is a directory.
     */
    fun getContent(): ByteArray

    /**
     * @return The UTF8 decoded ByteArray with [getSystemLineBreak] replaced with '\n'.
     */
    fun getAsUTF8String(): String = getContent().decodeToString().replace(getSystemLineBreak(), "\n")

    /**
     * Sets the content of the file.
     *
     * @param content The new content of the file as a ByteArray
     * @throws UnsupportedOperationException if this is a directory.
     */
    fun setContent(content: ByteArray)

    /**
     * Takes a string, replaces '\n' with [getSystemLineBreak] and then set UTF8 encoded ByteArray via [setContent].
     *
     * @throws UnsupportedOperationException if this is a directory.
     */
    fun setAsUTF8String(content: String) {
        setContent(content.replace("\n", getSystemLineBreak()).encodeToByteArray())
    }

    override fun toString(): String
}