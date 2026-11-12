package com.example.legacyshell.flow

import com.example.legacyshell.bridge.BridgeErrorCode
import com.example.legacyshell.bridge.BridgeException

/**
 * How a Flutter flow ended (docs/adr/0009). A Flutter feature is a bounded sub-flow
 * with an explicit exit — `flow.complete` — not a screen that never hands control back.
 */
sealed interface FlowResult {
    data class Completed(val payload: Map<String, Any?>) : FlowResult
    data object Cancelled : FlowResult
}

/** Decodes the `flowResult` payload in `contract/bridge_contract.json`. */
object FlowResultCodec {
    const val STATUS = "status"
    const val PAYLOAD = "payload"
    const val STATUS_COMPLETED = "completed"
    const val STATUS_CANCELLED = "cancelled"

    val keys: List<String> = listOf(STATUS, PAYLOAD)

    fun decode(raw: Any?): FlowResult {
        val map = raw as? Map<*, *> ?: invalid("flow result is not a map")
        return when (map[STATUS]) {
            STATUS_COMPLETED -> FlowResult.Completed(
                (map[PAYLOAD] as? Map<*, *>)?.entries?.associate { (k, v) -> k.toString() to v } ?: emptyMap(),
            )
            STATUS_CANCELLED -> FlowResult.Cancelled
            else -> invalid("\"$STATUS\" must be \"$STATUS_COMPLETED\" or \"$STATUS_CANCELLED\"")
        }
    }

    private fun invalid(message: String): Nothing = throw BridgeException(BridgeErrorCode.INVALID_ARGUMENT, message)
}
