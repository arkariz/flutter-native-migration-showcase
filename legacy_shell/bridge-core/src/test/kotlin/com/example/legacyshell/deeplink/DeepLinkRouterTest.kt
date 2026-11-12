package com.example.legacyshell.deeplink

import com.example.legacyshell.featureflags.MigrationFlagSet
import com.example.legacyshell.navigation.MigrationRoutes
import com.example.legacyshell.navigation.NativeScreen
import com.example.legacyshell.navigation.StartDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkRouterTest {

    private fun router(profileOnFlutter: Boolean = true, loginOnFlutter: Boolean = false) =
        DeepLinkRouter(MigrationRoutes(MigrationFlagSet(profileOnFlutter, loginOnFlutter)))

    @Test
    fun `profile follows the profile flag`() {
        assertEquals(StartDestination.Flutter("profile"), router(profileOnFlutter = true).resolve("profile"))
        assertEquals(StartDestination.Native(NativeScreen.PROFILE), router(profileOnFlutter = false).resolve("profile"))
    }

    @Test
    fun `login follows the login flag`() {
        assertEquals(StartDestination.Flutter("login"), router(loginOnFlutter = true).resolve("login"))
        assertEquals(StartDestination.Native(NativeScreen.LOGIN), router(loginOnFlutter = false).resolve("login"))
    }

    @Test
    fun `settings is Flutter regardless of flags`() {
        assertEquals(StartDestination.Flutter("settings"), router(profileOnFlutter = false).resolve("settings"))
    }

    @Test
    fun `paths are normalized`() {
        assertEquals(StartDestination.Flutter("settings"), router().resolve("/Settings/"))
    }

    @Test
    fun `unknown and empty paths are unresolved`() {
        assertNull(router().resolve("nonexistent"))
        assertNull(router().resolve(""))
    }
}
