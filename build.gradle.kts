import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.publish.PublishingExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.maven.publish) apply false
    id("jacoco-report-aggregation")
}

// Public ABI tracking: `./gradlew apiDump` regenerates the *.api files, `apiCheck`
// (wired into `build`) fails when the public surface changes without an update.
apiValidation {
    // Not published — its ABI is not a compatibility promise.
    ignoredProjects.add("kotlin-validator-testing")
}

// Single source of truth for the published version.
val validatorVersion = "0.1.0-beta.8"

// JDK to compile and test against. Defaults to the supported floor (17); CI overrides it
// (-PbuildJavaVersion=21) to also verify the library on newer JDKs. Published artifacts
// always target 17 since publishing does not pass this property.
val buildJavaVersion: Int = providers.gradleProperty("buildJavaVersion").map { it.toInt() }.getOrElse(17)

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
            jvmToolchain(buildJavaVersion)
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

// Publishing for every published module.
// Primary target: Maven Central via the Central Portal (com.vanniktech.maven.publish),
// which builds signed bundles with sources + javadoc jars and a complete POM.
// Secondary target: GitHub Packages.
// Credentials come from environment variables in CI (never committed):
//   ORG_GRADLE_PROJECT_mavenCentralUsername / ...Password  — Central Portal token
//   ORG_GRADLE_PROJECT_signingInMemoryKey / ...KeyPassword — ASCII-armored GPG key
//   GITHUB_ACTOR / GITHUB_TOKEN                             — GitHub Packages
subprojects {
    // Skip parent directories (no source) and the dev-only testing module.
    if (name in listOf("core", "adapters", "testing", "kotlin-validator-testing")) {
        return@subprojects
    }

    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            // Upload to the Central Portal; keep the release manual (review, then publish).
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
            // Sign only when a key is present (ORG_GRADLE_PROJECT_signingInMemoryKey in CI),
            // so local builds and publishToMavenLocal don't require one.
            if (project.findProperty("signingInMemoryKey") != null) {
                signAllPublications()
            }
            coordinates(project.group.toString(), project.name, project.version.toString())

            pom {
                name.set(project.name)
                description.set("Kotlin validation library with compile-time code generation using KSP")
                url.set("https://github.com/noovoweb/kotlin-validator")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("noovoweb")
                        name.set("NoovoWeb")
                        email.set("info@noovoweb.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/noovoweb/kotlin-validator.git")
                    developerConnection.set("scm:git:ssh://github.com/noovoweb/kotlin-validator.git")
                    url.set("https://github.com/noovoweb/kotlin-validator")
                }
            }
        }

        // Secondary target: GitHub Packages. vanniktech creates the publications; this
        // only adds the extra repository so publishAllPublicationsToGitHubPackagesRepository works.
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/noovoweb/kotlin-validator")
                    credentials {
                        username = (project.findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                        password = (project.findProperty("gpr.token") as String?) ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }

    apply(plugin = "com.vanniktech.maven.publish")
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
