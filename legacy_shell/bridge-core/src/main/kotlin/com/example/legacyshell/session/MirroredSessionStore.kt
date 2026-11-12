package com.example.legacyshell.session

/**
 * The only [SessionStore] the rest of the app uses during a storage migration
 * (docs/adr/0007). It hides which schema is canonical behind [phase]:
 *
 * - **read**: canonical first; if canonical is empty but the other side has a
 *   session (a user who upgraded across the phase flip), copy it forward
 *   (migrate-on-read) and return it.
 * - **write**: canonical first, then legacy too while [SessionMigrationPhase.writesLegacy].
 * - **clear**: always both sides, in every phase. Clearing is revocation, not a
 *   write — if it skipped legacy in [SessionMigrationPhase.CONTRACT],
 *   migrate-on-read would resurrect a logged-out session on the next read.
 */
class MirroredSessionStore(
    private val legacy: SessionStore,
    private val new: SessionStore,
    private val phase: SessionMigrationPhase,
) : SessionStore {

    override fun read(): UserSession? {
        val canonical = storeFor(phase.canonical)
        canonical.read()?.let { return it }

        val other = storeFor(other(phase.canonical))
        val carriedOver = other.read() ?: return null
        canonical.write(carriedOver)
        return carriedOver
    }

    override fun write(session: UserSession) {
        storeFor(phase.canonical).write(session)
        when (phase.canonical) {
            StoreSide.LEGACY -> new.write(session)
            StoreSide.NEW -> if (phase.writesLegacy) legacy.write(session)
        }
    }

    override fun clear() {
        legacy.clear()
        new.clear()
    }

    private fun storeFor(side: StoreSide): SessionStore = when (side) {
        StoreSide.LEGACY -> legacy
        StoreSide.NEW -> new
    }

    private fun other(side: StoreSide): StoreSide = when (side) {
        StoreSide.LEGACY -> StoreSide.NEW
        StoreSide.NEW -> StoreSide.LEGACY
    }
}
