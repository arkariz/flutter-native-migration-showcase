package com.example.legacyshell.readiness

import com.example.legacyshell.session.InMemorySessionStore
import com.example.legacyshell.session.LegacySession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ReadinessPipelineTest {

    @Test
    fun `run returns Ready when every gate passes`() {
        val pipeline = ReadinessPipeline(listOf(ReadinessGate { ReadinessResult.Pass }, ReadinessGate { ReadinessResult.Pass }))

        assertEquals(ReadinessOutcome.Ready, pipeline.run())
    }

    @Test
    fun `run stops at the first blocking gate`() {
        val secondGateCalled = BooleanArray(1)
        val gates = listOf(
            ReadinessGate { ReadinessResult.Block("first gate blocked") },
            ReadinessGate {
                secondGateCalled[0] = true
                ReadinessResult.Pass
            },
        )

        val outcome = ReadinessPipeline(gates).run()

        assertIs<ReadinessOutcome.Blocked>(outcome)
        assertEquals("first gate blocked", outcome.reason)
        assertEquals(false, secondGateCalled[0])
    }

    @Test
    fun `SessionPresentGate blocks when there is no session`() {
        val gate = SessionPresentGate(InMemorySessionStore(initial = null))

        assertEquals(ReadinessResult.Block("No session"), gate.check())
    }

    @Test
    fun `SessionPresentGate passes when a session exists`() {
        val gate = SessionPresentGate(
            InMemorySessionStore(LegacySession(userId = "u1", displayName = "Ada", authToken = "tok")),
        )

        assertEquals(ReadinessResult.Pass, gate.check())
    }

    @Test
    fun `MinimumAppVersionGate blocks below the minimum`() {
        val gate = MinimumAppVersionGate(currentVersionCode = 5, minimumVersionCode = 10)

        assertEquals(
            ReadinessResult.Block("App version 5 is below minimum 10"),
            gate.check(),
        )
    }

    @Test
    fun `MinimumAppVersionGate passes at or above the minimum`() {
        assertEquals(ReadinessResult.Pass, MinimumAppVersionGate(10, 10).check())
        assertEquals(ReadinessResult.Pass, MinimumAppVersionGate(11, 10).check())
    }
}
