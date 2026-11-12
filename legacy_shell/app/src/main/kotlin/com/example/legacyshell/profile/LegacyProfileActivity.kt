package com.example.legacyshell.profile

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.example.legacyshell.R
import com.example.legacyshell.session.SharedPreferencesSessionStore

/**
 * The native Profile screen being migrated away from — kept around as the
 * fallback for [com.example.legacyshell.featureflags.MigrationFlags.profileScreenOnFlutter] == false.
 * See docs/adr/0002 for why this stays in the codebase instead of being deleted
 * on a branch somewhere.
 */
class LegacyProfileActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legacy_profile)

        val session = SharedPreferencesSessionStore(applicationContext).read()
        findViewById<TextView>(R.id.legacyProfileDetails).text =
            session?.let { "${it.displayName} (${it.userId})" } ?: "Not logged in"
    }
}
