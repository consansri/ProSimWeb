package cengine.system

import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

actual fun getSystemLineBreak(): String = System.lineSeparator()
actual fun isAbsolutePathValid(path: String): Boolean {
    val pathObj  = Path(path)
    return pathObj.exists() && pathObj.isDirectory()
}

actual fun appTarget(): AppTarget =  AppTarget.DESKTOP
actual fun downloadDesktopApp(fileNameSuffix: String) {
}
