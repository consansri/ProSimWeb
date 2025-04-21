package cengine.vfs

import cengine.console.SysOut
import cengine.vfs.FPath.Companion.toFPath
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

    actual fun watchDirectory(path: FPath) {
        val dir = Paths.get(path.first(), *path.withoutFirst().toTypedArray())
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
                            val path = dir.pathString.replace(separator, FPath.DELIMITER).toFPath()
                            when (kind) {
                                StandardWatchEventKinds.ENTRY_CREATE -> {
                                    SysOut.debug { "FILE-$path-CREATED" }
                                    vfs[path]?.let {
                                        vfs.notifyFileCreated(it)
                                    }
                                }

                                StandardWatchEventKinds.ENTRY_DELETE -> {
                                    SysOut.debug { "FILE-$path-DELETED" }
                                    vfs[path]?.let {
                                        vfs.notifyFileDeleted(it)
                                    }
                                    vfs.deleteFile(path)
                                }

                                StandardWatchEventKinds.ENTRY_MODIFY -> {
                                    SysOut.debug { "FILE-$path-MODIFIED" }
                                    vfs[path]?.let {
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