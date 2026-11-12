package com.example.legacyshell.bridge

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/** Registers each [ChannelMethodHandler] onto [engine] as its own [MethodChannel]. See docs/adr/0006. */
class ChannelRegistry(private val engine: FlutterEngine) {

    fun registerAll(handlers: List<ChannelMethodHandler>) {
        for (handler in handlers) {
            MethodChannel(engine.dartExecutor.binaryMessenger, handler.channelName)
                .setMethodCallHandler(handler::onMethodCall)
        }
    }
}
