package cengine.project

import kotlinx.serialization.Serializable

@Serializable
enum class ToolContentType {
    FileTree,
    PsiAnalyzer,
    Console;
}