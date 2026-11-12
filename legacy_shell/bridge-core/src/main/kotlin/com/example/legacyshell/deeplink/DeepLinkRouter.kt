package com.example.legacyshell.deeplink

/**
 * Resolves a deep link path to a [DeepLinkDestination], consulting the same
 * per-screen migration flag manual navigation uses — see docs/adr/0005.
 * Deep links are not a separate routing mechanism with their own rules.
 */
class DeepLinkRouter(private val profileScreenOnFlutter: Boolean) {

    fun resolve(path: String): DeepLinkDestination = when (normalize(path)) {
        "profile" -> if (profileScreenOnFlutter) {
            DeepLinkDestination.Flutter("profile")
        } else {
            DeepLinkDestination.Legacy("profile")
        }

        // Never had a legacy implementation — always Flutter, flag-independent.
        "settings" -> DeepLinkDestination.Flutter("settings")

        else -> DeepLinkDestination.Unresolved
    }

    private fun normalize(path: String): String = path.trim('/').lowercase()
}
