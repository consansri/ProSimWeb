package cengine.vfs

import kotlinx.serialization.Serializable

/**
 * A FilePath which is used in the [VFileSystem] to identify [VirtualFile]s.
 * A Path should always contain the root Directory Name of the [VFileSystem] at index 0.
 */
@Serializable
class FPath(vararg val names: String) : Collection<String> {
    /**
     * Creates a new [FPath] with [VFileSystem.root] as the first element.
     */
    companion object {
        const val DELIMITER = "/"

        /**
         * Creates a new [FPath] with [VFileSystem.root] as the first element.
         */
        fun of(vfs: VFileSystem, vararg names: String): FPath = FPath(vfs.root.name, *names)

        /**
         * Creates a new [FPath] from a String, where elements are split by [DELIMITER].
         */
        fun delimited(delimitedPath: String) = FPath(*delimitedPath.split(DELIMITER).toTypedArray())
    }

    /**
     * Returns the element at the given index.
     */
    operator fun get(index: Int) = names[index]

    /**
     * Converts this [FPath] to a String using the given delimiter.
     */
    fun toString(delimiter: String): String = names.joinToString(delimiter) { it }

    /**
     * Creates a new [FPath] by appending the given [name] to the end of this path.
     */
    operator fun plus(name: String): FPath = FPath(*names, name)

    /**
     * Creates a new [FPath] by appending all the elements of the given [path] to the end of this path.
     */
    operator fun plus(path: FPath): FPath = FPath(*names, *path.names)

    /**
     * Converts this path to an absolute path by prepending the given [absRootPath].
     */
    fun toAbsolute(absRootPath: String): String = absRootPath + withoutFirst().joinToString("") { DELIMITER + it }

    /**
     * Creates a new [FPath] without the last element.
     */
    fun withoutLast(): FPath {
        val newNames = names.toMutableList()
        newNames.removeLastOrNull()
        return FPath(*newNames.toTypedArray())
    }

    /**
     * Creates a new [FPath] without the first element.
     */
    fun withoutFirst(): FPath {
        val newNames = names.toMutableList()
        newNames.removeFirstOrNull()
        return FPath(*newNames.toTypedArray())
    }

    /**
     * Returns the size of the path.
     */
    override val size: Int
        get() = names.size

    /**
     * Checks if all the elements in the given [Collection] are in this path.
     */
    override fun containsAll(elements: Collection<String>): Boolean {
        elements.forEach {
            if (!names.contains(it)) return false
        }
        return true
    }

    /**
     * Checks if the given [element] is in this path.
     */
    override fun contains(element: String): Boolean = names.contains(element)

    /**
     * Checks if this path is equal to the given [other].
     */
    override fun equals(other: Any?): Boolean {
        if (other !is FPath) return false
        return names.contentEquals(other.names)
    }

    /**
     * Returns the hash code of this path.
     */
    override fun hashCode(): Int {
        return names.contentHashCode()
    }

    /**
     * Checks if this path is empty.
     */
    override fun isEmpty(): Boolean = names.isEmpty()

    /**
     * Returns an iterator over the elements of this path.
     */
    override fun iterator(): Iterator<String> = names.iterator()

    /**
     * Converts this path to a String using [DELIMITER].
     */
    override fun toString(): String {
        return names.joinToString(DELIMITER) { it }
    }

}