package com.example.legacyshell.session

import com.example.legacyshell.readiness.TokenRefresher
import java.util.UUID

/**
 * Stand-in for a call to an auth server: always succeeds with a fresh token valid for
 * [validForMillis]. Return null here to see SessionGate clear the session and redirect
 * to sign-in instead.
 */
class FakeTokenRefresher(
    private val now: () -> Long,
    private val validForMillis: Long = SESSION_LIFETIME_MILLIS,
) : TokenRefresher {

    override fun refresh(expired: UserSession): UserSession =
        expired.copy(authToken = "demo-token-${UUID.randomUUID()}", expiresAtEpochMillis = now() + validForMillis)

    companion object {
        const val SESSION_LIFETIME_MILLIS = 60 * 60 * 1000L
    }
}
