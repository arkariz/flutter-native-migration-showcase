# flutter-native-migration-showcase

A reference implementation of a gradual, screen-by-screen native→Flutter
migration — the [Strangler Fig pattern](https://martinfowler.com/bliki/StranglerFigApplication.html),
applied to a minimal mobile app. Old and new code run side by side; one
screen moves over at a time, behind a feature flag; and there's exactly
**one** explicit, testable crossing point for shared state (a user's session)
instead of ad-hoc platform-channel calls scattered across the codebase.

This repo deliberately doesn't reproduce any company's codebase or naming —
it's written from scratch to demonstrate the *pattern*, generic enough to
apply to any native→Flutter migration. See
[`docs/architecture.md`](docs/architecture.md) for the full design and
[`docs/adr/`](docs/adr/) for the reasoning behind each decision.

## What's here

```
legacy_shell/          Native Android app ("the old codebase")
  bridge-core/            Pure Kotlin/JVM — the tested bridge logic
  app/                    The actual Android app (Activities, real Flutter engine wiring)
flutter_module/         Flutter module ("the new codebase"), added via the
                         standard add-to-app pattern
docs/
  architecture.md         Design + diagrams
  adr/                    3 short ADRs explaining the key decisions
```

## The pattern, in one sentence

`MainActivity` reads one feature flag to decide whether tapping "Profile"
opens the old native `LegacyProfileActivity` or launches a Flutter engine
showing the new `ProfileScreen` — and either way, the two sides agree on
who's logged in through exactly one `MethodChannel`
(`migration_showcase/session`), where **Flutter asks and native answers**,
specifically to avoid the classic add-to-app race where native sends data
before Flutter has finished registering its channel handler.

Full sequence diagram: [`docs/architecture.md`](docs/architecture.md#the-fetch-sequence--why-theres-no-race).

## Running it

```bash
# The tested bridge logic (no Android SDK needed):
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
