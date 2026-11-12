# ADR 0009: Flutter flows return a typed result, and screens never see the host

## Status

Accepted

## Context

Showing a Flutter *screen* is fire-and-forget. A *flow* isn't: sign-in, a
checkout or a form wizard is launched by native code that needs to know how it
ended. Two failure modes are common:

- Flutter screens call `MethodChannel` directly, so every screen is coupled to
  the embedding and can't run or be tested without it.
- Native "finds out" a flow ended by polling storage in `onResume`, which
  can't tell cancelled from completed.

## Decision

**A host port on the Flutter side.** `HostPort` is the module's only view of
whoever hosts it: `initialRoute`, `fetchSession`, `commitSession`,
`completeFlow` and `openNative`. It has two implementations:

- `ChannelHostPort` is the method channels in the contract. It is the only file
  that sees `MethodChannel` or `PlatformException`, and it maps every error to a
  typed `BridgeException` ([ADR-0011](0011-one-versioned-bridge-contract.md)).
- `StandaloneHostPort` is in-memory. `flutter run
  --dart-define=HOST_MODE=standalone` runs the module without the native app,
  and the full-app widget test uses it.

**Screens emit typed outputs; a coordinator decides.** `SignInScreen` emits a
sealed `SignInOutput` (`SignInSubmitted` or `SignInCancelled`) and knows nothing
else. `SignInFlowCoordinator` maps outputs to host calls, owns their ordering
([ADR-0008](0008-flutter-writes-back-through-one-commit.md)), and is unit-tested
against a fake host. System back is a cancellation and is reported like any
other result.

**One result per launch, on the native side.** The caller registers on a
`FlowResultBus` under a flow id, then launches with that id.
`flow.complete` delivers a sealed `FlowResult` (`Completed(payload)` or
`Cancelled`). Delivery is **one-shot**: the listener is removed, so a duplicate
completion can't fire the caller twice. If nobody is waiting, the bridge answers
`NO_ACTIVE_FLOW` and Flutter stays put.

**Flows get a fresh engine.** A plain screen reuses one cached engine per route.
A flow gets a new engine per launch, destroyed with its activity, so a second
sign-in never resumes a half-finished first one.

## Consequences

- Flutter UI work doesn't need the Android build: standalone mode plus widget
  tests cover it.
- The native caller's reaction (re-run readiness on `Completed`) is written at
  the launch site, next to the launch. It doesn't live in a lifecycle callback
  elsewhere.
- Simplification: the bus lives in process memory. If the process dies
  mid-flow, the result is lost and the user lands on the launcher, which
  re-runs readiness. That is the correct recovery for sign-in. A flow whose
  result must survive process death would persist its outcome instead.
