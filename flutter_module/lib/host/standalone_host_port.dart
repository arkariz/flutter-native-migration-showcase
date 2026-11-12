import '../flow/flow_result.dart';
import '../session/session_payload.dart';
import 'host_port.dart';

/// In-memory [HostPort] for running the module on its own
/// (`flutter run --dart-define=HOST_MODE=standalone --dart-define=INITIAL_ROUTE=login`)
/// and for full-app widget tests. Records what a native host would have received.
class StandaloneHostPort implements HostPort {
  StandaloneHostPort({required this.route, this.session});

  /// A signed-in demo user, starting at [route] (defaults to `--dart-define=INITIAL_ROUTE`).
  factory StandaloneHostPort.demo({String route = const String.fromEnvironment('INITIAL_ROUTE', defaultValue: 'profile')}) =>
      StandaloneHostPort(
        route: route,
        session: SessionPayload(
          userId: 'demo-user',
          displayName: 'Demo User',
          authToken: 'demo-token',
          expiresAt: DateTime.now().add(const Duration(hours: 1)),
        ),
      );

  final String route;

  /// What a native host would currently hold; [commitSession] replaces it.
  SessionPayload? session;

  final List<FlowResult> completedFlows = [];
  final List<NativeScreen> openedNativeScreens = [];

  @override
  Future<String> initialRoute() async => route;

  @override
  Future<SessionPayload?> fetchSession() async => session;

  @override
  Future<void> commitSession(SessionPayload session) async => this.session = session;

  @override
  Future<void> completeFlow(FlowResult result) async => completedFlows.add(result);

  @override
  Future<void> openNative(NativeScreen screen) async => openedNativeScreens.add(screen);
}
