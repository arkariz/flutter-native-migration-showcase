package com.example.legacyshell.bridge

import android.content.Context

/**
 * Owns the Flutter engine(s) used to show Flutter screens, and wires
 * [ChannelMethodHandler]s to them via [ChannelRegistry] (docs/adr/0006).
 * See [RealFlutterEngineHost] and docs/adr/0001.
 */
interface FlutterEngineHost {
    /**
     * Launches Flutter at [route]. With a [flowId], the launch is a *flow*: the caller
     * registered on the `FlowResultBus` under that id and gets exactly one result back
     * (docs/adr/0009). Without one, it's a plain screen.
     */
    fun launchRoute(context: Context, route: String, flowId: String? = null)
}
