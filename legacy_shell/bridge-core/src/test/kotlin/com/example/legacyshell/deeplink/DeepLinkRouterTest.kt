package com.example.legacyshell.deeplink

import kotlin.test.Test
import kotlin.test.assertEquals

class DeepLinkRouterTest {

    @Test
    fun `profile resolves to Flutter when the flag is on`() {
        val router = DeepLinkRouter(profileScreenOnFlutter = true)

        assertEquals(DeepLinkDestination.Flutter("profile"), router.resolve("profile"))
    }

    @Test
    fun `profile resolves to Legacy when the flag is off`() {
        val router = DeepLinkRouter(profileScreenOnFlutter = false)

        assertEquals(DeepLinkDestination.Legacy("profile"), router.resolve("profile"))
    }

    @Test
    fun `settings always resolves to Flutter regardless of the flag`() {
        assertEquals(DeepLinkDestination.Flutter("settings"), DeepLinkRouter(true).resolve("settings"))
        assertEquals(DeepLinkDestination.Flutter("settings"), DeepLinkRouter(false).resolve("settings"))
    }

    @Test
    fun `an unknown path is unresolved`() {
        assertEquals(DeepLinkDestination.Unresolved, DeepLinkRouter(true).resolve("nonexistent"))
    }

    @Test
    fun `path is normalized (slashes trimmed, case-insensitive)`() {
        assertEquals(DeepLinkDestination.Flutter("settings"), DeepLinkRouter(true).resolve("/Settings/"))
        assertEquals(DeepLinkDestination.Flutter("settings"), DeepLinkRouter(true).resolve("SETTINGS"))
    }

    @Test
    fun `empty path is unresolved`() {
        assertEquals(DeepLinkDestination.Unresolved, DeepLinkRouter(true).resolve(""))
    }
}
