package com.example.legacyshell.bridge

import android.content.Context

/**
 * Owns the Flutter engine used to show the new Profile screen and wires
 * [SessionBridge] to it as the `"migration_showcase/session"` channel's
 * `MethodCallHandler`. See [RealFlutterEngineHost] and docs/adr/0001.
 */
interface FlutterEngineHost {
    fun launchProfileScreen(context: Context, sessionBridge: SessionBridge)
}
