package com.example.legacyshell.session

/**
 * Reads/writes the current [UserSession]. Abstracted so this module (and its tests)
 * never need `android.content.SharedPreferences` — `:app` provides the real
 * implementations, one per storage schema (see docs/adr/0007).
 */
interface SessionStore {
    fun read(): UserSession?
    fun write(session: UserSession)
    fun clear()
}

/** In-memory [SessionStore] for tests and previews. */
class InMemorySessionStore(initial: UserSession? = null) : SessionStore {
    private var current: UserSession? = initial

    override fun read(): UserSession? = current

    override fun write(session: UserSession) {
        current = session
    }

    override fun clear() {
        current = null
    }
}
