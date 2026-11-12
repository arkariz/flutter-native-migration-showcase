package com.example.legacyshell.bridge

import com.example.legacyshell.flow.FlowResultCodec
import com.example.legacyshell.flow.NativeRoutes
import com.example.legacyshell.session.UserSessionCodec
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Fails if the native side drifts from contract/bridge_contract.json (docs/adr/0011).
 * flutter_module/test/bridge_contract_test.dart asserts the Dart side against the same
 * file, so the two runtimes can't disagree without one of these going red.
 */
class BridgeContractTest {

    private val contract: JsonObject =
        JsonParser.parseString(File(System.getProperty("bridgeContractPath")).readText()).asJsonObject

    @Test
    fun version() {
        assertEquals(contract["version"].asInt, BridgeContract.VERSION)
    }

    @Test
    fun channels() {
        val channels = contract["channels"].asJsonObject
        assertEquals(channels["session"].asString, BridgeContract.Channels.SESSION)
        assertEquals(channels["deeplink"].asString, BridgeContract.Channels.DEEPLINK)
        assertEquals(channels["flow"].asString, BridgeContract.Channels.FLOW)
    }

    @Test
    fun `every method lives on the channel the contract says`() {
        val channels = contract["channels"].asJsonObject
        val expected = contract["methods"].asJsonObject.entrySet()
            .associate { (method, channelKey) -> method to channels[channelKey.asString].asString }

        assertEquals(expected, BridgeContract.methodChannels)
    }

    @Test
    fun payloads() {
        val payloads = contract["payloads"].asJsonObject
        assertEquals(payloads.stringList("session"), UserSessionCodec.keys)
        assertEquals(payloads.stringList("flowResult"), FlowResultCodec.keys)
        assertEquals(payloads.stringList("openNative"), NativeRoutes.keys)
        assertEquals(
            contract["flowResultStatuses"].asJsonArray.map { it.asString },
            listOf(FlowResultCodec.STATUS_COMPLETED, FlowResultCodec.STATUS_CANCELLED),
        )
    }

    @Test
    fun `error codes`() {
        assertEquals(contract["errorCodes"].asJsonArray.map { it.asString }, BridgeErrorCode.entries.map { it.name })
    }

    private fun JsonObject.stringList(key: String) = this[key].asJsonArray.map { it.asString }
}
