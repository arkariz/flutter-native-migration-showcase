plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    // Only to parse contract/bridge_contract.json in BridgeContractTest.
    testImplementation("com.google.code.gson:gson:2.15.0")
}

tasks.test {
    useJUnitPlatform()
    // The single source of truth for the bridge, shared with flutter_module (docs/adr/0011).
    systemProperty("bridgeContractPath", rootProject.file("../contract/bridge_contract.json").absolutePath)
}

kotlin {
    jvmToolchain(21)
}
