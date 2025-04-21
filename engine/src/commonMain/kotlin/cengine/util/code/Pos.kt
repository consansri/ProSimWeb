package cengine.util.code

data class Pos(
    val index: Int,
    val line: Int,
    val column: Int
) : Comparable<Pos> {
    override fun compareTo(other: Pos): Int {
        return this.index.compareTo(other.index)
    }




}