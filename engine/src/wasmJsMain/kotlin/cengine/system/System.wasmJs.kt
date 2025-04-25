package cengine.system

import JSTools


actual fun getSystemLineBreak(): String = "\n"
actual fun appTarget(): AppTarget = AppTarget.WEB
actual fun downloadDesktopApp(type: DesktopDistribution) {
    JSTools.downloadFile(type.path, type.fileName)
}

actual fun presentDistributions(): List<DesktopDistribution> = DistributionLoader.getCachedAvailableDistributions()