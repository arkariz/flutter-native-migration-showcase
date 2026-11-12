package com.example.legacyshell.featureflags

/**
 * Per-screen switches deciding whether a screen shows its legacy native
 * implementation or its new Flutter one (see docs/adr/0002).
 *
 * In this reference repo these are hardcoded constants to keep the example
 * self-contained. In a real app this would be backed by whatever remote-config
 * or feature-flag system the app already uses — only where the boolean comes
 * from changes, never how it's used at the call site.
 */
object MigrationFlags {
    /**
     * true = launch the Flutter Profile screen.
     * false = launch the native `LegacyProfileActivity`.
     */
    const val profileScreenOnFlutter: Boolean = true
}
