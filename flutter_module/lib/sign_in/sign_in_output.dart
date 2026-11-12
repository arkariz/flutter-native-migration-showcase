/// What the sign-in screen can produce. The screen only emits these; it never talks
/// to the host — [SignInFlowCoordinator] decides what each one means (docs/adr/0009).
sealed class SignInOutput {
  const SignInOutput();
}

final class SignInSubmitted extends SignInOutput {
  const SignInSubmitted(this.username);

  final String username;
}

final class SignInCancelled extends SignInOutput {
  const SignInCancelled();
}
