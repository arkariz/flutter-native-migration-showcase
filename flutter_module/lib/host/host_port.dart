import '../flow/flow_result.dart';
import '../session/session_payload.dart';

/// Everything this module needs from whoever hosts it — the only seam between the
/// Flutter code and the platform (docs/adr/0009). Two implementations:
///
/// - `ChannelHostPort`: embedded in the legacy Android shell, over method channels.
/// - `StandaloneHostPort`: `flutter run --dart-define=HOST_MODE=standalone`, in-memory,
///   so screens can be built and tested without the native app.
///
/// Failures surface as `BridgeException`, never `PlatformException`.
abstract interface class HostPort {
  /// The route native resolved for this engine (`deeplink.initialRoute`).
  Future<String> initialRoute();

  /// The current session, or null when there is none (`session.fetch` / `NO_SESSION`).
  Future<SessionPayload?> fetchSession();

  /// Hands a session created in Flutter to native (`session.commit`, docs/adr/0008).
  Future<void> commitSession(SessionPayload session);

  /// Reports this flow's result to its native caller and leaves Flutter (`flow.complete`).
  Future<void> completeFlow(FlowResult result);

  /// Opens an allowlisted legacy screen (`navigation.openNative`, docs/adr/0010).
  Future<void> openNative(NativeScreen screen);
}

/// Picks the host from `--dart-define=HOST_MODE=...`; embedded is the default.
const String hostMode = String.fromEnvironment('HOST_MODE');
