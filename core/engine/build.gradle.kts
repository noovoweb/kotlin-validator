plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Coroutines for async validation. `api` (not `implementation`) because ValidationContext
    // exposes CoroutineDispatcher in its public API, so consumers must compile against it and
    // the published POM must declare it with a proper version constraint.
    api(libs.kotlinx.coroutines.core)

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
