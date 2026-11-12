package com.example.legacyshell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.legacyshell.session.FakeTokenRefresher
import com.example.legacyshell.session.UserSession
import java.util.UUID

/**
 * The native sign-in, used while `loginScreenOnFlutter` is off. Dummy: accepts any
 * non-empty username. It writes through the shared mirrored store, so a session made
 * here and one committed by Flutter's sign-in (docs/adr/0008) land in the same places.
 */
class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.usernameInput)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val username = usernameInput.text?.toString()?.trim().orEmpty()
            if (username.isEmpty()) {
                Toast.makeText(this, R.string.login_error_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            app.sessionStore.write(
                UserSession(
                    userId = UUID.randomUUID().toString(),
                    displayName = username,
                    authToken = "demo-token-${UUID.randomUUID()}",
                    expiresAtEpochMillis = System.currentTimeMillis() + FakeTokenRefresher.SESSION_LIFETIME_MILLIS,
                ),
            )

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
