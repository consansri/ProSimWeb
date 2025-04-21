package cengine.editor.text

interface TextModel : Editable, Informational {

    fun all(): String = substring(0, length)

    override fun toString(): String
}