package com.example.legacyshell.session

import android.content.Context

/**
 * The **legacy** schema (v1): flat keys in the `legacy_session` prefs file, exactly as
 * the pre-migration app wrote them. Older builds still in users' hands read this, which
 * is why [MirroredSessionStore] keeps writing it until the CONTRACT phase (docs/adr/0007).
 */
class SharedPreferencesSessionStore(context: Context) : SessionStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): UserSession? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, null) ?: return null
        val authToken = prefs.getString(KEY_AUTH_TOKEN, null) ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, Long.MIN_VALUE).takeIf { it != Long.MIN_VALUE } ?: return null
        return UserSession(userId, displayName, authToken, expiresAt)
    }

    override fun write(session: UserSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_AUTH_TOKEN, session.authToken)
            .putLong(KEY_EXPIRES_AT, session.expiresAtEpochMillis)
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
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
