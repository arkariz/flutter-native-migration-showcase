import 'package:flutter/services.dart';
import 'package:flutter_module/bridge/bridge_contract.dart';
import 'package:flutter_module/flow/flow_result.dart';
import 'package:flutter_module/host/channel_host_port.dart';
import 'package:flutter_module/session/session_payload.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  final messenger = TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;

  final calls = <MethodCall>[];
  var dismissed = 0;
  late ChannelHostPort host;

  void answer(String channel, Future<Object?> Function(MethodCall call) handler) {
    messenger.setMockMethodCallHandler(MethodChannel(channel), (call) {
      calls.add(call);
      return handler(call);
    });
  }

  setUp(() {
    calls.clear();
    dismissed = 0;
    host = ChannelHostPort(dismiss: () async => dismissed++);
  });

  tearDown(() {
    for (final channel in [BridgeContract.sessionChannel, BridgeContract.deeplinkChannel, BridgeContract.flowChannel]) {
      messenger.setMockMethodCallHandler(MethodChannel(channel), null);
    }
  });

  final session = SessionPayload(
    userId: 'u1',
    displayName: 'Ada Lovelace',
    authToken: 'tok-1',
    expiresAt: DateTime.fromMillisecondsSinceEpoch(10000, isUtc: true),
  );

  group('fetchSession', () {
    test('decodes the contract payload', () async {
      answer(BridgeContract.sessionChannel, (_) async => session.toChannel());

      final fetched = await host.fetchSession();

      expect(calls.single.method, BridgeContract.sessionFetch);
      expect(fetched?.displayName, 'Ada Lovelace');
      expect(fetched?.expiresAt, session.expiresAt);
    });

    test('NO_SESSION is an absent session, not a failure', () async {
      answer(BridgeContract.sessionChannel, (_) async => throw PlatformException(code: 'NO_SESSION'));

      expect(await host.fetchSession(), isNull);
    });

    test('a malformed payload is DECODE_FAILED', () async {
      answer(BridgeContract.sessionChannel, (_) async => {'userId': 'u1'});

      await expectLater(
        host.fetchSession(),
        throwsA(isA<BridgeException>().having((e) => e.code, 'code', BridgeErrorCode.decodeFailed)),
      );
    });

    test('a code from a newer native build degrades to unknown', () async {
      answer(BridgeContract.sessionChannel, (_) async => throw PlatformException(code: 'SOMETHING_NEW'));

      await expectLater(
        host.fetchSession(),
        throwsA(isA<BridgeException>().having((e) => e.code, 'code', BridgeErrorCode.unknown)),
      );
    });
  });

  test('initialRoute rejects an empty route', () async {
    answer(BridgeContract.deeplinkChannel, (_) async => '');

    await expectLater(host.initialRoute(), throwsA(isA<BridgeException>()));
  });

  test('commitSession sends the contract payload', () async {
    answer(BridgeContract.sessionChannel, (_) async => null);

    await host.commitSession(session);

    expect(calls.single.method, BridgeContract.sessionCommit);
    expect(calls.single.arguments, session.toChannel());
  });

  group('completeFlow', () {
    test('reports the result, then leaves Flutter', () async {
      answer(BridgeContract.flowChannel, (_) async => null);

      await host.completeFlow(const FlowCompleted({'userId': 'u1'}));

      expect(calls.single.method, BridgeContract.flowComplete);
      expect(calls.single.arguments, {'status': 'completed', 'payload': {'userId': 'u1'}});
      expect(dismissed, 1);
    });

    test('stays in Flutter when native has no caller waiting', () async {
      answer(BridgeContract.flowChannel, (_) async => throw PlatformException(code: 'NO_ACTIVE_FLOW'));

      await expectLater(
        host.completeFlow(const FlowCancelled()),
        throwsA(isA<BridgeException>().having((e) => e.code, 'code', BridgeErrorCode.noActiveFlow)),
      );
      expect(dismissed, 0);
    });
  });

  test('openNative sends the screen name', () async {
    answer(BridgeContract.flowChannel, (_) async => null);

    await host.openNative(NativeScreen.help);

    expect(calls.single.method, BridgeContract.navigationOpenNative);
    expect(calls.single.arguments, {'screen': 'help'});
  });
}
