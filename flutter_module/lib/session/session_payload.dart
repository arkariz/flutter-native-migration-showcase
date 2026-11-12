/// The Flutter module's typed view of the session native hands over via
/// [SessionChannel] (see docs/adr/0001). Decoding happens exactly once, here —
/// nothing downstream in this module reads the raw channel result.
class SessionPayload {
  const SessionPayload({
    required this.userId,
    required this.displayName,
    required this.authToken,
  });

  final String userId;
  final String displayName;
  final String authToken;

  /// Decodes native's `"session.fetch"` result. Throws [SessionDecodeException]
  /// if a required field is missing, empty, or not a [String].
  factory SessionPayload.fromChannelResult(Map<Object?, Object?> raw) {
    final userId = raw['userId'];
    final displayName = raw['displayName'];
    final authToken = raw['authToken'];

    if (userId is! String || userId.isEmpty) {
      throw const SessionDecodeException('Missing or invalid "userId"');
    }
    if (displayName is! String || displayName.isEmpty) {
      throw const SessionDecodeException('Missing or invalid "displayName"');
    }
    if (authToken is! String || authToken.isEmpty) {
      throw const SessionDecodeException('Missing or invalid "authToken"');
    }

    return SessionPayload(
      userId: userId,
      displayName: displayName,
      authToken: authToken,
    );
  }
}

/// Thrown when native's `"session.fetch"` result doesn't match the contract
/// [SessionPayload.fromChannelResult] expects.
class SessionDecodeException implements Exception {
  const SessionDecodeException(this.message);

  final String message;

  @override
  String toString() => 'SessionDecodeException: $message';
}
