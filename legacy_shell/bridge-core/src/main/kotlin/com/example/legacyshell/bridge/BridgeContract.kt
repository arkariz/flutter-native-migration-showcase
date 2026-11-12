package com.example.legacyshell.bridge

/**
 * Native-side mirror of `contract/bridge_contract.json` — the single source of truth
 * for every channel, method, payload key and error code crossing the platform
 * boundary (docs/adr/0011). `BridgeContractTest` fails if this drifts from the
 * JSON; flutter_module has the same test against the same file.
 */
object BridgeContract {
    const val VERSION = 1

    object Channels {
        const val SESSION = "migration_showcase/session"
        const val DEEPLINK = "migration_showcase/deeplink"
        const val FLOW = "migration_showcase/flow"
    }

    object Methods {
        const val SESSION_FETCH = "session.fetch"
        const val SESSION_COMMIT = "session.commit"
        const val DEEPLINK_INITIAL_ROUTE = "deeplink.initialRoute"
        const val FLOW_COMPLETE = "flow.complete"
        const val NAVIGATION_OPEN_NATIVE = "navigation.openNative"
    }

    /** Which channel each method belongs to. */
    val methodChannels: Map<String, String> = mapOf(
        Methods.SESSION_FETCH to Channels.SESSION,
        Methods.SESSION_COMMIT to Channels.SESSION,
        Methods.DEEPLINK_INITIAL_ROUTE to Channels.DEEPLINK,
        Methods.FLOW_COMPLETE to Channels.FLOW,
        Methods.NAVIGATION_OPEN_NATIVE to Channels.FLOW,
    )
}

/** Stable, machine-readable failure codes — the `code` of every `PlatformException` this bridge raises. */
enum class BridgeErrorCode {
    NO_SESSION,
    INVALID_ARGUMENT,
    DECODE_FAILED,
    UNKNOWN_ROUTE,
    NO_ACTIVE_FLOW,
}

/** A bridge failure carrying a [BridgeErrorCode]; `:app`'s handlers turn it into `result.error(code.name, ...)`. */
class BridgeException(val code: BridgeErrorCode, message: String) : RuntimeException(message)
