/// Dart-side mirror of `contract/bridge_contract.json`, the single source of truth for
/// every channel, method, payload key and error code crossing the platform boundary
/// (docs/adr/0011). `test/bridge_contract_test.dart` fails if this drifts from the JSON;
/// bridge-core has the same test in Kotlin against the same file.
abstract final class BridgeContract {
  static const int version = 1;

  static const String sessionChannel = 'migration_showcase/session';
  static const String deeplinkChannel = 'migration_showcase/deeplink';
  static const String flowChannel = 'migration_showcase/flow';

  static const String sessionFetch = 'session.fetch';
  static const String sessionCommit = 'session.commit';
  static const String deeplinkInitialRoute = 'deeplink.initialRoute';
  static const String flowComplete = 'flow.complete';
  static const String navigationOpenNative = 'navigation.openNative';

  /// Which channel each method belongs to.
  static const Map<String, String> methodChannels = {
    sessionFetch: sessionChannel,
    sessionCommit: sessionChannel,
    deeplinkInitialRoute: deeplinkChannel,
    flowComplete: flowChannel,
    navigationOpenNative: flowChannel,
  };
}

/// The stable `code` of every `PlatformException` the native bridge raises.
enum BridgeErrorCode {
  noSession('NO_SESSION'),
  invalidArgument('INVALID_ARGUMENT'),
  decodeFailed('DECODE_FAILED'),
  unknownRoute('UNKNOWN_ROUTE'),
  noActiveFlow('NO_ACTIVE_FLOW'),

  /// Not in the contract: what a code from a *newer* native build decodes to, so an
  /// older Flutter bundle degrades to a generic failure instead of crashing.
  unknown('');

  const BridgeErrorCode(this.wireName);

  final String wireName;

  static BridgeErrorCode fromWire(String code) =>
      values.firstWhere((c) => c != unknown && c.wireName == code, orElse: () => unknown);
}

/// A bridge failure with a typed [code]. Screens and coordinators catch this, never
/// `PlatformException` — only `ChannelHostPort` sees the raw channel.
class BridgeException implements Exception {
  const BridgeException(this.code, this.message);

  final BridgeErrorCode code;
  final String message;

  @override
  String toString() => 'BridgeException(${code.name}): $message';
}
