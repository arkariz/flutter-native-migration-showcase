package com.example.legacyshell

import android.app.Application
import com.example.legacyshell.bridge.RealFlutterEngineHost
import com.example.legacyshell.featureflags.MigrationFlags
import com.example.legacyshell.flow.FlowResultBus
import com.example.legacyshell.navigation.MigrationRoutes
import com.example.legacyshell.session.JsonSessionStore
import com.example.legacyshell.session.MirroredSessionStore
import com.example.legacyshell.session.SessionMigrationPhase
import com.example.legacyshell.session.SessionSchemaMigrator
import com.example.legacyshell.session.SharedPreferencesSessionStore
import com.example.legacyshell.session.SessionStore

/**
 * Composition root. Plain constructor wiring, no DI container: the graph is small enough
 * that a container would cost more than it saves (docs/di-decision-tree.md).
 */
class LegacyShellApplication : Application() {

    private val legacyStore by lazy { SharedPreferencesSessionStore(this) }
    private val newStore by lazy { JsonSessionStore(this) }

    /** The one [SessionStore] every screen and handler uses. Phase is a release decision (docs/adr/0007). */
    val sessionStore: SessionStore by lazy { MirroredSessionStore(legacyStore, newStore, PHASE) }

    val sessionMigrator by lazy { SessionSchemaMigrator(legacyStore, newStore) }

    val routes by lazy { MigrationRoutes(MigrationFlags.current) }

    val flowResults = FlowResultBus()

    val flutterEngines by lazy { RealFlutterEngineHost(this, sessionStore, flowResults) }

    private companion object {
        val PHASE = SessionMigrationPhase.MIGRATE
    }
}

val android.content.Context.app: LegacyShellApplication
    get() = applicationContext as LegacyShellApplication
