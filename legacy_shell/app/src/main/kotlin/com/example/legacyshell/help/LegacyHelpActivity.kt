package com.example.legacyshell.help

import android.app.Activity
import android.os.Bundle
import com.example.legacyshell.R

/**
 * A native screen with no Flutter equivalent yet. Flutter reaches it through
 * `navigation.openNative` — allowed only because [com.example.legacyshell.flow.NativeRoutes]
 * lists it, and only until that `@TemporaryCrossing` is removed (docs/adr/0010).
 */
class LegacyHelpActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legacy_help)
    }
}
