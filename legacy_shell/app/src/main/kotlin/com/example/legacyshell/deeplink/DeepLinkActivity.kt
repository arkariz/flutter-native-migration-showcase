package com.example.legacyshell.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.legacyshell.MainActivity
import com.example.legacyshell.bridge.RealFlutterEngineHost
import com.example.legacyshell.bridge.SessionBridge
import com.example.legacyshell.featureflags.MigrationFlags
import com.example.legacyshell.session.SharedPreferencesSessionStore

/**
 * Handles `migrationshowcase://open/<path>` deep links. Contains no
 * migration logic itself — it resolves the path with [DeepLinkRouter]
 * (the same per-screen flag manual navigation uses) and acts on the result.
 * See docs/adr/0005.
 */
class DeepLinkActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent?.data?.path.orEmpty()
        val router = DeepLinkRouter(profileScreenOnFlutter = MigrationFlags.profileScreenOnFlutter)

        when (val destination = router.resolve(path)) {
            is DeepLinkDestination.Flutter -> {
                val sessionStore = SharedPreferencesSessionStore(applicationContext)
                RealFlutterEngineHost(applicationContext)
                    .launchRoute(this, SessionBridge(sessionStore), destination.route)
            }

            is DeepLinkDestination.Legacy, DeepLinkDestination.Unresolved -> {
                // This reference only has one legacy screen (MainActivity's
                // Profile button); a real app would launch the specific
                // legacy Activity named in `destination.screen` here.
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        finish()
    }
}
