package com.example.legacyshell.session

/**
 * Eager, one-shot backfill from the legacy schema into the new one (docs/adr/0007).
 * [MirroredSessionStore]'s migrate-on-read already covers correctness; this exists
 * so backfill coverage can be measured *before* flipping to
 * [SessionMigrationPhase.MIGRATE], instead of discovering gaps after.
 *
 * Idempotent (safe to run on every launch) and non-destructive: it never
 * overwrites a session already in the new store — that one may be newer — and
 * never clears legacy.
 */
class SessionSchemaMigrator(
    private val legacy: SessionStore,
    private val new: SessionStore,
) {
    fun migrate(): SessionMigrationResult {
        if (new.read() != null) return SessionMigrationResult.AlreadyPresent
        val session = legacy.read() ?: return SessionMigrationResult.NothingToMigrate
        new.write(session)
        return SessionMigrationResult.Copied
    }
}

enum class SessionMigrationResult { Copied, AlreadyPresent, NothingToMigrate }
