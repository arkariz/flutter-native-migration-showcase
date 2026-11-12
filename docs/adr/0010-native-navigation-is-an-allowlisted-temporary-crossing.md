# ADR 0010: Flutter → native navigation is an allowlisted, temporary crossing

## Status

Accepted

## Context

Mid-migration, a Flutter screen sometimes needs a screen that hasn't migrated
yet. The example here is Profile → Help. The tempting fix is a generic
`openNative(name)`, which quietly becomes permanent: it accepts any screen, so
nothing ever forces the remaining native screens to migrate, and nothing shows
how much of the app Flutter still depends on.

## Decision

- `navigation.openNative` resolves through `NativeRoutes`, an **explicit
  allowlist** (`ALLOWED = setOf(HELP)`). Anything else, including real native
  screens such as `LOGIN`, fails with `UNKNOWN_ROUTE`.
- `NativeRoutes` carries a **death clause**:
  `@TemporaryCrossing(removeWhen = "...")` states the condition under which the
  crossing must be deleted. A test fails if that clause is blank.
- The Dart side mirrors the allowlist as `enum NativeScreen { help }`. Flutter
  can't even express a request for a screen outside it.

## Consequences

- `ALLOWED` is a live measure of migration debt: each entry is a native screen
  Flutter still leans on. When it's empty, the crossing and the annotation are
  deleted. That is the "Contract" step for navigation.
- Adding an entry is a visible, reviewed change in one file, not a string
  someone passes from Dart.
- `@TemporaryCrossing` is a convention plus a test, not a build-time lint. A
  larger codebase would add a static check listing every annotated crossing in
  CI output.
