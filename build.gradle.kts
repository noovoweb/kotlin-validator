import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    jacoco
}

allprojects {
    group = "com.noovoweb"
    version = "0.1.0-beta.1"

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
            jvmToolchain(21)
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
                        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
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

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    
    subprojects {
        // Skip parent directories (they don't have source code)
        if (name in listOf("core", "adapters", "testing")) {
            return@subprojects
        }
        
        this@register.sourceSets(this.the<SourceSetContainer>()["main"])
        this@register.executionData.from(tasks.withType<JacocoReport>().map { it.executionData })
    }
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

gradle.taskGraph.afterTask {
    if (name.startsWith("spotless") && name.endsWith("Check") && state.failure != null) {
        logger.lifecycle("\n⚠️  Formatting issues found. Run './gradlew spotlessApply build' to auto-fix and build.\n")
    }
}
