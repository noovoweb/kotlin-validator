import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
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

    // Enable explicit API mode for library modules (not testing/examples)
    plugins.withId("org.jetbrains.kotlin.jvm") {
        if (name !in listOf("kotlin-validator-testing")) {
            configure<KotlinJvmProjectExtension> {
                explicitApi()
            }
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktlint("1.0.1")
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
            ktlint("1.0.1")
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
