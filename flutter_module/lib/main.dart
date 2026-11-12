import 'package:flutter/material.dart';

import 'profile/profile_screen.dart';
import 'session/session_channel.dart';
import 'session/session_payload.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // The one and only place this module fetches a session — see docs/adr/0001.
  final session = await SessionChannel.fetch();

  runApp(ProfileApp(session: session));
}

class ProfileApp extends StatelessWidget {
  const ProfileApp({super.key, required this.session});

  final SessionPayload session;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: ProfileScreen(session: session),
    );
  }
}
