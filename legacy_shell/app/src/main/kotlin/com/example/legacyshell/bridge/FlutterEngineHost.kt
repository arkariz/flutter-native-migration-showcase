package com.example.legacyshell.bridge

import android.content.Context

/**
 * Owns the Flutter engine(s) used to show Flutter screens, and wires
 * [ChannelMethodHandler]s to them via [ChannelRegistry] (docs/adr/0006).
 * See [RealFlutterEngineHost] and docs/adr/0001.
 */
interface FlutterEngineHost {
    /** Launches Flutter at [route] (e.g. "profile", "settings"). */
    fun launchRoute(context: Context, sessionBridge: SessionBridge, route: String)
}
