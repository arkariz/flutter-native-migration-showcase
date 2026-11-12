# ADR 0004: App readiness is an ordered pipeline of gates, not scattered checks

## Status

Accepted

## Context

`MainActivity` originally decided what to show next with one inline check:
is there a session? As a migration like this grows, more preconditions tend
to show up the same way — is the app version still supported, has a remote
kill-switch tripped, is a required local migration step done — each usually
bolted onto whatever `onCreate()` already does. That scatters "is the app
actually usable right now" logic across call sites instead of keeping it in
one place with a clear order and a clear reason for blocking.

## Decision

Readiness is expressed as an ordered list of `ReadinessGate`s, each a single
precondition:

```kotlin
fun interface ReadinessGate {
    fun check(): ReadinessResult // Pass, or Block(reason)
}
```

`ReadinessPipeline` runs them in order and stops at the first block:

```kotlin
class ReadinessPipeline(private val gates: List<ReadinessGate>) {
    fun run(): ReadinessOutcome // Ready, or Blocked(gate, reason)
}
```

`MainActivity` runs one pipeline (`SessionPresentGate`, `MinimumAppVersionGate`
in this reference) and branches once on the outcome, instead of checking each
precondition inline.

## Consequences

- **Adding a precondition is additive.** A new gate is one new class and one
  new entry in the list passed to `ReadinessPipeline` — no existing gate
  needs to change.
- **Order is explicit and visible** at the single call site that builds the
  gate list, not implied by the order of `if` statements scattered through
  `onCreate()`.
- **Each gate is independently unit-testable** (see `ReadinessPipelineTest`)
  without needing an `Activity` or any Android framework class.
- **Deliberate simplification**: gates here are synchronous. A real app's
  preconditions are often async (a remote config fetch, a kill-switch check)
  — this reference keeps `ReadinessGate.check()` synchronous to stay
  dependency-free (no coroutines library) and easy to unit-test without a
  test dispatcher. A real implementation would likely make `check()` a
  `suspend fun`; the ordering/short-circuit contract doesn't change either
  way.
