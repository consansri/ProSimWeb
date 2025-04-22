package cengine.system

actual fun getSystemLineBreak(): String = System.lineSeparator()

actual fun appTarget(): AppTarget =  AppTarget.DESKTOP

actual fun downloadDesktopApp(type: DesktopDistribution) {

}

actual fun presentDistributions(): List<DesktopDistribution> = emptyList()