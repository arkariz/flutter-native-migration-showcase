import 'package:flutter/services.dart';

import '../bridge/bridge_contract.dart';
import '../flow/flow_result.dart';
import '../session/session_payload.dart';
import 'host_port.dart';

/// [HostPort] over the method channels in `contract/bridge_contract.json`. The only
/// file in the module that touches [MethodChannel] or sees a [PlatformException].
class ChannelHostPort implements HostPort {
  ChannelHostPort({Future<void> Function()? dismiss}) : _dismiss = dismiss ?? SystemNavigator.pop;

  static const MethodChannel _session = MethodChannel(BridgeContract.sessionChannel);
  static const MethodChannel _deeplink = MethodChannel(BridgeContract.deeplinkChannel);
  static const MethodChannel _flow = MethodChannel(BridgeContract.flowChannel);

  /// Closes the hosting FlutterActivity once a flow has reported back.
  final Future<void> Function() _dismiss;

  @override
  Future<String> initialRoute() async {
    final route = await _guard(() => _deeplink.invokeMethod<String>(BridgeContract.deeplinkInitialRoute));
    if (route == null || route.isEmpty) {
      throw const BridgeException(BridgeErrorCode.decodeFailed, 'deeplink.initialRoute returned no route');
    }
    return route;
  }

  @override
  Future<SessionPayload?> fetchSession() async {
    try {
      final raw = await _guard(() => _session.invokeMapMethod<Object?, Object?>(BridgeContract.sessionFetch));
      if (raw == null) {
        throw const BridgeException(BridgeErrorCode.decodeFailed, 'session.fetch returned null');
      }
      return SessionPayload.fromChannel(raw);
    } on BridgeException catch (e) {
      if (e.code == BridgeErrorCode.noSession) return null;
      rethrow;
    }
  }

  @override
  Future<void> commitSession(SessionPayload session) =>
      _guard(() => _session.invokeMethod<void>(BridgeContract.sessionCommit, session.toChannel()));

  @override
  Future<void> completeFlow(FlowResult result) async {
    await _guard(() => _flow.invokeMethod<void>(BridgeContract.flowComplete, result.toChannel()));
    await _dismiss();
  }

  @override
  Future<void> openNative(NativeScreen screen) => _guard(
        () => _flow.invokeMethod<void>(BridgeContract.navigationOpenNative, {NativeScreen.screenKey: screen.name}),
      );

  static Future<T> _guard<T>(Future<T> Function() call) async {
    try {
      return await call();
    } on PlatformException catch (e) {
      throw BridgeException(BridgeErrorCode.fromWire(e.code), e.message ?? e.code);
    }
  }
}
