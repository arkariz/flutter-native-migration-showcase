package com.example.legacyshell.readiness

import com.example.legacyshell.navigation.StartDestination
import com.example.legacyshell.session.SessionStore
import com.example.legacyshell.session.UserSession

/** The session's lifecycle, not a "session exists" boolean. */
sealed interface SessionState {
    data class Active(val session: UserSession) : SessionState
    data class Expired(val session: UserSession) : SessionState
    data object Absent : SessionState
}

/** Exchanges an expired session for a fresh one, or returns null if it can't. */
fun interface TokenRefresher {
    fun refresh(expired: UserSession): UserSession?
}

/**
 * Active → continue with the session in context. Expired → try one refresh; on
 * failure, clear and redirect. Absent → redirect. [signIn] comes from
 * `MigrationRoutes.signIn()`, so whether that's the native or the Flutter sign-in
 * screen is decided by the migration flag, not by this gate.
 */
class SessionGate(
    private val store: SessionStore,
    private val refresher: TokenRefresher,
    private val signIn: StartDestination,
    private val now: () -> Long,
) : ReadinessGate {

    override val name = "session"

    fun resolveState(): SessionState {
        val session = store.read() ?: return SessionState.Absent
        return if (session.isExpiredAt(now())) SessionState.Expired(session) else SessionState.Active(session)
    }

    override fun check(context: ReadinessContext): GateResult = when (val state = resolveState()) {
        is SessionState.Active -> GateResult.Proceed(context.with(ReadinessKeys.SESSION, state.session))
        is SessionState.Expired -> {
            val refreshed = refresher.refresh(state.session)
            if (refreshed != null) {
                store.write(refreshed)
                GateResult.Proceed(context.with(ReadinessKeys.SESSION, refreshed))
            } else {
                store.clear()
                GateResult.Redirect(signIn)
            }
        }
        SessionState.Absent -> GateResult.Redirect(signIn)
    }
}
