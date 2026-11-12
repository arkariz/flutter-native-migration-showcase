# ADR 0007: Session storage migrates in three phases — Expand, Migrate, Contract

## Status

Accepted

## Context

The new code wants a different storage schema for the session: one versioned
document (`session_v2`) instead of flat legacy keys (`legacy_session`). Changing
schemas in place is unsafe for a mobile app, for three reasons:

1. **Old builds stay installed.** Users roll back (staged rollout halted, a
   reinstalled APK) and old builds only read the legacy keys.
2. **The upgrade happens mid-session.** A user signed in on the old build must
   still be signed in after updating, without a "please sign in again".
3. **Sign-out must stick.** If one schema is cleared and the other isn't, a
   "copy forward if missing" read resurrects the session the user just ended.

## Decision

`MirroredSessionStore(legacy, new, phase)` is the only `SessionStore` the app
uses. The `SessionMigrationPhase` is a release decision, not a runtime one:

| Phase | Canonical (read first, written first) | Still writes legacy? | Rollback to an older build |
|---|---|---|---|
| `EXPAND` | legacy | yes (and mirrors to new) | safe |
| `MIGRATE` | new | yes | safe |
| `CONTRACT` | new | **no** | not safe; ship only after rollback risk is gone |

- **Read**: canonical first. If it's empty, take the other side and copy it into
  canonical (*migrate-on-read*). This covers a session written just before the
  phase flipped.
- **Write**: canonical, then the other side while the phase still writes it.
- **Clear**: always **both** sides, in every phase. This is the sign-out invariant.

`SessionSchemaMigrator` backfills legacy → new once at startup (the first
readiness gate, [ADR-0012](0012-readiness-gates-return-typed-outcomes.md)). It is
**idempotent** (it reports `AlreadyPresent` on later runs) and
**non-destructive**: it never deletes legacy and never overwrites a new-schema
session, which may be newer than legacy.

## Consequences

- Each phase is one constant in `LegacyShellApplication`. Moving to the next
  phase is a reviewed one-line change that ships with a release, not a code
  path that branches on remote state.
- `MirroredSessionStoreTest` pins the dangerous cases: migrate-on-read, CONTRACT
  not writing legacy, and clear-in-every-phase (the resurrection regression).
- Cost: until CONTRACT, every write happens twice. For a session that is
  negligible. For large data, the same phases apply, but the backfill needs
  batching and progress tracking. That isn't modelled here.
- Deleting the legacy file is a fourth step, after CONTRACT has been out long
  enough. It is deliberately not automated.
