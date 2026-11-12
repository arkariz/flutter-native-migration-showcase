package com.example.legacyshell.flow

/**
 * Routes a [FlowResult] from the Flutter side back to whichever native caller launched
 * that flow (docs/adr/0009). One-shot per flow: delivering removes the listener, so a
 * duplicate `flow.complete` can't fire the caller's reaction twice.
 */
class FlowResultBus {
    private val listeners = mutableMapOf<String, (FlowResult) -> Unit>()

    @Synchronized
    fun register(flowId: String, listener: (FlowResult) -> Unit) {
        listeners[flowId] = listener
    }

    @Synchronized
    fun unregister(flowId: String) {
        listeners.remove(flowId)
    }

    /** Returns false if no caller is waiting on [flowId] — the bridge reports that as `NO_ACTIVE_FLOW`. */
    fun deliver(flowId: String, result: FlowResult): Boolean {
        val listener = synchronized(this) { listeners.remove(flowId) } ?: return false
        listener(result)
        return true
    }
}
