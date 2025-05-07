package ikr.prosim.ui.ide.editor

internal object StringUtils {
    fun commonPrefixLength(s1: String, s2: String): Int {
        val len = minOf(s1.length, s2.length)
        for (i in 0 until len) {
            if (s1[i] != s2[i]) {
                return i
            }
        }
        return len
    }

    fun commonSuffixLength(s1: String, s2: String): Int {
        val len = minOf(s1.length, s2.length)
        for (i in 0 until len) {
            if (s1[s1.length - 1 - i] != s2[s2.length - 1 - i]) {
                return i
            }
        }
        return len
    }
}


