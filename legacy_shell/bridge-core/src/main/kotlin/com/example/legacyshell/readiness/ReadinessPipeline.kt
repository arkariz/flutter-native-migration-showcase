package com.example.legacyshell.readiness

import com.example.legacyshell.navigation.StartDestination

/**
 * Runs [gates] in order, threading a [ReadinessContext] through them and stopping at
 * the first [GateResult.Redirect] or [GateResult.Halt] (docs/adr/0012). A gate that
 * throws becomes [Blocker.Internal] instead of crashing app start.
 */
class ReadinessPipeline(private val gates: List<ReadinessGate>) {

    fun run(
        initial: ReadinessContext = ReadinessContext.EMPTY,
        onProgress: (ReadinessProgress) -> Unit = {},
    ): ReadinessOutcome {
        var context = initial
        gates.forEachIndexed { index, gate ->
            onProgress(ReadinessProgress(completed = index, total = gates.size, currentGate = gate.name))
            val result = try {
                gate.check(context)
            } catch (e: Exception) {
                GateResult.Halt(Blocker.Internal(gate.name, e.message ?: e::class.simpleName.orEmpty()))
            }
            when (result) {
                is GateResult.Proceed -> context = result.context
                is GateResult.Redirect -> return ReadinessOutcome.Redirected(result.destination, byGate = gate.name)
                is GateResult.Halt -> return ReadinessOutcome.Halted(result.blocker, byGate = gate.name)
            }
        }
        onProgress(ReadinessProgress(completed = gates.size, total = gates.size, currentGate = null))
        return ReadinessOutcome.Ready(context)
    }
}

sealed interface ReadinessOutcome {
    data class Ready(val context: ReadinessContext) : ReadinessOutcome
    data class Redirected(val destination: StartDestination, val byGate: String) : ReadinessOutcome
    data class Halted(val blocker: Blocker, val byGate: String) : ReadinessOutcome
}

/** Reported before each gate runs and once after the last — enough to drive a splash progress bar. */
data class ReadinessProgress(val completed: Int, val total: Int, val currentGate: String?)
