import 'package:flutter_module/bridge/bridge_contract.dart';
import 'package:flutter_module/flow/flow_result.dart';
import 'package:flutter_module/host/host_port.dart';
import 'package:flutter_module/session/session_payload.dart';
import 'package:flutter_module/sign_in/sign_in_flow_coordinator.dart';
import 'package:flutter_module/sign_in/sign_in_output.dart';
import 'package:flutter_test/flutter_test.dart';

/// Records host calls in order; can be told to fail the commit.
class _RecordingHost implements HostPort {
  final List<String> log = [];
  SessionPayload? committed;
  FlowResult? completed;
  bool failCommit = false;

  @override
  Future<void> commitSession(SessionPayload session) async {
    log.add('commit');
    if (failCommit) throw const BridgeException(BridgeErrorCode.invalidArgument, 'rejected');
    committed = session;
  }

  @override
  Future<void> completeFlow(FlowResult result) async {
    log.add('complete');
    completed = result;
  }

  @override
  Future<String> initialRoute() async => 'login';

  @override
  Future<SessionPayload?> fetchSession() async => null;

  @override
  Future<void> openNative(NativeScreen screen) async {}
}

void main() {
  late _RecordingHost host;
  late SignInFlowCoordinator coordinator;
  final now = DateTime.utc(2026, 1, 1, 12);

  setUp(() {
    host = _RecordingHost();
    coordinator = SignInFlowCoordinator(host, now: () => now, newId: () => 'id-1');
  });

  test('a submitted sign-in commits the session before completing the flow', () async {
    await coordinator.handle(const SignInSubmitted('ada'));

    expect(host.log, ['commit', 'complete']);
    expect(host.committed?.displayName, 'ada');
    expect(host.committed?.expiresAt, now.add(SignInFlowCoordinator.sessionLifetime));
    expect(host.completed, isA<FlowCompleted>().having((r) => r.payload, 'payload', {'userId': 'id-1'}));
  });

  test('a rejected commit never completes the flow, so native never sees a half sign-in', () async {
    host.failCommit = true;

    await expectLater(coordinator.handle(const SignInSubmitted('ada')), throwsA(isA<BridgeException>()));
    expect(host.log, ['commit']);
  });

  test('cancelling reports a cancelled flow and commits nothing', () async {
    await coordinator.handle(const SignInCancelled());

    expect(host.log, ['complete']);
    expect(host.completed, isA<FlowCancelled>());
  });
}
