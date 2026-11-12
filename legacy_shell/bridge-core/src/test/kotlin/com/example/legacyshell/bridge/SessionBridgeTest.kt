package com.example.legacyshell.bridge

import com.example.legacyshell.session.InMemorySessionStore
import com.example.legacyshell.session.LegacySession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionBridgeTest {

    @Test
    fun `handleFetch returns the current session as a result map`() {
        val store = InMemorySessionStore(
            LegacySession(userId = "u1", displayName = "Ada Lovelace", authToken = "tok-123"),
        )
        val bridge = SessionBridge(store)

        val result = bridge.handleFetch()

        assertEquals(
            mapOf(
                "userId" to "u1",
                "displayName" to "Ada Lovelace",
                "authToken" to "tok-123",
            ),
            result,
        )
    }

    @Test
    fun `handleFetch throws NoSessionException when nobody is logged in`() {
        val store = InMemorySessionStore(initial = null)
        val bridge = SessionBridge(store)

        assertFailsWith<SessionBridge.NoSessionException> { bridge.handleFetch() }
    }

    @Test
    fun `toResultMap never leaks extra fields beyond the documented contract`() {
        val session = LegacySession(userId = "u2", displayName = "Grace Hopper", authToken = "tok-456")

        val result = SessionBridge.toResultMap(session)

        assertEquals(setOf("userId", "displayName", "authToken"), result.keys)
    }
}
