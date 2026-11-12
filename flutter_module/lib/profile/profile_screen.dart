import 'package:flutter/material.dart';

import '../session/session_payload.dart';

/// The new Flutter Profile screen — what
/// [com.example.legacyshell.featureflags.MigrationFlags.profileScreenOnFlutter]
/// switches users to (see docs/adr/0002).
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key, required this.session});

  final SessionPayload session;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Profile (Flutter)')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(session.displayName, style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 8),
            Text('User ID: ${session.userId}'),
          ],
        ),
      ),
    );
  }
}
