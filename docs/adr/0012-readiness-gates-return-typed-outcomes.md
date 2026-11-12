# ADR 0012: Readiness gates return Proceed, Redirect or Halt, and thread a typed context

## Status

Accepted. Amends [ADR-0004](0004-readiness-pipeline.md).

## Context

ADR-0004's gates returned `Pass` or `Block(reason: String)`. Two problems
appeared as soon as a second kind of "not ready" existed:

- **Callers branched on strings.** `MainActivity` did
  `if (reason == "No session")` to tell "go and sign in" from "this version is
  dead". Renaming a message broke routing.
- **Gates repeated work.** The session gate read the store, and then
  `MainActivity` read it again with `!!` to get the name it had just checked.

Session presence was also a boolean, though a session has three states that
need different handling.

## Decision

```kotlin
sealed interface GateResult {
    data class Proceed(val context: ReadinessContext) : GateResult
    data class Redirect(val destination: StartDestination) : GateResult // normal: go somewhere else
    data class Halt(val blocker: Blocker) : GateResult                  // abnormal: can't continue
}
```

- **Redirect is not an error.** "No session → sign-in" is a `Redirect` to
  `MigrationRoutes.signIn()`, so the migration flag also decides *which* sign-in
  (native or Flutter, [ADR-0002](0002-feature-flag-per-screen-routing.md)).
- **Halt carries a typed `Blocker`**: `UpdateRequired(storeUrl)` or
  `Internal(gate, message)`. The caller uses an exhaustive `when` and never
  matches on text.
- **A gate that throws** becomes `Halt(Internal)` with the gate's name. A bug in
  one check shows a recoverable message instead of crashing app start.
- **Typed context.** Gates add values under `ContextKey<T>`s
  (`ReadinessKeys.SESSION`, `SESSION_MIGRATION`), and later gates and the caller
  read them back typed. `MainActivity` gets the session from the outcome, with
  no second read.
- **`SessionState` is `Active`, `Expired` or `Absent`.** `Expired` gets one
  refresh attempt. If that fails, the gate clears the session (both schemas,
  [ADR-0007](0007-session-storage-expand-migrate-contract.md)) and redirects.
- **Progress callback.** `run(onProgress)` reports `(completed, total, currentGate)`
  so a splash screen can show progress without knowing the gates.

The app's order is: schema migration (backfill first, so later gates read a
migrated store), then minimum version, then session.

## Consequences

- No string comparison anywhere in startup routing. Adding a blocker type is a
  compile error at every `when` that doesn't handle it.
- Every branch is unit-tested without Android: context threading, short-circuit
  on redirect and halt, exception to `Internal`, progress events, and each
  `SessionState` transition.
- Gates are still synchronous (see ADR-0004). The result and context types don't
  change when `check` becomes `suspend`.
