package com.example.legacyshell.readiness

import com.example.legacyshell.session.LegacySessionStore

/** Blocks unless a [LegacySessionStore] has a session. */
class SessionPresentGate(private val sessionStore: LegacySessionStore) : ReadinessGate {
    override fun check(): ReadinessResult =
        if (sessionStore.read() != null) {
            ReadinessResult.Pass
        } else {
            ReadinessResult.Block("No session")
        }
}
