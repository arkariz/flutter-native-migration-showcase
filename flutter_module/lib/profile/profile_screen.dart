import 'package:material_ui/material_ui.dart';

import '../session/session_payload.dart';

/// The new Flutter Profile screen — what native's `profileScreenOnFlutter` flag
/// switches users to (docs/adr/0002). Help has no Flutter version yet, so [onOpenHelp]
/// goes back to native through `navigation.openNative` (docs/adr/0010).
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key, required this.session, required this.onOpenHelp});

  final SessionPayload session;
  final VoidCallback onOpenHelp;

  static const Key helpButton = Key('profile.help');

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
            const SizedBox(height: 24),
            OutlinedButton(key: helpButton, onPressed: onOpenHelp, child: const Text('Help')),
          ],
        ),
      ),
    );
  }
}
