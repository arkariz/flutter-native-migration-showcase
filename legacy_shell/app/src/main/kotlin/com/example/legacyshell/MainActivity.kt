package com.example.legacyshell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.legacyshell.bridge.RealFlutterEngineHost
import com.example.legacyshell.bridge.SessionBridge
import com.example.legacyshell.featureflags.MigrationFlags
import com.example.legacyshell.profile.LegacyProfileActivity
import com.example.legacyshell.readiness.MinimumAppVersionGate
import com.example.legacyshell.readiness.ReadinessOutcome
import com.example.legacyshell.readiness.ReadinessPipeline
import com.example.legacyshell.readiness.SessionPresentGate
import com.example.legacyshell.session.SharedPreferencesSessionStore

/**
 * The legacy app's home screen. Gated by a [ReadinessPipeline] (docs/adr/0004)
 * before showing anything; the only place [MigrationFlags.profileScreenOnFlutter]
 * is read for manual navigation (docs/adr/0002) — everything else just has one
 * "open Profile" button.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sessionStore = SharedPreferencesSessionStore(applicationContext)

        val pipeline = ReadinessPipeline(
            listOf(
                SessionPresentGate(sessionStore),
                // Demo baseline: this build satisfies its own minimum, so the gate
                // never blocks in practice. A real app would compare against a
                // minimum fetched from remote config.
                MinimumAppVersionGate(currentVersionCode = BuildConfig.VERSION_CODE, minimumVersionCode = 1),
            ),
        )

        when (val outcome = pipeline.run()) {
            is ReadinessOutcome.Blocked -> {
                if (outcome.reason == "No session") {
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    Toast.makeText(this, outcome.reason, Toast.LENGTH_LONG).show()
                }
                finish()
                return
            }
            ReadinessOutcome.Ready -> Unit
        }

        val session = sessionStore.read()!! // ReadinessPipeline already guaranteed this above.

        findViewById<TextView>(R.id.welcomeText).text =
            getString(R.string.home_welcome, session.displayName)

        findViewById<Button>(R.id.profileButton).setOnClickListener {
            if (MigrationFlags.profileScreenOnFlutter) {
                RealFlutterEngineHost(applicationContext)
                    .launchRoute(this, SessionBridge(sessionStore), route = "profile")
            } else {
                startActivity(Intent(this, LegacyProfileActivity::class.java))
            }
        }
    }
}
