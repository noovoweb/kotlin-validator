plugins {
    kotlin("jvm")
}

dependencies {
    // Module dependencies
    implementation(project(":kotlin-validator-annotations"))
    implementation(project(":kotlin-validator-engine"))
    implementation(project(":kotlin-validator-runtime"))

    // KSP API
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.28")

    // KotlinPoet for code generation
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.6.0")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.6.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xopt-in=com.tschuchort.compiletesting.ExperimentalKotlinCompilerApi")
    }
}

tasks.test {
    useJUnitPlatform()
}

// Kotlinpoet and KSP use wildcard imports idiomatically
tasks.matching { it.name.startsWith("ktlint") }.configureEach {
    enabled = name.contains("KotlinScripts")
}
