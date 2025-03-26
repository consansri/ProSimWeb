package cengine.lang

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.psi.PsiManager
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.psi.core.PsiService
import cengine.vfs.VFileSystem


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
     * The run configuration associated with this language service.
     */
    abstract val runConfig: Runner<*>

    /**
     * Creates a [PsiManager] associated with this language service
     */
    abstract fun createManager(vfs: VFileSystem): PsiManager<*, *>

}