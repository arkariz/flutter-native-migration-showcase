# ADR 0011: One versioned bridge contract, checked from both runtimes

## Status

Accepted

## Context

A platform channel is a stringly-typed API between two codebases in two
languages. The usual way it breaks is a rename on one side: `"user_id"` becomes
`"userId"` in Kotlin, the Dart tests still pass against their own mocks, and the
app fails at runtime. Error handling degrades the same way. Native sends
`result.error("no_session", ...)`, and Dart matches on a message string.

## Decision

`contract/bridge_contract.json` is the **single source of truth** for:

- the contract `version`;
- channel names, and which channel each method belongs to;
- payload keys (`session`, `flowResult`, `openNative`) and the flow-result
  statuses;
- the stable **error codes**: `NO_SESSION`, `INVALID_ARGUMENT`,
  `DECODE_FAILED`, `UNKNOWN_ROUTE` and `NO_ACTIVE_FLOW`.

Each runtime mirrors it in code (`BridgeContract.kt`, `bridge_contract.dart`),
and **each has a test that parses the JSON file and compares**:
`BridgeContractTest` in bridge-core (via Gson) and `bridge_contract_test.dart` in
flutter_module. A rename that doesn't update the JSON fails one of the two CI
jobs. A rename that does update it fails the other side until both match.

Errors are typed end to end. Native throws `BridgeException(BridgeErrorCode)`,
and `:app`'s handlers answer `result.error(code.name, ...)` through one helper.
`ChannelHostPort` maps the code back to a Dart `BridgeErrorCode`. A code it
doesn't know decodes to `unknown` instead of crashing: an older Flutter bundle
may meet a newer native build.

## Consequences

- Drift is a red build, not a production incident.
- The contract file doubles as the documentation of the bridge: one place to
  read every method and payload.
- `version` is compared but not yet negotiated at runtime. Both halves ship in
  one APK here. A setup where they can be updated independently (code push)
  would have Flutter check `version` at startup and refuse to run against an
  incompatible host.
- Decoding quirks are recorded where they're handled. One example is
  `StandardMessageCodec` sending a small Dart `int` as a Kotlin `Int`, so
  `expiresAtEpochMillis` is decoded as any `Number`.
