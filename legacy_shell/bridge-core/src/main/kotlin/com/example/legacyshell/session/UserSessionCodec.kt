package com.example.legacyshell.session

import com.example.legacyshell.bridge.BridgeErrorCode
import com.example.legacyshell.bridge.BridgeException

/** Encodes/decodes [UserSession] to the `session` payload in `contract/bridge_contract.json`. */
object UserSessionCodec {
    const val USER_ID = "userId"
    const val DISPLAY_NAME = "displayName"
    const val AUTH_TOKEN = "authToken"
    const val EXPIRES_AT = "expiresAtEpochMillis"

    val keys: List<String> = listOf(USER_ID, DISPLAY_NAME, AUTH_TOKEN, EXPIRES_AT)

    fun encode(session: UserSession): Map<String, Any> = mapOf(
        USER_ID to session.userId,
        DISPLAY_NAME to session.displayName,
        AUTH_TOKEN to session.authToken,
        EXPIRES_AT to session.expiresAtEpochMillis,
    )

    /** Throws [BridgeException] with [BridgeErrorCode.INVALID_ARGUMENT] on any contract violation. */
    fun decode(raw: Any?): UserSession {
        val map = raw as? Map<*, *> ?: invalid("session payload is not a map")
        return UserSession(
            userId = map.nonEmptyString(USER_ID),
            displayName = map.nonEmptyString(DISPLAY_NAME),
            authToken = map.nonEmptyString(AUTH_TOKEN),
            // Flutter's StandardMessageCodec sends a Dart int that fits in 32 bits as an
            // Integer and a larger one as a Long, so accept any Number here.
            expiresAtEpochMillis = (map[EXPIRES_AT] as? Number)?.toLong() ?: invalid("\"$EXPIRES_AT\" missing or not a number"),
        )
    }

    private fun Map<*, *>.nonEmptyString(key: String): String =
        (this[key] as? String)?.takeIf { it.isNotEmpty() } ?: invalid("\"$key\" missing or empty")

    private fun invalid(message: String): Nothing = throw BridgeException(BridgeErrorCode.INVALID_ARGUMENT, message)
}
