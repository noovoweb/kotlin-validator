plugins {
    kotlin("jvm") version "2.0.21" apply false
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
