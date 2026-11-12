package com.example.legacyshell.readiness

import com.example.legacyshell.session.SessionMigrationResult
import com.example.legacyshell.session.UserSession

/** A typed key into [ReadinessContext]. Identity-compared: two keys with the same name are different keys. */
class ContextKey<T : Any>(val name: String) {
    override fun toString(): String = "ContextKey($name)"
}

/**
 * Immutable, typed scratchpad gates hand forward to later gates (docs/adr/0012) —
 * e.g. the session `SessionGate` resolved, so nothing after it re-reads storage.
 * Each gate returns a new context; none mutates the one it was given.
 */
class ReadinessContext private constructor(private val values: Map<ContextKey<*>, Any>) {

    @Suppress("UNCHECKED_CAST") // Safe: values are only ever written through with(), which pairs a key with its own T.
    operator fun <T : Any> get(key: ContextKey<T>): T? = values[key] as T?

    fun <T : Any> with(key: ContextKey<T>, value: T): ReadinessContext = ReadinessContext(values + (key to value))

    companion object {
        val EMPTY = ReadinessContext(emptyMap())
    }
}

/** The keys this app's gates write. */
object ReadinessKeys {
    val SESSION = ContextKey<UserSession>("session")
    val SESSION_MIGRATION = ContextKey<SessionMigrationResult>("sessionMigration")
}
