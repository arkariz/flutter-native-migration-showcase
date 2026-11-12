package com.example.legacyshell.bridge

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * Answers Flutter's `"deeplink.initialRoute"` call with the [route]
 * [RealFlutterEngineHost] already resolved before creating this engine —
 * the same pull pattern [SessionChannelHandler] uses (see docs/adr/0001,
 * 0005, 0006).
 */
class DeepLinkChannelHandler(private val route: String) : ChannelMethodHandler {

    override val channelName: String = CHANNEL_NAME

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            METHOD_INITIAL_ROUTE -> result.success(route)
            else -> result.notImplemented()
        }
    }

    companion object {
        const val CHANNEL_NAME = "migration_showcase/deeplink"
        const val METHOD_INITIAL_ROUTE = "deeplink.initialRoute"
    }
}
