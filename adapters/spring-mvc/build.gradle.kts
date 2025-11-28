plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "2.0.21"
}

dependencies {
    // Core validator modules
    api(project(":kotlin-validator-engine"))
    api(project(":kotlin-validator-runtime"))

    // Spring Boot MVC
    api("org.springframework.boot:spring-boot-starter-web:3.5.7")
    api("org.springframework.boot:spring-boot-autoconfigure:3.5.7")

    // Coroutines for suspend function support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")

    // Optional: Configuration processor for IDE support
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.5.7")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
