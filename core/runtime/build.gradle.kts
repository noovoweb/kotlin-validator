plugins {
    kotlin("jvm")
}

dependencies {
    // Core module dependency (includes coroutines)
    api(project(":kotlin-validator-engine"))

    // Coroutines (transitive from core, but explicit for clarity)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

// Test files have inline comments for test data documentation
tasks.matching { it.name == "ktlintTestSourceSetCheck" }.configureEach {
    enabled = false
}
