import 'package:material_ui/material_ui.dart';

import 'profile/profile_screen.dart';
import 'routing/initial_route_channel.dart';
import 'session/session_channel.dart';
import 'session/session_payload.dart';
import 'settings/settings_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // The two crossing points this module uses — see docs/adr/0001, 0005, 0006.
  final session = await SessionChannel.fetch();
  final route = await InitialRouteChannel.fetch();

  runApp(MigrationShowcaseApp(session: session, route: route));
}

class MigrationShowcaseApp extends StatelessWidget {
  const MigrationShowcaseApp({super.key, required this.session, required this.route});

  final SessionPayload session;
  final String route;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: switch (route) {
        'settings' => const SettingsScreen(),
        _ => ProfileScreen(session: session),
      },
    );
  }
}
