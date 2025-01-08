import config.BuildConfig

/**
 * Holds constants of the application.
 *
 * @property NAME The name of the application.
 * @property VERSION The version of the application.
 * @property TITLE The title of the application.
 */
data object Constants {
    /**
     * The name of the application.
     */
    const val NAME = BuildConfig.NAME

    /**
     * The version of the application.
     */
    const val VERSION = BuildConfig.VERSION

    /**
     * The title of the application.
     */
    const val TITLE = "$NAME - $VERSION"
}