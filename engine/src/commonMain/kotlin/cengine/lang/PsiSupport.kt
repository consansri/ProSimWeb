package cengine.lang

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.highlighting.HighlightProvider
import cengine.psi.lexer.PsiLexerSet
import cengine.psi.parser.PsiTreeParser

data class PsiSupport(
    val lexerSet: PsiLexerSet,
    val psiTreeParser: PsiTreeParser,
    val completionProvider: CompletionProvider? = null,
    val annotationProvider: AnnotationProvider? = null,
    val highlightProvider: HighlightProvider? = null
)