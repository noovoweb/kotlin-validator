plugins {
    kotlin("jvm") version "2.0.21" apply false
}

allprojects {
    group = "com.noovoweb"
    version = "0.1.0-beta.1"

    repositories {
        mavenCentral()
    }
}

// Apply publishing configuration to all publishable modules
subprojects {
    // Skip parent directories (they don't have source code)
    if (name in listOf("core", "adapters", "testing")) {
        return@subprojects
    }
    
    // Skip testing module - it's for development only
    if (name != "kotlin-validator-testing") {
        apply(from = "$rootDir/gradle/publish.gradle.kts")
    }
}
