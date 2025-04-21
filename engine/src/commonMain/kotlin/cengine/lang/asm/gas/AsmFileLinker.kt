package cengine.lang.asm.gas

import cengine.console.IOContext
import cengine.console.SysOut
import cengine.lang.asm.gas.AsmEvaluator.evaluate
import cengine.lang.asm.psi.AsmDirective
import cengine.project.Project
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiFile
import cengine.vfs.FPath.Companion.toFPath
import cengine.vfs.VirtualFile
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Links assembly files by resolving `.include` directives for Kotlin Common (Desktop/WasmJs).
 * Handles cycles, avoids redundant processing, and manages suspendable PSI updates.
 * Uses Mutex for shared state protection on multi-threaded targets (Desktop).
 */
class AsmFileLinker(
    private val project: Project,
    private val io: IOContext = SysOut,
) {

    // --- Shared State ---
    // Use standard mutable sets, protected by a Mutex for common compatibility.
    private val visitedFiles: MutableSet<PsiFile> = mutableSetOf()
    private val activeJobs: MutableSet<Job> = mutableSetOf()
    private val stateMutex = Mutex() // Mutex from kotlinx-coroutines-core (common)

    // Coroutine scope for managing launched jobs
    private val linkerJob = SupervisorJob()

    // Use Dispatchers.Default for JVM parallelism; WasmJS runs single-threaded.
    private val coroutineScope = CoroutineScope(Dispatchers.Default + linkerJob)

    // --- Public API ---

    /**
     * Starts the linking process for the given entry file.
     * Suspends until the entry file and all includes are processed or found cyclic.
     */
    suspend fun link(entryFile: PsiFile) {
        io.info("Starting linking process for: ${entryFile.file.path}")
        // Clear shared state under lock
        stateMutex.withLock {
            visitedFiles.clear()
            activeJobs.clear()
        }

        try {
            // Start the recursive walk
            walkFile(entryFile, emptySet(), null)

            // Wait for all launched jobs to complete.
            // Loop until the activeJobs set is empty, checking under lock.
            while (true) {
                val jobsToWait = stateMutex.withLock { activeJobs.toList() } // Copy under lock
                if (jobsToWait.isEmpty()) break // Exit loop when no jobs are active
                // io.info("Waiting for ${jobsToWait.size} include processing job(s)...")
                try {
                    jobsToWait.joinAll() // Wait outside the lock
                } catch (e: CancellationException) {
                    io.warn("Waiting for jobs cancelled.")
                    // Propagate cancellation if the waiting itself is cancelled
                    throw e
                }
                // Re-check activeJobs in the next iteration, as jobs might complete
                // and new ones might be added while joinAll was waiting.
            }

            io.info("Linking process finished for: ${entryFile.file.path}")

        } catch (e: CancellationException) {
            io.warn("Linking process was cancelled.")
            // Ensure the main job is cancelled if cancellation originates elsewhere
            linkerJob.cancel("Linking process cancelled externally", e)
            throw e
        } catch (e: Exception) {
            io.error("An unexpected error occurred during linking: ${e.message}")
            linkerJob.cancel("Fatal error during linking", e) // Cancel ongoing jobs
        } finally {
            // Optional: Cancel the scope if the linker instance is strictly single-use
            // linkerJob.cancel()
        }
    }

    // --- Private Implementation ---

    /**
     * Recursively walks a file (suspending). Manages visited state and cycles using Mutex.
     */
    private suspend fun walkFile(
        fileToWalk: PsiFile,
        processingChain: Set<PsiFile>,
        triggeredBy: AsmDirective.Include?,
    ) {
        var addedToVisited = false // Track if *this invocation* added the file to visited

        // 1. --- Visited Check (Protected) ---
        stateMutex.withLock {
            if (fileToWalk !in visitedFiles) {
                visitedFiles.add(fileToWalk)
                addedToVisited = true
            }
        }

        // If it was already visited (by another concurrent branch or previous run), stop.
        if (!addedToVisited) {
            // io.info("Skipping already visited (checked under lock): ${fileToWalk.name}")
            return
        }

        // 2. --- Cycle Detection ---
        if (fileToWalk in processingChain) {
            val cyclePath = processingChain.joinToString(" -> ") { it.file.name } + " -> ${fileToWalk.file.name}"
            val errorMsg = "Circular dependency detected: $cyclePath"
            io.error(errorMsg)
            triggeredBy?.pathExpr?.addError(errorMsg)
            // Leave in visitedFiles to prevent reprocessing attempts down this failed path.
            return
        }

        // 3. --- Prepare for processing children ---
        val nextProcessingChain = processingChain + fileToWalk
        // io.info("Walking: ${fileToWalk.name} (Chain depth: ${nextProcessingChain.size})")

        // 4. --- Find and process Include Directives (Sequentially within this file) ---
        // Use DFS to find all includes within the current file's PSI tree.
        val elementsToScan = ArrayDeque<PsiElement>()
        elementsToScan.addAll(fileToWalk.children)

        while (elementsToScan.isNotEmpty()) {
            val element = elementsToScan.removeFirst() // Use removeFirst for DFS

            if (element is AsmDirective.Include) {
                // processIncludeDirective is now suspend, call it directly
                try {
                    processIncludeDirective(element, nextProcessingChain)
                } catch (e: CancellationException) {
                    io.warn("Include processing cancelled for ${element.pathExpr.evaluate()} in ${fileToWalk.file}")
                    throw e // Let cancellation propagate
                } catch (e: Exception) {
                    io.error("Error processing include ${element.pathExpr.evaluate()} in ${fileToWalk.file}: ${e.message}")
                    element.pathExpr.addError("Failed to process include: ${e.message}")
                    // Continue processing other includes in the file? Generally yes.
                }
            }
            // Scan children to find nested includes
            elementsToScan.addAll(0, element.children.toList()) // Add children to front for DFS
        }
        // io.info("Finished walking: ${fileToWalk.name}")
    }

    /**
     * Processes a single include directive (suspending). Resolves path, VirtualFile, and suspendable PsiFile.
     * Launches a new `walkFile` coroutine for the target if not already visited.
     */
    private suspend fun processIncludeDirective(
        includeDirective: AsmDirective.Include,
        processingChainForTarget: Set<PsiFile>,
    ) {
        val currentVirtualFile = includeDirective.getFile()?.file ?: run {
            includeDirective.pathExpr.addError("Cannot determine containing file.")
            return
        }

        val path = try {
            includeDirective.pathExpr.evaluate().toFPath()
        } catch (e: Exception) {
            includeDirective.pathExpr.addError("Failed to evaluate include path: ${e.message}")
            io.error("Path evaluation failed in ${currentVirtualFile.name}: ${e.message}")
            null
        } ?: return

        val parent = currentVirtualFile.parent()
        val referencedVirtualFile = (if (parent != null) project.fileSystem[parent, path] else project.fileSystem[path]) ?: run {
            includeDirective.pathExpr.addError("Cannot resolve file: $path")
            return
        }

        // --- Resolve PsiFile (Suspend Call) ---
        val referencedPsiFile = getProjectPsiFile(referencedVirtualFile) ?: run {
            includeDirective.pathExpr.addError("Cannot get/parse PSI for file: ${referencedVirtualFile.path}")
            return
        }

        // --- Set Reference ---
        includeDirective.reference = referencedPsiFile
        io.info("${currentVirtualFile.path}: linked ${referencedPsiFile.file.path}")

        // --- Launch Walker for Included File (Protected Check & Launch) ---
        var shouldLaunch = false
        // Check visited status again under lock before launching
        stateMutex.withLock {
            if (referencedPsiFile !in visitedFiles) {
                shouldLaunch = true
                // We don't add to visitedFiles here; walkFile does that atomically.
            }
        }

        if (shouldLaunch) {
            // Launch the walk for the included file in a new coroutine
            // Use launch directly on the linker's scope.
            coroutineScope.launch {
                val job = coroutineContext.job // Get the Job for this specific coroutine

                // Add job to tracker *within* the new coroutine (under lock)
                stateMutex.withLock { activeJobs.add(job) }

                try {
                    // Call the recursive walk function
                    walkFile(referencedPsiFile, processingChainForTarget, includeDirective)
                } finally {
                    // Ensure removal from tracker on completion/cancellation (under lock)
                    stateMutex.withLock { activeJobs.remove(job) }
                }
            }
            // Note: We don't capture the job returned by launch here, as it's managed inside.
        } else {
            io.info("Skipping launch for already visited target (checked before launch): ${referencedPsiFile.name}")
        }
    }

    /**
     * Gets or updates the PsiFile for a VirtualFile using the suspendable manager function.
     */
    private suspend fun getProjectPsiFile(vFile: VirtualFile): PsiFile? {
        return try {
            val manager = project.getManager(vFile) ?: run {
                io.error("Cannot find PsiManager for ${vFile.path}")
                return null
            }
            // --- Suspend Call ---
            // First try getting existing, then update (which involves suspend IO/parsing)
            manager.getPsiFile(vFile) ?: manager.updatePsi(vFile)
        } catch (e: CancellationException) {
            io.warn("PSI retrieval cancelled for ${vFile.path}")
            throw e // Re-throw cancellation
        } catch (e: Exception) {
            io.error("Failed to get/parse PsiFile for ${vFile.path}: ${e.message}")
            null // Return null on failure
        }
    }
}