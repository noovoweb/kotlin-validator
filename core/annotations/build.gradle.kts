plugins {
    kotlin("jvm")
}

dependencies {
    // No dependencies - annotations only
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

// Boolean Validators file name is intentional - contains @Accepted annotation
tasks.matching { it.name.startsWith("ktlint") }.configureEach {
    enabled = name.contains("KotlinScripts")
}
