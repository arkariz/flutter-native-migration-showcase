# ADR 0001: One explicit session bridge, not ad-hoc channel calls

## Status

Accepted

## Context

In a gradual native→Flutter migration (Strangler Fig), the native "legacy" side and the
Flutter "new" side need to agree on who the current user is — at minimum a user id, a
display name, and an auth token the Flutter side can use to call the same backend the
native side already calls. Flutter's `MethodChannel` lets any Dart code and any native
code establish an ad-hoc call between each other, from anywhere.

If every Flutter screen reaches for its own `MethodChannel` to ask the native side "who's
logged in right now?", the real contract between the two codebases ends up scattered
across call sites instead of being one place anyone can read. That's expensive to keep
correct as more screens migrate: a change to what "session" means has to be tracked down
across every ad-hoc channel call instead of one.

## Decision

There is exactly **one** crossing point for session data: a single `MethodChannel`
(`"migration_showcase/session"`), and the direction of the call matters as much as its
existence —

- Flutter (`flutter_module`) asks. `SessionChannel.fetch()` is the first thing
  `main()` does, before `runApp()`: it calls `"session.fetch"` on the channel and awaits
  the result. Nothing downstream in the Flutter module talks to the platform channel
  directly — everything reads the resulting typed `SessionPayload` instead.
- Native (`legacy_shell`) answers. `SessionBridge` registers the channel's
  `MethodCallHandler` on the `FlutterEngine` as part of launching it (`FlutterEngineHost`),
  *before* the engine's Dart entrypoint runs — so the handler is already listening by the
  time Flutter's first call arrives. On `"session.fetch"` it reads the current session from
  `LegacySessionStore` (backed by `SharedPreferences` here; a real app's store doesn't
  change this pattern) and returns it as the call's result.

Flutter pulling, rather than native pushing, is deliberate: it sidesteps the classic
add-to-app race where native sends data before Flutter's Dart isolate has finished
registering its channel handler. Because the call only happens once Flutter is already
listening for the *response*, there's no window where a send can arrive with nothing on
the other end.

No other `MethodChannel` for session data exists anywhere else in either codebase.

## Consequences

- **Easy to audit.** Anyone asking "what does Flutter know about the current user, and
  where does that come from?" has exactly one file to read on each side.
- **Easy to evolve.** Adding a field to the session (e.g. a feature entitlement flag) means
  changing `SessionBridge`/`SessionPayload` and nothing else — no hunting for scattered
  channel calls that also need updating.
- **A real cost, accepted deliberately:** every Flutter screen that needs session data gets
  it from the same snapshot, decided at launch time. A screen that needs a session change
  to be reflected live (e.g. the user logs out while that screen is open) needs its own
  explicit mechanism for that — this bridge intentionally doesn't try to be a general
  pub/sub channel, because that's exactly the ad-hoc-calls-everywhere problem this ADR
  avoids.
