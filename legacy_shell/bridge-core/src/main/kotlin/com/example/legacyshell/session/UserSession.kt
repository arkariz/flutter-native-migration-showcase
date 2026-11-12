package com.example.legacyshell.session

/** Who's logged in, shared by both the legacy and the new storage schema (see docs/adr/0007). */
data class UserSession(
    val userId: String,
    val displayName: String,
    val authToken: String,
    val expiresAtEpochMillis: Long,
) {
    fun isExpiredAt(nowEpochMillis: Long): Boolean = nowEpochMillis >= expiresAtEpochMillis
}
