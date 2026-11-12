package com.example.legacyshell.readiness

/**
 * Runs a fixed list of [ReadinessGate]s in order, stopping at the first
 * [ReadinessResult.Block] (see docs/adr/0004).
 */
class ReadinessPipeline(private val gates: List<ReadinessGate>) {

    fun run(): ReadinessOutcome {
        for (gate in gates) {
            when (val result = gate.check()) {
                is ReadinessResult.Block -> return ReadinessOutcome.Blocked(gate, result.reason)
                ReadinessResult.Pass -> continue
            }
        }
        return ReadinessOutcome.Ready
    }
}

/** The outcome of running a [ReadinessPipeline]. */
sealed interface ReadinessOutcome {
    data object Ready : ReadinessOutcome
    data class Blocked(val gate: ReadinessGate, val reason: String) : ReadinessOutcome
}
