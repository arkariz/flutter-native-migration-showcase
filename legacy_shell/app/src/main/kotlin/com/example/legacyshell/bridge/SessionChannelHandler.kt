package com.example.legacyshell.bridge

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * Adapts [SessionBridge] (plain Kotlin, see bridge-core) to [ChannelMethodHandler]
 * so [ChannelRegistry] can wire it to a real engine. See docs/adr/0001, 0006.
 */
class SessionChannelHandler(private val sessionBridge: SessionBridge) : ChannelMethodHandler {

    override val channelName: String = SessionBridge.CHANNEL_NAME

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            SessionBridge.METHOD_FETCH -> try {
                result.success(sessionBridge.handleFetch())
            } catch (e: SessionBridge.NoSessionException) {
                result.error("no_session", e.message, null)
            }
            else -> result.notImplemented()
        }
    }
}
