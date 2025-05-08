package cengine.system

import Constants


/**
 * Returns the current platform system line break.
 */
expect fun getSystemLineBreak(): String

/**
 * Downloads the desktop app.
 *
 * @param type The distribution type.
 */
expect fun downloadDesktopApp(type: DesktopDistribution)

/**
 * @return A list of all DesktopDistributions downloadable through the resources.
 */
expect fun presentDistributions(): List<DesktopDistribution>

/**
 * @return The target platform for the current app.
 */
expect fun appTarget(): AppTarget

/**
 * DesktopDistribution Type which is possible to download
 */
enum class DesktopDistribution(val fileSuffix: String, val subfolder: String? = null) {
    JAR(".jar", "jar"),
    MSI(".msi", "msi"),
    DEB(".deb", "deb"),
    DMG(".dmg", "dmg"),
    EXE(".exe", "exe");

    val path = "desktop/" + if(subfolder != null) "$subfolder/" else ""
    val fileName = "${Constants.NAME}-${Constants.VERSION}$fileSuffix"
}

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