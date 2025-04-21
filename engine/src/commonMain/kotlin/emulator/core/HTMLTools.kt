package emulator.core

/**
 * This Object contains HTML encoding functions which replace certain characters with HTML symbols.
 */
object HTMLTools {
    fun encodeBeforeHTML(value: String): String {
        val builder = StringBuilder()

        for (c in value) {
            when (c) {
                '<' -> builder.append("&lt;")
                '>' -> builder.append("&gt;")
                else -> builder.append(c)
            }
        }

        return builder.toString()
    }

    fun encodeAfterHTML(value: String): String {
        val builder = StringBuilder()

        for (c in value) {
            when (c) {
                '&' -> builder.append("&amp;")
                '"' -> builder.append("&quot;")
                else -> builder.append(c)
            }
        }

        return builder.toString()
    }

    fun encodeHTML(value: String): String {
        val builder = StringBuilder()

        for (c in value) {
            when (c) {
                '<' -> builder.append("&lt;")
                '>' -> builder.append("&gt;")
                '&' -> builder.append("&amp;")
                '"' -> builder.append("&quot;")
                else -> builder.append(c)
            }
        }

        return builder.toString()
    }
}