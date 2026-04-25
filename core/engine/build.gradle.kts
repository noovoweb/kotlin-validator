plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Coroutines for async validation
    implementation(libs.kotlinx.coroutines.core)

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
