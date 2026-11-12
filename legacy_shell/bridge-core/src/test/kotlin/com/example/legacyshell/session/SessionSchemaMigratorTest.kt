package com.example.legacyshell.session

import com.example.legacyshell.session
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionSchemaMigratorTest {

    private val legacy = InMemorySessionStore()
    private val new = InMemorySessionStore()
    private val migrator = SessionSchemaMigrator(legacy, new)

    @Test
    fun `copies a legacy session into an empty new store`() {
        legacy.write(session("a"))

        assertEquals(SessionMigrationResult.Copied, migrator.migrate())
        assertEquals(session("a"), new.read())
    }

    @Test
    fun `is idempotent`() {
        legacy.write(session("a"))
        migrator.migrate()

        assertEquals(SessionMigrationResult.AlreadyPresent, migrator.migrate())
        assertEquals(session("a"), new.read())
    }

    @Test
    fun `never overwrites the new store, which may hold a newer session`() {
        legacy.write(session("older"))
        new.write(session("newer"))

        assertEquals(SessionMigrationResult.AlreadyPresent, migrator.migrate())
        assertEquals(session("newer"), new.read())
    }

    @Test
    fun `is non-destructive to legacy`() {
        legacy.write(session("a"))
        migrator.migrate()

        assertEquals(session("a"), legacy.read())
    }

    @Test
    fun `reports when there is nothing to migrate`() {
        assertEquals(SessionMigrationResult.NothingToMigrate, migrator.migrate())
    }
}
