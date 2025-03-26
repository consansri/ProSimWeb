package cengine.lang.mif

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.LanguageService
import cengine.lang.Runner
import cengine.lang.mif.ast.MifPsiFile
import cengine.lang.mif.features.MifHighlighter
import cengine.psi.PsiManager
import cengine.vfs.VFileSystem

object MifLang : LanguageService() {

    const val OUTPUT_DIR = ".mif"

    override val name: String = "MIF"
    override val fileSuffix: String = ".mif"
    override val completionProvider: CompletionProvider? = null
    override val annotationProvider: AnnotationProvider? = null
    override val highlightProvider: HighlightProvider = MifHighlighter()
    override val formatter: Formatter? = null
    override val runConfig: Runner<MifLang> = MifRunner

    override fun createManager(vfs: VFileSystem): PsiManager<*, *> = PsiManager<MifLang, MifPsiFile>(
        vfs,
        PsiManager.Mode.TEXT,
        fileSuffix,
        MifParser
    ){
        completionProvider?.buildCompletionSet(it)
        annotationProvider?.updateAnnotations(it)
    }
}