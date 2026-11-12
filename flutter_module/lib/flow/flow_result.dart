/// What a Flutter flow reports back to the native caller that launched it
/// (`flow.complete`, docs/adr/0009). Sealed, so every consumer handles both cases.
sealed class FlowResult {
  const FlowResult();

  static const String statusKey = 'status';
  static const String payloadKey = 'payload';
  static const String completedStatus = 'completed';
  static const String cancelledStatus = 'cancelled';

  /// The contract's `payloads.flowResult` keys and `flowResultStatuses`.
  static const List<String> keys = [statusKey, payloadKey];
  static const List<String> statuses = [completedStatus, cancelledStatus];

  Map<String, Object?> toChannel();
}

final class FlowCompleted extends FlowResult {
  const FlowCompleted([this.payload = const {}]);

  final Map<String, Object?> payload;

  @override
  Map<String, Object?> toChannel() =>
      {FlowResult.statusKey: FlowResult.completedStatus, FlowResult.payloadKey: payload};
}

final class FlowCancelled extends FlowResult {
  const FlowCancelled();

  @override
  Map<String, Object?> toChannel() => {FlowResult.statusKey: FlowResult.cancelledStatus};
}

/// Legacy screens Flutter may open via `navigation.openNative`. Mirrors native's
/// `NativeRoutes.ALLOWED` allowlist — a temporary crossing with a removal condition
/// (docs/adr/0010), so this enum is expected to shrink to nothing.
enum NativeScreen {
  help;

  static const String screenKey = 'screen';

  /// The contract's `payloads.openNative` keys.
  static const List<String> keys = [screenKey];
}
