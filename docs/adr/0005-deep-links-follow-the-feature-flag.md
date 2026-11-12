# ADR 0005: Deep links are resolved by the same per-screen flag as manual navigation

## Status

Accepted

## Context

Once a screen can be either legacy or Flutter depending on a feature flag
(see [ADR-0002](0002-feature-flag-per-screen-routing.md)), anything that can
navigate to that screen needs to respect the same flag — including deep
links, which arrive through a completely different code path (an `Intent`
with `ACTION_VIEW`, not a button tap inside the app). It's easy for a deep
link handler to be written against "the old screen" and never updated when
that screen migrates, because nothing forces the two routing paths to agree.

## Decision

`DeepLinkRouter` is the single place that maps a deep link path to a
destination, and it takes the *same* flag `MainActivity` uses:

```kotlin
class DeepLinkRouter(private val profileScreenOnFlutter: Boolean) {
    fun resolve(path: String): DeepLinkDestination // Legacy, Flutter, or Unresolved
}
```

`DeepLinkActivity` (the `android:name` an incoming `migrationshowcase://open/...`
intent-filter targets) does nothing but parse the path out of the intent,
ask `DeepLinkRouter`, and act on the result — it contains no migration logic
of its own.

Not every Flutter destination needs a legacy counterpart: `"settings"`
resolves to Flutter unconditionally in this reference, because that screen
never existed natively. `DeepLinkDestination` models that directly (`Legacy`
and `Flutter` are independent cases, not two sides of one flag).

## Consequences

- **One flag, one decision.** `MigrationFlags.profileScreenOnFlutter` has
  exactly one reader for manual navigation (`MainActivity`) and exactly one
  reader for deep links (`DeepLinkActivity`, via `DeepLinkRouter`) — both
  constructed the same way, so they can't silently disagree.
- **Testable without an `Intent` or an `Activity`.** `DeepLinkRouter.resolve()`
  is a pure function of a path string and a boolean; see `DeepLinkRouterTest`.
- **A new deep-link-only screen (no legacy equivalent) is one new `when`
  branch**, not a parallel "deep link screens" registry disconnected from
  the flag-based one.
