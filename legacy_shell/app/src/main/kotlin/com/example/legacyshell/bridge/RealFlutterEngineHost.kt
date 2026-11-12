package com.example.legacyshell.bridge

import android.content.Context
import android.content.Intent
import com.example.legacyshell.flow.FlowResultBus
import com.example.legacyshell.navigation.NativeScreen
import com.example.legacyshell.navigation.activityClass
import com.example.legacyshell.session.SessionStore
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

/**
 * Real [FlutterEngineHost]. Registers every [ChannelMethodHandler] *before* the Dart
 * entrypoint runs, so Flutter's first calls (`session.fetch`, `deeplink.initialRoute`)
 * always find a listener (docs/adr/0001).
 *
 * Two engine lifetimes:
 * - **Screens** (no flow id) get one cached engine per route, reused across launches.
 * - **Flows** get a fresh engine per launch that is destroyed with its activity, so a
 *   second sign-in never resumes a half-finished first one (docs/adr/0009).
 *
 * Requires the real Flutter engine embedding dependency (see `app/build.gradle.kts`).
 */
class RealFlutterEngineHost(
    private val appContext: Context,
    private val sessionStore: SessionStore,
    private val flowResults: FlowResultBus,
) : FlutterEngineHost {

    override fun launchRoute(context: Context, route: String, flowId: String?) {
        val engineId = if (flowId == null) "screen_$route" else "flow_$flowId"
        val cache = FlutterEngineCache.getInstance()
        if (flowId != null || cache.get(engineId) == null) {
            cache.put(engineId, createEngine(route, flowId, onFlowFinished = { cache.remove(engineId) }))
        }

        context.startActivity(
            FlutterActivity.withCachedEngine(engineId)
                .destroyEngineWithActivity(flowId != null)
                .build(context),
        )
    }

    private fun createEngine(route: String, flowId: String?, onFlowFinished: () -> Unit): FlutterEngine {
        val engine = FlutterEngine(appContext)

        ChannelRegistry(engine).registerAll(
            listOf(
                SessionChannelHandler(SessionBridge(sessionStore)),
                DeepLinkChannelHandler(route),
                FlowChannelHandler(flowId, flowResults, ::openNative, onFlowFinished),
            ),
        )

        engine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
        return engine
    }

    private fun openNative(screen: NativeScreen) {
        appContext.startActivity(
            Intent(appContext, screen.activityClass()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
