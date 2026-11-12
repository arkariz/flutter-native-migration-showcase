# flutter-native-migration-showcase

A reference implementation of a gradual, screen-by-screen native→Flutter
migration — the [Strangler Fig pattern](https://martinfowler.com/bliki/StranglerFigApplication.html),
applied to a minimal mobile app. Old and new code run side by side; each
screen moves over at its own pace, behind a feature flag; every crossing
point between the two sides — session data, deep-link routing, app
readiness — is explicit and testable, instead of ad-hoc platform-channel
calls and scattered checks.

This repo deliberately doesn't reproduce any company's codebase or naming —
it's written from scratch to demonstrate the *patterns*, generic enough to
apply to any native→Flutter migration. See
[`docs/architecture.md`](docs/architecture.md) for the full design and
[`docs/adr/`](docs/adr/) for the reasoning behind each decision.

## What's here

```
legacy_shell/          Native Android app ("the old codebase")
  bridge-core/            Pure Kotlin/JVM — the tested logic (bridges, routing, readiness)
  app/                    The actual Android app (Activities, real Flutter engine wiring)
flutter_module/         Flutter module ("the new codebase"), added via the
                         standard add-to-app pattern
docs/
  architecture.md         Design + diagrams
  adr/                    6 short ADRs explaining the key decisions
```

## The patterns, in one sentence each

- **Feature-flag routing**: `MainActivity` reads one flag to decide whether
  "Profile" opens the old native `LegacyProfileActivity` or launches Flutter
  — see [ADR-0002](docs/adr/0002-feature-flag-per-screen-routing.md).
- **One bridge per crossing point, pull not push**: session data and the
  deep-link target each cross through exactly one `MethodChannel`, where
  **Flutter asks and native answers** — avoiding the classic add-to-app race
  where native sends data before Flutter's handler is registered — see
  [ADR-0001](docs/adr/0001-single-session-bridge.md).
- **Deep links follow the same flag**: a `migrationshowcase://open/profile`
  link is resolved by the *same* `DeepLinkRouter` logic manual navigation
  uses — not a parallel routing mechanism that can drift out of sync — see
  [ADR-0005](docs/adr/0005-deep-links-follow-the-feature-flag.md).
- **Multiple bridges, one registry**: as crossing points multiply, they
  register onto the engine through one `ChannelRegistry` instead of
  hand-wiring a `MethodChannel` per feature — see
  [ADR-0006](docs/adr/0006-channel-registry.md).
- **Readiness is a pipeline, not scattered checks**: whether the app is
  usable right now (session present, minimum version) is one ordered list of
  `ReadinessGate`s, not `if`s bolted onto `onCreate()` — see
  [ADR-0004](docs/adr/0004-readiness-pipeline.md).

Full sequence diagram: [`docs/architecture.md`](docs/architecture.md#the-fetch-sequence--why-theres-no-race).

## Running it

```bash
# The tested bridge/routing/readiness logic (no Android SDK needed):
cd legacy_shell && ./gradlew :bridge-core:test

# The Flutter module:
cd flutter_module && flutter pub get && flutter test
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
