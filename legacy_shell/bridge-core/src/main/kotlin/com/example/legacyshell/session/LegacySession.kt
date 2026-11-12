package com.example.legacyshell.session

/** The legacy app's notion of "who's logged in". */
data class LegacySession(
    val userId: String,
    val displayName: String,
    val authToken: String,
)
