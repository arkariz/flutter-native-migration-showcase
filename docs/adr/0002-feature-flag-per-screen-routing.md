# ADR 0002: Feature flag per screen, not a separate branch

## Status

Accepted

## Context

A gradual migration needs a way to decide, per screen, whether the user sees the old
native implementation or the new Flutter one. Two common ways to do that:

1. Build the new screen on a long-lived branch and merge it (and switch users over) in one
   cutover per screen.
2. Ship both implementations in the same build, behind a flag that decides which one a
   given user/build sees.

Option 1 means every in-progress screen lives on a branch that drifts further from `main`
the longer it takes, and "is this screen done" becomes a merge event instead of something
observable in a running build. It also makes it hard to test the new screen against real
production data/config without shipping it to everyone at once.

## Decision

Each screen being migrated gets a boolean entry in `MigrationFlags` (`legacy_shell`), e.g.
`profileScreen: true` means "launch the Flutter Profile screen"; `false` means "use the
native `LegacyProfileActivity`". The flag is read at the single navigation call site for
that screen — nowhere else branches on it.

In this reference implementation the flag is just a local constant (see
`MigrationFlags.kt`) to keep the example self-contained. In a real app it would be backed
by whatever remote-config/feature-flag system the app already uses — the pattern doesn't
change, only where the boolean comes from.

## Consequences

- **Both implementations ship together**, so a flag flip (even per-user, if backed by
  remote config) is the only thing needed to move traffic — no redeploy, no merge event.
- **The old implementation isn't deleted until the flag has been fully rolled out** and
  stayed flipped for a while — it's dead code behind a flag, not a ghost on an old branch
  nobody's diffing against `main` anymore.
- **Cost accepted deliberately:** the native app temporarily carries two implementations of
  the same screen. That's the whole point of Strangler Fig — the new system grows around
  the edges of the old one instead of replacing it in one risky cutover — but it does mean
  the "old" code has to be actively deleted once a screen's flag is fully rolled out, or it
  becomes permanent clutter. This repo's `docs/architecture.md` calls that out as a known
  follow-up discipline, not something this flag mechanism enforces by itself.
