package com.example.legacyshell.readiness

import com.example.legacyshell.navigation.NativeScreen
import com.example.legacyshell.navigation.StartDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ReadinessPipelineTest {

    private val greeting = ContextKey<String>("greeting")

    @Test
    fun `threads context from gate to gate`() {
        val outcome = ReadinessPipeline(
            listOf(
                readinessGate("writes") { GateResult.Proceed(it.with(greeting, "hello")) },
                readinessGate("reads") { ctx -> GateResult.Proceed(ctx.with(greeting, ctx[greeting] + " world")) },
            ),
        ).run()

        assertIs<ReadinessOutcome.Ready>(outcome)
        assertEquals("hello world", outcome.context[greeting])
    }

    @Test
    fun `stops at the first redirect and names the gate`() {
        var laterGateRan = false
        val login = StartDestination.Native(NativeScreen.LOGIN)

        val outcome = ReadinessPipeline(
            listOf(
                readinessGate("session") { GateResult.Redirect(login) },
                readinessGate("later") { laterGateRan = true; GateResult.Proceed(it) },
            ),
        ).run()

        assertEquals(ReadinessOutcome.Redirected(login, byGate = "session"), outcome)
        assertEquals(false, laterGateRan)
    }

    @Test
    fun `stops at a halt with a typed blocker`() {
        val outcome = ReadinessPipeline(
            listOf(readinessGate("version") { GateResult.Halt(Blocker.UpdateRequired("store://app")) }),
        ).run()

        assertEquals(ReadinessOutcome.Halted(Blocker.UpdateRequired("store://app"), byGate = "version"), outcome)
    }

    @Test
    fun `a throwing gate halts with Internal instead of crashing app start`() {
        val outcome = ReadinessPipeline(
            listOf(readinessGate("flaky") { error("disk on fire") }),
        ).run()

        assertEquals(ReadinessOutcome.Halted(Blocker.Internal("flaky", "disk on fire"), byGate = "flaky"), outcome)
    }

    @Test
    fun `reports progress before each gate and once at the end`() {
        val events = mutableListOf<ReadinessProgress>()

        ReadinessPipeline(
            listOf(readinessGate("a") { GateResult.Proceed(it) }, readinessGate("b") { GateResult.Proceed(it) }),
        ).run(onProgress = events::add)

        assertEquals(
            listOf(ReadinessProgress(0, 2, "a"), ReadinessProgress(1, 2, "b"), ReadinessProgress(2, 2, null)),
            events,
        )
    }

    @Test
    fun `MinimumAppVersionGate halts below the minimum and passes at or above it`() {
        assertEquals(
            GateResult.Halt(Blocker.UpdateRequired("store://app")),
            MinimumAppVersionGate(5, 10, "store://app").check(ReadinessContext.EMPTY),
        )
        assertIs<GateResult.Proceed>(MinimumAppVersionGate(10, 10, "store://app").check(ReadinessContext.EMPTY))
    }
}
