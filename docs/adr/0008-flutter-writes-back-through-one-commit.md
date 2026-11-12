# ADR 0008: Flutter writes legacy-visible state through exactly one method

## Status

Accepted

## Context

[ADR-0001](0001-single-session-bridge.md) made session data flow native → Flutter
through one pull (`session.fetch`). Once a *producing* screen migrates, data
must flow the other way too. The example here is sign-in, behind
`loginScreenOnFlutter`. Without a rule, each migrated screen grows its own way
of poking native state: a channel here, a shared-preferences plugin writing the
legacy file there. Soon nobody can say which code writes the session.

## Decision

`session.commit` is the **only** way new code writes state the legacy side can
see. Specifically:

- Flutter sends the same `session` payload `session.fetch` returns (one shape,
  both directions, defined in the contract, [ADR-0011](0011-one-versioned-bridge-contract.md)).
- Native decodes it with the same `UserSessionCodec` and writes it to the
  `MirroredSessionStore` ([ADR-0007](0007-session-storage-expand-migrate-contract.md)).
  A Flutter sign-in therefore lands in every schema the current phase writes,
  exactly like a native sign-in.
- A malformed payload fails with `INVALID_ARGUMENT`, and **nothing is stored**.
- Flutter never touches native storage directly: no plugin reads or writes the
  legacy prefs file.

The Flutter `SignInFlowCoordinator` commits **before** it completes the flow
([ADR-0009](0009-flows-return-typed-results.md)). Completing the flow makes
native re-run its readiness pipeline, which must already find the session. If
the commit fails, the flow is not completed and the user can retry.

## Consequences

- "Who writes the session?" has two answers: `LoginActivity` and
  `SessionBridge.handleCommit`. Both go through the same store.
- The ordering rule (commit, then complete) is a coordinator unit test, not
  something to rediscover from a bug report.
- It becomes cheap to audit. A new legacy-visible write means a new contract
  method, and a new contract method shows up in review.
