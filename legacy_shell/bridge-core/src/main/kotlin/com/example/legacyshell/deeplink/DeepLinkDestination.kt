package com.example.legacyshell.deeplink

/** Where a resolved deep link should land (see docs/adr/0005). */
sealed interface DeepLinkDestination {
    /** Launch the legacy native [screen]. */
    data class Legacy(val screen: String) : DeepLinkDestination

    /** Launch Flutter at [route]. */
    data class Flutter(val route: String) : DeepLinkDestination

    /** No known destination for the given path. */
    data object Unresolved : DeepLinkDestination
}
