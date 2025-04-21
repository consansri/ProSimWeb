package cengine.vfs

import cengine.console.SysOut

open class FileSystemException(message: String) : Exception(message) {
    init {
        SysOut.error(message)
    }
}

class NoSuchFileException(path: FPath) : FileSystemException("Path does not exist: $path")
class FileAlreadyExistsException(path: FPath) : FileSystemException("Path already exists: $path")
class IsDirectoryException(path: FPath) : FileSystemException("Path is a directory: $path")
class NotDirectoryException(path: FPath) : FileSystemException("Path is not a directory: $path")
class DirectoryNotEmptyException(path: FPath) : FileSystemException("Directory is not empty: $path")
class QuotaExceededException(message: String) : FileSystemException(message)
class Base64EncodingException(path: FPath, cause: Throwable) : FileSystemException("Failed to decode Base64 for $path: ${cause.message}")