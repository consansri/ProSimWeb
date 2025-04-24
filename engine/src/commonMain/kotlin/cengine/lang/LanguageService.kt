package cengine.lang

import cengine.console.IOContext
import cengine.console.SysOut
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.psi.core.PsiParser
import cengine.psi.elements.PsiFile
import cengine.psi.lexer.PsiLexer
import cengine.psi.parser.PsiBuilder
import cengine.psi.tree.PsiTreeBuilder
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile


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
     * The run configuration associated with this language service.
     */
    abstract val runConfig: Runner<*>
    abstract val psiSupport: PsiSupport?
    open val psiParser: PsiParser = DefaultParser()

    /**
     * The init function which will be called on Project initialization
     */
    open fun init(project: Project) {
        SysOut.log("Initialize LanguageService $name ...")
    }

    /**
     * Creates a [PsiManager] associated with this language service
     */
    abstract fun createManager(vfs: VFileSystem): PsiManager<*>

    inner class DefaultParser : PsiParser {
        override suspend fun parse(file: VirtualFile, ioContext: IOContext): PsiFile {
            val psiSupport = psiSupport ?: return PsiFile(file, false)

            val lexer = PsiLexer(file.getAsUTF8String(), psiSupport.lexerSet)
            val psiBuilder = PsiBuilder(lexer.tokenize(), psiSupport.psiFileType, ioContext)
            with(psiSupport.psiTreeParser) {
                psiBuilder.parseFileContent()
            }

            val treeBuilder = PsiTreeBuilder(psiBuilder.getResult(), ioContext)
            val psiFile = treeBuilder.build(file)
            psiSupport.annotationProvider?.updateAnnotations(psiFile)
            return psiFile
        }
    }

}