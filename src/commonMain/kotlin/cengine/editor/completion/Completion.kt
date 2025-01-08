package cengine.editor.completion

import cengine.editor.CodeEditor
import cengine.editor.EditorModification
import cengine.editor.annotation.Severity

/**
 * A single completion item that can be suggested to the user.
 *
 * @property text         The text that will be shown in the completion popup.
 * @property insertion    The text that will be inserted into the editor when the user selects this item.
 * @property kind         The kind of this completion item.
 */
data class Completion(val text: String, val insertion: String, val kind: CompletionItemKind? = null) : EditorModification {
    override val severity: Severity? = null
    override val displayText: String get() = text
    override val execute: (CodeEditor) -> Unit = {
        it.textStateModel.insert(it.selector.caret, insertion)
    }
}
