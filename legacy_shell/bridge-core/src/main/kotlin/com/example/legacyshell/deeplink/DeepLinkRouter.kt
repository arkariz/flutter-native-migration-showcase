package com.example.legacyshell.deeplink

import com.example.legacyshell.navigation.MigrationRoutes
import com.example.legacyshell.navigation.StartDestination

/**
 * Resolves a deep link path using the same [MigrationRoutes] manual navigation uses
 * (docs/adr/0005) — deep links are not a separate routing mechanism with their own
 * rules. Returns null for a path nobody handles.
 */
class DeepLinkRouter(private val routes: MigrationRoutes) {

    fun resolve(path: String): StartDestination? = when (path.trim('/').lowercase()) {
        MigrationRoutes.Routes.PROFILE -> routes.profile()
        MigrationRoutes.Routes.SETTINGS -> routes.settings()
        MigrationRoutes.Routes.LOGIN -> routes.signIn()
        else -> null
    }
}
