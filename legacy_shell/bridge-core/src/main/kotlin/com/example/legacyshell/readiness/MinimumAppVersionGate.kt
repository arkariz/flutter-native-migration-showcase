package com.example.legacyshell.readiness

/** Halts with [Blocker.UpdateRequired] below [minimumVersionCode]. Runs before [SessionGate]: a forced update doesn't depend on being signed in. */
class MinimumAppVersionGate(
    private val currentVersionCode: Int,
    private val minimumVersionCode: Int,
    private val storeUrl: String,
) : ReadinessGate {

    override val name = "minimumAppVersion"

    override fun check(context: ReadinessContext): GateResult =
        if (currentVersionCode >= minimumVersionCode) {
            GateResult.Proceed(context)
        } else {
            GateResult.Halt(Blocker.UpdateRequired(storeUrl))
        }
}
