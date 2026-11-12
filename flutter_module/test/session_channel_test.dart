import 'package:flutter/services.dart';
import 'package:flutter_module/session/session_channel.dart';
import 'package:flutter_module/session/session_payload.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel('migration_showcase/session');

  void mockNativeResponse(Future<Object?> Function(MethodCall call) handler) {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, handler);
  }

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  group('SessionPayload.fromChannelResult', () {
    test('decodes a valid result', () {
      final payload = SessionPayload.fromChannelResult(const {
        'userId': 'u1',
        'displayName': 'Ada Lovelace',
        'authToken': 'tok-123',
      });

      expect(payload.userId, 'u1');
      expect(payload.displayName, 'Ada Lovelace');
      expect(payload.authToken, 'tok-123');
    });

    test('throws when a required field is missing', () {
      expect(
        () => SessionPayload.fromChannelResult(const {
          'displayName': 'Ada Lovelace',
          'authToken': 'tok-123',
        }),
        throwsA(isA<SessionDecodeException>()),
      );
    });

    test('throws when a field is the wrong type', () {
      expect(
        () => SessionPayload.fromChannelResult(const {
          'userId': 42,
          'displayName': 'Ada Lovelace',
          'authToken': 'tok-123',
        }),
        throwsA(isA<SessionDecodeException>()),
      );
    });

    test('throws when a field is an empty string', () {
      expect(
        () => SessionPayload.fromChannelResult(const {
          'userId': '',
          'displayName': 'Ada Lovelace',
          'authToken': 'tok-123',
        }),
        throwsA(isA<SessionDecodeException>()),
      );
    });
  });

  group('SessionChannel.fetch', () {
    test('decodes a successful native response', () async {
      mockNativeResponse((call) async {
        expect(call.method, 'session.fetch');
        return <String, String>{
          'userId': 'u2',
          'displayName': 'Grace Hopper',
          'authToken': 'tok-456',
        };
      });

      final payload = await SessionChannel.fetch();

      expect(payload.displayName, 'Grace Hopper');
    });

    test('propagates a PlatformException when native reports no session', () async {
      mockNativeResponse((call) async {
        throw PlatformException(code: 'no_session', message: 'No session to hand to Flutter');
      });

      expect(SessionChannel.fetch(), throwsA(isA<PlatformException>()));
    });

    test('throws SessionDecodeException when native returns a malformed result', () async {
      mockNativeResponse((call) async {
        return <String, String>{'userId': 'u3'};
      });

      expect(SessionChannel.fetch(), throwsA(isA<SessionDecodeException>()));
    });

    test('throws SessionDecodeException when native returns null', () async {
      mockNativeResponse((call) async => null);

      expect(SessionChannel.fetch(), throwsA(isA<SessionDecodeException>()));
    });
  });
}
