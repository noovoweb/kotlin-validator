plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    // Core validator modules
    api(project(":kotlin-validator-engine"))
    api(project(":kotlin-validator-runtime"))

    // Spring Boot WebFlux
    api(libs.spring.boot.starter.webflux)
    api(libs.spring.boot.autoconfigure)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)

    // Optional: Configuration processor for IDE support
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
