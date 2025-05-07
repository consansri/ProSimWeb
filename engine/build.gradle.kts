import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

val DIST_NAME: String by project
val DIST_VERSION: String by project
val DIST_YEAR: String by project
val DIST_DEV: String by project
val DIST_ORG: String by project
val DIST_ORG_SHORT: String by project
val DIST_FILENAME = "$DIST_NAME-$DIST_VERSION"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.buildconfig.kmp)
    alias(libs.plugins.atomicfu)
}

buildConfig {
    packageName("config")

    // Generates fields for all source sets by default.
    buildConfigField("String", "NAME", "\"$DIST_NAME\"")
    buildConfigField("String", "VERSION", "\"$DIST_VERSION\"")
    buildConfigField("String", "YEAR", "\"$DIST_YEAR\"")
    buildConfigField("String", "DEV", "\"$DIST_DEV\"")
    buildConfigField("String", "ORG", "\"$DIST_ORG\"")
    buildConfigField("String", "ORG_SHORT", "\"$DIST_ORG_SHORT\"")
    buildConfigField("String", "FILENAME", "\"$DIST_FILENAME\"")
    buildConfigField("Boolean", "DEBUG", "false")
}

kotlin {
    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "app"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
            testTask {
                dependsOn(":app:wasmJsTestTestDevelopmentExecutableCompileSync")
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {


            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ionspin.bignum)
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
    }
}

group = "prosim"
version = DIST_VERSION

kotlin {
    jvmToolchain(17)
}





