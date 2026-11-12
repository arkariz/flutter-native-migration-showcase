# Testing strategy for a migrated screen

A migrated screen has two implementations in one build. Each kind of test has to
be clear about which question it answers. Otherwise a migration either ships
regressions ("it's a rewrite, of course it's different") or stalls ("it isn't
pixel-identical to the old one").

## Parity or revamp: decide per screen, up front

Before a screen migrates, its ticket says which of these it is:

| | **Parity** migration | **Revamp** migration |
|---|---|---|
| Goal | Same behaviour on a new stack | New behaviour, and the new stack is incidental |
| Oracle | The legacy screen | The new spec or design |
| Behaviour diff vs legacy | A bug, by definition | Expected, and listed in the ticket |
| Rollout | Flag on for everyone once tests match | Flag on gradually, with product metrics compared |

Mixing the two in one change is what makes reviews unanswerable ("is this
difference intended?"). If a parity migration finds a legacy bug worth fixing,
the fix is a separate, named change.

## Triage: the three boxes

Every difference found in QA goes in exactly one box:

1. **Regression**: parity said "same", and it isn't. This blocks rollout of the
   flag.
2. **Intended change**: revamp, and the difference is in the ticket's list. No
   action.
3. **Undecided**: nobody wrote down whether this should change. This goes to
   the product owner, not engineering. It is the box that signals a missing
   parity/revamp decision.

## What's tested where, in this repo

| Risk | Test | Runs without |
|---|---|---|
| Bridge payloads drift between Kotlin and Dart | `BridgeContractTest` and `bridge_contract_test.dart`, both against `contract/bridge_contract.json` ([ADR-0011](adr/0011-one-versioned-bridge-contract.md)) | either other runtime |
| Session lost or resurrected across a schema change | `MirroredSessionStoreTest`, `SessionSchemaMigratorTest` ([ADR-0007](adr/0007-session-storage-expand-migrate-contract.md)) | Android |
| Wrong start screen, string-matched blockers | `ReadinessPipelineTest`, `SessionGateTest` ([ADR-0012](adr/0012-readiness-gates-return-typed-outcomes.md)) | Android |
| Deep link and button disagree about a flag | `DeepLinkRouterTest`, via `MigrationRoutes` ([ADR-0005](adr/0005-deep-links-follow-the-feature-flag.md)) | Android |
| Flow result lost or delivered twice; Flutter opening arbitrary native screens | `FlowTest` ([ADR-0009](adr/0009-flows-return-typed-results.md), [ADR-0010](adr/0010-native-navigation-is-an-allowlisted-temporary-crossing.md)) | Android |
| Channel errors leaking as `PlatformException` | `channel_host_port_test.dart` | the native app |
| Commit/complete ordering | `sign_in_flow_coordinator_test.dart` | any UI |
| The module as a whole | `standalone_app_test.dart`, against `StandaloneHostPort` | the native app |

What is **not** automated here: an on-device run of `:app` with the real
engine. That needs `flutter build aar` (see the README). In a real project it
is the smoke test per flag flip: launch with the flag off, then on, and walk
each entry point (button, deep link, readiness redirect).
