package cengine.project

import cengine.editor.CodeEditor
import cengine.lang.LanguageService
import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.TargetSpec
import cengine.psi.PsiManager
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import nativeLog
import org.jetbrains.skia.impl.Managed

/**
 * @property services [AsmLang] gets always added by the [Project] with specification from [ProjectState].
 */
class Project(initialState: ProjectState, vararg languageServices: LanguageService) : FileChangeListener {

    val projectState: ProjectState = initialState
    val fileSystem: VFileSystem = VFileSystem(projectState.absRootPath)

    val services: Set<LanguageService> = setOf(AsmLang(TargetSpec.specs.firstOrNull { it.name == initialState.target } ?: TargetSpec.specs.first())) + languageServices.toSet()
    val managers: Map<LanguageService, PsiManager<*, *>> = services.associateWith { it.createManager(fileSystem) }

    val currentEditors: MutableList<CodeEditor> = mutableListOf()

    init {
        fileSystem.addChangeListener(this)
        nativeLog("Project ${initialState.absRootPath} Initialized!")
    }

    fun getAsmLang(): AsmLang? = services.filterIsInstance<AsmLang>().firstOrNull()
    fun getManager(lang: LanguageService): PsiManager<*, *>? = managers[lang]

    fun getLang(file: VirtualFile): LanguageService? = services.firstOrNull { file.name.endsWith(it.fileSuffix) }

    fun getManager(file: VirtualFile): PsiManager<*, *>? = managers[getLang(file)]

    fun getLangAndManager(file: VirtualFile): Pair<LanguageService, PsiManager<*,*>>? {
        val lang = getLang(file) ?: return null
        val manager = managers[lang] ?: return null
        return lang to manager
    }

    fun register(editor: CodeEditor) {
        currentEditors.add(editor)
    }

    fun unregister(editor: CodeEditor) {
        currentEditors.remove(editor)
    }

    override fun onFileChanged(file: VirtualFile) {
        currentEditors.firstOrNull { it.file == file }?.loadFromFile()
    }

    override fun onFileCreated(file: VirtualFile) {
        // nothing
    }

    override fun onFileDeleted(file: VirtualFile) {
        // nothing
    }

    fun close() {
        fileSystem.close()
        currentEditors.clear()
    }
}