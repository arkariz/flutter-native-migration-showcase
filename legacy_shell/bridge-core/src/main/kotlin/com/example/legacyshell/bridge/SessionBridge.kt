package com.example.legacyshell.bridge

import com.example.legacyshell.session.SessionStore
import com.example.legacyshell.session.UserSession
import com.example.legacyshell.session.UserSessionCodec

/**
 * The single crossing point for session data, in both directions:
 *
 * - [handleFetch] (`session.fetch`, Flutter → native read): docs/adr/0001.
 * - [handleCommit] (`session.commit`, Flutter → native write after a Flutter-side
 *   sign-in): docs/adr/0008 — the *only* place new code writes legacy-visible state.
 *
 * Framework-agnostic (no `android.*`) so it's testable with plain JUnit. [sessionStore]
 * is the [com.example.legacyshell.session.MirroredSessionStore] in the real app, so a
 * commit lands in whichever schemas the current migration phase writes.
 */
class SessionBridge(private val sessionStore: SessionStore) {

    /** Returns the current session as a contract payload, or throws [BridgeErrorCode.NO_SESSION]. */
    fun handleFetch(): Map<String, Any> {
        val session = sessionStore.read()
            ?: throw BridgeException(BridgeErrorCode.NO_SESSION, "No session to hand to Flutter")
        return UserSessionCodec.encode(session)
    }

    /** Validates and stores a session Flutter produced; throws [BridgeErrorCode.INVALID_ARGUMENT]. */
    fun handleCommit(arguments: Any?): UserSession {
        val session = UserSessionCodec.decode(arguments)
        sessionStore.write(session)
        return session
    }
}
