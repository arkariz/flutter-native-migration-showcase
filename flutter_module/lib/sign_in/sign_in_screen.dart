import 'package:material_ui/material_ui.dart';

import '../bridge/bridge_contract.dart';
import 'sign_in_output.dart';

/// The Flutter sign-in, shown when native's `loginScreenOnFlutter` flag is on.
/// Emits [SignInOutput]s through [onOutput]; it doesn't know what a host is.
class SignInScreen extends StatefulWidget {
  const SignInScreen({super.key, required this.onOutput});

  final Future<void> Function(SignInOutput output) onOutput;

  static const Key usernameField = Key('signIn.username');
  static const Key submitButton = Key('signIn.submit');
  static const Key cancelButton = Key('signIn.cancel');

  @override
  State<SignInScreen> createState() => _SignInScreenState();
}

class _SignInScreenState extends State<SignInScreen> {
  final _username = TextEditingController();
  String? _error;
  bool _busy = false;

  @override
  void dispose() {
    _username.dispose();
    super.dispose();
  }

  Future<void> _emit(SignInOutput output) async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await widget.onOutput(output);
    } on BridgeException catch (e) {
      if (mounted) setState(() => _error = "Couldn't sign in (${e.code.name}). Try again.");
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  void _submit() {
    final username = _username.text.trim();
    if (username.isEmpty) {
      setState(() => _error = 'Enter a username');
      return;
    }
    _emit(SignInSubmitted(username));
  }

  @override
  Widget build(BuildContext context) {
    // System back is a cancellation, reported to native like any other result.
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop && !_busy) _emit(const SignInCancelled());
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('Sign in (Flutter)')),
        body: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              TextField(
                key: SignInScreen.usernameField,
                controller: _username,
                decoration: InputDecoration(labelText: 'Username', errorText: _error),
                onSubmitted: (_) => _submit(),
              ),
              const SizedBox(height: 16),
              FilledButton(
                key: SignInScreen.submitButton,
                onPressed: _busy ? null : _submit,
                child: const Text('Sign in'),
              ),
              TextButton(
                key: SignInScreen.cancelButton,
                onPressed: _busy ? null : () => _emit(const SignInCancelled()),
                child: const Text('Cancel'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
