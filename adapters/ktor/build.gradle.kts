plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // Core validator modules
    api(project(":kotlin-validator-engine"))
    api(project(":kotlin-validator-runtime"))

    // Ktor server core
    api(libs.ktor.server.core)

    // Kotlinx Serialization (for ValidationErrorResponse)
    api(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

// Ktor uses wildcard imports idiomatically
tasks.matching { it.name.startsWith("ktlint") }.configureEach {
    enabled = name.contains("KotlinScripts")
}
