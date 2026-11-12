package com.example.legacyshell.flow

import com.example.legacyshell.bridge.BridgeErrorCode
import com.example.legacyshell.bridge.BridgeException
import com.example.legacyshell.navigation.NativeScreen

/**
 * Lets Flutter open a *legacy* screen it doesn't have an equivalent for yet
 * (`navigation.openNative`, docs/adr/0010). An explicit allowlist, not "any screen
 * by name": Flutter can only reach what's listed here, and each entry is debt.
 */
@TemporaryCrossing(removeWhen = "Every screen in ALLOWED has a Flutter implementation and its MigrationFlagSet entry is fully rolled out")
object NativeRoutes {
    const val SCREEN = "screen"

    val keys: List<String> = listOf(SCREEN)

    val ALLOWED: Set<NativeScreen> = setOf(NativeScreen.HELP)

    /** Throws [BridgeErrorCode.UNKNOWN_ROUTE] for anything not on the allowlist. */
    fun resolve(raw: Any?): NativeScreen {
        val name = (raw as? Map<*, *>)?.get(SCREEN) as? String
            ?: throw BridgeException(BridgeErrorCode.INVALID_ARGUMENT, "\"$SCREEN\" missing")
        val screen = NativeScreen.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
        if (screen == null || screen !in ALLOWED) {
            throw BridgeException(BridgeErrorCode.UNKNOWN_ROUTE, "\"$name\" is not an openable native screen")
        }
        return screen
    }
}
