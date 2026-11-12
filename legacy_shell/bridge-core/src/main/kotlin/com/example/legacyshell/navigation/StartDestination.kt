package com.example.legacyshell.navigation

/** Native screens the Flutter side may address by name (see NativeRoutes, docs/adr/0010). */
enum class NativeScreen { LOGIN, HOME, PROFILE, HELP }

/** Where the app should go next — either world, as plain data (docs/adr/0012). */
sealed interface StartDestination {
    data class Native(val screen: NativeScreen) : StartDestination
    data class Flutter(val route: String) : StartDestination
}
