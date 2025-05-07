package ikr.prosim.ui.ide.editor.state

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

interface TextModCommand {
    val description: String
    fun undo(old: TextFieldValue): TextFieldValue
    fun redo(old: TextFieldValue): TextFieldValue

    data class InsertCommand(
        val position: Int,
        val text: String,
    ) : TextModCommand {
        override val description: String get() = "Insert '$text' at $position"

        override fun undo(old: TextFieldValue): TextFieldValue {
            val newText = old.text.substring(0, position) + old.text.substring(position + text.length)
            return TextFieldValue(newText, selection = TextRange(position))
        }

        override fun redo(old: TextFieldValue): TextFieldValue {
            val newText = old.text.substring(0, position) + text + old.text.substring(position)
            return TextFieldValue(newText, TextRange(position + text.length))
        }

        override fun toString(): String = "(I:$position:$text)"
    }

    data class DeleteCommand(
        val position: Int,
        val deletedText: String,
    ) : TextModCommand {
        override val description: String get() = "Delete '$deletedText' at $position"

        override fun undo(old: TextFieldValue): TextFieldValue {
            val newText = old.text.substring(0, position) + deletedText + old.text.substring(position)
            return TextFieldValue(newText, TextRange(position + deletedText.length))
        }

        override fun redo(old: TextFieldValue): TextFieldValue {
            val newText = old.text.substring(0, position) + old.text.substring(position + deletedText.length)
            return TextFieldValue(newText, TextRange(position))
        }

        override fun toString(): String = "(D:$position:$deletedText)"
    }

    data class CharacterReplaceCommand(
        val position: Int,
        val deletedText: String,
        val insertedText: String,
    ) : TextModCommand {
        override val description: String get() = "Replace '$deletedText' with '$insertedText' at $position"

        override fun undo(old: TextFieldValue): TextFieldValue {
            val textAfterRemovingInserted = old.text.substring(0, position) + old.text.substring(position + insertedText.length)
            val finalNewText = textAfterRemovingInserted.substring(0, position) + deletedText + textAfterRemovingInserted.substring(position)
            return TextFieldValue(finalNewText, TextRange(position, position + deletedText.length))
        }

        override fun redo(old: TextFieldValue): TextFieldValue {
            val textAfterRemovingDeleted = old.text.substring(0, position) + old.text.substring(position + deletedText.length)
            val finalNewText = textAfterRemovingDeleted.substring(0, position) + insertedText + textAfterRemovingDeleted.substring(position)
            return TextFieldValue(finalNewText, TextRange(position + insertedText.length))
        }

        override fun toString(): String = "(R:$position:$deletedText->$insertedText)"
    }

    data class ReplaceAllCommand(
        val oldTextContent: String, // The entire text before replacement
        val newTextContent: String,  // The entire new text
    ) : TextModCommand {
        override val description: String get() = "Replace all text (old length ${oldTextContent.length}) with new text (length ${newTextContent.length})"

        override fun undo(old: TextFieldValue): TextFieldValue {
            // old.text is expected to be newTextContent
            return TextFieldValue(oldTextContent, TextRange(oldTextContent.length)) // Restore old text, cursor at end
        }

        override fun redo(old: TextFieldValue): TextFieldValue {
            // old.text is expected to be oldTextContent
            return TextFieldValue(newTextContent, TextRange(newTextContent.length)) // Apply new text, cursor at end
        }

        override fun toString(): String = "(RAll:$oldTextContent->$newTextContent)"
    }

}

