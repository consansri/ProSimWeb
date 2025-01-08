package cengine.editor.annotation

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.psi.core.Interval
import cengine.psi.core.PsiFile
import cengine.util.string.lineAndColumn

/**
 * Represents an annotation in the editor, which can highlight a range in the code with a message and a severity level.
 * Annotations can also specify an optional action to be executed in the code editor.
 *
 * @property range the range of code that this annotation applies to
 * @property message the message associated with the annotation
 * @property severity the severity level of the annotation
 * @property execute an optional action to be executed in the code editor
 */
data class Annotation(val range: IntRange, val message: String, override val severity: Severity, override val execute: (CodeEditor) -> Unit = {}) : EditorModification {

    companion object {

        /**
         * Creates an error annotation.
         *
         * @param element the interval of the error
         * @param message the error message
         * @param execute an optional action to be executed in the code editor
         * @return an error annotation
         */
        fun error(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Annotation = Annotation(element.range, message, Severity.ERROR, execute)

        /**
         * Creates a warning annotation.
         *
         * @param element the interval of the warning
         * @param message the warning message
         * @param execute an optional action to be executed in the code editor
         * @return a warning annotation
         */
        fun warn(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Annotation = Annotation(element.range, message, Severity.WARNING, execute)

        /**
         * Creates an info annotation.
         *
         * @param element the interval of the info
         * @param message the info message
         * @param execute an optional action to be executed in the code editor
         * @return an info annotation
         */
        fun info(element: Interval, message: String, execute: (CodeEditor) -> Unit = {}): Annotation = Annotation(element.range, message, Severity.INFO, execute)
    }

    /**
     * Returns the location of the annotation in the specified file as a string
     * in the format "line:column".
     *
     * @param file the PSI file to get the location from
     * @return a string representing the location of the annotation
     */
    fun location(file: PsiFile): String {
        val (line, column) = file.content.lineAndColumn(range.first)
        return "${line + 1}:${column + 1}"
    }

    /**
     * Returns a string representing the location of the annotation in the format "path:location message".
     */
    fun createConsoleMessage(file: PsiFile): String {
        return "${file.file.path}:${location(file)} $message"
    }

    /**
     * Returns a string that can be used in the console to represent the annotation.
     */
    override val displayText: String get() = message
}
