package cengine.psi.tree

import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiError
import cengine.psi.lexer.PsiToken

object PsiTreeDebugger {

    /**
     * Creates a string representation of the PSI tree structure starting from this element.
     *
     * @param includeRanges If true, includes the text range start..end for each element.
     * @return An indented string representing the tree.
     */
    fun PsiElement.dumpTree(includeRanges: Boolean = true): String {
        val sb = StringBuilder()
        dumpElementRecursive(this, 0, sb, includeRanges)
        return sb.toString()
    }

    private fun dumpElementRecursive(
        element: PsiElement,
        indent: Int,
        sb: StringBuilder,
        includeRanges: Boolean
    ) {
        sb.append("  ".repeat(indent))

        // --- Element Type Info ---
        sb.append(element::class.simpleName ?: "UnknownElement")

        // --- Range Info ---
        // Assumes PsiElement has a 'range: IntRange' property populated by PsiTreeBuilder
        if (includeRanges) {
            try {
                // Use property access syntax, handle potential errors if range isn't always available
                val range = element.range
                // Use inclusive range display as is common for text spans
                sb.append(" [${range.first}..${range.last}]")
            } catch (e: Exception) {
                // Catch potential exceptions if 'range' isn't implemented or accessible
                sb.append(" [range unavailable]")
            }
        }

        // --- Specific Info for Tokens/Errors ---
        when (element) {
            is PsiToken -> {
                // Handle potentially long token values gracefully
                val displayValue = if (element.value.length > 40) {
                    element.value.substring(0, 37) + "..."
                } else {
                    element.value
                }.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") // Escape control chars

                sb.append(" (${element.type.typeName}: \"${displayValue}\")")
            }
            is PsiError -> {
                sb.append(" (ERROR: \"${element.errorMessage}\")")
            }
            // Add other specific cases if needed, e.g., for identifiers:
            // is PsiStatement.Expr.Identifier -> {
            //     val name = element.children.filterIsInstance<PsiToken>().firstOrNull()?.value ?: "?"
            //     sb.append(" (\"$name\")")
            // }
        }

        sb.appendLine() // Finish the current node's line

        // --- Recursively call for children ---
        element.children.forEach { child ->
            dumpElementRecursive(child, indent + 1, sb, includeRanges)
        }
    }

}