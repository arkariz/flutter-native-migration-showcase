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
