# ADR 0003: The bridge is unit-tested in isolation, not through widget tests

## Status

Accepted

## Context

`SessionChannel` (Flutter side) decodes whatever arguments `SessionBridge` (native side)
sent across the platform channel into a typed `SessionPayload`. That boundary is exactly
where a migration like this actually breaks in practice: a field gets renamed on one side
and not the other, a value is unexpectedly null, an int comes across as a different numeric
type than expected. None of that is a Flutter *rendering* problem — it's a data-contract
problem — so it shouldn't be proven by driving a widget tree.

## Decision

`SessionChannel`'s decoding logic is covered by plain Dart unit tests
(`test/session_channel_test.dart`) that call it directly with hand-built argument maps —
valid, missing required fields, and malformed types — and assert on the resulting
`SessionPayload` or thrown error. No `WidgetTester`, no pumped widget tree, no native code
involved at all: these tests only need the Dart VM.

Widget tests still exist separately for `ProfileScreen` itself, but they're given an
already-decoded `SessionPayload` directly — they don't also have to exercise the channel
decoding to prove the screen renders correctly. The two concerns are tested at the layer
where they actually live.

## Consequences

- **Fast, deterministic tests for the riskiest part.** Unit tests for the bridge run in
  milliseconds and don't flake the way widget tests touching platform channels sometimes
  do.
- **Failures point at the right place.** A broken session contract shows up as a failing
  bridge unit test with a clear "expected X, decoded Y" message, not as a failing widget
  test whose stack trace is three layers removed from the actual bug.
- **This does not replace an end-to-end check.** Nothing here proves the *native* side
  still sends what the Dart side expects — that would need an integration test launching
  the real `legacy_shell` app, which is out of scope for this reference repo. A real
  migration should still have that layer; it's a known gap here, not a claim that unit
  tests alone are sufficient.
