import 'package:material_ui/material_ui.dart';

/// A screen with no legacy equivalent — reachable only via deep link (see
/// docs/adr/0005). Demonstrates that not every Flutter screen needs a
/// migration flag: some simply never existed natively.
class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings (Flutter-only)')),
      body: const Padding(
        padding: EdgeInsets.all(24),
        child: Text('This screen only ever existed in Flutter.'),
      ),
    );
  }
}
