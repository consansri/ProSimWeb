package cengine.system

import cengine.console.SysOut
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.fetch.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// --- WasmJs Specific Distribution Loading ---

@Serializable // Define or import from common if needed there too
private data class DistributionManifest(val available: List<String> = emptyList())

/**
 * Manages the state and loading of available distributions purely within WasmJs.
 */
object DistributionLoader {

    // Path to the manifest file relative to the web server root
    private const val MANIFEST_PATH = "desktop/distributions-manifest.json"

    // StateFlow to hold and emit the list of available distributions
    private val _availableDistributionsFlow = MutableStateFlow<List<DesktopDistribution>>(emptyList())
    val availableDistributionsFlow: StateFlow<List<DesktopDistribution>> = _availableDistributionsFlow.asStateFlow()

    // JSON parser configuration
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    // Create a dedicated scope for this loader's background tasks.
    // SupervisorJob ensures failure of one job doesn't cancel the scope.
    // Using Default dispatcher for potentially blocking network/parse operations.
    private val loaderScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        initiateLoading()
    }

    /**
     * Internal function to launch the loading coroutine. Called by init.
     */
    private fun initiateLoading() {
        val manifestUrl = "${window.location.origin}/${MANIFEST_PATH.trimStart('/')}"
        SysOut.log("[Loader] Auto-initiating manifest load from:", manifestUrl)

        // Use the object's internal scope
        loaderScope.launch {
            try {
                val jsonString = fetchText(manifestUrl)
                val manifest = jsonParser.decodeFromString<DistributionManifest>(jsonString)
                val foundDistributions = manifest.available.mapNotNull { name ->
                    try {
                        DesktopDistribution.valueOf(name.uppercase())
                    } catch (e: IllegalArgumentException) {
                        SysOut.warn("[Loader] Unknown distribution name in manifest:", name)
                        null
                    }
                }
                _availableDistributionsFlow.value = foundDistributions
                SysOut.log("[Loader] Manifest loaded successfully. Found:", foundDistributions.map { it.name })
            } catch (e: Throwable) {
                SysOut.error("[Loader] Error loading or parsing manifest '$manifestUrl':", e.message, e)
                _availableDistributionsFlow.value = emptyList() // Reset on error
            }
        }
    }

    /**
     * Gets the currently cached list of available distributions.
     * Returns the state from the last successful load or an empty list.
     */
    fun getCachedAvailableDistributions(): List<DesktopDistribution> {
        return _availableDistributionsFlow.value
    }

    /**
     * Internal suspend function to fetch text content using fetch and suspendCoroutine.
     * This bridges the JavaScript Promise with Kotlin Coroutines manually.
     */
    private suspend fun fetchText(url: String): String = suspendCoroutine { continuation ->
        window.fetch(url)
            .then(
                onFulfilled = { response: Response ->
                    if (response.ok) {
                        // response.text() returns another Promise<String>
                        response.text().then(
                            onFulfilled = { text: JsString? ->
                                continuation.resume(text?.toString() ?: "")
                                null
                            },
                            onRejected = { error: JsAny? ->
                                continuation.resumeWithException(handleFetchError(error, url))
                                null
                            }
                        )
                    } else {
                        // Handle HTTP errors (like 404 Not Found)
                        continuation.resumeWithException(
                            RuntimeException("Failed to fetch '$url': ${response.status} ${response.statusText}")
                        )
                    }
                    null
                },
                onRejected = { error ->
                    // Handle network errors (CORS, DNS, etc.)
                    continuation.resumeWithException(handleFetchError(error, url))
                    null
                }
            )
    }

    /**
     * Helper to convert JavaScript errors to Kotlin Throwables for better handling.
     */
    private fun handleFetchError(error: JsAny?, url: String): Throwable {
        val message = "Fetch error for '$url': $error"
        // You could potentially inspect the error type further if needed
        return RuntimeException(message)
    }
}