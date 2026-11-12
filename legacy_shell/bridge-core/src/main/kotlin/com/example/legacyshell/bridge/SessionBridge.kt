package com.example.legacyshell.bridge

import com.example.legacyshell.session.LegacySession
import com.example.legacyshell.session.LegacySessionStore

/**
 * The single crossing point for session data between `legacy_shell` and
 * `flutter_module` (see docs/adr/0001). Flutter's `SessionChannel.fetch()` calls
 * [CHANNEL_NAME]'s [METHOD_FETCH] method; this class answers it.
 *
 * Deliberately framework-agnostic (no `android.*` imports) so it's testable with
 * plain JUnit — see `SessionBridgeTest`. The `:app` module's `FlutterEngineHost`
 * is what actually wires this to a real `io.flutter.embedding.android.FlutterEngine`'s
 * `MethodChannel`.
 */
class SessionBridge(private val sessionStore: LegacySessionStore) {

    /**
     * Handles a `"session.fetch"` call: returns the current session as a result
     * map, or throws [NoSessionException] if nobody is logged in.
     */
    fun handleFetch(): Map<String, String> {
        val session = sessionStore.read() ?: throw NoSessionException()
        return toResultMap(session)
    }

    class NoSessionException : IllegalStateException("No session to hand to Flutter")

    companion object {
        const val CHANNEL_NAME = "migration_showcase/session"
        const val METHOD_FETCH = "session.fetch"

        fun toResultMap(session: LegacySession): Map<String, String> = mapOf(
            "userId" to session.userId,
            "displayName" to session.displayName,
            "authToken" to session.authToken,
        )
    }
}
