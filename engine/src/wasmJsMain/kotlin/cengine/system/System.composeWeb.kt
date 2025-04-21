package cengine.system

import JSTools
import config.BuildConfig

actual fun getSystemLineBreak(): String = "\n"
actual fun isAbsolutePathValid(path: String): Boolean = true
actual fun appTarget(): AppTarget = AppTarget.WEB
actual fun downloadDesktopApp(fileNameSuffix: String) {
    JSTools.downloadFile("", BuildConfig.FILENAME + fileNameSuffix)
}

