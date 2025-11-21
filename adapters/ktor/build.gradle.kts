plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.21"
}

dependencies {
    // Core validator modules
    api(project(":kotlin-validator-engine"))
    api(project(":kotlin-validator-runtime"))

    // Ktor server core
    api("io.ktor:ktor-server-core:2.3.12")

    // Kotlinx Serialization (for ValidationErrorResponse)
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.12")
    testImplementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
