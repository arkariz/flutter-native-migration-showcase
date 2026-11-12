package com.example.legacyshell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.legacyshell.bridge.RealFlutterEngineHost
import com.example.legacyshell.bridge.SessionBridge
import com.example.legacyshell.featureflags.MigrationFlags
import com.example.legacyshell.profile.LegacyProfileActivity
import com.example.legacyshell.session.SharedPreferencesSessionStore

/**
 * The legacy app's home screen. The only place [MigrationFlags.profileScreenOnFlutter]
 * is read (see docs/adr/0002) — everything else just has one "open Profile" button.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sessionStore = SharedPreferencesSessionStore(applicationContext)
        val session = sessionStore.read()
        if (session == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        findViewById<TextView>(R.id.welcomeText).text =
            getString(R.string.home_welcome, session.displayName)

        findViewById<Button>(R.id.profileButton).setOnClickListener {
            if (MigrationFlags.profileScreenOnFlutter) {
                RealFlutterEngineHost(applicationContext)
                    .launchProfileScreen(this, SessionBridge(sessionStore))
            } else {
                startActivity(Intent(this, LegacyProfileActivity::class.java))
            }
        }
    }
}
