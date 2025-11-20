rootProject.name = "kotlin-validator"

// Core modules with original names as paths for backward compatibility
include(":kotlin-validator-annotations")
include(":kotlin-validator-runtime")
include(":kotlin-validator-core")
include(":kotlin-validator-ksp")

// Framework adapters
include(":kotlin-validator-spring-webflux")
include(":kotlin-validator-spring-mvc")
include(":kotlin-validator-ktor")

// Testing
include(":kotlin-validator-testing")

// Map logical paths to physical directories
project(":kotlin-validator-annotations").projectDir = file("core/annotations")
project(":kotlin-validator-runtime").projectDir = file("core/runtime")
project(":kotlin-validator-core").projectDir = file("core/engine")
project(":kotlin-validator-ksp").projectDir = file("core/processor")
project(":kotlin-validator-spring-webflux").projectDir = file("adapters/spring-webflux")
project(":kotlin-validator-spring-mvc").projectDir = file("adapters/spring-mvc")
project(":kotlin-validator-ktor").projectDir = file("adapters/ktor")
project(":kotlin-validator-testing").projectDir = file("testing/integration-tests")
