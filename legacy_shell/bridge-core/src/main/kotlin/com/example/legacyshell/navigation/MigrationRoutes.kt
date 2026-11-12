package com.example.legacyshell.navigation

import com.example.legacyshell.featureflags.MigrationFlagSet

/**
 * The one place a [MigrationFlagSet] turns into destinations. Manual navigation
 * (`MainActivity`), deep links ([com.example.legacyshell.deeplink.DeepLinkRouter])
 * and readiness redirects ([com.example.legacyshell.readiness.SessionGate]) all ask
 * this class — none of them reads a flag directly (docs/adr/0002, 0005).
 */
class MigrationRoutes(private val flags: MigrationFlagSet) {

    fun profile(): StartDestination =
        if (flags.profileScreenOnFlutter) StartDestination.Flutter(Routes.PROFILE) else StartDestination.Native(NativeScreen.PROFILE)

    fun signIn(): StartDestination =
        if (flags.loginScreenOnFlutter) StartDestination.Flutter(Routes.LOGIN) else StartDestination.Native(NativeScreen.LOGIN)

    /** Never had a native implementation — Flutter regardless of flags. */
    fun settings(): StartDestination = StartDestination.Flutter(Routes.SETTINGS)

    object Routes {
        const val PROFILE = "profile"
        const val LOGIN = "login"
        const val SETTINGS = "settings"
    }
}
