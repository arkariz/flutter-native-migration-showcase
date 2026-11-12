# ADR 0006: Multiple bridges share one channel registry

## Status

Accepted

## Context

[ADR-0001](0001-single-session-bridge.md) establishes one `MethodChannel` for
session data. Deep links ([ADR-0005](0005-deep-links-follow-the-feature-flag.md))
need a second crossing point — telling Flutter which route to open — and a
real migration accumulates more over time (analytics, remote config, you
name it). Hand-wiring each one directly in `RealFlutterEngineHost`
(`MethodChannel(engine.dartExecutor.binaryMessenger, "...").setMethodCallHandler { ... }`,
repeated per channel) is exactly the "ad-hoc calls scattered across the
codebase" ADR-0001 already rejected for session data — it just rejected it
one channel at a time instead of for all of them.

## Decision

Each crossing point implements a small interface:

```kotlin
interface ChannelMethodHandler {
    val channelName: String
    fun onMethodCall(call: MethodCall, result: MethodChannel.Result)
}
```

`ChannelRegistry` registers a list of them onto a `FlutterEngine` in one
place:

```kotlin
class ChannelRegistry(private val engine: FlutterEngine) {
    fun registerAll(handlers: List<ChannelMethodHandler>)
}
```

`RealFlutterEngineHost` builds the list (`SessionChannelHandler`,
`DeepLinkChannelHandler`) and hands it to `ChannelRegistry` once, instead of
repeating the registration boilerplate per channel. The underlying
`SessionBridge` and `DeepLinkRouter` stay in `bridge-core`, framework-agnostic
and unit-tested on their own — `SessionChannelHandler`/`DeepLinkChannelHandler`
in `:app` are thin adapters translating their plain Kotlin results into
`MethodChannel.Result` calls, nothing more.

## Consequences

- **Adding a crossing point is additive**: a new `ChannelMethodHandler`
  implementation plus one entry in the list `ChannelRegistry` is given —
  `RealFlutterEngineHost`'s engine-creation code doesn't grow per channel.
- **The registration mechanics (MethodChannel construction, handler wiring)
  are tested once**, in `ChannelRegistry`'s own tests, not re-proven for
  every new bridge.
- **The actual decision logic per channel stays exactly where ADR-0001 and
  ADR-0005 put it** — in `bridge-core`, testable without any of this
  registration machinery. `ChannelRegistry` only decides *how* handlers are
  wired to an engine, never *what* they answer.
