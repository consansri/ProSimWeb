package cengine.system

/**
 * Returns the current platform system line break.
 */
expect fun getSystemLineBreak(): String

/**
 * Checks if the given path is valid on the current platform.
 */
expect fun isAbsolutePathValid(path: String): Boolean

/**
 * Downloads the desktop app.
 *
 * @param fileNameSuffix The suffix of the file name.
 */
expect fun downloadDesktopApp(fileNameSuffix: String)

/**
 * @return The target platform for the current app.
 */
expect fun appTarget(): AppTarget

/**
 * Enum representing the target platform for the current app.
 */
enum class AppTarget {
    /**
     * The app is running on the web.
     */
    WEB,

    /**
     * The app is running on the desktop.
     */
    DESKTOP;
}