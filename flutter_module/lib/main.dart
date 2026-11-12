import 'package:material_ui/material_ui.dart';

import 'flow/flow_result.dart';
import 'host/channel_host_port.dart';
import 'host/host_port.dart';
import 'host/standalone_host_port.dart';
import 'profile/profile_screen.dart';
import 'session/session_payload.dart';
import 'settings/settings_screen.dart';
import 'sign_in/sign_in_flow_coordinator.dart';
import 'sign_in/sign_in_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final HostPort host = hostMode == 'standalone' ? StandaloneHostPort.demo() : ChannelHostPort();

  // Pull, don't push: native registered its handlers before this entrypoint ran
  // (docs/adr/0001), so these calls can't race it.
  final route = await host.initialRoute();
  final session = await host.fetchSession();

  runApp(MigrationShowcaseApp(host: host, route: route, session: session));
}

class MigrationShowcaseApp extends StatelessWidget {
  const MigrationShowcaseApp({super.key, required this.host, required this.route, required this.session});

  final HostPort host;
  final String route;
  final SessionPayload? session;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: _screenFor(route),
    );
  }

  Widget _screenFor(String route) {
    final session = this.session;
    return switch (route) {
      'login' => SignInScreen(onOutput: SignInFlowCoordinator(host).handle),
      'settings' => const SettingsScreen(),
      _ when session == null => const _SignedOutScreen(),
      _ => ProfileScreen(session: session, onOpenHelp: () => host.openNative(NativeScreen.help)),
    };
  }
}

/// Native's readiness pipeline normally prevents this; it covers a session that
/// was revoked between native's check and this engine's first frame.
class _SignedOutScreen extends StatelessWidget {
  const _SignedOutScreen();

  @override
  Widget build(BuildContext context) =>
      const Scaffold(body: Center(child: Text('Signed out. Return to the app to sign in again.')));
}
