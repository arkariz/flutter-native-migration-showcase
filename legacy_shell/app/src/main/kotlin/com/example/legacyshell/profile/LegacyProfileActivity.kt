package com.example.legacyshell.profile

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.example.legacyshell.R
import com.example.legacyshell.app

/**
 * The native Profile screen being migrated away from — the fallback while
 * `profileScreenOnFlutter` is off. It stays in the codebase until the flag is fully
 * rolled out (docs/adr/0002).
 */
class LegacyProfileActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legacy_profile)

        val session = app.sessionStore.read()
        findViewById<TextView>(R.id.legacyProfileDetails).text =
            session?.let { "${it.displayName} (${it.userId})" } ?: "Not logged in"
    }
}
