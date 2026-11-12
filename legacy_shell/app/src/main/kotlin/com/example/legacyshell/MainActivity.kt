package com.example.legacyshell

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.legacyshell.flow.FlowResult
import com.example.legacyshell.navigation.StartDestination
import com.example.legacyshell.navigation.open
import com.example.legacyshell.readiness.Blocker
import com.example.legacyshell.readiness.MinimumAppVersionGate
import com.example.legacyshell.readiness.ReadinessKeys
import com.example.legacyshell.readiness.ReadinessOutcome
import com.example.legacyshell.readiness.ReadinessPipeline
import com.example.legacyshell.readiness.SessionGate
import com.example.legacyshell.readiness.SessionSchemaMigrationGate
import com.example.legacyshell.session.FakeTokenRefresher

/**
 * The legacy app's home screen and launcher. Nothing renders until a
 * [ReadinessPipeline] (docs/adr/0004, amended by 0012) says so, and this activity
 * branches on *typed* outcomes — never on a message string.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val now = System::currentTimeMillis
        val pipeline = ReadinessPipeline(
            listOf(
                // Backfill first, so the gates after it read an already-migrated store.
                SessionSchemaMigrationGate(app.sessionMigrator),
                // Demo baseline: this build satisfies its own minimum. A real app would
                // compare against a minimum fetched from remote config.
                MinimumAppVersionGate(BuildConfig.VERSION_CODE, minimumVersionCode = 1, storeUrl = STORE_URL),
                SessionGate(app.sessionStore, FakeTokenRefresher(now), app.routes.signIn(), now),
            ),
        )

        when (val outcome = pipeline.run()) {
            is ReadinessOutcome.Ready -> showHome(outcome.context[ReadinessKeys.SESSION]?.displayName.orEmpty())
            is ReadinessOutcome.Redirected -> {
                redirect(outcome.destination)
                finish()
            }
            is ReadinessOutcome.Halted -> showBlocker(outcome.blocker)
        }
    }

    private fun showHome(displayName: String) {
        findViewById<TextView>(R.id.welcomeText).text = getString(R.string.home_welcome, displayName)
        findViewById<Button>(R.id.profileButton).setOnClickListener { open(app.routes.profile()) }
    }

    /** A Flutter sign-in is a *flow*: it reports back, and only then do we come home. */
    private fun redirect(destination: StartDestination) {
        if (destination is StartDestination.Flutter) {
            val appContext = applicationContext
            app.flowResults.register(SIGN_IN_FLOW) { result ->
                if (result is FlowResult.Completed) {
                    appContext.startActivity(
                        Intent(appContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
            open(destination, flowId = SIGN_IN_FLOW)
        } else {
            open(destination)
        }
    }

    private fun showBlocker(blocker: Blocker) {
        val builder = AlertDialog.Builder(this).setCancelable(false)
        when (blocker) {
            is Blocker.UpdateRequired -> builder
                .setMessage(R.string.blocker_update_required)
                .setPositiveButton(R.string.blocker_update_action) { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(blocker.storeUrl)))
                    finish()
                }
            is Blocker.Internal -> builder
                .setMessage(getString(R.string.blocker_internal, blocker.gate))
                .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
        }
        builder.show()
    }

    private companion object {
        const val SIGN_IN_FLOW = "signIn"
        const val STORE_URL = "market://details?id=com.example.legacyshell"
    }
}
