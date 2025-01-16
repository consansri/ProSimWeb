package cengine.editor

import cengine.editor.annotation.Annotation
import cengine.editor.indentation.BasicIndenation
import cengine.editor.indentation.IndentationProvider
import cengine.editor.selection.Selector
import cengine.editor.text.Editable
import cengine.editor.text.RopeModel
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.lang.LanguageService
import cengine.project.Project
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiService
import cengine.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This model is representing the state of the rendered code.
 */
interface CodeEditor : Editable {

    companion object {
        fun create(file: VirtualFile, project: Project, invalidateContent: (CodeEditor) -> Unit, invalidateAnalytics: (CodeEditor) -> Unit): CodeEditor {
            return object : CodeEditor {
                override val psiManager: PsiManager<*,*>? = project.getManager(file)
                override val lang: LanguageService? = project.getLang(file)
                override val file: VirtualFile = file
                override val textModel: TextModel = RopeModel(file.getAsUTF8String())
                override val selector: Selector = Selector(textModel)
                override val textStateModel: TextStateModel = TextStateModel(this, textModel, selector)
                override val indentationProvider: IndentationProvider = BasicIndenation(textStateModel, textModel)

                override var currentElement: PsiElement? = null
                override var annotations: Set<Annotation> = setOf()

                override fun invalidateContent(editor: CodeEditor) {
                    invalidateContent(editor)
                }

                override fun invalidateAnalytics(editor: CodeEditor) {
                    invalidateAnalytics(editor)
                }
            }
        }
    }

    val psiManager: PsiManager<*,*>?
    val lang: LanguageService?
    val file: VirtualFile
    val textModel: TextModel
    val textStateModel: TextStateModel
    val selector: Selector
    val indentationProvider: IndentationProvider
    var currentElement: PsiElement?
    var annotations: Set<Annotation>
    val psiFile: PsiFile? get() = psiManager?.getPsiFile(file)

    fun saveToFile() {
        file.setAsUTF8String(textModel.toString())
        psiManager?.queueUpdate(file,  ::slowFinish)
    }

    fun loadFromFile() {
        textModel.replaceAll(file.getAsUTF8String().replace("\t", "    "))
        psiManager?.queueUpdate(file,  ::slowFinish)
    }

    override fun insert(index: Int, new: String) {
        textModel.insert(index, new)
        psiManager?.queueInsertion(file, index, new.length, ::fastFinish)
    }

    override fun delete(start: Int, end: Int) {
        textModel.delete(start, end)
        psiManager?.queueDeletion(file,  start, end, ::fastFinish)
    }

    override fun replaceAll(new: String) {
        textModel.replaceAll(new)
        psiManager?.queueUpdate(file,  ::fastFinish)
    }

    private suspend fun fastFinish(psiFile: PsiFile) {
        withContext(Dispatchers.Main) {
            invalidateContent(this@CodeEditor)
        }
    }

    private suspend fun slowFinish(psiFile: PsiFile) {
        withContext(Dispatchers.Main) {
            invalidateContent(this@CodeEditor)
        }
        annotations = PsiService.collectNotations(psiFile)
        invalidateAnalytics(this)
    }

    fun invalidateContent(editor: CodeEditor)

    fun invalidateAnalytics(editor: CodeEditor)
}