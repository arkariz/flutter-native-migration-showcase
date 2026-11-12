package com.example.legacyshell.bridge

import android.content.Context
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

/**
 * Real [FlutterEngineHost]: creates and caches a [FlutterEngine], wires
 * [SessionBridge] to it as [SessionBridge.CHANNEL_NAME]'s `MethodCallHandler`
 * *before* the Dart entrypoint runs, then launches [FlutterActivity] against
 * that cached engine. See docs/adr/0001 for why Flutter calls
 * `"session.fetch"` rather than native pushing the session proactively — this
 * ordering (handler registered, then entrypoint executed) is what makes that
 * safe: by the time `main()` runs and `SessionChannel.fetch()` makes its call,
 * this handler is already listening.
 *
 * Requires the real Flutter engine embedding dependency (see
 * `app/build.gradle.kts`) — not pinned in this repo, see README.md.
 */
class RealFlutterEngineHost(private val appContext: Context) : FlutterEngineHost {

    override fun launchProfileScreen(context: Context, sessionBridge: SessionBridge) {
        if (FlutterEngineCache.getInstance().get(ENGINE_ID) == null) {
            createAndCacheEngine(sessionBridge)
        }

        context.startActivity(
            FlutterActivity.withCachedEngine(ENGINE_ID).build(context),
        )
    }

    private fun createAndCacheEngine(sessionBridge: SessionBridge): FlutterEngine {
        val engine = FlutterEngine(appContext)

        MethodChannel(engine.dartExecutor.binaryMessenger, SessionBridge.CHANNEL_NAME)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    SessionBridge.METHOD_FETCH -> try {
                        result.success(sessionBridge.handleFetch())
                    } catch (e: SessionBridge.NoSessionException) {
                        result.error("no_session", e.message, null)
                    }
                    else -> result.notImplemented()
                }
            }

        // The handler above is registered before this call, so it's already
        // listening by the time flutter_module's main() issues "session.fetch".
        engine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())

        FlutterEngineCache.getInstance().put(ENGINE_ID, engine)
        return engine
    }

    companion object {
        private const val ENGINE_ID = "profile_screen_engine"
    }
}
