package cengine.lang

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService


/**
 * Abstract class that represents a language service. A language service is a collection of features that
 * are associated with a particular programming language. These features can include code completion,
 * syntax highlighting, code formatting, and more.
 */
abstract class LanguageService {

    /**
     * The name of the language service.
     */
    abstract val name: String

    /**
     * The file suffix associated with this language service.
     */
    abstract val fileSuffix: String

    /**
     * The [PsiParser] associated with this language service.
     */
    abstract val psiParser: PsiParser<*>

    /**
     * The [PsiService] associated with this language service.
     */
    abstract val psiService: PsiService

    /**
     * The run configuration associated with this language service.
     */
    abstract val runConfig: Runner<*>

    /**
     * The code [CompletionProvider] associated with this language service.
     */
    abstract val completionProvider: CompletionProvider?

    /**
     * The [AnnotationProvider] associated with this language service.
     */
    abstract val annotationProvider: AnnotationProvider?

    /**
     * The syntax [HighlightProvider] associated with this language service.
     */
    abstract val highlightProvider: HighlightProvider?

    /**
     * The code [Formatter] associated with this language service.
     */
    abstract val formatter: Formatter?

    /**
     * Updates the analytics associated with this language service for the given file.
     */
    fun updateAnalytics(file: PsiFile) {
        completionProvider?.buildCompletionSet(file)
        annotationProvider?.updateAnnotations(file)
    }
}