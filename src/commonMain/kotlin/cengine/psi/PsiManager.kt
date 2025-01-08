package cengine.psi

import androidx.compose.runtime.mutableStateMapOf
import cengine.lang.LanguageService
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
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
class PsiManager<L : LanguageService, F: PsiFile>(
    vfs: VFileSystem,
    val lang: L,
    val psiParser: PsiParser<F>
) {
    private var job: Job? = null
    val psiCache = mutableStateMapOf<FPath, F>()
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
    fun queueUpdate(file: VirtualFile, onfinish: suspend (F) -> Unit = {}) {
        job?.cancel()
        job = psiUpdateScope.launch {
            delay(1000L)
            onfinish(updatePsi(file))
        }
    }

    /**
     * Updates the PSI for a given file, creating it if it doesn't exist.
     *
     * @param file the file to update
     * @return the updated or newly created PSI file
     */
    suspend fun updatePsi(file: VirtualFile): F {
        val psiFile = psiCache[file.path]
        return if (psiFile == null) {
            val created = createPsiFile(file)
            lang.updateAnalytics(created)
            created
        } else {
            psiFile.update()
            lang.updateAnalytics(psiFile)
            psiFile
        }
    }

    /**
     * Handles insertion of text in a file and updates the PSI accordingly.
     *
     * @param file the file where insertion occurs
     * @param index the index at which text is inserted
     * @param length the length of the inserted text
     * @return the updated PSI file or null if no PSI file exists for the given file
     */
    fun inserted(file: VirtualFile, index: Int, length: Int): F? {
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
    fun deleted(file: VirtualFile, start: Int, end: Int): F? {
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
    fun queueInsertion(file: VirtualFile, index: Int, length: Int, onfinish: suspend (F) -> Unit = {}) {
        queueUpdate(file, onfinish)
        psiUpdateScope.launch {
            onfinish(inserted(file, index, length) ?: run {
                createPsiFile(file)
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
    fun queueDeletion(file: VirtualFile, start: Int, end: Int, onfinish: suspend (F) -> Unit = {}) {
        queueUpdate(file, onfinish)
        psiUpdateScope.launch {
            onfinish(deleted(file, start, end) ?: run {
                createPsiFile(file)
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
            val psiFile = createPsiFile(file)
            lang.updateAnalytics(psiFile)
            onfinish(psiFile)
        }
    }

    /**
     * Replaces the existing PSI in the cache with a new one.
     *
     * @param psiFile the new PSI file
     */
    private fun replacePsi(psiFile: F){
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
    private suspend fun createPsiFile(file: VirtualFile): F {
        return withContext(Dispatchers.Default) {
            val psiFile = psiParser.parse(file)
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
    fun getPsiFile(file: VirtualFile): F? {
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
}