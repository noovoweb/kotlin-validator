plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

dependencies {
    // All modules for integration testing
    implementation(project(":kotlin-validator-annotations"))
    implementation(project(":kotlin-validator-engine"))
    implementation(project(":kotlin-validator-runtime"))
    ksp(project(":kotlin-validator-processor"))

    // Coroutines (needed by generated code)
    implementation(libs.kotlinx.coroutines.core)

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}

// Exclude generated KSP code from ktlint checks
tasks.matching { it.name.startsWith("ktlint") }.configureEach {
    enabled = name.contains("KotlinScripts")
}
