package com.example.legacyshell.readiness

import com.example.legacyshell.session.SessionSchemaMigrator

/** Runs the idempotent backfill first, recording its result for later gates/analytics (docs/adr/0007). Never blocks. */
class SessionSchemaMigrationGate(private val migrator: SessionSchemaMigrator) : ReadinessGate {

    override val name = "sessionSchemaMigration"

    override fun check(context: ReadinessContext): GateResult =
        GateResult.Proceed(context.with(ReadinessKeys.SESSION_MIGRATION, migrator.migrate()))
}
