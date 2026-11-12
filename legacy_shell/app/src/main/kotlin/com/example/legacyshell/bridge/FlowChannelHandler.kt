package com.example.legacyshell.bridge

import com.example.legacyshell.flow.FlowResultBus
import com.example.legacyshell.flow.FlowResultCodec
import com.example.legacyshell.flow.NativeRoutes
import com.example.legacyshell.navigation.NativeScreen
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * The Flutter → native half of a flow's lifecycle (docs/adr/0009, 0010):
 *
 * - `flow.complete` hands a typed result to whoever launched [flowId] via [results].
 *   An engine launched as a plain screen has no flow id and answers `NO_ACTIVE_FLOW`.
 * - `navigation.openNative` opens an allowlisted legacy screen ([NativeRoutes]).
 */
class FlowChannelHandler(
    private val flowId: String?,
    private val results: FlowResultBus,
    private val openNative: (NativeScreen) -> Unit,
    private val onFlowFinished: () -> Unit,
) : ChannelMethodHandler {

    override val channelName: String = BridgeContract.Channels.FLOW

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            BridgeContract.Methods.FLOW_COMPLETE -> result.reply {
                val flowResult = FlowResultCodec.decode(call.arguments)
                if (flowId == null || !results.deliver(flowId, flowResult)) {
                    throw BridgeException(BridgeErrorCode.NO_ACTIVE_FLOW, "Nothing is waiting on this flow")
                }
                onFlowFinished()
                null
            }
            BridgeContract.Methods.NAVIGATION_OPEN_NATIVE -> result.reply {
                openNative(NativeRoutes.resolve(call.arguments))
                null
            }
            else -> result.notImplemented()
        }
    }
}
