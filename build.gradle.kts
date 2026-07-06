import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    id("jacoco-report-aggregation")
}

// Single source of truth for the published version (gradle.properties is not tracked,
// so the version must live in a committed file for CI and tag-triggered publishing).
val validatorVersion = "0.1.0-beta.4"

allprojects {
    group = "com.noovoweb"
    version = validatorVersion

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "jacoco")
    apply(plugin = "com.diffplug.spotless")

    // Unified JVM toolchain and explicit API mode for library modules
    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmProjectExtension> {
            jvmToolchain(17)
            if (name !in listOf("kotlin-validator-testing")) {
                explicitApi()
            }
        }
    }

    tasks.matching { it.name == "compileKotlin" }.configureEach {
        dependsOn("spotlessCheck")
    }

    configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktlint("1.8.0")
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_no-wildcard-imports" to "disabled",
                        "ktlint_standard_filename" to "disabled",
                        "ktlint_standard_comment-wrapping" to "disabled",
                        "ktlint_standard_value-argument-comment" to "disabled",
                        "ktlint_standard_value-parameter-comment" to "disabled",
                        "ktlint_standard_discouraged-comment-location" to "disabled",
                    ),
                )
            trimTrailingWhitespace()
            endWithNewline()
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktlint("1.8.0")
        }

        format("misc") {
            target("*.md", ".gitignore")
            trimTrailingWhitespace()
            endWithNewline()
        }
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

// Aggregated coverage report across all modules: ./gradlew jacocoTestReport
// (output in build/reports/jacoco/jacocoTestReport)
dependencies {
    subprojects.forEach { jacocoAggregation(it) }
}

reporting {
    reports {
        create<JacocoCoverageReport>("jacocoTestReport") {
            testSuiteName = "test"
        }
    }
}

gradle.taskGraph.afterTask {
    if (name.startsWith("spotless") && name.endsWith("Check") && state.failure != null) {
        logger.lifecycle("\n⚠️  Formatting issues found. Run './gradlew spotlessApply build' to auto-fix and build.\n")
    }
}
