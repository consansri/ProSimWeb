package cengine.psi

import androidx.compose.runtime.mutableStateMapOf
import cengine.console.IOContext
import cengine.console.SysOut
import cengine.lang.LanguageService
import cengine.psi.elements.PsiFile
import cengine.vfs.FPath
import cengine.vfs.FileChangeListener
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import kotlinx.coroutines.*

/**
 * PsiManager is responsible for managing the PSI (Program Structure Interface) files
 * and updates related to a specific language within a virtual file system (VFS).
 * It listens to file changes and manages the creation, updating, and caching of PSI files.
 *
 * @param L the type of LanguageService
 * @param F the type of PsiFile
 * @property vfs the virtual file system to monitor
 * @property lang the language service for handling language-specific operations
 * @property psiParser the parser for creating PSI files
 */
class PsiManager<L : LanguageService>(
    val vfs: VFileSystem,
    val mode: Mode,
    val lang: LanguageService,
    val updateAnalytics: (psiFile: PsiFile) -> Unit,
) {
    private var job: Job? = null
    val psiCache = mutableStateMapOf<FPath, PsiFile>()
    private val psiUpdateScope = CoroutineScope(Dispatchers.Default)
    private val listener = VFSListener()

    init {
        vfs.addChangeListener(listener)
    }

    /**
     * Queues an update for a specific file. Cancels any ongoing job before starting a new one.
     *
     * @param file the file to update
     * @param onfinish the callback to execute after update
     */
    fun queueUpdate(file: VirtualFile, onfinish: suspend (PsiFile) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            delay(1000L)
            onfinish(updatePsi(file))
        }
    }

    /**
     * Searches for a file with relative [path] if it can be found it will update it's psi.
     *
     * @param path the path where to search for the file
     */
    suspend fun findAndUpdate(context: VirtualFile, path: FPath, ioContext: IOContext = SysOut): PsiFile? {
        val vfile = vfs[context, path] ?: return null
        return updatePsi(vfile, ioContext)
    }

    /**
     * Searches for a file with absolute [path] if it can be found it will update it's psi.
     *
     * @param path the path where to search for the file
     */
    suspend fun findAndUpdate(path: FPath, ioContext: IOContext = SysOut): PsiFile? {
        val vfile = vfs[path] ?: return null
        return updatePsi(vfile)
    }

    /**
     * Updates the PSI for a given file, creating it if it doesn't exist.
     *
     * @param file the file to update
     * @return the updated or newly created PSI file
     */
    suspend fun updatePsi(file: VirtualFile, ioContext: IOContext = SysOut): PsiFile {
        val created = parsePsiFile(file, ioContext)
        updateAnalytics(created)
        return created
    }

    /**
     * Handles insertion of text in a file and updates the PSI accordingly.
     *
     * @param file the file where insertion occurs
     * @param index the index at which text is inserted
     * @param length the length of the inserted text
     * @return the updated PSI file or null if no PSI file exists for the given file
     */
    fun inserted(file: VirtualFile, index: Int, length: Int): PsiFile? {
        val psiFile = getPsiFile(file) ?: return null

        psiFile.inserted(index, length)

        return psiFile
    }

    /**
     * Handles deletion of text in a file and updates the PSI accordingly.
     *
     * @param file the file where deletion occurs
     * @param start the starting index of the deletion
     * @param end the ending index of the deletion
     * @return the updated PSI file or null if no PSI file exists for the given file
     */
    fun deleted(file: VirtualFile, start: Int, end: Int): PsiFile? {
        val psiFile = getPsiFile(file) ?: return null

        psiFile.deleted(start, end)

        return psiFile
    }

    /**
     * Queues an insertion operation and updates the PSI.
     *
     * @param file the file where insertion occurs
     * @param index the index at which text is inserted
     * @param length the length of the inserted text
     * @param onfinish the callback to execute after insertion
     */
    fun queueInsertion(file: VirtualFile, index: Int, length: Int, onfinish: suspend (PsiFile) -> Unit = {}) {
        queueUpdate(file, onfinish)
        psiUpdateScope.launch {
            onfinish(inserted(file, index, length) ?: run {
                parsePsiFile(file, SysOut)
            })
        }
    }

    /**
     * Queues a deletion operation and updates the PSI.
     *
     * @param file the file where deletion occurs
     * @param start the starting index of the deletion
     * @param end the ending index of the deletion
     * @param onfinish the callback to execute after deletion
     */
    fun queueDeletion(file: VirtualFile, start: Int, end: Int, onfinish: suspend (PsiFile) -> Unit = {}) {
        queueUpdate(file, onfinish)
        psiUpdateScope.launch {
            onfinish(deleted(file, start, end) ?: run {
                parsePsiFile(file, SysOut)
            })
        }
    }

    /**
     * Creates a new PSI for a file and updates analytics.
     *
     * @param file the file to create PSI for
     * @param onfinish the callback to execute after creation
     */
    private fun createPsi(file: VirtualFile, onfinish: suspend (PsiFile) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            val psiFile = parsePsiFile(file, SysOut)
            updateAnalytics(psiFile)
            onfinish(psiFile)
        }
    }

    /**
     * Replaces the existing PSI in the cache with a new one.
     *
     * @param psiFile the new PSI file
     */
    private fun replacePsi(psiFile: PsiFile) {
        psiCache.remove(psiFile.file.path)
        psiCache[psiFile.file.path] = psiFile
    }

    /**
     * Removes a PSI from the cache for a given file.
     *
     * @param file the file whose PSI should be removed
     */
    private fun removePsi(file: VirtualFile) {
        psiCache.remove(file.path)
    }

    /**
     * Creates a new PSI file from a virtual file.
     *
     * @param file the virtual file to parse
     * @return the newly created PSI file
     */
    private suspend fun parsePsiFile(file: VirtualFile, ioContext: IOContext): PsiFile {
        return withContext(Dispatchers.Default) {
            val psiFile = lang.psiParser.parse(file, ioContext)
            replacePsi(psiFile)
            psiFile
        }
    }

    /**
     * Retrieves a PSI file from the cache.
     *
     * @param file the file to retrieve the PSI for
     * @return the PSI file or null if not found
     */
    fun getPsiFile(file: VirtualFile): PsiFile? {
        return psiCache[file.path]
    }

    /**
     * Returns a string representation of the current PSI cache.
     *
     * @return the string representation of the PSI cache
     */
    fun printCache(): String = psiCache.toString()

    /**
     * Listens to changes in the Virtual File System (VFS) for this project.
     * If a file is changed, created or deleted, it will invalidate the PSI
     * and reparse the file.
     */
    inner class VFSListener : FileChangeListener {
        override fun onFileChanged(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) queueUpdate(file)
        }

        override fun onFileCreated(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) createPsi(file)
        }

        override fun onFileDeleted(file: VirtualFile) {
            if (file.name.endsWith(lang.fileSuffix)) removePsi(file)
        }
    }

    enum class Mode {
        TEXT,
        BINARY
    }
}