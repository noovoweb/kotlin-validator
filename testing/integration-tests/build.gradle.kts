plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

dependencies {
    // All modules for integration testing
    implementation(project(":kotlin-validator-annotations"))
    implementation(project(":kotlin-validator-engine"))
    implementation(project(":kotlin-validator-runtime"))
    ksp(project(":kotlin-validator-processor"))

    // Coroutines (needed by generated code)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
