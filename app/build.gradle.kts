import groovy.json.JsonOutput
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
            packageName = DIST_NAME
            packageVersion = DIST_VERSION
        }
    }
}

val fatJarTask = tasks.register<Jar>("fatJar") {
    group = "distribution" // Optional: Add to 'distribution' group in Gradle tasks view
    description = "Assembles a 'fat' JAR containing the application and all dependencies."

    // 1. Set a unique name for the output JAR
    archiveBaseName.set(DIST_NAME)
    archiveVersion.set(DIST_VERSION)

    // 2. Specify the Main Class in the Manifest
    manifest {
        attributes["Main-Class"] = "ikr.prosim.AppKt"
    }

    // 3. Include compiled classes from your desktop source set
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

// --- Task to Generate the Manifest JSON ---
val generateManifestTask = tasks.register("generateManifest") {
    group = "distribution"
    description = "Generates distributions-manifest.json based on available desktop packages."

    // Define Inputs: The tasks that produce the distributions we care about
    // This ensures this task runs after them and can check their outputs.
    val packageDmgTask = tasks.findByName("packageReleaseDmg")
    val packageMsiTask = tasks.findByName("packageReleaseMsi")
    val packageDebTask = tasks.findByName("packageReleaseDeb")

    // Explicitly declare task dependencies for clarity and correctness
    dependsOn(fatJarTask)
    if (packageDmgTask != null) dependsOn(packageDmgTask)
    if (packageMsiTask != null) dependsOn(packageMsiTask)
    if (packageDebTask != null) dependsOn(packageDebTask)


    // Define Output: The manifest file itself
    // Place it directly where the 'release' task will expect it.
    val manifestFile = project.layout.buildDirectory.file("release-bundle/desktop/distributions-manifest.json")
    outputs.file(manifestFile)

    doLast { // Action to perform when the task executes
        val availableDistributions = mutableListOf<String>()

        // 1. Check for Fat JAR
        // Use the task's output file property
        val fatJarFile = fatJarTask.get().outputs.files.singleFile
        if (fatJarFile.exists()) {
            logger.info("Found distribution: JAR")
            availableDistributions.add("JAR")
        } else {
            logger.warn("Fat JAR file not found: ${fatJarFile.path}")
        }

        // Base directory where native packages are expected
        // Default: build/compose/binaries/main-release/
        // Adjust if you changed nativeDistributions.outputBaseDir
        val nativeDistBaseDir = project.layout.buildDirectory.dir("compose/binaries/main-release")

        // 2. Check for DMG (if task exists)
        if (packageDmgTask != null) {
            // Find the .dmg file within the task's output directory
            val dmgFile = nativeDistBaseDir.get().asFileTree.find { it.name.endsWith(".dmg") }
            if (dmgFile != null && dmgFile.exists()) {
                logger.info("Found distribution: DMG")
                availableDistributions.add("DMG")
            } else {
                logger.warn("DMG file not found in ${nativeDistBaseDir.get().asFile.path}")
            }
        }

        // 3. Check for MSI (if task exists)
        if (packageMsiTask != null) {
            val msiFile = nativeDistBaseDir.get().asFileTree.find { it.name.endsWith(".msi") }
            if (msiFile != null && msiFile.exists()) {
                logger.info("Found distribution: MSI")
                availableDistributions.add("MSI")
            } else {
                logger.warn("MSI file not found in ${nativeDistBaseDir.get().asFile.path}")
            }
        }

        // 4. Check for DEB (if task exists)
        if (packageDebTask != null) {
            val debFile = nativeDistBaseDir.get().asFileTree.find { it.name.endsWith(".deb") }
            if (debFile != null && debFile.exists()) {
                logger.info("Found distribution: DEB")
                availableDistributions.add("DEB")
            } else {
                logger.warn("DEB file not found in ${nativeDistBaseDir.get().asFile.path}")
            }
        }

        logger.lifecycle("[ManifestGen] Final list before JSON conversion: $availableDistributions")

        if (availableDistributions.isEmpty()) {
            logger.warn("[ManifestGen] The availableDistributions list is empty before creating JSON. Manifest will be empty.")
        }

        // 5. Create JSON structure
        val manifestData = mapOf("available" to availableDistributions)
        var jsonOutput: String? = null
        var prettyJsonOutput: String? = null
        try {
            jsonOutput = JsonOutput.toJson(manifestData)
            prettyJsonOutput = JsonOutput.prettyPrint(jsonOutput)
            logger.info("[ManifestGen] Generated JSON string (pretty): \n$prettyJsonOutput") // Log the JSON content
        } catch (e: Exception) {
            logger.error("[ManifestGen] Error during JSON generation: ${e.message}", e)
            // Skip writing if JSON generation failed
            return@doLast
        }

        if (prettyJsonOutput.isNullOrBlank() && availableDistributions.isNotEmpty()) {
            logger.warn("[ManifestGen] WARNING: JSON output is blank/null even though the input list was not empty!")
        }

        // 6. Write JSON to the output file
        val outputFile = manifestFile.get().asFile
        try {
            logger.lifecycle("[ManifestGen] Attempting to write JSON to: ${outputFile.absolutePath}")
            outputFile.parentFile.mkdirs() // Ensure the 'desktop' directory exists
            outputFile.writeText(prettyJsonOutput ?: "{}") // Write pretty JSON, or empty object if null
            logger.lifecycle("[ManifestGen] Successfully wrote JSON to: ${outputFile.absolutePath}")

            // Optional: Read back immediately to verify
            val writtenContent = outputFile.readText()
            logger.info("[ManifestGen] Content read back from file: \n$writtenContent")

        } catch (e: Exception) {
            logger.error("[ManifestGen] CRITICAL: Error writing manifest file to ${outputFile.absolutePath}: ${e.message}", e)
        }

        // This final log remains useful
        logger.lifecycle("[ManifestGen] Task finished. Target file: ${outputFile.path}. Expected distributions in JSON: $availableDistributions")
    }
}

tasks.register<Copy>("release") {
    group = "distribution"
    description = "Aggregates web distribution and available desktop distributions into a single folder."

    // --- Dependencies ---
    // Ensure manifest is generated before this task copies anything
    dependsOn(generateManifestTask)
    // Other dependencies remain
    dependsOn("wasmJsBrowserDistribution")
    // Rely on generateManifestTask's dependencies for packaging tasks

    // --- Define the MAIN destination directory ---
    into(project.layout.buildDirectory.dir("release-bundle"))

    // --- Define what to copy ---

    // 1. Copy WasmJS files (no 'into' needed here, copies directly to root of destination)
    from(project.layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))

    // 2. Copy Fat JAR into 'desktop/jar' subfolder
    from(fatJarTask) { // Reference the fatJarTask directly
        into("desktop/jar")
    }

    // 3. Copy native packages into 'desktop/<subfolder>'
    // Base directory where native packages are found
    val nativeDistBaseDir = project.layout.buildDirectory.dir("compose/binaries/main-release")

    // Copy DMG if it exists
    from(nativeDistBaseDir) {
        include("*.dmg")
        into("desktop/dmg") // Place in specific subfolder
        // onlyIf { packageDmgTask != null && !packageDmgTask.outputs.files.isEmpty } // Optional: Condition
    }
    // Copy MSI if it exists
    from(nativeDistBaseDir) {
        include("*.msi")
        into("desktop/msi")
        // onlyIf { packageMsiTask != null && !packageMsiTask.outputs.files.isEmpty } // Optional: Condition
    }
    // Copy DEB if it exists
    from(nativeDistBaseDir) {
        include("*.deb")
        into("desktop/deb")
        // onlyIf { packageDebTask != null && !packageDebTask.outputs.files.isEmpty } // Optional: Condition
    }

    // Optional: Log the final structure (useful for debugging)
    doLast {
        logger.lifecycle("Release bundle created at: ${destinationDir.absolutePath}")
        logger.lifecycle("Bundle contents:")
        // Simple listing, could be more elaborate if needed
        project.fileTree(destinationDir).visit {
            logger.lifecycle("- $relativePath")
        }
    }
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

