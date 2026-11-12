import 'package:flutter/services.dart';

/// The second crossing point (see docs/adr/0005, 0006) — mirrors
/// [SessionChannel]'s pull pattern: Flutter asks `"deeplink.initialRoute"`,
/// native answers with the route it already resolved via `DeepLinkRouter`
/// before this engine was even created.
class InitialRouteChannel {
  InitialRouteChannel._();

  static const MethodChannel _channel = MethodChannel('migration_showcase/deeplink');
  static const String _method = 'deeplink.initialRoute';

  static Future<String> fetch() async {
    final route = await _channel.invokeMethod<String>(_method);
    if (route == null || route.isEmpty) {
      throw const InitialRouteDecodeException('deeplink.initialRoute returned empty');
    }
    return route;
  }
}

/// Thrown when native's `"deeplink.initialRoute"` result is missing or empty.
class InitialRouteDecodeException implements Exception {
  const InitialRouteDecodeException(this.message);

  final String message;

  @override
  String toString() => 'InitialRouteDecodeException: $message';
}
