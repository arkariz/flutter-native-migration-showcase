package com.example.legacyshell.bridge

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * Answers Flutter's `deeplink.initialRoute` call with the [route] the native side
 * resolved before creating this engine — the same pull pattern as
 * [SessionChannelHandler] (docs/adr/0001, 0005, 0006).
 */
class DeepLinkChannelHandler(private val route: String) : ChannelMethodHandler {

    override val channelName: String = BridgeContract.Channels.DEEPLINK

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            BridgeContract.Methods.DEEPLINK_INITIAL_ROUTE -> result.success(route)
            else -> result.notImplemented()
        }
    }
}
