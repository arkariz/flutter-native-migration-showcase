import 'dart:convert';
import 'dart:io';

import 'package:flutter_module/bridge/bridge_contract.dart';
import 'package:flutter_module/flow/flow_result.dart';
import 'package:flutter_module/session/session_payload.dart';
import 'package:flutter_test/flutter_test.dart';

/// The Dart half of the cross-runtime contract check (docs/adr/0011). bridge-core's
/// BridgeContractTest reads the same file, so a rename on either side fails a build.
void main() {
  final contract = jsonDecode(File('../contract/bridge_contract.json').readAsStringSync()) as Map<String, dynamic>;

  test('version matches', () => expect(BridgeContract.version, contract['version']));

  test('channels and the method → channel mapping match', () {
    final channels = (contract['channels'] as Map).cast<String, String>();
    expect(BridgeContract.sessionChannel, channels['session']);
    expect(BridgeContract.deeplinkChannel, channels['deeplink']);
    expect(BridgeContract.flowChannel, channels['flow']);

    final methods = (contract['methods'] as Map).cast<String, String>();
    expect(BridgeContract.methodChannels, methods.map((method, key) => MapEntry(method, channels[key]!)));
  });

  test('payload keys match', () {
    final payloads = contract['payloads'] as Map<String, dynamic>;
    expect(SessionPayload.keys, payloads['session']);
    expect(FlowResult.keys, payloads['flowResult']);
    expect(NativeScreen.keys, payloads['openNative']);
    expect(FlowResult.statuses, contract['flowResultStatuses']);
  });

  test('error codes match, excluding the local-only fallback', () {
    expect(
      BridgeErrorCode.values.where((c) => c != BridgeErrorCode.unknown).map((c) => c.wireName),
      contract['errorCodes'],
    );
  });
}
