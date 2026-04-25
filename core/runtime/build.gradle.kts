plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Core module dependency (includes coroutines)
    api(project(":kotlin-validator-engine"))

    // Coroutines (transitive from core, but explicit for clarity)
    implementation(libs.kotlinx.coroutines.core)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

// Test files have inline comments for test data documentation
tasks.matching { it.name == "ktlintTestSourceSetCheck" }.configureEach {
    enabled = false
}
