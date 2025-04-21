import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)

}

val DIST_VERSION: String by project
val DIST_NAME: String by project

version = DIST_VERSION

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
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ionspin.bignum)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)

            implementation(project(":engine"))
        }

        wasmJsMain.dependencies {
            implementation(project(":engine"))
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(project(":engine"))
        }
    }

    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "ikr.prosim.AppKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ProSim"
            packageVersion = DIST_VERSION
        }
    }
}

val fatJarTask = tasks.register<Jar>("fatJar") {
    group = "distribution" // Optional: Add to 'distribution' group in Gradle tasks view
    description = "Assembles a 'fat' JAR containing the application and all dependencies."

    // 1. Set a unique name for the output JAR
    archiveBaseName.set(DIST_NAME) // e.g., "app-desktop-fat"
    archiveVersion.set(DIST_VERSION) // Use project version (ensure DIST_VERSION is compatible or use project.version)

    // 2. Specify the Main Class in the Manifest
    manifest {
        attributes["Main-Class"] = "ikr.prosim.AppKt" // Replace with your actual main class if different
    }

    // 3. Include compiled classes from your desktop source set
    // Assumes your JVM target is named "desktop" as in your kotlin block
    val desktopMainCompilation = kotlin.jvm("desktop").compilations.getByName("main")
    from(desktopMainCompilation.output.classesDirs)

    // 4. Include all runtime dependencies
    // Use the runtime classpath configuration for the desktop target
    val desktopRuntimeClasspath = configurations.getByName("${kotlin.jvm("desktop").name}RuntimeClasspath")
    dependsOn(desktopRuntimeClasspath) // Ensure dependencies are resolved before JAR creation
    from(desktopRuntimeClasspath.map { if (it.isDirectory) it else zipTree(it) }) {
        // Strategy for handling duplicate files found in multiple dependencies
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Exclude duplicates - safer than INCLUDE

        // Explicitly exclude problematic files often found in META-INF
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/MANIFEST.MF", "module-info.class")
    }
}

tasks.register<Copy>("release") {
    group = "distribution"
    description = "Aggregates web distribution and available desktop distributions into a single folder."


    // --- Dependencies ---
    dependsOn(fatJarTask)
    dependsOn("wasmJsBrowserDistribution")
    dependsOn("packageReleaseDmg", "packageReleaseMsi", "packageReleaseDeb", "packageReleaseDistributionForCurrentOS")

    // --- Define what to copy ---

    // 1. Copy WasmJS files into a 'web' subfolder
    from(project.layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))

    // 2. Copy Fat JAR into a 'desktop' subfolder
    from(fatJarTask) { // Reference the fatJarTask directly to get its output
        into("desktop") // Put the Fat JAR into a 'desktop' subfolder
    }

    // 3. Copy native packages into a 'desktop/native' subfolder
    from(project.layout.buildDirectory.dir("compose/binaries/main-release")) {
        // Include only the final package files, checking by extension
        /*include("*." + TargetFormat.Deb.fileExt) // Include .deb if present
        include("*." + TargetFormat.Dmg.fileExt) // Include .dmg if present
        include("*." + TargetFormat.Msi.fileExt) // Include .msi if present
        // If packageDistributionForCurrentOS produces a useful artifact (e.g. zip), include it too
        include("*.zip")*/
        into("desktop") // Put native installers into a 'desktop/native' subfolder
    }

    // --- Define the MAIN destination directory ---
    // Use into() at the top level of the configuration block
    into(project.layout.buildDirectory.dir("release-bundle"))
}

// Optional: If you want to create a ZIP archive of the final bundle
tasks.register<Zip>("releaseZip") {
    group = "distribution"
    description = "Creates a ZIP archive containing the aggregated release bundle."
    dependsOn("release") // Depends on the copy task above

    from(project.layout.buildDirectory.dir("release-bundle")) // Zip the contents of the release-bundle dir
    destinationDirectory.set(project.layout.buildDirectory.dir("distributions")) // Output zip to build/distributions
    archiveFileName.set("${DIST_NAME}-${DIST_VERSION}-release.zip") // Set the output zip file name
}

