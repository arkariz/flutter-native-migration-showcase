package com.example.legacyshell.session

import com.example.legacyshell.session
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MirroredSessionStoreTest {

    private val legacy = InMemorySessionStore()
    private val new = InMemorySessionStore()

    private fun store(phase: SessionMigrationPhase) = MirroredSessionStore(legacy, new, phase)

    @Test
    fun `EXPAND writes both schemas and reads legacy first`() {
        store(SessionMigrationPhase.EXPAND).write(session("a"))

        assertEquals(session("a"), legacy.read())
        assertEquals(session("a"), new.read())

        legacy.write(session("legacy-wins"))
        assertEquals(session("legacy-wins"), store(SessionMigrationPhase.EXPAND).read())
    }

    @Test
    fun `MIGRATE reads new first but still writes legacy, so rolling back to an older build keeps working`() {
        store(SessionMigrationPhase.MIGRATE).write(session("a"))

        assertEquals(session("a"), legacy.read())
        new.write(session("new-wins"))
        assertEquals(session("new-wins"), store(SessionMigrationPhase.MIGRATE).read())
    }

    @Test
    fun `CONTRACT stops writing legacy`() {
        legacy.write(session("old"))

        store(SessionMigrationPhase.CONTRACT).write(session("fresh"))

        assertEquals(session("fresh"), new.read())
        assertEquals(session("old"), legacy.read())
    }

    @Test
    fun `migrate-on-read copies a session forward when the canonical side is empty`() {
        legacy.write(session("upgraded-across-the-flip"))

        val read = store(SessionMigrationPhase.MIGRATE).read()

        assertEquals(session("upgraded-across-the-flip"), read)
        assertEquals(session("upgraded-across-the-flip"), new.read())
    }

    @Test
    fun `read returns null when neither side has a session`() {
        assertNull(store(SessionMigrationPhase.MIGRATE).read())
    }

    @Test
    fun `clear revokes both sides in every phase, so migrate-on-read can't resurrect a signed-out session`() {
        for (phase in SessionMigrationPhase.entries) {
            legacy.write(session("a"))
            new.write(session("a"))

            store(phase).clear()

            assertNull(store(phase).read(), "phase $phase")
        }
    }
}
