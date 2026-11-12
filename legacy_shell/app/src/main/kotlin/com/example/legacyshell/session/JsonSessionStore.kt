package com.example.legacyshell.session

import android.content.Context
import org.json.JSONException
import org.json.JSONObject

/**
 * The **new** schema (v2): one versioned JSON document in its own prefs file, keyed by
 * the same field names as the bridge contract's `session` payload. Physically separate
 * from the legacy file so the CONTRACT phase can delete that file outright (docs/adr/0007).
 */
class JsonSessionStore(context: Context) : SessionStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): UserSession? {
        val raw = prefs.getString(KEY_DOCUMENT, null) ?: return null
        return try {
            val json = JSONObject(raw)
            if (json.optInt(KEY_SCHEMA) != SCHEMA_VERSION) return null
            UserSession(
                userId = json.getString(UserSessionCodec.USER_ID),
                displayName = json.getString(UserSessionCodec.DISPLAY_NAME),
                authToken = json.getString(UserSessionCodec.AUTH_TOKEN),
                expiresAtEpochMillis = json.getLong(UserSessionCodec.EXPIRES_AT),
            )
        } catch (e: JSONException) {
            // A corrupt document reads as "absent": MirroredSessionStore then falls back
            // to the other schema instead of crashing app start.
            null
        }
    }

    override fun write(session: UserSession) {
        val json = JSONObject(UserSessionCodec.encode(session)).put(KEY_SCHEMA, SCHEMA_VERSION)
        prefs.edit().putString(KEY_DOCUMENT, json.toString()).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "session_v2"
        const val KEY_DOCUMENT = "session"
        const val KEY_SCHEMA = "schema"
        const val SCHEMA_VERSION = 2
    }
}
