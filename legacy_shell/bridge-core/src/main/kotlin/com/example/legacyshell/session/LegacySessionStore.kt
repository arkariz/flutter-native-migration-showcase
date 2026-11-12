package com.example.legacyshell.session

/**
 * Reads/writes the current [LegacySession]. Abstracted behind an interface so this
 * module (and its tests) never need a real `android.content.SharedPreferences` — the
 * `:app` module provides the real implementation; see `SharedPreferencesSessionStore`.
 */
interface LegacySessionStore {
    fun read(): LegacySession?
    fun write(session: LegacySession)
    fun clear()
}

/** In-memory [LegacySessionStore] used by tests and previews. */
class InMemorySessionStore(initial: LegacySession? = null) : LegacySessionStore {
    private var current: LegacySession? = initial

    override fun read(): LegacySession? = current

    override fun write(session: LegacySession) {
        current = session
    }

    override fun clear() {
        current = null
    }
}
