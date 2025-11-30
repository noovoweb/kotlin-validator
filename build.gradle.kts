import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask

plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
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
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Enable explicit API mode for library modules (not testing/examples)
    plugins.withId("org.jetbrains.kotlin.jvm") {
        if (name !in listOf("kotlin-validator-testing")) {
            configure<KotlinJvmProjectExtension> {
                explicitApi()
            }
        }
    }

    configure<KtlintExtension> {
        // Use editorConfigOverride instead of disabledRules (ktlint 0.48+)
        filter {
            exclude("**/build/**")
            exclude("**/generated/**")
        }
    }

    // Disable specific ktlint rules via .editorconfig overrides
    tasks.withType<BaseKtLintCheckTask>().configureEach {
        exclude("**/build/**")
        exclude("**/generated/**")
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
