package cengine.vfs

import Constants
import cengine.console.SysOut
import cengine.vfs.FPath.Companion.toFPath
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Platform-specific implementation of the actual file system operations.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object ActualFileSystem {

    actual val DELIMITER: String = FileSystems.getDefault().separator

    /**
     * Reads the content of a file.
     *
     * @param path The relative path of the file to read.
     * @return The content of the file as ByteArray.
     */
    actual fun readFile(path: FPath): ByteArray = Files.readAllBytes(path.toNioPath())

    /**
     * Writes content to a file.
     *
     * @param path The relative path of the file to write.
     * @param content The content to write to the file.
     */
    actual fun writeFile(path: FPath, content: ByteArray) {
        Files.write(path.toNioPath(), content)
    }


    /**
     * Deletes a file or directory.
     *
     * @param path The relative path of the file or directory to delete.
     */
    actual fun deleteFile(path: FPath) {
        try {
            Files.delete(path.toNioPath())
        } catch (e: Exception) {
            // if not existent, then it shouldn't need to be deleted.
        }
    }

    /**
     * Creates a file or directory.
     *
     * @param path The relative path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    actual fun createFile(path: FPath, isDirectory: Boolean) {
        try {
            if (path.withoutLast().isNotEmpty() && !Files.exists(path.withoutLast().toNioPath())) {
                createFile(path.withoutLast(), true)
            }

            if (isDirectory) {
                Files.createDirectory(path.toNioPath())
            } else {
                Files.createFile(path.toNioPath())
            }

        } catch (e: Exception) {
            SysOut.error("Couldn't create file $path! \n${e.message}")
            SysOut.debug { e.stackTraceToString() }
            throw e
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @param path The relative path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    actual fun listDirectory(path: FPath): List<String> {
        return try {
            Files.list(path.toNioPath()).use { stream ->
                stream.map { it.fileName.toString() }.toList()
            }
        } catch (e: Exception) {
            SysOut.error("Couldn't list directory $path! \n${e.message}")
            SysOut.debug { e.stackTraceToString() }
            emptyList()
        }
    }

    /**
     * Checks if a path represents a directory.
     *
     * @param path The relative path to check.
     * @return True if the path is a directory, false otherwise.
     */
    actual fun isDirectory(path: FPath): Boolean = try {
        Files.isDirectory(path.toNioPath())
    } catch (e: Exception) {
        SysOut.error("Couldn't check if path $path is a directory! \n${e.message}")
        SysOut.debug { e.stackTraceToString() }
        false
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param path The relative path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    actual fun exists(path: FPath): Boolean = Files.exists(path.toNioPath())

    /**
     * Returns the [FPath] to the app state directory.
     */
    actual fun getAppStateDir(): FPath {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        val appName = Constants.NAME
        val companyName = Constants.ORG

        return when {
            os.contains("mac") -> FPath( userHome) + "Library" + "Application Support" + appName
            os.contains("win") -> System.getenv("LOCALAPPDATA").toFPath() + companyName + appName
            os.contains("nux") || os.contains("linux") -> userHome.toFPath() + ".local" + "share" + appName
            else -> userHome.toFPath() + ".$appName"
        }
    }

    private fun FPath.toNioPath() = Paths.get(first(), *withoutFirst().parts)

}