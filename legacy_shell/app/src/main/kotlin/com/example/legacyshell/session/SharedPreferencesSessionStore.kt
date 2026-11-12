package com.example.legacyshell.session

import android.content.Context

/** Real, Android-backed [LegacySessionStore]. The pure-Kotlin [SessionBridge]
 *  it's handed to never needs to know this exists — see bridge-core. */
class SharedPreferencesSessionStore(context: Context) : LegacySessionStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): LegacySession? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, null) ?: return null
        val authToken = prefs.getString(KEY_AUTH_TOKEN, null) ?: return null
        return LegacySession(userId, displayName, authToken)
    }

    override fun write(session: LegacySession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_AUTH_TOKEN, session.authToken)
            .apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "legacy_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_AUTH_TOKEN = "auth_token"
    }
}
