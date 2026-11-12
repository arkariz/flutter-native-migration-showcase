import '../bridge/bridge_contract.dart';

/// The module's typed view of the contract's `session` payload. Decoded exactly once,
/// at the host port; nothing downstream reads the raw channel map.
class SessionPayload {
  const SessionPayload({
    required this.userId,
    required this.displayName,
    required this.authToken,
    required this.expiresAt,
  });

  static const String userIdKey = 'userId';
  static const String displayNameKey = 'displayName';
  static const String authTokenKey = 'authToken';
  static const String expiresAtKey = 'expiresAtEpochMillis';

  /// The contract's `payloads.session` keys, in order.
  static const List<String> keys = [userIdKey, displayNameKey, authTokenKey, expiresAtKey];

  final String userId;
  final String displayName;
  final String authToken;
  final DateTime expiresAt;

  /// Throws [BridgeException] with [BridgeErrorCode.decodeFailed] on any contract violation.
  factory SessionPayload.fromChannel(Map<Object?, Object?> raw) {
    String requireString(String key) {
      final value = raw[key];
      if (value is! String || value.isEmpty) {
        throw BridgeException(BridgeErrorCode.decodeFailed, 'Missing or invalid "$key"');
      }
      return value;
    }

    final expiresAt = raw[expiresAtKey];
    if (expiresAt is! int) {
      throw const BridgeException(BridgeErrorCode.decodeFailed, 'Missing or invalid "$expiresAtKey"');
    }

    return SessionPayload(
      userId: requireString(userIdKey),
      displayName: requireString(displayNameKey),
      authToken: requireString(authTokenKey),
      expiresAt: DateTime.fromMillisecondsSinceEpoch(expiresAt, isUtc: true),
    );
  }

  Map<String, Object> toChannel() => {
        userIdKey: userId,
        displayNameKey: displayName,
        authTokenKey: authToken,
        expiresAtKey: expiresAt.millisecondsSinceEpoch,
      };
}
