package com.example.legacyshell.navigation

import android.content.Context
import android.content.Intent
import com.example.legacyshell.LoginActivity
import com.example.legacyshell.MainActivity
import com.example.legacyshell.app
import com.example.legacyshell.help.LegacyHelpActivity
import com.example.legacyshell.profile.LegacyProfileActivity

/** The native screen each [NativeScreen] names. The only `when` over screens in `:app`. */
fun NativeScreen.activityClass(): Class<*> = when (this) {
    NativeScreen.LOGIN -> LoginActivity::class.java
    NativeScreen.HOME -> MainActivity::class.java
    NativeScreen.PROFILE -> LegacyProfileActivity::class.java
    NativeScreen.HELP -> LegacyHelpActivity::class.java
}

/**
 * Opens a [StartDestination] produced by [MigrationRoutes] — manual navigation, deep
 * links and readiness redirects all end here, so none of them branches on a flag.
 * [flowId] marks a Flutter destination as a flow whose result comes back over
 * `flow.complete` (docs/adr/0009).
 */
fun Context.open(destination: StartDestination, flowId: String? = null) {
    when (destination) {
        is StartDestination.Native -> startActivity(Intent(this, destination.screen.activityClass()))
        is StartDestination.Flutter -> app.flutterEngines.launchRoute(this, destination.route, flowId)
    }
}
