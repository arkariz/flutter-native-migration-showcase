package com.example.legacyshell.featureflags

/**
 * Per-screen switches: legacy native implementation or new Flutter one (docs/adr/0002).
 * Read in exactly one place — [com.example.legacyshell.navigation.MigrationRoutes] —
 * so manual navigation, deep links and readiness redirects can't disagree.
 */
data class MigrationFlagSet(
    val profileScreenOnFlutter: Boolean,
    val loginScreenOnFlutter: Boolean,
)

/**
 * Hardcoded here to keep the example self-contained; a real app would load this
 * from its remote-config system. Flip [MigrationFlagSet.loginScreenOnFlutter] to
 * exercise the Flutter sign-in → `session.commit` path (docs/adr/0008).
 */
object MigrationFlags {
    val current = MigrationFlagSet(
        profileScreenOnFlutter = true,
        loginScreenOnFlutter = false,
    )
}
