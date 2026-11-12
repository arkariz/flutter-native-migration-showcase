package com.example.legacyshell.bridge

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * One crossing point's worth of method handling, registered onto an engine
 * by [ChannelRegistry]. See docs/adr/0006.
 */
interface ChannelMethodHandler {
    val channelName: String
    fun onMethodCall(call: MethodCall, result: MethodChannel.Result)
}

/**
 * Runs [block] and replies with its value, turning a [BridgeException] into
 * `result.error(code.name, …)`. Every handler answers through this, so Flutter sees
 * a contract error code (docs/adr/0011), never a free-form string.
 */
inline fun MethodChannel.Result.reply(block: () -> Any?) {
    try {
        success(block())
    } catch (e: BridgeException) {
        error(e.code.name, e.message, null)
    }
}
