package cengine.vfs

import debug.DebugTools
import nativeLog
import java.nio.file.*
import kotlin.concurrent.thread
import kotlin.io.path.pathString

/**
 * Target specific implementation of FileWatcher which deletes/creates Files in the [vfs] or notifies the [vfs] when changes where recognized in the [watchDirectory].
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FileWatcher actual constructor(actual val vfs: VFileSystem) {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val separator = FileSystems.getDefault().separator
    private val watchKeys = mutableMapOf<WatchKey, Path>()
    private var watchThread: Thread? = null
    private var isWatching = false

    actual fun watchDirectory(path: String) {
        val dir = Paths.get(path)
        val watchKey = dir.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )
        watchKeys[watchKey] = dir
    }

    actual fun startWatching() {
        if (isWatching) return
        isWatching = true
        watchThread = thread(start = true) {
            while (isWatching) {
                val key = watchService.take()
                val dir = watchKeys[key]
                if (dir != null) {
                    for (event in key.pollEvents()) {
                        val kind = event.kind()
                        val fileName = event.context() as? FPath
                        if (fileName != null) {
                            val relativePath = vfs.toRelative(dir.pathString.replace(separator, FPath.DELIMITER))
                            when (kind) {
                                StandardWatchEventKinds.ENTRY_CREATE -> {
                                    if (DebugTools.ENGINE_showFileWatcherInfo) nativeLog("FILE-$relativePath-CREATED")
                                    vfs.findFile(relativePath)?.let {
                                        vfs.notifyFileCreated(it)
                                    }
                                }

                                StandardWatchEventKinds.ENTRY_DELETE -> {
                                    if (DebugTools.ENGINE_showFileWatcherInfo) nativeLog("FILE-$relativePath-DELETED")
                                    vfs.findFile(relativePath)?.let {
                                        vfs.notifyFileDeleted(it)
                                    }
                                    vfs.deleteFile(relativePath)
                                }

                                StandardWatchEventKinds.ENTRY_MODIFY -> {
                                    if (DebugTools.ENGINE_showFileWatcherInfo) nativeLog("FILE-$relativePath-MODIFIED")
                                    vfs.findFile(relativePath)?.let {
                                        vfs.notifyFileChanged(it)
                                    }
                                }
                            }
                        }
                    }
                }
                key.reset()
            }
        }
    }

    actual fun stopWatching() {
        isWatching = false
        watchThread?.interrupt()
        watchThread = null
        watchService.close()
    }

}