package cengine.editor.annotation

import cengine.psi.style.CodeStyle

/**
 * Severity of the [Annotation].
 */
enum class Severity(val color: CodeStyle?) {
    INFO(null),
    WARNING(CodeStyle.YELLOW),
    ERROR(CodeStyle.RED);
}