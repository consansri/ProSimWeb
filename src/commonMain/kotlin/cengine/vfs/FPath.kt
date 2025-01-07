package cengine.vfs

import kotlinx.serialization.Serializable

/**
 * A FilePath which is used in the [VFileSystem] to identify [VirtualFile]s.
 * A Path should always contain the root Directory Name of the [VFileSystem] at index 0.
 */
@Serializable
class FPath(vararg val names: String) : Collection<String> {
    companion object {
        const val DELIMITER = "/"

        fun of(vfs: VFileSystem, vararg names: String): FPath = FPath(vfs.root.name, *names)

        fun delimited(delimitedPath: String) = FPath(*delimitedPath.split(DELIMITER).toTypedArray())
    }

    operator fun get(index: Int) = names[index]

    fun toString(delimiter: String): String = names.joinToString(delimiter) { it }

    operator fun plus(name: String): FPath = FPath(*names, name)

    operator fun plus(path: FPath): FPath = FPath(*names, *path.names)

    fun toAbsolute(absRootPath: String): String = absRootPath + withoutFirst().joinToString("") { DELIMITER + it }

    fun withoutLast(): FPath {
        val newNames = names.toMutableList()
        newNames.removeLastOrNull()
        return FPath(*newNames.toTypedArray())
    }

    fun withoutFirst(): FPath {
        val newNames = names.toMutableList()
        newNames.removeFirstOrNull()
        return FPath(*newNames.toTypedArray())
    }

    override val size: Int
        get() = names.size

    override fun containsAll(elements: Collection<String>): Boolean {
        elements.forEach {
            if (!names.contains(it)) return false
        }
        return true
    }

    override fun contains(element: String): Boolean = names.contains(element)

    override fun equals(other: Any?): Boolean {
        if (other !is FPath) return false
        return names.contentEquals(other.names)
    }

    override fun hashCode(): Int {
        return names.contentHashCode()
    }

    override fun isEmpty(): Boolean = names.isEmpty()

    override fun iterator(): Iterator<String> = names.iterator()

    override fun toString(): String {
        return names.joinToString(DELIMITER) { it }
    }

}