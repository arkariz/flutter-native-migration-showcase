package com.example.legacyshell.readiness

import com.example.legacyshell.featureflags.MigrationFlagSet
import com.example.legacyshell.navigation.MigrationRoutes
import com.example.legacyshell.navigation.NativeScreen
import com.example.legacyshell.navigation.StartDestination
import com.example.legacyshell.session
import com.example.legacyshell.session.InMemorySessionStore
import com.example.legacyshell.session.SessionSchemaMigrator
import com.example.legacyshell.session.SessionMigrationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SessionGateTest {

    private val nativeSignIn = StartDestination.Native(NativeScreen.LOGIN)
    private val noRefresh = TokenRefresher { null }

    private fun gate(store: InMemorySessionStore, refresher: TokenRefresher = noRefresh, now: Long = 1_000) =
        SessionGate(store, refresher, nativeSignIn, now = { now })

    @Test
    fun `an active session proceeds and lands in context`() {
        val result = gate(InMemorySessionStore(session(expiresAtEpochMillis = 5_000))).check(ReadinessContext.EMPTY)

        assertIs<GateResult.Proceed>(result)
        assertEquals(session(expiresAtEpochMillis = 5_000), result.context[ReadinessKeys.SESSION])
    }

    @Test
    fun `an expired session is refreshed and the fresh one is stored`() {
        val store = InMemorySessionStore(session(authToken = "stale", expiresAtEpochMillis = 500))
        val fresh = session(authToken = "fresh", expiresAtEpochMillis = 9_000)

        val result = gate(store, refresher = { fresh }).check(ReadinessContext.EMPTY)

        assertIs<GateResult.Proceed>(result)
        assertEquals(fresh, store.read())
        assertEquals(fresh, result.context[ReadinessKeys.SESSION])
    }

    @Test
    fun `an expired session that can't be refreshed is cleared and redirected to sign-in`() {
        val store = InMemorySessionStore(session(expiresAtEpochMillis = 500))

        assertEquals(GateResult.Redirect(nativeSignIn), gate(store).check(ReadinessContext.EMPTY))
        assertNull(store.read())
    }

    @Test
    fun `no session redirects to sign-in`() {
        assertEquals(GateResult.Redirect(nativeSignIn), gate(InMemorySessionStore()).check(ReadinessContext.EMPTY))
    }

    @Test
    fun `which sign-in screen is decided by the migration flag, through MigrationRoutes`() {
        val flutterLogin = MigrationRoutes(MigrationFlagSet(profileScreenOnFlutter = true, loginScreenOnFlutter = true)).signIn()

        val result = SessionGate(InMemorySessionStore(), noRefresh, flutterLogin, now = { 0 }).check(ReadinessContext.EMPTY)

        assertEquals(GateResult.Redirect(StartDestination.Flutter("login")), result)
    }

    @Test
    fun `SessionSchemaMigrationGate records the backfill result and never blocks`() {
        val legacy = InMemorySessionStore(session())
        val result = SessionSchemaMigrationGate(SessionSchemaMigrator(legacy, InMemorySessionStore())).check(ReadinessContext.EMPTY)

        assertIs<GateResult.Proceed>(result)
        assertEquals(SessionMigrationResult.Copied, result.context[ReadinessKeys.SESSION_MIGRATION])
    }
}
