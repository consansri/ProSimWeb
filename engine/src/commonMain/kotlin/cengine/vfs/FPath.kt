package cengine.vfs

import cengine.system.getSystemLineBreak
import cengine.vfs.FPath.Companion.DELIMITER
import kotlinx.serialization.Serializable

/**
 * A FilePath which is used in the [VFileSystem] to identify [VirtualFile]s.
 * A Path should always contain the root Directory Name of the [VFileSystem] at index 0.
 */
@Serializable
class FPath(vararg val parts: String) : Collection<String> {

    /**
     * Creates a new [FPath] with [VFileSystem.root] as the first element.
     */
    companion object {
        const val DELIMITER = "/"

        /**
         * Creates a new [FPath] from a String, where elements are split by [DELIMITER].
         */
        fun String.toFPath(): FPath = FPath(*split(DELIMITER, "\\").filter { it.isNotEmpty() }.toTypedArray())

    }

    /**
     * Returns the element at the given index.
     */
    operator fun get(index: Int) = parts[index]

    /**
     * Converts this [FPath] to a String using the given delimiter.
     */
    fun toString(delimiter: String): String = parts.joinToString(delimiter) { it }

    /**
     * Creates a new [FPath] by appending the given [name] to the end of this path.
     */
    operator fun plus(name: String): FPath = FPath(*parts, name)

    /**
     * Creates a new [FPath] by appending all the elements of the given [path] to the end of this path.
     */
    operator fun plus(path: FPath): FPath = FPath(*parts, *path.parts)

    /**
     * Return the relative path of [this] from [other]
     *
     * This function computes the FPath that, when applied to [other]'s location,
     * will point to the same location as [this].
     *
     * For example, if [other] has the absolute path "/root/dir1" and [this] is
     * "/root/dir2/file.txt", the result will be "../dir2/file.txt".
     */
    fun relativeTo(other: VirtualFile): FPath {
        val basePath = other.path
        val targetParts = parts
        val baseParts = basePath.parts

        // Find common prefix length
        var commonLength = 0
        while (commonLength < baseParts.size && commonLength < baseParts.size && targetParts[commonLength] == baseParts[commonLength]) {
            commonLength++
        }

        // For each remaining element in the base path, we need a ".."
        var relativePath = FPath()
        for (i in commonLength until baseParts.size) {
            relativePath += ".."
        }

        // Then append the remaining parts from the target path.
        for (i in commonLength until targetParts.size) {
            relativePath += targetParts[i]
        }

        return relativePath
    }

    /**
     * Creates a new [FPath] without the last element.
     */
    fun withoutLast(): FPath {
        val newNames = parts.toMutableList()
        newNames.removeLastOrNull()
        return FPath(*newNames.toTypedArray())
    }

    /**
     * Creates a new [FPath] without the first element.
     */
    fun withoutFirst(): FPath {
        val newNames = parts.toMutableList()
        newNames.removeFirstOrNull()
        return FPath(*newNames.toTypedArray())
    }

    /**
     * Returns the size of the path.
     */
    override val size: Int
        get() = parts.size

    /**
     * Checks if all the elements in the given [Collection] are in this path.
     */
    override fun containsAll(elements: Collection<String>): Boolean {
        elements.forEach {
            if (!parts.contains(it)) return false
        }
        return true
    }

    /**
     * Checks if the given [element] is in this path.
     */
    override fun contains(element: String): Boolean = parts.contains(element)

    /**
     * Checks if this path is equal to the given [other].
     */
    override fun equals(other: Any?): Boolean {
        if (other !is FPath) return false
        return parts.contentEquals(other.parts)
    }

    /**
     * Returns the hash code of this path.
     */
    override fun hashCode(): Int {
        return parts.contentHashCode()
    }

    /**
     * Checks if this path is empty.
     */
    override fun isEmpty(): Boolean = parts.isEmpty()

    /**
     * Returns an iterator over the elements of this path.
     */
    override fun iterator(): Iterator<String> = parts.iterator()

    /**
     * Converts this path to a String using [DELIMITER].
     */
    override fun toString(): String {
        return parts.joinToString(DELIMITER) { it }
    }

}