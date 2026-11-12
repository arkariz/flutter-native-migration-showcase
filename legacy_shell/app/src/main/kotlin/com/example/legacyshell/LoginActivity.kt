package com.example.legacyshell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.legacyshell.session.LegacySession
import com.example.legacyshell.session.SharedPreferencesSessionStore
import java.util.UUID

/**
 * Dummy login: accepts any non-empty username, no real credential check. This
 * reference repo cares about what happens *after* login (the migration
 * pattern), not auth itself.
 */
class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString()?.trim().orEmpty()
            if (username.isEmpty()) {
                Toast.makeText(this, R.string.login_error_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val session = LegacySession(
                userId = UUID.randomUUID().toString(),
                displayName = username,
                authToken = "demo-token-${UUID.randomUUID()}",
            )
            SharedPreferencesSessionStore(applicationContext).write(session)

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
