import 'package:flutter_module/flow/flow_result.dart';
import 'package:flutter_module/host/standalone_host_port.dart';
import 'package:flutter_module/main.dart';
import 'package:flutter_module/profile/profile_screen.dart';
import 'package:flutter_module/sign_in/sign_in_screen.dart';
import 'package:flutter_test/flutter_test.dart';

/// The whole module against [StandaloneHostPort] — no native shell, no channels.
void main() {
  Future<StandaloneHostPort> pumpApp(WidgetTester tester, StandaloneHostPort host) async {
    await tester.pumpWidget(
      MigrationShowcaseApp(host: host, route: await host.initialRoute(), session: await host.fetchSession()),
    );
    return host;
  }

  testWidgets('sign-in commits a session and completes the flow', (tester) async {
    final host = await pumpApp(tester, StandaloneHostPort(route: 'login'));

    await tester.enterText(find.byKey(SignInScreen.usernameField), 'Grace');
    await tester.tap(find.byKey(SignInScreen.submitButton));
    await tester.pumpAndSettle();

    expect(host.session?.displayName, 'Grace');
    expect(host.completedFlows.single, isA<FlowCompleted>());
  });

  testWidgets('an empty username is rejected without calling the host', (tester) async {
    final host = await pumpApp(tester, StandaloneHostPort(route: 'login'));

    await tester.tap(find.byKey(SignInScreen.submitButton));
    await tester.pump();

    expect(find.text('Enter a username'), findsOneWidget);
    expect(host.completedFlows, isEmpty);
  });

  testWidgets('profile shows the session and opens native Help', (tester) async {
    final host = await pumpApp(tester, StandaloneHostPort.demo(route: 'profile'));

    expect(find.text('Demo User'), findsOneWidget);
    await tester.tap(find.byKey(ProfileScreen.helpButton));

    expect(host.openedNativeScreens, [NativeScreen.help]);
  });

  testWidgets('profile without a session shows the signed-out state', (tester) async {
    await pumpApp(tester, StandaloneHostPort(route: 'profile'));

    expect(find.textContaining('Signed out'), findsOneWidget);
  });
}
