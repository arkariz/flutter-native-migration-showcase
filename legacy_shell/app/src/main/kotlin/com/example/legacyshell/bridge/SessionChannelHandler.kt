package com.example.legacyshell.bridge

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * Adapts [SessionBridge] (plain Kotlin, see bridge-core) to [ChannelMethodHandler]:
 * `session.fetch` (docs/adr/0001) and `session.commit` (docs/adr/0008).
 */
class SessionChannelHandler(private val sessionBridge: SessionBridge) : ChannelMethodHandler {

    override val channelName: String = BridgeContract.Channels.SESSION

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            BridgeContract.Methods.SESSION_FETCH -> result.reply { sessionBridge.handleFetch() }
            BridgeContract.Methods.SESSION_COMMIT -> result.reply { sessionBridge.handleCommit(call.arguments); null }
            else -> result.notImplemented()
        }
    }
}
