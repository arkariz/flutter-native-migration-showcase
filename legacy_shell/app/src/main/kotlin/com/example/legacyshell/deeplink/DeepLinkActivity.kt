package com.example.legacyshell.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.legacyshell.MainActivity
import com.example.legacyshell.app
import com.example.legacyshell.navigation.open

/**
 * Handles `migrationshowcase://open/<path>` deep links. No migration logic of its own:
 * [DeepLinkRouter] asks the same `MigrationRoutes` manual navigation does (docs/adr/0005).
 * Unknown paths fall back to home, which runs the readiness pipeline as usual.
 */
class DeepLinkActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destination = DeepLinkRouter(app.routes).resolve(intent?.data?.path.orEmpty())
        if (destination != null) {
            open(destination)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }
}
