package com.example.legacyshell.readiness

/** One precondition that must hold before the app is usable (see docs/adr/0004). */
fun interface ReadinessGate {
    fun check(): ReadinessResult
}

/** The result of a single [ReadinessGate] check. */
sealed interface ReadinessResult {
    data object Pass : ReadinessResult
    data class Block(val reason: String) : ReadinessResult
}
