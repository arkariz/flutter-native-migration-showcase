package com.example.legacyshell.readiness

import com.example.legacyshell.navigation.StartDestination

/** One precondition, deciding to continue, go somewhere else, or stop (docs/adr/0012). */
interface ReadinessGate {
    val name: String
    fun check(context: ReadinessContext): GateResult
}

/** Convenience for small gates and tests. */
fun readinessGate(name: String, check: (ReadinessContext) -> GateResult): ReadinessGate =
    object : ReadinessGate {
        override val name = name
        override fun check(context: ReadinessContext) = check(context)
    }

sealed interface GateResult {
    /** Continue to the next gate with [context] (possibly enriched). */
    data class Proceed(val context: ReadinessContext) : GateResult

    /** Stop and send the user to [destination] — native or Flutter. */
    data class Redirect(val destination: StartDestination) : GateResult

    /** Stop; the app can't be used until [blocker] is resolved. */
    data class Halt(val blocker: Blocker) : GateResult
}

/** Why the app can't continue — typed, so callers branch on cases, not on message strings. */
sealed interface Blocker {
    data class UpdateRequired(val storeUrl: String) : Blocker

    /** A gate threw. Never crashes app start; [ReadinessPipeline] converts the exception into this. */
    data class Internal(val gate: String, val message: String) : Blocker
}
