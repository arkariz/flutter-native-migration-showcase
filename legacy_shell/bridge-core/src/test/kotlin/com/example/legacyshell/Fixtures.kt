package com.example.legacyshell

import com.example.legacyshell.session.UserSession

fun session(
    userId: String = "u1",
    displayName: String = "Ada Lovelace",
    authToken: String = "tok-1",
    expiresAtEpochMillis: Long = 10_000,
) = UserSession(userId, displayName, authToken, expiresAtEpochMillis)
