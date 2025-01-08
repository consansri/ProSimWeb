package cengine.editor.annotation

import cengine.lang.asm.CodeStyle

/**
 * Severity of the [Annotation].
 */
enum class Severity(val color: CodeStyle?) {
    INFO(null),
    WARNING(CodeStyle.YELLOW),
    ERROR(CodeStyle.RED);
}