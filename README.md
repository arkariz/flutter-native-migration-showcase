# flutter-native-migration-showcase

A reference implementation of a gradual, screen-by-screen native→Flutter
migration — the [Strangler Fig pattern](https://martinfowler.com/bliki/StranglerFigApplication.html),
applied to a minimal mobile app. Old and new code run side by side; each
screen moves over at its own pace, behind a feature flag; every crossing
point between the two sides is explicit, typed and tested: session data in
both directions, start routing, flow results and navigation back to native.
That replaces ad-hoc platform-channel calls and scattered checks.

This repo deliberately doesn't reproduce any company's codebase or naming —
it's written from scratch to demonstrate the *patterns*, generic enough to
apply to any native→Flutter migration. See
[`docs/architecture.md`](docs/architecture.md) for the full design and
[`docs/adr/`](docs/adr/) for the reasoning behind each decision.

## What's here

```
contract/
  bridge_contract.json    The bridge's single source of truth, tested from both runtimes
legacy_shell/          Native Android app ("the old codebase")
  bridge-core/            Pure Kotlin/JVM: every decision (storage migration, readiness,
                          routing, flows, codecs), tested without Android
  app/                    The Android app: Activities, engines, channel handlers
flutter_module/         Flutter module ("the new codebase"), added via add-to-app
docs/
  architecture.md         Design and sequence diagrams
  adr/                    12 short ADRs, one per decision
  testing-strategy.md     Parity vs revamp, three-box triage, test-to-risk map
  di-decision-tree.md     Where each object gets created, and why there's no container
```

## The patterns, in one sentence each

**Crossing the boundary**
- **Pull, not push**: Flutter asks and native answers, with handlers registered
  before Dart runs, so there's no startup race
  ([ADR-0001](docs/adr/0001-single-session-bridge.md),
  [0006](docs/adr/0006-channel-registry.md)).
- **One versioned contract**: channels, payload keys and stable error codes live
  in one JSON file, mirrored by Kotlin and Dart and checked by a test on each
  side ([ADR-0011](docs/adr/0011-one-versioned-bridge-contract.md)).

**Migrating state**
- **Expand → Migrate → Contract**: the session moves from legacy keys to a v2
  schema in release-gated phases, with migrate-on-read, a rollback window and a
  clear that can't be undone by a later read
  ([ADR-0007](docs/adr/0007-session-storage-expand-migrate-contract.md)).
- **One write-back**: a Flutter sign-in reaches legacy-visible state only
  through `session.commit`
  ([ADR-0008](docs/adr/0008-flutter-writes-back-through-one-commit.md)).

**Lifecycle**
- **Typed readiness**: gates return `Proceed`, `Redirect` or `Halt`, thread a
  typed context, and treat an expired session differently from a missing one
  ([ADR-0012](docs/adr/0012-readiness-gates-return-typed-outcomes.md), amending
  [0004](docs/adr/0004-readiness-pipeline.md)).
- **Flows return results**: screens emit typed outputs, a coordinator talks to
  a `HostPort` (channels, or standalone for running Flutter alone), and native
  gets exactly one `FlowResult` per launch
  ([ADR-0009](docs/adr/0009-flows-return-typed-results.md)).

**Routing**
- **One flag reader**: buttons, deep links and readiness redirects all ask
  `MigrationRoutes`, so they can't disagree
  ([ADR-0002](docs/adr/0002-feature-flag-per-screen-routing.md),
  [0005](docs/adr/0005-deep-links-follow-the-feature-flag.md)).
- **Crossings with a death clause**: Flutter opens native screens only from an
  allowlist marked `@TemporaryCrossing(removeWhen = …)`
  ([ADR-0010](docs/adr/0010-native-navigation-is-an-allowlisted-temporary-crossing.md)).

Diagrams: [`docs/architecture.md`](docs/architecture.md).

## Running it

```bash
# The tested bridge/routing/readiness logic (no Android SDK needed):
cd legacy_shell && ./gradlew :bridge-core:test

# The Flutter module (also checks contract/bridge_contract.json):
cd flutter_module && flutter pub get && flutter analyze && flutter test

# Run the Flutter module on its own, without the native app:
flutter run --dart-define=HOST_MODE=standalone --dart-define=INITIAL_ROUTE=login
```

Building `legacy_shell/app` itself (the real Android app with the Flutter
engine embedded) additionally needs a real Flutter SDK to run
`flutter build aar` and produce the engine artifact `app/build.gradle.kts`
depends on — that step is standard Flutter add-to-app tooling, not something
this repo reproduces. CI (`.github/workflows/ci.yml`) builds and tests
`bridge-core` and `flutter_module` on every push/PR.

## Why this exists

Written as part of a portfolio (see the
[Tanukonomy case study](https://github.com/arkariz/Saldough)'s notification-capture
write-up for the other native-integration piece) to make a migration pattern
*verifiable* — readable code and passing tests — rather than something to
just take on faith from a resume bullet.
