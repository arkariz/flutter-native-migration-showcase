package com.example.legacyshell.bridge

import android.content.Context
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

/**
 * Real [FlutterEngineHost]: for each distinct [route], creates and caches a
 * [FlutterEngine], registers [ChannelMethodHandler]s onto it via
 * [ChannelRegistry] *before* the Dart entrypoint runs, then launches
 * [FlutterActivity] against that cached engine. See docs/adr/0001 for why
 * Flutter calls rather than native pushing — this ordering (handlers
 * registered, then entrypoint executed) is what makes that safe.
 *
 * Requires the real Flutter engine embedding dependency (see
 * `app/build.gradle.kts`) — not pinned in this repo, see README.md.
 */
class RealFlutterEngineHost(private val appContext: Context) : FlutterEngineHost {

    override fun launchRoute(context: Context, sessionBridge: SessionBridge, route: String) {
        val id = engineId(route)
        if (FlutterEngineCache.getInstance().get(id) == null) {
            createAndCacheEngine(sessionBridge, route)
        }

        context.startActivity(
            FlutterActivity.withCachedEngine(id).build(context),
        )
    }

    private fun createAndCacheEngine(sessionBridge: SessionBridge, route: String): FlutterEngine {
        val engine = FlutterEngine(appContext)

        // The handlers below are registered before this call, so they're
        // already listening by the time flutter_module's main() issues its
        // "session.fetch" / "deeplink.initialRoute" calls.
        ChannelRegistry(engine).registerAll(
            listOf(
                SessionChannelHandler(sessionBridge),
                DeepLinkChannelHandler(route),
            ),
        )

        engine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())

        FlutterEngineCache.getInstance().put(engineId(route), engine)
        return engine
    }

    private fun engineId(route: String) = "engine_$route"
}
