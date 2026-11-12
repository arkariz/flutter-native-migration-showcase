package com.example.legacyshell.session

/** Which storage schema a store belongs to. */
enum class StoreSide { LEGACY, NEW }

/**
 * Expand → migrate → contract, per docs/adr/0007. The canonical side is the one
 * whose writer is still the writer of record — not "whichever is newer".
 */
enum class SessionMigrationPhase(
    val canonical: StoreSide,
    val writesLegacy: Boolean,
) {
    /** Legacy is canonical; the new schema is a shadow copy kept in sync. */
    EXPAND(canonical = StoreSide.LEGACY, writesLegacy = true),

    /** New is canonical; legacy is still written so rolling back to an older build keeps working. */
    MIGRATE(canonical = StoreSide.NEW, writesLegacy = true),

    /** Rollback window closed: legacy is no longer written. It is never deleted by this code. */
    CONTRACT(canonical = StoreSide.NEW, writesLegacy = false),
}
