/**
 * Shared Maven publishing configuration for all kotlin-validator modules.
 *
 * This configuration:
 * - Publishes to Maven Local for local development and testing
 * - Generates proper POM files with metadata
 */

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

// Apply plugins
apply(plugin = "maven-publish")

// Configure Java plugin to generate sources and javadoc jars
plugins.withType<JavaPlugin> {
    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }
}

afterEvaluate {
    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set("Kotlin validation library with compile-time code generation using KSP")
                    url.set("https://github.com/noovoweb/kotlin-validator")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("noovoweb")
                            name.set("Noovo Web")
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
        }

        repositories {
            // Maven Local for local development and testing
            mavenLocal()
        }
    }
}
