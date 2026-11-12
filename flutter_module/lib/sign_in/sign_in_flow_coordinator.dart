import '../flow/flow_result.dart';
import '../host/host_port.dart';
import '../session/session_payload.dart';
import 'sign_in_output.dart';

/// Turns [SignInOutput]s into host calls. The ordering is the point:
///
/// 1. `session.commit` first, so the session exists natively (in every schema the
///    current migration phase writes, docs/adr/0007) *before* native reacts;
/// 2. then `flow.complete`, which makes native re-run its readiness pipeline — which
///    must now find the session.
///
/// If the commit fails, the flow is not completed: the error propagates to the screen
/// and the user can retry (docs/adr/0008).
class SignInFlowCoordinator {
  SignInFlowCoordinator(this._host, {DateTime Function()? now, String Function()? newId})
      : _now = now ?? DateTime.now,
        _newId = newId ?? _timestampId;

  static const Duration sessionLifetime = Duration(hours: 1);

  final HostPort _host;
  final DateTime Function() _now;
  final String Function() _newId;

  Future<void> handle(SignInOutput output) async {
    switch (output) {
      case SignInSubmitted(:final username):
        // Dummy sign-in, like the native one: any non-empty username succeeds.
        final session = SessionPayload(
          userId: _newId(),
          displayName: username,
          authToken: 'demo-token-${_newId()}',
          expiresAt: _now().add(sessionLifetime),
        );
        await _host.commitSession(session);
        await _host.completeFlow(FlowCompleted({SessionPayload.userIdKey: session.userId}));
      case SignInCancelled():
        await _host.completeFlow(const FlowCancelled());
    }
  }

  static String _timestampId() => DateTime.now().microsecondsSinceEpoch.toRadixString(36);
}
