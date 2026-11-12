import 'package:flutter/services.dart';

import 'session_payload.dart';

/// The Dart side of the single session bridge (see docs/adr/0001). This is
/// the *only* place in the module that touches [MethodChannel] for session
/// data — every other screen reads a [SessionPayload] instead.
class SessionChannel {
  SessionChannel._();

  static const MethodChannel _channel = MethodChannel('migration_showcase/session');
  static const String _fetchMethod = 'session.fetch';

  /// Calls native's `"session.fetch"` and decodes the result into a typed
  /// [SessionPayload]. This is called once, at startup, before `runApp()` —
  /// native's handler is already registered on the engine by then, so there's
  /// no race (see docs/adr/0001).
  static Future<SessionPayload> fetch() async {
    final result = await _channel.invokeMapMethod<Object?, Object?>(_fetchMethod);
    if (result == null) {
      throw const SessionDecodeException('session.fetch returned null');
    }
    return SessionPayload.fromChannelResult(result);
  }
}
