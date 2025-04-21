package cengine.editor.selection

data class Selection(
    var start: Int? = null,
    var end: Int? = null
) {

    val lower: Int?
        get() {
            val currStart = start ?: return null
            val currEnd = end ?: return null
            if (currStart < currEnd) return currStart
            return currEnd
        }
    val higher: Int?
        get() {
            val currStart = start ?: return null
            val currEnd = end ?: return null
            if (currStart < currEnd) return currEnd
            return currStart
        }

    fun select(from: Int?, to: Int?) {
        start = from
        end = to
    }

    fun deselect() {
        select(null, null)
    }

    fun select(pair: Pair<Int?, Int?>) {
        select(pair.first, pair.second)
    }

    fun asRange(): IntRange? {
        val currStart = start ?: return null
        val currEnd = end ?: return null
        return if (currStart < currEnd) {
            currStart..<currEnd
        } else {
            currEnd..<currStart
        }
    }

    fun valid(): Boolean {
        return start != null && end != null
    }

    fun moveEnd(newEnd: Int) {
        select(start, newEnd)
    }

    fun moveStart(newStart: Int) {
        select(newStart, end)
    }

    override fun toString(): String = "[$start:$end]"
}