plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Module dependencies
    implementation(project(":kotlin-validator-annotations"))
    implementation(project(":kotlin-validator-engine"))
    implementation(project(":kotlin-validator-runtime"))

    // KSP API
    implementation(libs.ksp.symbol.processing.api)

    // KotlinPoet for code generation
    implementation(libs.kotlinpoet.core)
    implementation(libs.kotlinpoet.ksp)

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.compile.testing)
    testImplementation(libs.compile.testing.ksp)
    testImplementation(kotlin("test"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=com.tschuchort.compiletesting.ExperimentalKotlinCompilerApi")
    }
}

tasks.test {
    useJUnitPlatform()
}

// Kotlinpoet and KSP use wildcard imports idiomatically
tasks.matching { it.name.startsWith("ktlint") }.configureEach {
    enabled = name.contains("KotlinScripts")
}
