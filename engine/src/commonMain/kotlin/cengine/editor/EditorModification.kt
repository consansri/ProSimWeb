package cengine.editor

import cengine.editor.annotation.Severity

interface EditorModification {
    val displayText: String
    val severity: Severity?
    val execute: (CodeEditor) -> Unit
}