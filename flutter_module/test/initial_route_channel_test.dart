import 'package:flutter/services.dart';
import 'package:flutter_module/routing/initial_route_channel.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel('migration_showcase/deeplink');

  void mockNativeResponse(Future<Object?> Function(MethodCall call) handler) {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, handler);
  }

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('fetch returns the route native resolved', () async {
    mockNativeResponse((call) async {
      expect(call.method, 'deeplink.initialRoute');
      return 'settings';
    });

    expect(await InitialRouteChannel.fetch(), 'settings');
  });

  test('fetch throws when native returns an empty route', () async {
    mockNativeResponse((call) async => '');

    expect(InitialRouteChannel.fetch(), throwsA(isA<InitialRouteDecodeException>()));
  });

  test('fetch throws when native returns null', () async {
    mockNativeResponse((call) async => null);

    expect(InitialRouteChannel.fetch(), throwsA(isA<InitialRouteDecodeException>()));
  });
}
