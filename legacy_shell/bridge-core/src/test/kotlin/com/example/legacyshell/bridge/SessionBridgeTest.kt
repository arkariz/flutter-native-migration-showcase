package com.example.legacyshell.bridge

import com.example.legacyshell.session
import com.example.legacyshell.session.InMemorySessionStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionBridgeTest {

    @Test
    fun `fetch returns the session as a contract payload`() {
        val bridge = SessionBridge(InMemorySessionStore(session()))

        assertEquals(
            mapOf("userId" to "u1", "displayName" to "Ada Lovelace", "authToken" to "tok-1", "expiresAtEpochMillis" to 10_000L),
            bridge.handleFetch(),
        )
    }

    @Test
    fun `fetch fails with NO_SESSION when nobody is signed in`() {
        val error = assertFailsWith<BridgeException> { SessionBridge(InMemorySessionStore()).handleFetch() }

        assertEquals(BridgeErrorCode.NO_SESSION, error.code)
    }

    @Test
    fun `commit stores what Flutter sent`() {
        val store = InMemorySessionStore()

        SessionBridge(store).handleCommit(
            mapOf("userId" to "u2", "displayName" to "Grace", "authToken" to "tok-2", "expiresAtEpochMillis" to 99L),
        )

        assertEquals(session("u2", "Grace", "tok-2", 99L), store.read())
    }

    @Test
    fun `commit accepts expiresAt as Int, the way StandardMessageCodec sends small Dart ints`() {
        val store = InMemorySessionStore()

        SessionBridge(store).handleCommit(
            mapOf("userId" to "u2", "displayName" to "Grace", "authToken" to "tok-2", "expiresAtEpochMillis" to 99),
        )

        assertEquals(99L, store.read()?.expiresAtEpochMillis)
    }

    @Test
    fun `commit rejects contract violations with INVALID_ARGUMENT and stores nothing`() {
        val store = InMemorySessionStore()
        val bad = listOf(
            null,
            "not a map",
            mapOf("displayName" to "Grace", "authToken" to "t", "expiresAtEpochMillis" to 1L),
            mapOf("userId" to "", "displayName" to "Grace", "authToken" to "t", "expiresAtEpochMillis" to 1L),
            mapOf("userId" to "u", "displayName" to "Grace", "authToken" to "t", "expiresAtEpochMillis" to "soon"),
        )

        for (arguments in bad) {
            val error = assertFailsWith<BridgeException>("for $arguments") { SessionBridge(store).handleCommit(arguments) }
            assertEquals(BridgeErrorCode.INVALID_ARGUMENT, error.code)
        }
        assertEquals(null, store.read())
    }
}
