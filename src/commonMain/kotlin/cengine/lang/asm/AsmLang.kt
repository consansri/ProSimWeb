package cengine.lang.asm

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.formatting.Formatter
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.LanguageService
import cengine.lang.Runner
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.features.AsmAnnotator
import cengine.lang.asm.features.AsmCompleter
import cengine.lang.asm.features.AsmFormatter
import cengine.lang.asm.features.AsmHighlighter
import cengine.psi.PsiManager
import cengine.vfs.VFileSystem

class AsmLang(spec: TargetSpec<*>) : LanguageService() {

    companion object {
        const val OUTPUT_DIR = ".asm"
    }

    var spec: TargetSpec<*> = spec
        set(value) {
            field = value
            completionProvider = AsmCompleter(value)
            highlightProvider = AsmHighlighter(value)
        }

    override var runConfig: Runner<AsmLang> = AsmRunner(this)
    
    override val name: String = "Assembly"
    override val fileSuffix: String = ".s"
    
    override var completionProvider: CompletionProvider = AsmCompleter(spec)
    
    override val annotationProvider: AnnotationProvider = AsmAnnotator()
    
    override var highlightProvider: HighlightProvider = AsmHighlighter(spec)
    
    override val formatter: Formatter = AsmFormatter()

    override fun createManager(vfs: VFileSystem): PsiManager<*, *> = PsiManager<AsmLang, AsmFile>(
        vfs,
        PsiManager.Mode.TEXT,
        fileSuffix,
        AsmPsiParser(spec, this)
    ){
        completionProvider.buildCompletionSet(it)
        annotationProvider.updateAnnotations(it)
    }

}