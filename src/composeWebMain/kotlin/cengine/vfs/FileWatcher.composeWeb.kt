package cengine.vfs

/**
 * Target specific implementation of FileWatcher which deletes/creates Files in the [vfs] or notifies the [vfs] when changes where recognized in the [watchDirectory].
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FileWatcher actual constructor(actual val vfs: VFileSystem) {
    /**
     * Stats watching a directory for changes.
     *
     * @param path The absolute path of the directory to watch.
     */
    actual fun watchDirectory(path: String) {
    }

    /**
     * Starts the file-watching process.
     */
    actual fun startWatching() {
    }

    /**
     * Stops the file-watching process.
     */
    actual fun stopWatching() {
    }

}