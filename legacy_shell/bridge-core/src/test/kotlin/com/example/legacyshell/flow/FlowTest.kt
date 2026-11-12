package com.example.legacyshell.flow

import com.example.legacyshell.bridge.BridgeErrorCode
import com.example.legacyshell.bridge.BridgeException
import com.example.legacyshell.navigation.NativeScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FlowTest {

    @Test
    fun `FlowResultCodec decodes completed and cancelled`() {
        assertEquals(
            FlowResult.Completed(mapOf("signedIn" to true)),
            FlowResultCodec.decode(mapOf("status" to "completed", "payload" to mapOf("signedIn" to true))),
        )
        assertEquals(FlowResult.Completed(emptyMap()), FlowResultCodec.decode(mapOf("status" to "completed")))
        assertEquals(FlowResult.Cancelled, FlowResultCodec.decode(mapOf("status" to "cancelled")))
    }

    @Test
    fun `FlowResultCodec rejects an unknown status`() {
        val error = assertFailsWith<BridgeException> { FlowResultCodec.decode(mapOf("status" to "done")) }
        assertEquals(BridgeErrorCode.INVALID_ARGUMENT, error.code)
    }

    @Test
    fun `FlowResultBus delivers once to the waiting caller`() {
        val bus = FlowResultBus()
        val received = mutableListOf<FlowResult>()
        bus.register("profile", received::add)

        assertTrue(bus.deliver("profile", FlowResult.Cancelled))
        assertEquals(false, bus.deliver("profile", FlowResult.Cancelled))
        assertEquals(listOf<FlowResult>(FlowResult.Cancelled), received)
    }

    @Test
    fun `FlowResultBus reports when nobody is waiting`() {
        assertEquals(false, FlowResultBus().deliver("nobody", FlowResult.Cancelled))
    }

    @Test
    fun `NativeRoutes only opens allowlisted screens`() {
        assertEquals(NativeScreen.HELP, NativeRoutes.resolve(mapOf("screen" to "help")))

        val notAllowed = assertFailsWith<BridgeException> { NativeRoutes.resolve(mapOf("screen" to "LOGIN")) }
        assertEquals(BridgeErrorCode.UNKNOWN_ROUTE, notAllowed.code)

        val unknown = assertFailsWith<BridgeException> { NativeRoutes.resolve(mapOf("screen" to "nope")) }
        assertEquals(BridgeErrorCode.UNKNOWN_ROUTE, unknown.code)

        val missing = assertFailsWith<BridgeException> { NativeRoutes.resolve(emptyMap<String, Any>()) }
        assertEquals(BridgeErrorCode.INVALID_ARGUMENT, missing.code)
    }

    @Test
    fun `NativeRoutes carries a non-blank death clause`() {
        val crossing = NativeRoutes::class.java.getAnnotation(TemporaryCrossing::class.java)

        assertTrue(crossing != null && crossing.removeWhen.isNotBlank())
    }
}
