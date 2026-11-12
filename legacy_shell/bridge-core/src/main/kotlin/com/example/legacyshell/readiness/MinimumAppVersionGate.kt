package com.example.legacyshell.readiness

/** Blocks if [currentVersionCode] is below [minimumVersionCode] — a forced-update gate. */
class MinimumAppVersionGate(
    private val currentVersionCode: Int,
    private val minimumVersionCode: Int,
) : ReadinessGate {
    override fun check(): ReadinessResult =
        if (currentVersionCode >= minimumVersionCode) {
            ReadinessResult.Pass
        } else {
            ReadinessResult.Block("App version $currentVersionCode is below minimum $minimumVersionCode")
        }
}
