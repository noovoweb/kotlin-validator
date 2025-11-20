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
